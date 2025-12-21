package com.Investube.mvc.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FinancialData {
    private Integer financialId;
    private String stockCode;
    private String stockName; // 조인용 필드
    private Integer fiscalYear;
    private Integer fiscalQuarter;
    
    // 재무제표 원본 데이터
    private Long revenue;
    private Long operatingProfit;
    private Long netIncome;
    private Long totalAssets;
    private Long totalEquity;
    private Long totalLiabilities;
    private Long cashFlowOperating;
    private Long cashFlowInvesting;
    private Long cashFlowFinancing;
    
    // 시장 데이터
    private Long marketCap;
    private BigDecimal stockPrice;
    private Long sharesOutstanding;
    
    // 계산된 재무 지표
    private BigDecimal revenueGrowthRate;
    private BigDecimal operatingMargin;
    private BigDecimal roe;
    private BigDecimal debtRatio;
    private Long fcf;
    private BigDecimal perRatio;
    private BigDecimal pbrRatio;
    
    // 종합 점수
    private BigDecimal totalScore;
    
    // 메타 정보
    private String dataSource;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    // Getters and Setters
    public Integer getFinancialId() {
        return financialId;
    }

    public void setFinancialId(Integer financialId) {
        this.financialId = financialId;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public Integer getFiscalQuarter() {
        return fiscalQuarter;
    }

    public void setFiscalQuarter(Integer fiscalQuarter) {
        this.fiscalQuarter = fiscalQuarter;
    }

    public Long getRevenue() {
        return revenue;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }

    public Long getOperatingProfit() {
        return operatingProfit;
    }

    public void setOperatingProfit(Long operatingProfit) {
        this.operatingProfit = operatingProfit;
    }

    public Long getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(Long netIncome) {
        this.netIncome = netIncome;
    }

    public Long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(Long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public Long getTotalEquity() {
        return totalEquity;
    }

    public void setTotalEquity(Long totalEquity) {
        this.totalEquity = totalEquity;
    }

    public Long getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(Long totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public Long getCashFlowOperating() {
        return cashFlowOperating;
    }

    public void setCashFlowOperating(Long cashFlowOperating) {
        this.cashFlowOperating = cashFlowOperating;
    }

    public Long getCashFlowInvesting() {
        return cashFlowInvesting;
    }

    public void setCashFlowInvesting(Long cashFlowInvesting) {
        this.cashFlowInvesting = cashFlowInvesting;
    }

    public Long getCashFlowFinancing() {
        return cashFlowFinancing;
    }

    public void setCashFlowFinancing(Long cashFlowFinancing) {
        this.cashFlowFinancing = cashFlowFinancing;
    }

    public Long getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }

    public BigDecimal getStockPrice() {
        return stockPrice;
    }

    public void setStockPrice(BigDecimal stockPrice) {
        this.stockPrice = stockPrice;
    }

    public Long getSharesOutstanding() {
        return sharesOutstanding;
    }

    public void setSharesOutstanding(Long sharesOutstanding) {
        this.sharesOutstanding = sharesOutstanding;
    }

    public BigDecimal getRevenueGrowthRate() {
        return revenueGrowthRate;
    }

    public void setRevenueGrowthRate(BigDecimal revenueGrowthRate) {
        this.revenueGrowthRate = revenueGrowthRate;
    }

    public BigDecimal getOperatingMargin() {
        return operatingMargin;
    }

    public void setOperatingMargin(BigDecimal operatingMargin) {
        this.operatingMargin = operatingMargin;
    }

    public BigDecimal getRoe() {
        return roe;
    }

    public void setRoe(BigDecimal roe) {
        this.roe = roe;
    }

    public BigDecimal getDebtRatio() {
        return debtRatio;
    }

    public void setDebtRatio(BigDecimal debtRatio) {
        this.debtRatio = debtRatio;
    }

    public Long getFcf() {
        return fcf;
    }

    public void setFcf(Long fcf) {
        this.fcf = fcf;
    }

    public BigDecimal getPerRatio() {
        return perRatio;
    }

    public void setPerRatio(BigDecimal perRatio) {
        this.perRatio = perRatio;
    }

    public BigDecimal getPbrRatio() {
        return pbrRatio;
    }

    public void setPbrRatio(BigDecimal pbrRatio) {
        this.pbrRatio = pbrRatio;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
