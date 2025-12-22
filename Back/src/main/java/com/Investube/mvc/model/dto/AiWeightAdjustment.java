package com.Investube.mvc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

/**
 * AI가 제안하는 가중치 보정 값
 * -10 ~ +10 범위
 */
public class AiWeightAdjustment {
    
    @JsonProperty("revenueGrowth")
    private BigDecimal revenueGrowth = BigDecimal.ZERO;
    
    @JsonProperty("operatingMargin")
    private BigDecimal operatingMargin = BigDecimal.ZERO;
    
    @JsonProperty("roe")
    private BigDecimal roe = BigDecimal.ZERO;
    
    @JsonProperty("debtRatio")
    private BigDecimal debtRatio = BigDecimal.ZERO;
    
    @JsonProperty("fcf")
    private BigDecimal fcf = BigDecimal.ZERO;
    
    @JsonProperty("per")
    private BigDecimal per = BigDecimal.ZERO;
    
    @JsonProperty("pbr")
    private BigDecimal pbr = BigDecimal.ZERO;

    public AiWeightAdjustment() {}

    // Getters and Setters
    public BigDecimal getRevenueGrowth() {
        return revenueGrowth;
    }

    public void setRevenueGrowth(BigDecimal revenueGrowth) {
        this.revenueGrowth = revenueGrowth;
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

    public BigDecimal getFcf() {
        return fcf;
    }

    public void setFcf(BigDecimal fcf) {
        this.fcf = fcf;
    }

    public BigDecimal getPer() {
        return per;
    }

    public void setPer(BigDecimal per) {
        this.per = per;
    }

    public BigDecimal getPbr() {
        return pbr;
    }

    public void setPbr(BigDecimal pbr) {
        this.pbr = pbr;
    }
}
