package com.example.stock_analysis.controller;

import com.example.stock_analysis.entity.StockHistory;
import com.example.stock_analysis.service.IStockHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stock-history")
public class StockHistoryController {

    @Autowired
    private IStockHistoryService stockHistoryService;


    @GetMapping
    public ResponseEntity<?> getStockHistoryByCompany(@RequestParam("companyName") String companyName) {
        List<StockHistory> records = stockHistoryService.lambdaQuery()
                .eq(StockHistory::getTicker, companyName)
                .list();

        List<String> headers = Arrays.asList("date", "open", "high", "low", "close", "volume", "dividends", "stock_splits");

        List<Map<String, Object>> data = records.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", record.getDate());
            map.put("open", record.getOpen());
            map.put("high", record.getHigh());
            map.put("low", record.getLow());
            map.put("close", record.getClose());
            map.put("volume", record.getVolume());
            map.put("dividends", record.getDividends());
            map.put("stock_splits", record.getStockSplits());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        result.put("data", data);
        return ResponseEntity.ok(result);
    }
}
