import os
from fastapi import FastAPI, HTTPException
import chromadb
from sentence_transformers import SentenceTransformer
from chromadb.api.types import EmbeddingFunction

# 获取持久化目录（例如 Docker 中挂载到 /data）
persist_dir = os.environ.get("CHROMADB_PERSIST_DIRECTORY", "./chroma")

# 加载 SentenceTransformer 模型
model = SentenceTransformer("sentence-transformers/all-mpnet-base-v2")

# 自定义嵌入函数类
class CustomEmbeddingFunction(EmbeddingFunction):
    def __init__(self, model):
        self.model = model
    def __call__(self, input):
        # input 是字符串列表
        return self.model.encode(input).tolist()

embedding_fn = CustomEmbeddingFunction(model)

# 创建/获取 Chromadb Client
client = chromadb.PersistentClient(path=persist_dir)

# 两个集合名称
report_collection_name = "research_reports"
call_collection_name = "earnings_call_transcripts"

all_collections = client.list_collections()

if report_collection_name in all_collections:
    report_collection = client.get_collection(name=report_collection_name, embedding_function=embedding_fn)
else:
    raise ValueError(f"Collection '{report_collection_name}' does not exist at {persist_dir}.")

if call_collection_name in all_collections:
    call_collection = client.get_collection(name=call_collection_name, embedding_function=embedding_fn)
else:
    raise ValueError(f"Collection '{call_collection_name}' does not exist at {persist_dir}.")

# -----------------------------------------------------------------------------
# 定义硬编码映射：用于 10K 和 会议记录
# 对于 research_reports（10K）的 "conm"
report_mapping = {
    "amazon": "AMAZON.COM INC",
    "ford motor": "FORD MOTOR CO",
    "american airlines": "AMERICAN AIRLINES GROUP INC"
}
# 对于 call_transcripts 的 "company_name"
call_mapping = {
    "amazon": "Amazon.com, Inc.",
    "ford motor": "Ford Motor Company",
    "american airlines": "American Airlines Group Inc."
}

# -----------------------------------------------------------------------------
# 辅助函数：执行带“三年筛选 + 排序”的查询（公司名称按硬编码匹配）
# -----------------------------------------------------------------------------
def do_query_with_3yr_filter(
    collection,          # 要查询的集合 (call_collection 或 report_collection)
    query_text: str,     # 固定好的查询关键词
    company_name: str,   # 公司名称，用于判断匹配
    n_results: int,      # 最终返回的数量
    year_field: str      # 元数据中表示年份的字段名，如 "fyear" 或 "fiscal_year"
):
    """
    1) 根据 company_name 判断是否包含关键字，如果包含则使用硬编码的值进行精确匹配（$eq），否则直接使用原值。
    2) 查询结果中提取年份，计算 max_year，只保留最近 3 年（year >= max_year - 2）。
    3) 在这部分数据中先按 distance (升序) 排序，再按 year (降序) 排序。
    4) 返回前 n_results 条记录。
    """
    try:
        intermediate_n = max(n_results * 5, 50)
        company_lower = company_name.lower()

        # 判断并设置 where_filter，不同集合使用不同的字段和映射
        if year_field == "fyear":
            # research_reports 集合使用字段 "conm"
            # 检查是否包含映射关键字
            search_val = None
            for key, val in report_mapping.items():
                if key in company_lower:
                    search_val = val
                    break
            if not search_val:
                search_val = company_name  # 如果不包含，则直接用原值进行匹配
            where_filter = {"conm": {"$eq": search_val}}
        else:
            # call_transcripts 集合使用字段 "company_name"
            search_val = None
            for key, val in call_mapping.items():
                if key in company_lower:
                    search_val = val
                    break
            if not search_val:
                search_val = company_name
            where_filter = {"company_name": {"$eq": search_val}}

        raw_results = collection.query(
            query_texts=[query_text],
            where=where_filter,
            n_results=intermediate_n,
            include=["documents", "metadatas", "distances"]
        )

        # 解包返回的结果（ids 默认返回）
        ids_list = raw_results["ids"][0]
        docs_list = raw_results["documents"][0]
        metas_list = raw_results["metadatas"][0]
        dists_list = raw_results["distances"][0]

        if not ids_list:
            return {
                "message": f"No results found for company (filtered as '{search_val}') with query '{query_text}'",
                "max_year": None,
                "year_cutoff": None,
                "results": []
            }

        results_all = []
        for i in range(len(ids_list)):
            meta = metas_list[i]
            dist = dists_list[i]
            if year_field not in meta:
                continue
            try:
                year_int = int(meta[year_field])
            except:
                continue
            results_all.append({
                "id": ids_list[i],
                "document": docs_list[i],
                "metadata": meta,
                "distance": dist,
                "year": year_int
            })

        if not results_all:
            return {
                "message": f"No valid {year_field} found in metadata for company (filtered as '{search_val}')",
                "max_year": None,
                "year_cutoff": None,
                "results": []
            }

        max_year = max(r["year"] for r in results_all)
        year_cutoff = max_year - 2
        filtered = [r for r in results_all if r["year"] >= year_cutoff]
        if not filtered:
            return {
                "message": f"No data within the last 3 years (cutoff={year_cutoff}) for company (filtered as '{search_val}')",
                "max_year": max_year,
                "year_cutoff": year_cutoff,
                "results": []
            }
        filtered_sorted = sorted(filtered, key=lambda r: (r["distance"], -r["year"]))
        final = filtered_sorted[:n_results]

        return {
            "message": f"Found {len(filtered)} results in the last 3 years; returning top {len(final)}.",
            "max_year": max_year,
            "year_cutoff": year_cutoff,
            "results": [
                {
                    "id": r["id"],
                    "document": r["document"],
                    "metadata": r["metadata"],
                    "distance": r["distance"],
                    "year": r["year"]
                }
                for r in final
            ]
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# -----------------------------------------------------------------------------
# 统一接口：一次性获取“财报电话会议”和“研究报告”查询结果
# -----------------------------------------------------------------------------
app = FastAPI(title="ChromaDB Query Service")

@app.get("/query/stock_data")
async def query_stock_data(company_name: str, n_results: int = 10):
    """
    统一接口，根据 GET 参数 company_name（例如 "amazon", "Ford Motor", "American Airlines"）
    对 10K 和 会议记录集合分别进行硬编码匹配搜索：
      - 对 research_reports 使用固定查询关键词，并在 conm 字段中做精确匹配
      - 对 call_transcripts 使用固定查询关键词，并在 company_name 字段中做精确匹配
    返回结果中只保留最近 3 年的数据，排序规则：向量距离优先，其次年份降序。
    """
    call_query_text = (
        "latest earnings financial performance guidance "
        "revenue growth business outlook recent developments"
    )
    report_query_text = (
        "annual financial results revenue profit loss "
        "business strategy market share key events "
        "competitive position recent developments"
    )

    call_transcripts_results = do_query_with_3yr_filter(
        collection=call_collection,
        query_text=call_query_text,
        company_name=company_name,
        n_results=n_results,
        year_field="fiscal_year"
    )

    research_reports_results = do_query_with_3yr_filter(
        collection=report_collection,
        query_text=report_query_text,
        company_name=company_name,
        n_results=n_results,
        year_field="fyear"
    )

    return {
        "call_transcripts_results": call_transcripts_results,
        "research_reports_results": research_reports_results
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)