package com.example.stock_analysis.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.util.Date;

@TableName("stock_history")
public class StockHistory {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String ticker; // 公司代码或名称
    private Date date;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private Long volume;
    private BigDecimal dividends;
    private BigDecimal stockSplits;

    // Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public BigDecimal getOpen() {
        return open;
    }
    public void setOpen(BigDecimal open) {
        this.open = open;
    }
    public BigDecimal getHigh() {
        return high;
    }
    public void setHigh(BigDecimal high) {
        this.high = high;
    }
    public BigDecimal getLow() {
        return low;
    }
    public void setLow(BigDecimal low) {
        this.low = low;
    }
    public BigDecimal getClose() {
        return close;
    }
    public void setClose(BigDecimal close) {
        this.close = close;
    }
    public Long getVolume() {
        return volume;
    }
    public void setVolume(Long volume) {
        this.volume = volume;
    }
    public BigDecimal getDividends() {
        return dividends;
    }
    public void setDividends(BigDecimal dividends) {
        this.dividends = dividends;
    }
    public BigDecimal getStockSplits() {
        return stockSplits;
    }
    public void setStockSplits(BigDecimal stockSplits) {
        this.stockSplits = stockSplits;
    }
}