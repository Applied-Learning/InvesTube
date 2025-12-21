package com.Investube.mvc.service;

import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.InvestmentProfile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 재무 분석 및 점수 계산 서비스
 */
@Service
public class FinancialAnalysisService {

    /**
     * 재무 지표 계산
     * 
     * @param current  현재 연도 재무 데이터
     * @param previous 전년도 재무 데이터 (매출 성장률 계산용)
     * @return 계산된 재무 지표가 포함된 FinancialData
     */
    public FinancialData calculateFinancialMetrics(FinancialData current, FinancialData previous) {
        // 매출 성장률 계산
        if (previous != null && previous.getRevenue() != null && previous.getRevenue() > 0) {
            BigDecimal growth = BigDecimal.valueOf(current.getRevenue() - previous.getRevenue())
                    .divide(BigDecimal.valueOf(previous.getRevenue()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            current.setRevenueGrowthRate(growth);
        }

        // 영업이익 성장률 계산
        if (previous != null && previous.getOperatingProfit() != null && previous.getOperatingProfit() != 0) {
            BigDecimal opGrowth = BigDecimal.valueOf(current.getOperatingProfit() - previous.getOperatingProfit())
                    .divide(BigDecimal.valueOf(previous.getOperatingProfit()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            current.setOperatingProfitGrowthRate(opGrowth);
        }

        // 영업이익률 계산: (영업이익 / 매출액) * 100
        if (current.getRevenue() != null && current.getRevenue() > 0 && current.getOperatingProfit() != null) {
            BigDecimal margin = BigDecimal.valueOf(current.getOperatingProfit())
                    .divide(BigDecimal.valueOf(current.getRevenue()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            current.setOperatingMargin(margin);
        }

        // ROE 계산: (당기순이익 / 자본총계) * 100
        if (current.getTotalEquity() != null && current.getTotalEquity() > 0 && current.getNetIncome() != null) {
            BigDecimal roe = BigDecimal.valueOf(current.getNetIncome())
                    .divide(BigDecimal.valueOf(current.getTotalEquity()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            current.setRoe(roe);
        }

        // 부채비율 계산: (부채총계 / 자본총계) * 100
        if (current.getTotalEquity() != null && current.getTotalEquity() > 0 && current.getTotalLiabilities() != null) {
            BigDecimal debtRatio = BigDecimal.valueOf(current.getTotalLiabilities())
                    .divide(BigDecimal.valueOf(current.getTotalEquity()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            current.setDebtRatio(debtRatio);
        }

        // FCF 계산: 영업활동 현금흐름 - 투자활동 현금흐름
        if (current.getCashFlowOperating() != null && current.getCashFlowInvesting() != null) {
            Long fcf = current.getCashFlowOperating() - current.getCashFlowInvesting();
            current.setFcf(fcf);
        }

        // PER 계산: 시가총액 / 당기순이익
        if (current.getNetIncome() != null && current.getNetIncome() > 0 && current.getMarketCap() != null) {
            BigDecimal per = BigDecimal.valueOf(current.getMarketCap())
                    .divide(BigDecimal.valueOf(current.getNetIncome()), 4, RoundingMode.HALF_UP);
            current.setPerRatio(per);
        }

        // PBR 계산: 시가총액 / 자본총계
        if (current.getTotalEquity() != null && current.getTotalEquity() > 0 && current.getMarketCap() != null) {
            BigDecimal pbr = BigDecimal.valueOf(current.getMarketCap())
                    .divide(BigDecimal.valueOf(current.getTotalEquity()), 4, RoundingMode.HALF_UP);
            current.setPbrRatio(pbr);
        }

        return current;
    }

    /**
     * 투자 점수 계산 (0~100점)
     * 
     * @param financialData 재무 데이터
     * @param profile       투자 성향 프로필
     * @return 0~100 사이의 점수
     */
    public BigDecimal calculateInvestmentScore(FinancialData financialData, InvestmentProfile profile) {
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO; // 실제 사용된 가중치 합계

        // 1. 매출 성장률 점수 (높을수록 좋음, 0~50% 범위로 정규화)
        if (financialData.getRevenueGrowthRate() != null) {
            BigDecimal revenueScore = normalizeScore(financialData.getRevenueGrowthRate(),
                    BigDecimal.valueOf(-10), BigDecimal.valueOf(50), true);
            totalScore = totalScore.add(revenueScore.multiply(profile.getWeightRevenueGrowth()));
            totalWeight = totalWeight.add(profile.getWeightRevenueGrowth());
        }

        // 2. 영업이익률 점수 (높을수록 좋음, 0~30% 범위로 정규화)
        if (financialData.getOperatingMargin() != null) {
            BigDecimal marginScore = normalizeScore(financialData.getOperatingMargin(),
                    BigDecimal.valueOf(-10), BigDecimal.valueOf(30), true);
            totalScore = totalScore.add(marginScore.multiply(profile.getWeightOperatingMargin()));
            totalWeight = totalWeight.add(profile.getWeightOperatingMargin());
        }

        // 3. ROE 점수 (높을수록 좋음, 0~30% 범위로 정규화)
        if (financialData.getRoe() != null) {
            BigDecimal roeScore = normalizeScore(financialData.getRoe(),
                    BigDecimal.valueOf(-10), BigDecimal.valueOf(30), true);
            totalScore = totalScore.add(roeScore.multiply(profile.getWeightRoe()));
            totalWeight = totalWeight.add(profile.getWeightRoe());
        }

        // 4. 부채비율 점수 (낮을수록 좋음, 0~300% 범위로 정규화, 역방향)
        if (financialData.getDebtRatio() != null) {
            BigDecimal debtScore = normalizeScore(financialData.getDebtRatio(),
                    BigDecimal.ZERO, BigDecimal.valueOf(300), false);
            totalScore = totalScore.add(debtScore.multiply(profile.getWeightDebtRatio()));
            totalWeight = totalWeight.add(profile.getWeightDebtRatio());
        }

        // 5. FCF 점수 (높을수록 좋음, -1000억~5000억 범위로 정규화)
        if (financialData.getFcf() != null) {
            BigDecimal fcfInBillion = BigDecimal.valueOf(financialData.getFcf()).divide(BigDecimal.valueOf(100), 4,
                    RoundingMode.HALF_UP);
            BigDecimal fcfScore = normalizeScore(fcfInBillion,
                    BigDecimal.valueOf(-1000), BigDecimal.valueOf(5000), true);
            totalScore = totalScore.add(fcfScore.multiply(profile.getWeightFcf()));
            totalWeight = totalWeight.add(profile.getWeightFcf());
        }

        // 6. PER 점수 (낮을수록 좋음, 0~50 범위로 정규화, 역방향)
        if (financialData.getPerRatio() != null) {
            BigDecimal perScore = normalizeScore(financialData.getPerRatio(),
                    BigDecimal.ZERO, BigDecimal.valueOf(50), false);
            totalScore = totalScore.add(perScore.multiply(profile.getWeightPer()));
            totalWeight = totalWeight.add(profile.getWeightPer());
        }

        // 7. PBR 점수 (낮을수록 좋음, 0~5 범위로 정규화, 역방향)
        if (financialData.getPbrRatio() != null) {
            BigDecimal pbrScore = normalizeScore(financialData.getPbrRatio(),
                    BigDecimal.ZERO, BigDecimal.valueOf(5), false);
            totalScore = totalScore.add(pbrScore.multiply(profile.getWeightPbr()));
            totalWeight = totalWeight.add(profile.getWeightPbr());
        }

        // 실제 사용된 가중치 합계로 나누어 정규화
        // normalizeScore가 이미 0~100 범위를 반환하므로, 가중치 평균만 계산
        if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
            return totalScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        }

        // 지표가 하나도 없으면 0점
        return BigDecimal.ZERO;
    }

    /**
     * 값을 0~100 범위로 정규화
     * 
     * @param value          원본 값
     * @param min            최소값
     * @param max            최대값
     * @param higherIsBetter true면 값이 높을수록 좋음, false면 낮을수록 좋음
     * @return 0~100 사이의 점수
     */
    private BigDecimal normalizeScore(BigDecimal value, BigDecimal min, BigDecimal max, boolean higherIsBetter) {
        if (value == null)
            return BigDecimal.ZERO;

        // 범위를 벗어나면 경계값으로 제한
        if (value.compareTo(min) < 0)
            value = min;
        if (value.compareTo(max) > 0)
            value = max;

        // 0~1 범위로 정규화
        BigDecimal normalized = value.subtract(min)
                .divide(max.subtract(min), 4, RoundingMode.HALF_UP);

        // 낮을수록 좋은 지표는 반전
        if (!higherIsBetter) {
            normalized = BigDecimal.ONE.subtract(normalized);
        }

        // 0~100 범위로 변환
        return normalized.multiply(BigDecimal.valueOf(100));
    }

    /**
     * 기본 투자 성향 프로필 생성
     * 
     * @param profileType 프로필 타입 (안정형, 성장형, 균형형, 가치형, 현금흐름형)
     * @return 투자 성향 프로필
     */
    public InvestmentProfile createDefaultProfile(String profileType) {
        InvestmentProfile profile = new InvestmentProfile();
        profile.setProfileName(profileType);

        switch (profileType) {
            case "안정형":
                profile.setWeightRevenueGrowth(BigDecimal.valueOf(10));
                profile.setWeightOperatingMargin(BigDecimal.valueOf(25));
                profile.setWeightRoe(BigDecimal.valueOf(15));
                profile.setWeightDebtRatio(BigDecimal.valueOf(25));
                profile.setWeightFcf(BigDecimal.valueOf(15));
                profile.setWeightPer(BigDecimal.valueOf(5));
                profile.setWeightPbr(BigDecimal.valueOf(5));
                break;

            case "성장형":
                profile.setWeightRevenueGrowth(BigDecimal.valueOf(30));
                profile.setWeightOperatingMargin(BigDecimal.valueOf(15));
                profile.setWeightRoe(BigDecimal.valueOf(25));
                profile.setWeightDebtRatio(BigDecimal.valueOf(10));
                profile.setWeightFcf(BigDecimal.valueOf(10));
                profile.setWeightPer(BigDecimal.valueOf(5));
                profile.setWeightPbr(BigDecimal.valueOf(5));
                break;

            case "가치형":
                profile.setWeightRevenueGrowth(BigDecimal.valueOf(15));
                profile.setWeightOperatingMargin(BigDecimal.valueOf(15));
                profile.setWeightRoe(BigDecimal.valueOf(10));
                profile.setWeightDebtRatio(BigDecimal.valueOf(15));
                profile.setWeightFcf(BigDecimal.valueOf(10));
                profile.setWeightPer(BigDecimal.valueOf(20));
                profile.setWeightPbr(BigDecimal.valueOf(15));
                break;

            case "현금흐름형":
                profile.setWeightRevenueGrowth(BigDecimal.valueOf(15));
                profile.setWeightOperatingMargin(BigDecimal.valueOf(15));
                profile.setWeightRoe(BigDecimal.valueOf(10));
                profile.setWeightDebtRatio(BigDecimal.valueOf(20));
                profile.setWeightFcf(BigDecimal.valueOf(30));
                profile.setWeightPer(BigDecimal.valueOf(5));
                profile.setWeightPbr(BigDecimal.valueOf(5));
                break;

            default: // 균형형
                profile.setWeightRevenueGrowth(BigDecimal.valueOf(20));
                profile.setWeightOperatingMargin(BigDecimal.valueOf(20));
                profile.setWeightRoe(BigDecimal.valueOf(15));
                profile.setWeightDebtRatio(BigDecimal.valueOf(15));
                profile.setWeightFcf(BigDecimal.valueOf(15));
                profile.setWeightPer(BigDecimal.valueOf(10));
                profile.setWeightPbr(BigDecimal.valueOf(5));
                break;
        }

        return profile;
    }
}
