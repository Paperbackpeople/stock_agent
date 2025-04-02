package com.example.stock_analysis.controller;

import com.example.stock_analysis.entity.CompanyFinancialRatios;
import com.example.stock_analysis.service.ICompanyFinancialRatiosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/financial-ratios")
public class StockFinancialRatiosController {

    @Autowired
    private ICompanyFinancialRatiosService financialRatiosService;

    /**
     * 根据公司名称查询所有财务数据
     * 例如请求: GET /api/financial-ratios?companyName=American Airlines Group Inc
     */
    @GetMapping
    public ResponseEntity<?> getFinancialRatiosByCompany(@RequestParam("companyName") String companyName) {
        List<CompanyFinancialRatios> list = financialRatiosService.lambdaQuery()
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

        List<Map<String, Object>> data = list.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("company_id", record.getCompanyId());
            map.put("company_name", record.getCompanyName());
            map.put("fiscal_year", record.getFiscalYear());
            map.put("latest_update_date", record.getLatestUpdateDate());
            map.put("shareholders_equity", record.getShareholdersEquity());
            map.put("cash_and_cash_equivalents", record.getCashAndCashEquivalents());
            map.put("total_current_asset", record.getTotalCurrentAsset());
            map.put("total_current_liab", record.getTotalCurrentLiab());
            map.put("long_term_debt", record.getLongTermDebt());
            map.put("short_term_investment", record.getShortTermInvestment());
            map.put("other_short_term_liab", record.getOtherShortTermLiab());
            map.put("shares_outstanding", record.getSharesOutstanding());
            map.put("current_debt", record.getCurrentDebt());
            map.put("total_asset", record.getTotalAsset());
            map.put("total_equity", record.getTotalEquity());
            map.put("total_liab", record.getTotalLiab());
            map.put("net_income", record.getNetIncome());
            map.put("total_revenue", record.getTotalRevenue());
            map.put("inventory", record.getInventory());
            map.put("investment_in_assets", record.getInvestmentInAssets());
            map.put("net_debt", record.getNetDebt());
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("headers", headers);
        result.put("data", data);

        return ResponseEntity.ok(result);
    }
}