package com.example.stock_analysis.dto;

public class ReportGenerationResponse {
    private String reportId;
    private String reportContent;

    // getters & setters
    public String getReportId() {
        return reportId;
    }
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    public String getReportContent() {
        return reportContent;
    }
    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }
}