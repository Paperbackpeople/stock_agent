package com.example.stock_analysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/vector")
public class VectorQueryController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${vector.service.base.url}")
    private String vectorServiceBaseUrl;

    @GetMapping("/stock_data")
    public ResponseEntity<String> queryStockData(@RequestParam("company_name") String companyName,
                                                 @RequestParam(value = "n_results", defaultValue = "10") int nResults) {
        String url = vectorServiceBaseUrl + "/query/stock_data?company_name=" + companyName + "&n_results=" + nResults;
        String result = restTemplate.getForObject(url, String.class);
        return ResponseEntity.ok(result);
    }
}