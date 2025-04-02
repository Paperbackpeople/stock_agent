package com.example.stock_analysis.service.impl;

import com.example.stock_analysis.dto.QueryResult;
import com.example.stock_analysis.service.VectorQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class VectorQueryServiceImpl implements VectorQueryService {

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
        if (cached != null && cached instanceof QueryResult) {
            return (QueryResult) cached;
        }

        String url = vectorServiceBaseUrl + "/query/stock_data?company_name=" + queryText + "&n_results=" + nResults;
        Map<String, Object> externalResult = restTemplate.getForObject(url, Map.class);
        Map<String, Object> researchReports = (Map<String, Object>) externalResult.get("research_reports_results");

        QueryResult result = new QueryResult();
        result.setMessage((String) researchReports.get("message"));
        result.setMaxYear((Integer) researchReports.get("max_year"));
        result.setYearCutoff((Integer) researchReports.get("year_cutoff"));
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) researchReports.get("results");
        List<QueryResult.Result> results = new ArrayList<>();
        for (Map<String, Object> m : resultList) {
            QueryResult.Result r = new QueryResult.Result();
            r.setId((String) m.get("id"));
            r.setDocument((String) m.get("document"));
            r.setMetadata((Map<String, Object>) m.get("metadata"));
            r.setDistance(((Number) m.get("distance")).doubleValue());
            r.setYear(((Number) m.get("year")).intValue());
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
        if (cached != null && cached instanceof QueryResult) {
            return (QueryResult) cached;
        }

        String url = vectorServiceBaseUrl + "/query/stock_data?company_name=" + queryText + "&n_results=" + nResults;
        Map<String, Object> externalResult = restTemplate.getForObject(url, Map.class);
        Map<String, Object> callTranscripts = (Map<String, Object>) externalResult.get("call_transcripts_results");

        QueryResult result = new QueryResult();
        result.setMessage((String) callTranscripts.get("message"));
        result.setMaxYear((Integer) callTranscripts.get("max_year"));
        result.setYearCutoff((Integer) callTranscripts.get("year_cutoff"));
        List<Map<String, Object>> resultList = (List<Map<String, Object>>) callTranscripts.get("results");
        List<QueryResult.Result> results = new ArrayList<>();
        for (Map<String, Object> m : resultList) {
            QueryResult.Result r = new QueryResult.Result();
            r.setId((String) m.get("id"));
            r.setDocument((String) m.get("document"));
            r.setMetadata((Map<String, Object>) m.get("metadata"));
            r.setDistance(((Number) m.get("distance")).doubleValue());
            r.setYear(((Number) m.get("year")).intValue());
            results.add(r);
        }
        result.setResults(results);

        redisTemplate.opsForValue().set(cacheKey, result, 3600, TimeUnit.SECONDS);
        return result;
    }
}