package com.example.stock_analysis.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.stock_analysis.entity.UserReports;
import com.example.stock_analysis.mapper.UserReportsMapper;
import com.example.stock_analysis.service.IUserReportsService;
import org.springframework.stereotype.Service;

@Service
public class UserReportsServiceImpl
        extends ServiceImpl<UserReportsMapper, UserReports>
        implements IUserReportsService {

}