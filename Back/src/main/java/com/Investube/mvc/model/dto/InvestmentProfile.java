package com.Investube.mvc.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvestmentProfile {
    private Integer profileId;
    private Integer userId;
    private String profileName;
    
    // 각 지표별 가중치
    private BigDecimal weightRevenueGrowth;
    private BigDecimal weightOperatingMargin;
    private BigDecimal weightRoe;
    private BigDecimal weightDebtRatio;
    private BigDecimal weightFcf;
    private BigDecimal weightPer;
    private BigDecimal weightPbr;
    
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Integer getProfileId() {
        return profileId;
    }

    public void setProfileId(Integer profileId) {
        this.profileId = profileId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public BigDecimal getWeightRevenueGrowth() {
        return weightRevenueGrowth;
    }

    public void setWeightRevenueGrowth(BigDecimal weightRevenueGrowth) {
        this.weightRevenueGrowth = weightRevenueGrowth;
    }

    public BigDecimal getWeightOperatingMargin() {
        return weightOperatingMargin;
    }

    public void setWeightOperatingMargin(BigDecimal weightOperatingMargin) {
        this.weightOperatingMargin = weightOperatingMargin;
    }

    public BigDecimal getWeightRoe() {
        return weightRoe;
    }

    public void setWeightRoe(BigDecimal weightRoe) {
        this.weightRoe = weightRoe;
    }

    public BigDecimal getWeightDebtRatio() {
        return weightDebtRatio;
    }

    public void setWeightDebtRatio(BigDecimal weightDebtRatio) {
        this.weightDebtRatio = weightDebtRatio;
    }

    public BigDecimal getWeightFcf() {
        return weightFcf;
    }

    public void setWeightFcf(BigDecimal weightFcf) {
        this.weightFcf = weightFcf;
    }

    public BigDecimal getWeightPer() {
        return weightPer;
    }

    public void setWeightPer(BigDecimal weightPer) {
        this.weightPer = weightPer;
    }

    public BigDecimal getWeightPbr() {
        return weightPbr;
    }

    public void setWeightPbr(BigDecimal weightPbr) {
        this.weightPbr = weightPbr;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
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
}
