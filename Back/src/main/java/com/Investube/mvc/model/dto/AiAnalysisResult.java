package com.Investube.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * AI 분석 결과
 */
public class AiAnalysisResult {
    
    @JsonProperty("scoreAdjustment")
    private BigDecimal scoreAdjustment = BigDecimal.ZERO; // -10 ~ +10
    
    @JsonProperty("weightAdjustment")
    private AiWeightAdjustment weightAdjustment;
    
    @JsonProperty("summary")
    private String summary;
    
    @JsonProperty("riskLevel")
    private String riskLevel; // LOW / MEDIUM / HIGH
    
    // 최종 점수 (기본 점수 + AI 보정)
    private BigDecimal finalScore;
    
    // 기본 점수 (참고용)
    private BigDecimal baseScore;

    public AiAnalysisResult() {
        this.weightAdjustment = new AiWeightAdjustment();
    }

    // Getters and Setters
    public BigDecimal getScoreAdjustment() {
        return scoreAdjustment;
    }

    public void setScoreAdjustment(BigDecimal scoreAdjustment) {
        this.scoreAdjustment = scoreAdjustment;
    }

    public AiWeightAdjustment getWeightAdjustment() {
        return weightAdjustment;
    }

    public void setWeightAdjustment(AiWeightAdjustment weightAdjustment) {
        this.weightAdjustment = weightAdjustment;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public BigDecimal getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(BigDecimal baseScore) {
        this.baseScore = baseScore;
    }
}
