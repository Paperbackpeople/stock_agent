package com.example.stock_analysis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.stock_analysis.dto.QueryResult;
import com.example.stock_analysis.dto.ReportGenerationRequest;
import com.example.stock_analysis.dto.ReportGenerationResponse;
import com.example.stock_analysis.entity.CompanyFinancialRatios;
import com.example.stock_analysis.entity.ReportContent;
import com.example.stock_analysis.entity.StockHistory;
import com.example.stock_analysis.entity.UserReports;
import com.example.stock_analysis.mapper.ReportContentMapper;
import com.example.stock_analysis.mapper.UserReportsMapper;
import com.example.stock_analysis.service.ReportGenerationService;
import com.example.stock_analysis.service.ICompanyFinancialRatiosService;
import com.example.stock_analysis.service.IStockHistoryService;
import com.example.stock_analysis.service.VectorQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ReportGenerationServiceImpl.class);

    // Create a WebClient instance for asynchronous streaming calls
    private final WebClient webClient = WebClient.create();

    @Autowired
    private IStockHistoryService stockHistoryService;

    @Autowired
    private ICompanyFinancialRatiosService financialRatiosService;

    @Autowired
    private VectorQueryService vectorQueryService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserReportsMapper userReportsMapper;

    @Autowired
    private ReportContentMapper reportContentMapper;

    private RestTemplate restTemplate = createRestTemplateWithTimeout();

    @Value("${dify.generate.url}")
    private String difyGenerateUrl;

    @Value("${dify.generate.api-key}")
    private String difyGenerateApiKey;

    @Value("${dify.validate.url}")
    private String difyValidateUrl;

    @Value("${dify.validate.api-key}")
    private String difyValidateApiKey;

    /**
     * Asynchronously generates a report using various data sources and an external API in streaming mode.
     */
    @Async
    @Override
    public CompletableFuture<ReportGenerationResponse> generateReport(ReportGenerationRequest request) {
        Long userId = request.getUserId();
        String companyName = request.getCompanyName();
        log.info("Starting report generation for user {} and company {}", userId, companyName);

        // Generate a reportId using userId, companyName (without spaces) and the current hour.
        String currentHour = new SimpleDateFormat("yyyyMMddHH").format(new Date());
        String reportId = userId + "-" + companyName.replaceAll("\\s+", "") + "-" + currentHour;
        log.info("Generated reportId: {}", reportId);

        // Check Redis to enforce one report per company per hour.
        String redisKey = "report-gen:" + userId + ":" + companyName.replaceAll("\\s+", "") + ":" + currentHour;
        Boolean exists = redisTemplate.hasKey(redisKey);
        log.info("Checking Redis key {} existence: {}", redisKey, exists);
        if (exists != null && exists) {
            log.error("Report already generated in the current hour for key {}", redisKey);
            throw new RuntimeException("Report already generated in one hour");
        }

        // 1. Query stock history and serialize it to JSON.
        log.info("Fetching stock history info for company: {}", companyName);
        String stockHistoryInfo = getStockHistoryInfo(companyName);
        log.debug("Stock history info (truncated): {}",
                stockHistoryInfo.length() > 100 ? stockHistoryInfo.substring(0, 100) : stockHistoryInfo);

        // 2. Query financial ratios and serialize it to JSON.
        log.info("Fetching financial ratios for company: {}", companyName);
        String companyFinancialRatio = getCompanyFinancialRatio(companyName);
        log.debug("Financial ratios info (truncated): {}",
                companyFinancialRatio.length() > 100 ? companyFinancialRatio.substring(0, 100) : companyFinancialRatio);

        log.info("Querying report text from vector service for company: {}", companyName);
        QueryResult reportResult = vectorQueryService.queryReport(companyName, 2);
        StringBuilder reportTextBuilder = new StringBuilder();
        if (reportResult != null && reportResult.getResults() != null && !reportResult.getResults().isEmpty()) {
            for (QueryResult.Result item : reportResult.getResults()) {
                // Append the year and document from each result
                reportTextBuilder.append(item.getYear())
                        .append(": ")
                        .append(item.getDocument())
                        .append("\n");
            }
        } else {
            log.error("No report result found for company: {}", companyName);
            throw new RuntimeException("No report result found");
        }
        String reportText = reportTextBuilder.toString();
        log.info("Obtained report text (truncated): {}", reportText.substring(0, Math.min(100, reportText.length())));

        log.info("Querying call transcript from vector service for company: {}", companyName);
        QueryResult callTranscriptResult = vectorQueryService.queryCallTranscripts(companyName, 2);
        StringBuilder callTranscriptBuilder = new StringBuilder();
        if (callTranscriptResult != null && callTranscriptResult.getResults() != null && !callTranscriptResult.getResults().isEmpty()) {
            for (QueryResult.Result item : callTranscriptResult.getResults()) {
                // Append the year and document from each result
                callTranscriptBuilder.append(item.getYear())
                        .append(": ")
                        .append(item.getDocument())
                        .append("\n");
            }
        } else {
            log.error("No call transcript result found for company: {}", companyName);
            throw new RuntimeException("No call transcript result found");
        }
        String callTranscript = callTranscriptBuilder.toString();
        log.info("Obtained call transcript text (truncated): {}", callTranscript.substring(0, Math.min(100, callTranscript.length())));
        // Build the request body for the Dify API with streaming response mode.
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("stock_history_info", stockHistoryInfo);
        inputs.put("name", companyName);
        inputs.put("company_financial_ratios", companyFinancialRatio);
        inputs.put("report", reportText);
        inputs.put("call_transcripts", callTranscript);

        Map<String, Object> difyRequestBody = new HashMap<>();
        difyRequestBody.put("inputs", inputs);
        difyRequestBody.put("response_mode", "streaming"); // Use streaming response mode
        difyRequestBody.put("user", userId.toString());

        log.debug("Dify request body: {}", difyRequestBody);

        log.info("Sending streaming request to Dify API at URL: {}", difyGenerateUrl);
        CompletableFuture<ReportGenerationResponse> futureResponse =
                webClient.post()
                        .uri(difyGenerateUrl)
                        .header("Authorization", "Bearer " + difyGenerateApiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(difyRequestBody)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .retrieve()
                        .bodyToFlux(String.class)
                        // Log each raw event received
                        .doOnNext(event -> log.debug("Received raw event: {}", event))
                        // Filter for the event that indicates the workflow has finished
                        .filter(event -> event.contains("\"event\": \"workflow_finished\""))
                        // Instead of extracting report via extractReportContent, extract the output field directly
                        .map(event -> {
                            String output = extractOutput(event);
                            log.info("Output field: {}", output);
                            try {
                                ObjectMapper mapper = new ObjectMapper();
                                JsonNode outputJson = mapper.readTree(output);
                                // Directly get the "report" field from output JSON
                                return outputJson.path("report").toString();
                            } catch (Exception e) {
                                log.error("Failed to extract report from output: {}", output, e);
                                return "";
                            }
                        })
                        .collectList()
                        .map(messages -> {
                            // Merge all report fragments into a single string (assuming one valid report)
                            String validReport = String.join("", messages);
                            log.info("Final report content: {}", validReport);

                            // Validate the report using the structured JSON format
                            if (!validateGeneratedReport(userId, validReport)) {
                                log.error("Validation failed for report: {}", validReport);
                                throw new RuntimeException("Report validation failed");
                            }
                            log.info("Report validation passed");

                            // Save the original report in the database
                            saveReportToDB(userId, companyName, reportId, validReport);
                            log.info("Report saved to the database with reportId: {}", reportId);

                            // Set a Redis key to prevent generating a duplicate report within one hour
                            redisTemplate.opsForValue().set(redisKey, true, 3600, TimeUnit.SECONDS);
                            log.info("Set Redis key {} with a TTL of 3600 seconds", redisKey);

                            // Build the response object
                            ReportGenerationResponse response = new ReportGenerationResponse();
                            response.setReportId(reportId);
                            response.setReportContent(validReport);
                            log.info("Report generation completed successfully for reportId: {}", reportId);
                            return response;
                        })
                        .toFuture();

        return futureResponse;
    }

    @Override
    public List<ReportGenerationResponse> getReportsByUser(Long userId) {
        String cacheKey = "user:reports:" + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && cached instanceof List<?>) {
            return (List<ReportGenerationResponse>) cached;
        }

        // Query UserReports from database
        LambdaQueryWrapper<UserReports> query = new LambdaQueryWrapper<>();
        query.eq(UserReports::getUserId, userId);
        List<UserReports> urList = userReportsMapper.selectList(query);

        List<ReportGenerationResponse> resultList = new ArrayList<>();
        // For each report, use getReportDetail to retrieve the full report details
        for (UserReports ur : urList) {
            ReportGenerationResponse detail = getReportDetail(ur.getReportId());
            if (detail != null) {
                resultList.add(detail);
            }
        }

        // Cache the list of report details for 3600 seconds
        redisTemplate.opsForValue().set(cacheKey, resultList, 3600, TimeUnit.SECONDS);
        return resultList;
    }

    @Override
    public ReportGenerationResponse getReportDetail(String reportId) {
        String cacheKey = "report:detail:" + reportId;

        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && cached instanceof ReportGenerationResponse) {
            return (ReportGenerationResponse) cached;
        }

        ReportContent rc = getReportContentByReportId(reportId);
        if (rc == null) {
            return null;
        }

        ReportGenerationResponse response = new ReportGenerationResponse();
        response.setReportId(reportId);
        response.setReportContent(rc.getContent());

        redisTemplate.opsForValue().set(cacheKey, response, 3600, TimeUnit.SECONDS);
        return response;
    }
    // Helper method: Query stock history and return a JSON string.
    private String getStockHistoryInfo(String companyName) {
        Map<String, String> companyTickerMapping = new HashMap<>();
        companyTickerMapping.put("WeWork Inc", "WEWKQ");
        companyTickerMapping.put("Amazon.com Inc", "AMZN");
        companyTickerMapping.put("Ford Motor Co", "F");
        companyTickerMapping.put("American Airlines Group Inc", "AAL");

        String ticker = companyTickerMapping.get(companyName);
        if (ticker == null) {
            return "{\"headers\":[], \"data\": []}";
        }

        List<StockHistory> historyList = stockHistoryService.lambdaQuery()
                .eq(StockHistory::getTicker, ticker)
                .list();

        List<String> headers = Arrays.asList("date", "open", "high", "low", "close", "volume", "dividends", "stock_splits");
        List<Map<String, Object>> data = new ArrayList<>();
        for (StockHistory sh : historyList) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", sh.getDate());
            map.put("open", sh.getOpen());
            map.put("high", sh.getHigh());
            map.put("low", sh.getLow());
            map.put("close", sh.getClose());
            map.put("volume", sh.getVolume());
            map.put("dividends", sh.getDividends());
            map.put("stock_splits", sh.getStockSplits());
            data.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        result.put("data", data);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Error serializing stock history info for {}: {}", companyName, e.getMessage());
            return "Error serializing stock history info for " + companyName;
        }
    }

    // Helper method: Query financial ratios and return a JSON string.
    private String getCompanyFinancialRatio(String companyName) {
        List<CompanyFinancialRatios> ratiosList = financialRatiosService.lambdaQuery()
                .eq(CompanyFinancialRatios::getCompanyName, companyName)
                .list();

        List<String> headers = Arrays.asList(
                "company_id", "company_name", "fiscal_year", "latest_update_date",
                "shareholders_equity", "cash_and_cash_equivalents", "total_current_asset",
                "total_current_liab", "long_term_debt", "short_term_investment",
                "other_short_term_liab", "shares_outstanding", "current_debt",
                "total_asset", "total_equity", "total_liab", "net_income",
                "total_revenue", "inventory", "investment_in_assets", "net_debt"
        );

        List<Map<String, Object>> data = new ArrayList<>();
        for (CompanyFinancialRatios ratio : ratiosList) {
            Map<String, Object> map = new HashMap<>();
            map.put("company_id", ratio.getCompanyId());
            map.put("company_name", ratio.getCompanyName());
            map.put("fiscal_year", ratio.getFiscalYear());
            map.put("latest_update_date", ratio.getLatestUpdateDate());
            map.put("shareholders_equity", ratio.getShareholdersEquity());
            map.put("cash_and_cash_equivalents", ratio.getCashAndCashEquivalents());
            map.put("total_current_asset", ratio.getTotalCurrentAsset());
            map.put("total_current_liab", ratio.getTotalCurrentLiab());
            map.put("long_term_debt", ratio.getLongTermDebt());
            map.put("short_term_investment", ratio.getShortTermInvestment());
            map.put("other_short_term_liab", ratio.getOtherShortTermLiab());
            map.put("shares_outstanding", ratio.getSharesOutstanding());
            map.put("current_debt", ratio.getCurrentDebt());
            map.put("total_asset", ratio.getTotalAsset());
            map.put("total_equity", ratio.getTotalEquity());
            map.put("total_liab", ratio.getTotalLiab());
            map.put("net_income", ratio.getNetIncome());
            map.put("total_revenue", ratio.getTotalRevenue());
            map.put("inventory", ratio.getInventory());
            map.put("investment_in_assets", ratio.getInvestmentInAssets());
            map.put("net_debt", ratio.getNetDebt());
            data.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        result.put("data", data);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(result);
        } catch (Exception e) {
            log.error("Error serializing financial ratio info for {}: {}", companyName, e.getMessage());
            return "Error serializing financial ratio info for " + companyName;
        }
    }

    // Validate the generated report using the Dify validate API.
    private boolean validateGeneratedReport(Long userId, String generatedReport) {
        Map<String, Object> validateInputs = Collections.singletonMap("report", generatedReport);
        Map<String, Object> validateRequestBody = new HashMap<>();
        validateRequestBody.put("inputs", validateInputs);
        validateRequestBody.put("response_mode", "blocking");
        validateRequestBody.put("user", userId.toString());

        org.springframework.http.HttpHeaders validateHeaders = new org.springframework.http.HttpHeaders();
        validateHeaders.set("Authorization", "Bearer " + difyValidateApiKey);
        validateHeaders.set("Content-Type", "application/json");
        org.springframework.http.HttpEntity<Map<String, Object>> validateEntity =
                new org.springframework.http.HttpEntity<>(validateRequestBody, validateHeaders);

        log.info("Sending validation request to Dify validate API at URL: {}", difyValidateUrl);
        Map<String, Object> validateResponse = restTemplate.postForObject(difyValidateUrl, validateEntity, Map.class);
        if (validateResponse == null) {
            log.error("Validation response is null");
            return false;
        }

        Object dataObj = validateResponse.get("data");
        if (dataObj == null || !(dataObj instanceof Map)) {
            log.error("Validation response 'data' field is missing or invalid: {}", validateResponse);
            return false;
        }
        Map<String, Object> data = (Map<String, Object>) dataObj;
        Object outputsObj = data.get("outputs");
        if (outputsObj == null || !(outputsObj instanceof Map)) {
            log.error("Validation response 'outputs' field is missing or invalid: {}", data);
            return false;
        }
        Map<String, Object> outputs = (Map<String, Object>) outputsObj;

        Object textObj = outputs.get("text");
        if (textObj == null || !(textObj instanceof String)) {
            log.error("Validation response 'text' field is missing or invalid: {}", outputs);
            return false;
        }
        String text = (String) textObj;
        log.info("Validation text: {}", text);

        boolean pass;
        try {
            JsonNode node = new ObjectMapper().readTree(text);
            pass = node.path("pass").asBoolean();
        } catch (Exception e) {
            log.error("Failed to parse validation text as JSON: {}", e.getMessage());
            return false;
        }
        log.info("Validation result: {}", pass);
        return pass;
    }

    private void saveReportToDB(Long userId, String companyName, String reportId, String content) {
        log.info("Saving report to database with reportId: {}", reportId);

        UserReports ur = new UserReports();
        ur.setUserId(userId);
        ur.setReportId(reportId);
        ur.setCompanyName(companyName);
        userReportsMapper.insert(ur);

        ReportContent rc = new ReportContent();
        rc.setReportId(reportId);
        rc.setContent(content);
        reportContentMapper.insert(rc);

        String userReportsCacheKey = "user:reports:" + userId;
        String reportDetailCacheKey = "report:detail:" + reportId;

        redisTemplate.delete(userReportsCacheKey);
        redisTemplate.delete(reportDetailCacheKey);
        log.info("Deleted old cache entries for keys: {} and {}", userReportsCacheKey, reportDetailCacheKey);

        ReportGenerationResponse detailResponse = new ReportGenerationResponse();
        detailResponse.setReportId(reportId);
        detailResponse.setReportContent(content);
        redisTemplate.opsForValue().set(reportDetailCacheKey, detailResponse, 3600, TimeUnit.SECONDS);
        log.info("Inserted new report detail into cache with key: {}", reportDetailCacheKey);
       }

    // Retrieve a report content by its reportId.
    private ReportContent getReportContentByReportId(String reportId) {
        LambdaQueryWrapper<ReportContent> query = new LambdaQueryWrapper<>();
        query.eq(ReportContent::getReportId, reportId);
        return reportContentMapper.selectOne(query);
    }

    // Create a RestTemplate with custom timeout settings.
    private RestTemplate createRestTemplateWithTimeout() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(300_000);
        return new RestTemplate(factory);
    }


    private String extractOutput(String event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(event);
            JsonNode outputNode = root.path("data").path("outputs").path("output");
            return outputNode.asText();
        } catch (Exception e) {
            log.error("Failed to extract output from event: {}", event);
            return "";
        }
    }
}