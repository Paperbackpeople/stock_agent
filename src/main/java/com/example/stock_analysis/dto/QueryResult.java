package com.example.stock_analysis.dto;

import java.util.List;
import java.util.Map;

public class QueryResult {
    private String message;
    private int maxYear;
    private int yearCutoff;
    private List<Result> results;

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getMaxYear() {
        return maxYear;
    }
    public void setMaxYear(int maxYear) {
        this.maxYear = maxYear;
    }
    public int getYearCutoff() {
        return yearCutoff;
    }
    public void setYearCutoff(int yearCutoff) {
        this.yearCutoff = yearCutoff;
    }
    public List<Result> getResults() {
        return results;
    }
    public void setResults(List<Result> results) {
        this.results = results;
    }

    public static class Result {
        private String id;
        private String document;
        private Map<String, Object> metadata;
        private double distance;
        private int year;

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getDocument() {
            return document;
        }
        public void setDocument(String document) {
            this.document = document;
        }
        public Map<String, Object> getMetadata() {
            return metadata;
        }
        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
        public double getDistance() {
            return distance;
        }
        public void setDistance(double distance) {
            this.distance = distance;
        }
        public int getYear() {
            return year;
        }
        public void setYear(int year) {
            this.year = year;
        }
    }
}