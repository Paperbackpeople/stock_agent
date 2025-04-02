package com.example.stock_analysis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.stock_analysis.mapper")

public class StockAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockAnalysisApplication.class, args);
    }

}
