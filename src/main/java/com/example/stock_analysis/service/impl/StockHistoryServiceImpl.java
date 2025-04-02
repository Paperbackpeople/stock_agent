package com.example.stock_analysis.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.stock_analysis.entity.StockHistory;
import com.example.stock_analysis.mapper.StockHistoryMapper;
import com.example.stock_analysis.service.IStockHistoryService;
import org.springframework.stereotype.Service;

@Service
public class StockHistoryServiceImpl extends ServiceImpl<StockHistoryMapper, StockHistory> implements IStockHistoryService {
}