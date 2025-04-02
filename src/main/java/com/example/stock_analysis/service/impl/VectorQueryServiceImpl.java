package com.example.stock_analysis.service.impl;

import com.example.stock_analysis.dto.QueryResult;
import com.example.stock_analysis.service.VectorQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class VectorQueryServiceImpl implements VectorQueryService {
    private static final Logger log = LoggerFactory.getLogger(VectorQueryServiceImpl.class);

    @Value("${vector.service.base.url}")
    private String vectorServiceBaseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public QueryResult queryReport(String queryText, int nResults) {
        String cacheKey = "vector:query:report:" + queryText + ":" + nResults;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof QueryResult) {
            return (QueryResult) cached;
        }

        String url = vectorServiceBaseUrl + "/query/stock_data?company_name=" + queryText + "&n_results=" + nResults;
        Map<String, Object> externalResult = restTemplate.getForObject(url, Map.class);
        // 注意这里返回的 key 是 "research_reports_results"
        Map<String, Object> researchReports = (Map<String, Object>) externalResult.get("research_reports_results");

        QueryResult result = new QueryResult();
        result.setMessage((String) researchReports.get("message"));

        // 防御性处理 max_year 和 year_cutoff
        Object maxYearObj = researchReports.get("max_year");
        if (maxYearObj instanceof Number) {
            result.setMaxYear(((Number) maxYearObj).intValue());
        } else {
            result.setMaxYear(2019);
        }

        Object yearCutoffObj = researchReports.get("year_cutoff");
        if (yearCutoffObj instanceof Number) {
            result.setYearCutoff(((Number) yearCutoffObj).intValue());
        } else {
            result.setYearCutoff(2019);
        }

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) researchReports.get("results");
        List<QueryResult.Result> results = new ArrayList<>();
        for (Map<String, Object> m : resultList) {
            // 如果 metadata 中缺少 year 字段，则跳过这条记录
            Object yearObj = m.get("year");
            if (!(yearObj instanceof Number)) {
                continue;
            }
            int yearValue = ((Number) yearObj).intValue();

            // 对 distance 字段做防御性处理，如果为空则可以设置一个默认较大值或跳过记录
            Object distanceObj = m.get("distance");
            double distanceValue;
            if (distanceObj instanceof Number) {
                distanceValue = ((Number) distanceObj).doubleValue();
            } else {
                continue;
            }

            QueryResult.Result r = new QueryResult.Result();
            r.setId((String) m.get("id"));
            r.setDocument((String) m.get("document"));
            r.setMetadata((Map<String, Object>) m.get("metadata"));
            r.setDistance(distanceValue);
            r.setYear(yearValue);
            results.add(r);
        }
        result.setResults(results);

        redisTemplate.opsForValue().set(cacheKey, result, 3600, TimeUnit.SECONDS);
        return result;
    }

    @Override
    public QueryResult queryCallTranscripts(String queryText, int nResults) {
        String cacheKey = "vector:query:call:" + queryText + ":" + nResults;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof QueryResult) {
            return (QueryResult) cached;
        }

        String url = vectorServiceBaseUrl + "/query/stock_data?company_name=" + queryText + "&n_results=" + nResults;
        Map<String, Object> externalResult = restTemplate.getForObject(url, Map.class);
        Map<String, Object> callTranscripts = (Map<String, Object>) externalResult.get("call_transcripts_results");

        QueryResult result = new QueryResult();
        result.setMessage((String) callTranscripts.get("message"));

        Object maxYearObj = callTranscripts.get("max_year");
        result.setMaxYear(maxYearObj instanceof Integer ? (Integer) maxYearObj : null);

        Object yearCutoffObj = callTranscripts.get("year_cutoff");
        result.setYearCutoff(yearCutoffObj instanceof Integer ? (Integer) yearCutoffObj : null);

        List<Map<String, Object>> resultList = (List<Map<String, Object>>) callTranscripts.get("results");
        if (resultList == null) {
            resultList = Collections.emptyList();
        }

        List<QueryResult.Result> results = new ArrayList<>();
        for (Map<String, Object> m : resultList) {
            try {
                QueryResult.Result r = new QueryResult.Result();
                r.setId((String) m.get("id"));
                r.setDocument((String) m.get("document"));
                r.setMetadata((Map<String, Object>) m.get("metadata"));

                // distance 防御
                Object distObj = m.get("distance");
                r.setDistance(distObj instanceof Number ? ((Number) distObj).doubleValue() : 0.0);

                // year 防御
                Object yearObj = m.get("year");
                r.setYear(yearObj instanceof Number ? ((Number) yearObj).intValue() : null);

                results.add(r);
            } catch (Exception e) {
                log.warn("Failed to parse call transcript result item: {}", m, e);
            }
        }

        result.setResults(results);
        redisTemplate.opsForValue().set(cacheKey, result, 3600, TimeUnit.SECONDS);
        return result;
    }
}