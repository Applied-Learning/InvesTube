package com.Investube.mvc.model.dto;

/**
 * 챗봇 응답 DTO
 */
public class ChatResponse {
    private String message;
    private boolean success;

    public ChatResponse() {
    }

    public ChatResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
