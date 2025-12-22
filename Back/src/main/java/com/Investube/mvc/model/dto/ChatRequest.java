package com.Investube.mvc.model.dto;

/**
 * 챗봇 요청 DTO
 */
public class ChatRequest {
    private String stockCode;
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String stockCode, String message) {
        this.stockCode = stockCode;
        this.message = message;
    }

    public String getStockCode() {
        return stockCode;
    }

    public void setStockCode(String stockCode) {
        this.stockCode = stockCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
