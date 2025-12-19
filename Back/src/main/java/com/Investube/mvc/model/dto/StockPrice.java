package com.Investube.mvc.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class StockPrice {
    private int priceId;
    private String stockCode;
    private LocalDate tradeDate;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private Long volume;
    private Long marketCap;
    private LocalDateTime createdAt;
    
    public StockPrice() {}
    
    public StockPrice(String stockCode, LocalDate tradeDate, BigDecimal closePrice, Long volume) {
        this.stockCode = stockCode;
        this.tradeDate = tradeDate;
        this.closePrice = closePrice;
        this.volume = volume;
    }
    
    // Getters and Setters
    public int getPriceId() {
        return priceId;
    }
    
    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }
    
    public String getStockCode() {
        return stockCode;
    }
    
    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }
    
    public LocalDate getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    public BigDecimal getOpenPrice() {
        return openPrice;
    }
    
    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }
    
    public BigDecimal getHighPrice() {
        return highPrice;
    }
    
    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }
    
    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    
    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }
    
    public BigDecimal getClosePrice() {
        return closePrice;
    }
    
    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }
    
    public Long getVolume() {
        return volume;
    }
    
    public void setVolume(Long volume) {
        this.volume = volume;
    }
    
    public Long getMarketCap() {
        return marketCap;
    }
    
    public void setMarketCap(Long marketCap) {
        this.marketCap = marketCap;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "StockPrice [priceId=" + priceId + ", stockCode=" + stockCode + ", tradeDate=" + tradeDate + 
               ", closePrice=" + closePrice + ", volume=" + volume + "]";
    }
}
