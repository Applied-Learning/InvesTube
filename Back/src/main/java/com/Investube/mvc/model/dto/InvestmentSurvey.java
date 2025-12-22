package com.Investube.mvc.model.dto;

/**
 * 투자 성향 설문 응답 DTO
 */
public class InvestmentSurvey {

    // Q1: 투자 기간 (1: 1년 미만, 2: 1~3년, 3: 3년 이상)
    private int investmentPeriod;

    // Q2: 손실 감내 수준 (1: 10% 이내, 2: 10~30%, 3: 30% 이상)
    private int riskTolerance;

    // Q3: 투자 목적 (1: 원금 보존, 2: 안정적 수익, 3: 고수익 추구)
    private int investmentGoal;

    // Q4: 투자 경험 (1: 초보, 2: 중급, 3: 고급)
    private int investmentExperience;

    // Q5: 변동성 대응 (1: 즉시 매도, 2: 관망, 3: 추가 매수)
    private int volatilityResponse;

    // Getters and Setters
    public int getInvestmentPeriod() {
        return investmentPeriod;
    }

    public void setInvestmentPeriod(int investmentPeriod) {
        this.investmentPeriod = investmentPeriod;
    }

    public int getRiskTolerance() {
        return riskTolerance;
    }

    public void setRiskTolerance(int riskTolerance) {
        this.riskTolerance = riskTolerance;
    }

    public int getInvestmentGoal() {
        return investmentGoal;
    }

    public void setInvestmentGoal(int investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public int getInvestmentExperience() {
        return investmentExperience;
    }

    public void setInvestmentExperience(int investmentExperience) {
        this.investmentExperience = investmentExperience;
    }

    public int getVolatilityResponse() {
        return volatilityResponse;
    }

    public void setVolatilityResponse(int volatilityResponse) {
        this.volatilityResponse = volatilityResponse;
    }

    /**
     * 총점 계산 (5~15점)
     * 5~8: 안정형, 9~12: 균형형, 13~15: 공격형
     */
    public int getTotalScore() {
        return investmentPeriod + riskTolerance + investmentGoal +
                investmentExperience + volatilityResponse;
    }

    /**
     * 설문 결과로 투자 성향 결정
     */
    public String determineProfileType() {
        int score = getTotalScore();
        if (score <= 8) {
            return "안정형";
        } else if (score <= 12) {
            return "균형형";
        } else {
            return "공격형";
        }
    }
}
