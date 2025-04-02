package com.example.stock_analysis.service;

import com.example.stock_analysis.dto.ReportGenerationRequest;
import com.example.stock_analysis.dto.ReportGenerationResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ReportGenerationService {
    CompletableFuture<ReportGenerationResponse> generateReport(ReportGenerationRequest request);
    List<ReportGenerationResponse> getReportsByUser(Long userId);
    ReportGenerationResponse getReportDetail(String reportId);
}