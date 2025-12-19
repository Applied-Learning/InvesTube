package com.Investube.mvc.model.dto;

import java.time.LocalDateTime;

public class Stock {
    private int stockId;
    private String stockCode;
    private String stockName;
    private String market;
    private String industry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public Stock() {}
    
    public Stock(String stockCode, String stockName, String market, String industry) {
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.market = market;
        this.industry = industry;
    }
    
    // Getters and Setters
    public int getStockId() {
        return stockId;
    }
    
    public void setStockId(int stockId) {
        this.stockId = stockId;
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
    
    public String getMarket() {
        return market;
    }
    
    public void setMarket(String market) {
        this.market = market;
    }
    
    public String getIndustry() {
        return industry;
    }
    
    public void setIndustry(String industry) {
        this.industry = industry;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "Stock [stockId=" + stockId + ", stockCode=" + stockCode + ", stockName=" + stockName + 
               ", market=" + market + ", industry=" + industry + "]";
    }
}
