package com.example.stock_analysis.service;


import com.example.stock_analysis.dto.QueryResult;

public interface VectorQueryService {
    QueryResult queryReport(String queryText, int nResults);
    QueryResult queryCallTranscripts(String queryText, int nResults);
}