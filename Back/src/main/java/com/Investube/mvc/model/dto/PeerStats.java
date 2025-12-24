package com.Investube.mvc.model.dto;

import java.math.BigDecimal;

/**
 * 동종업계 통계 데이터
 * 같은 업종 기업들의 재무 지표 평균/중앙값 및 순위 정보
 */
public class PeerStats {
    private String industry; // 업종명
    private int peerCount; // 동종업계 기업 수

    // 평균 지표
    private BigDecimal avgOperatingMargin;
    private BigDecimal avgRoe;
    private BigDecimal avgDebtRatio;
    private BigDecimal avgPer;
    private BigDecimal avgPbr;
    private BigDecimal avgRevenueGrowthRate;

    // 중앙값 지표
    private BigDecimal medianOperatingMargin;
    private BigDecimal medianRoe;
    private BigDecimal medianDebtRatio;
    private BigDecimal medianPer;
    private BigDecimal medianPbr;

    // 현재 기업의 백분위 순위 (상위 몇 %인지, 0~100)
    private Integer operatingMarginPercentile; // 높을수록 좋음
    private Integer roePercentile; // 높을수록 좋음
    private Integer debtRatioPercentile; // 낮을수록 좋음 (하위 몇 %)
    private Integer perPercentile; // 낮을수록 좋음
    private Integer pbrPercentile; // 낮을수록 좋음

    // Getters and Setters
    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(int peerCount) {
        this.peerCount = peerCount;
    }

    public BigDecimal getAvgOperatingMargin() {
        return avgOperatingMargin;
    }

    public void setAvgOperatingMargin(BigDecimal avgOperatingMargin) {
        this.avgOperatingMargin = avgOperatingMargin;
    }

    public BigDecimal getAvgRoe() {
        return avgRoe;
    }

    public void setAvgRoe(BigDecimal avgRoe) {
        this.avgRoe = avgRoe;
    }

    public BigDecimal getAvgDebtRatio() {
        return avgDebtRatio;
    }

    public void setAvgDebtRatio(BigDecimal avgDebtRatio) {
        this.avgDebtRatio = avgDebtRatio;
    }

    public BigDecimal getAvgPer() {
        return avgPer;
    }

    public void setAvgPer(BigDecimal avgPer) {
        this.avgPer = avgPer;
    }

    public BigDecimal getAvgPbr() {
        return avgPbr;
    }

    public void setAvgPbr(BigDecimal avgPbr) {
        this.avgPbr = avgPbr;
    }

    public BigDecimal getAvgRevenueGrowthRate() {
        return avgRevenueGrowthRate;
    }

    public void setAvgRevenueGrowthRate(BigDecimal avgRevenueGrowthRate) {
        this.avgRevenueGrowthRate = avgRevenueGrowthRate;
    }

    public BigDecimal getMedianOperatingMargin() {
        return medianOperatingMargin;
    }

    public void setMedianOperatingMargin(BigDecimal medianOperatingMargin) {
        this.medianOperatingMargin = medianOperatingMargin;
    }

    public BigDecimal getMedianRoe() {
        return medianRoe;
    }

    public void setMedianRoe(BigDecimal medianRoe) {
        this.medianRoe = medianRoe;
    }

    public BigDecimal getMedianDebtRatio() {
        return medianDebtRatio;
    }

    public void setMedianDebtRatio(BigDecimal medianDebtRatio) {
        this.medianDebtRatio = medianDebtRatio;
    }

    public BigDecimal getMedianPer() {
        return medianPer;
    }

    public void setMedianPer(BigDecimal medianPer) {
        this.medianPer = medianPer;
    }

    public BigDecimal getMedianPbr() {
        return medianPbr;
    }

    public void setMedianPbr(BigDecimal medianPbr) {
        this.medianPbr = medianPbr;
    }

    public Integer getOperatingMarginPercentile() {
        return operatingMarginPercentile;
    }

    public void setOperatingMarginPercentile(Integer operatingMarginPercentile) {
        this.operatingMarginPercentile = operatingMarginPercentile;
    }

    public Integer getRoePercentile() {
        return roePercentile;
    }

    public void setRoePercentile(Integer roePercentile) {
        this.roePercentile = roePercentile;
    }

    public Integer getDebtRatioPercentile() {
        return debtRatioPercentile;
    }

    public void setDebtRatioPercentile(Integer debtRatioPercentile) {
        this.debtRatioPercentile = debtRatioPercentile;
    }

    public Integer getPerPercentile() {
        return perPercentile;
    }

    public void setPerPercentile(Integer perPercentile) {
        this.perPercentile = perPercentile;
    }

    public Integer getPbrPercentile() {
        return pbrPercentile;
    }

    public void setPbrPercentile(Integer pbrPercentile) {
        this.pbrPercentile = pbrPercentile;
    }

    /**
     * AI 프롬프트용 비교 문자열 생성
     */
    public String toPeerComparisonPrompt(FinancialData currentData) {
        if (peerCount < 2) {
            return String.format("동종업계(%s) 비교: 비교 가능한 동종업계 기업이 부족합니다 (%d개).",
                    industry, peerCount);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("동종업계 비교 (업종: %s, %d개 기업):\n", industry, peerCount));

        // 영업이익률
        if (avgOperatingMargin != null && currentData.getOperatingMargin() != null) {
            sb.append(String.format("- 영업이익률: 업계 평균 %.1f%%, 현재 기업 %.1f%%",
                    avgOperatingMargin, currentData.getOperatingMargin()));
            if (operatingMarginPercentile != null) {
                sb.append(String.format(" (상위 %d%%)\n", operatingMarginPercentile));
            } else {
                sb.append("\n");
            }
        }

        // ROE
        if (avgRoe != null && currentData.getRoe() != null) {
            sb.append(String.format("- ROE: 업계 평균 %.1f%%, 현재 기업 %.1f%%",
                    avgRoe, currentData.getRoe()));
            if (roePercentile != null) {
                sb.append(String.format(" (상위 %d%%)\n", roePercentile));
            } else {
                sb.append("\n");
            }
        }

        // 부채비율
        if (avgDebtRatio != null && currentData.getDebtRatio() != null) {
            sb.append(String.format("- 부채비율: 업계 평균 %.1f%%, 현재 기업 %.1f%%",
                    avgDebtRatio, currentData.getDebtRatio()));
            if (debtRatioPercentile != null) {
                sb.append(String.format(" (하위 %d%% - 낮을수록 좋음)\n", debtRatioPercentile));
            } else {
                sb.append("\n");
            }
        }

        // PER
        if (avgPer != null && currentData.getPerRatio() != null) {
            sb.append(String.format("- PER: 업계 평균 %.1f배, 현재 기업 %.1f배",
                    avgPer, currentData.getPerRatio()));
            if (perPercentile != null) {
                sb.append(String.format(" (하위 %d%% - 낮을수록 저평가)\n", perPercentile));
            } else {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
