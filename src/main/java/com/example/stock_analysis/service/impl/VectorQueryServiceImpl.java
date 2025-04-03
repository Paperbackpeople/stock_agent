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
    private RedisTemplate redisTemplate;

    @Override
    public QueryResult queryReport(String queryText, int nResults) {
        String cacheKey = "vector:query:report:" + queryText + ":" + nResults;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof QueryResult) {
            return (QueryResult) cached;
        }

        String url = vectorServiceBaseUrl + "/query/stock_data?company_name=" + queryText + "&n_results=" + nResults;
        Map externalResult = restTemplate.getForObject(url, Map.class);
        Map researchReports = (Map) externalResult.get("research_reports_results");

        QueryResult result = new QueryResult();
        result.setMessage((String) researchReports.get("message"));

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

        List<Map> resultList = (List<Map>) researchReports.get("results");
        List<QueryResult.Result> results = new ArrayList<>();
        if (resultList != null) {
            for (Map m : resultList) {
                Object yearObj = m.get("year");
                if (!(yearObj instanceof Number)) {
                    continue;
                }
                int yearValue = ((Number) yearObj).intValue();

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
                r.setMetadata((Map) m.get("metadata"));
                r.setDistance(distanceValue);
                r.setYear(yearValue);
                results.add(r);
            }
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
        Map externalResult = restTemplate.getForObject(url, Map.class);
        Map callTranscripts = (Map) externalResult.get("call_transcripts_results");

        QueryResult result = new QueryResult();
        result.setMessage((String) callTranscripts.get("message"));

        Object maxYearObj = callTranscripts.get("max_year");
        if (maxYearObj instanceof Number) {
            result.setMaxYear(((Number) maxYearObj).intValue());
        } else {
            result.setMaxYear(2019); 
        }

        Object yearCutoffObj = callTranscripts.get("year_cutoff");
        if (yearCutoffObj instanceof Number) {
            result.setYearCutoff(((Number) yearCutoffObj).intValue());
        } else {
            result.setYearCutoff(2019); 
        }

        List<Map> resultList = (List<Map>) callTranscripts.get("results");
        if (resultList == null) {
            resultList = Collections.emptyList();
        }

        List<QueryResult.Result> results = new ArrayList<>();
        for (Map m : resultList) {
            try {
                QueryResult.Result r = new QueryResult.Result();
                r.setId((String) m.get("id"));
                r.setDocument((String) m.get("document"));
                r.setMetadata((Map) m.get("metadata"));

                Object distObj = m.get("distance");
                r.setDistance(distObj instanceof Number ? ((Number) distObj).doubleValue() : 0.0);

                Object yearObj = m.get("year");
                if (yearObj instanceof Number) {
                    r.setYear(((Number) yearObj).intValue());
                } else {
                    r.setYear(0);
                }

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