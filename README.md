# 📊 Stock Agent - AI-Powered Equity Research Reports

> [🧠 GitHub Repo](https://github.com/Paperbackpeople/stock_agent.git)  
> 🌐 Deployed at: [www.wangzhaoyu.online](https://www.wangzhaoyu.online)

---

## 🚀 Features

- Auto-generates professional equity research reports using LLMs
- Integrates structured (MySQL) + unstructured (ChromaDB) data
- Uses Redis for fast intermediate state storage
- Supports dual LLM providers (OpenAI + Anthropic)
- Deployed via Docker Compose with Nginx as reverse proxy and HTTPS via Certbot

---

## 📌 API Usage

### 📝 Generate Report

**POST** `https://www.wangzhaoyu.online/api/reports/generate`  
**Request Body:**

```json
{
  "userId": 1241077523,
  "companyName": "WeWork Inc"
}
```

**Available Companies:**
- WeWork Inc
- Amazon.com Inc
- Ford Motor Co
- American Airlines Group Inc

> ℹ️ `userId` must be an integer.

---

### 📚 Get User Report History

**GET** `https://www.wangzhaoyu.online/api/reports/history?userId=1241077523`

---

## 🧱 System Architecture

- **Spring Boot Backend** for core logic and API handling
- **MySQL** stores stock prices and financial ratios
- **ChromaDB** stores vectorized meeting and 10-K documents
- **LLMs** (via Dify) generate and validate reports:
    - Step 1: Summarize stock history
    - Step 2: Combine data to draft report
    - Step 3: Refine and generate recommendation
    - Step 4: Final verification (to prevent prompt injection)
- **Redis** caches intermediate and final results for performance

---

## 🐳 Deployment


To start the full system:

```bash
docker-compose up --build -d
```

