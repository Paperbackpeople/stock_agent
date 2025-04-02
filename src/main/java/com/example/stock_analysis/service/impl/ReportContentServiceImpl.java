package com.example.stock_analysis.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.stock_analysis.entity.ReportContent;
import com.example.stock_analysis.mapper.ReportContentMapper;
import com.example.stock_analysis.service.IReportContentService;
import org.springframework.stereotype.Service;

@Service
public class ReportContentServiceImpl
        extends ServiceImpl<ReportContentMapper, ReportContent>
        implements IReportContentService {

}