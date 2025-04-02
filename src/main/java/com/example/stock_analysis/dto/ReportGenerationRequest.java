package com.example.stock_analysis.dto;

public class ReportGenerationRequest {
    private Long userId;
    private String companyName;

    // getters & setters
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public String getCompanyName() {
        return companyName;
    }
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}