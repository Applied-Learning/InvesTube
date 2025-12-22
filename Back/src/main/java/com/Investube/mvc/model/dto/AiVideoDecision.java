package com.Investube.mvc.model.dto;

public class AiVideoDecision {

    private boolean allowed;
    private String reason;
    private Integer recommendedCategoryId;
    private String recommendedCategoryName;
    private java.util.List<Integer> recommendedCategoryIds;
    private java.util.List<String> recommendedCategoryNames;
    private Double confidence;

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getRecommendedCategoryId() {
        return recommendedCategoryId;
    }

    public void setRecommendedCategoryId(Integer recommendedCategoryId) {
        this.recommendedCategoryId = recommendedCategoryId;
    }

    public String getRecommendedCategoryName() {
        return recommendedCategoryName;
    }

    public void setRecommendedCategoryName(String recommendedCategoryName) {
        this.recommendedCategoryName = recommendedCategoryName;
    }

    public java.util.List<Integer> getRecommendedCategoryIds() {
        return recommendedCategoryIds;
    }

    public void setRecommendedCategoryIds(java.util.List<Integer> recommendedCategoryIds) {
        this.recommendedCategoryIds = recommendedCategoryIds;
    }

    public java.util.List<String> getRecommendedCategoryNames() {
        return recommendedCategoryNames;
    }

    public void setRecommendedCategoryNames(java.util.List<String> recommendedCategoryNames) {
        this.recommendedCategoryNames = recommendedCategoryNames;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
