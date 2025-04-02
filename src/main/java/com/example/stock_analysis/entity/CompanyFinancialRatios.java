package com.example.stock_analysis.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;


@TableName("company_financial_ratios")
public class CompanyFinancialRatios {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer companyId;
    private String companyName;
    private Integer fiscalYear;
    private Date latestUpdateDate;
    private BigDecimal shareholdersEquity;
    private BigDecimal cashAndCashEquivalents;
    private BigDecimal totalCurrentAsset;
    private BigDecimal totalCurrentLiab;
    private BigDecimal longTermDebt;
    private BigDecimal shortTermInvestment;
    private BigDecimal otherShortTermLiab;
    private BigDecimal sharesOutstanding;
    private BigDecimal currentDebt;
    private BigDecimal totalAsset;
    private BigDecimal totalEquity;
    private BigDecimal totalLiab;
    private BigDecimal netIncome;
    private BigDecimal totalRevenue;
    private BigDecimal inventory;
    private BigDecimal investmentInAssets;
    private BigDecimal netDebt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public Date getLatestUpdateDate() {
        return latestUpdateDate;
    }

    public void setLatestUpdateDate(Date latestUpdateDate) {
        this.latestUpdateDate = latestUpdateDate;
    }

    public BigDecimal getShareholdersEquity() {
        return shareholdersEquity;
    }

    public void setShareholdersEquity(BigDecimal shareholdersEquity) {
        this.shareholdersEquity = shareholdersEquity;
    }

    public BigDecimal getCashAndCashEquivalents() {
        return cashAndCashEquivalents;
    }

    public void setCashAndCashEquivalents(BigDecimal cashAndCashEquivalents) {
        this.cashAndCashEquivalents = cashAndCashEquivalents;
    }

    public BigDecimal getTotalCurrentAsset() {
        return totalCurrentAsset;
    }

    public void setTotalCurrentAsset(BigDecimal totalCurrentAsset) {
        this.totalCurrentAsset = totalCurrentAsset;
    }

    public BigDecimal getTotalCurrentLiab() {
        return totalCurrentLiab;
    }

    public void setTotalCurrentLiab(BigDecimal totalCurrentLiab) {
        this.totalCurrentLiab = totalCurrentLiab;
    }

    public BigDecimal getLongTermDebt() {
        return longTermDebt;
    }

    public void setLongTermDebt(BigDecimal longTermDebt) {
        this.longTermDebt = longTermDebt;
    }

    public BigDecimal getShortTermInvestment() {
        return shortTermInvestment;
    }

    public void setShortTermInvestment(BigDecimal shortTermInvestment) {
        this.shortTermInvestment = shortTermInvestment;
    }

    public BigDecimal getOtherShortTermLiab() {
        return otherShortTermLiab;
    }

    public void setOtherShortTermLiab(BigDecimal otherShortTermLiab) {
        this.otherShortTermLiab = otherShortTermLiab;
    }

    public BigDecimal getSharesOutstanding() {
        return sharesOutstanding;
    }

    public void setSharesOutstanding(BigDecimal sharesOutstanding) {
        this.sharesOutstanding = sharesOutstanding;
    }

    public BigDecimal getCurrentDebt() {
        return currentDebt;
    }

    public void setCurrentDebt(BigDecimal currentDebt) {
        this.currentDebt = currentDebt;
    }

    public BigDecimal getTotalAsset() {
        return totalAsset;
    }

    public void setTotalAsset(BigDecimal totalAsset) {
        this.totalAsset = totalAsset;
    }

    public BigDecimal getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(BigDecimal totalEquity) {
        this.totalEquity = totalEquity;
    }

    public BigDecimal getTotalLiab() {
        return totalLiab;
    }

    public void setTotalLiab(BigDecimal totalLiab) {
        this.totalLiab = totalLiab;
    }

    public BigDecimal getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getInventory() {
        return inventory;
    }

    public void setInventory(BigDecimal inventory) {
        this.inventory = inventory;
    }

    public BigDecimal getInvestmentInAssets() {
        return investmentInAssets;
    }

    public void setInvestmentInAssets(BigDecimal investmentInAssets) {
        this.investmentInAssets = investmentInAssets;
    }

    public BigDecimal getNetDebt() {
        return netDebt;
    }

    public void setNetDebt(BigDecimal netDebt) {
        this.netDebt = netDebt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CompanyFinancialRatios that = (CompanyFinancialRatios) o;
        return Objects.equals(id, that.id) && Objects.equals(companyId, that.companyId) && Objects.equals(companyName, that.companyName) && Objects.equals(fiscalYear, that.fiscalYear) && Objects.equals(latestUpdateDate, that.latestUpdateDate) && Objects.equals(shareholdersEquity, that.shareholdersEquity) && Objects.equals(cashAndCashEquivalents, that.cashAndCashEquivalents) && Objects.equals(totalCurrentAsset, that.totalCurrentAsset) && Objects.equals(totalCurrentLiab, that.totalCurrentLiab) && Objects.equals(longTermDebt, that.longTermDebt) && Objects.equals(shortTermInvestment, that.shortTermInvestment) && Objects.equals(otherShortTermLiab, that.otherShortTermLiab) && Objects.equals(sharesOutstanding, that.sharesOutstanding) && Objects.equals(currentDebt, that.currentDebt) && Objects.equals(totalAsset, that.totalAsset) && Objects.equals(totalEquity, that.totalEquity) && Objects.equals(totalLiab, that.totalLiab) && Objects.equals(netIncome, that.netIncome) && Objects.equals(totalRevenue, that.totalRevenue) && Objects.equals(inventory, that.inventory) && Objects.equals(investmentInAssets, that.investmentInAssets) && Objects.equals(netDebt, that.netDebt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyId, companyName, fiscalYear, latestUpdateDate, shareholdersEquity, cashAndCashEquivalents, totalCurrentAsset, totalCurrentLiab, longTermDebt, shortTermInvestment, otherShortTermLiab, sharesOutstanding, currentDebt, totalAsset, totalEquity, totalLiab, netIncome, totalRevenue, inventory, investmentInAssets, netDebt);
    }
}