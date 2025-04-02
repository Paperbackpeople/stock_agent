package com.example.stock_analysis.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.stock_analysis.entity.CompanyFinancialRatios;
import com.example.stock_analysis.mapper.CompanyFinancialRatiosMapper;
import com.example.stock_analysis.service.ICompanyFinancialRatiosService;
import org.springframework.stereotype.Service;

@Service
public class CompanyFinancialRatiosServiceImpl
        extends ServiceImpl<CompanyFinancialRatiosMapper, CompanyFinancialRatios>
        implements ICompanyFinancialRatiosService {
}