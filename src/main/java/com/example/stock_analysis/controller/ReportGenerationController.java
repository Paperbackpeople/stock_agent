package com.example.stock_analysis.controller;

import com.example.stock_analysis.dto.ReportGenerationRequest;
import com.example.stock_analysis.dto.ReportGenerationResponse;
import com.example.stock_analysis.service.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reports")
public class ReportGenerationController {

    @Autowired
    private ReportGenerationService reportGenerationService;

    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<ReportGenerationResponse>> generateReport(@RequestBody ReportGenerationRequest request) {
        return reportGenerationService.generateReport(request)
                .thenApply(ResponseEntity::ok);
    }


    @GetMapping("/history")
    public ResponseEntity<List<ReportGenerationResponse>> getReportsByUser(@RequestParam("userId") Long userId) {
        List<ReportGenerationResponse> list = reportGenerationService.getReportsByUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/detail")
    public ResponseEntity<ReportGenerationResponse> getReportDetail(@RequestParam("reportId") String reportId) {
        ReportGenerationResponse response = reportGenerationService.getReportDetail(reportId);
        return ResponseEntity.ok(response);
    }
}