package com.Investube.mvc.service;

import com.Investube.mvc.model.dto.*;
import com.Investube.mvc.util.AiPromptBuilder;
import com.Investube.mvc.util.GeminiApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AI 기반 재무 분석 서비스
 * 
 * 역할:
 * 1. 기존 rule-based 점수 계산 유지
 * 2. AI는 점수/가중치 보정 + 해석만 담당
 * 3. 확장 가능한 구조 (나중에 챗봇 추가)
 */
@Service
public class AIAnalysisService {

    @Autowired
    private GeminiApiClient geminiClient;

    @Autowired
    private AiPromptBuilder promptBuilder;

    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI 기반 재무 분석 수행
     * 
     * @param stockCode     종목 코드
     * @param stockName     종목명
     * @param financialData 재무 데이터
     * @param profile       투자 성향 프로필
     * @return AI 분석 결과
     */
    public AiAnalysisResult analyzeFinancials(
            String stockCode,
            String stockName,
            FinancialData financialData,
            InvestmentProfile profile) {
        try {
            // 1. 기본 점수 계산 (rule-based)
            BigDecimal baseScore = financialAnalysisService.calculateInvestmentScore(financialData, profile);

            // 2. AI 프롬프트 생성 (시스템 프롬프트와 유저 프롬프트 분리)
            String[] prompts = promptBuilder.buildFinancialAnalysisPromptWithRoles(
                    stockCode,
                    stockName,
                    financialData,
                    baseScore,
                    profile);
            String systemPrompt = prompts[0];
            String userPrompt = prompts[1];

            // 3. OpenAI GPT API 호출 (system/user 역할 분리)
            String aiResponse = geminiClient.callGemini(systemPrompt, userPrompt);

            // 4. JSON 응답 파싱
            String jsonResponse = geminiClient.extractJsonFromResponse(aiResponse);
            AiAnalysisResult result = objectMapper.readValue(jsonResponse, AiAnalysisResult.class);

            // 5. 보정 범위 검증 및 제한
            validateAndClampAdjustments(result);

            // 6. 최종 점수 계산
            BigDecimal finalScore = baseScore.add(result.getScoreAdjustment());

            // 0~100 범위로 제한
            if (finalScore.compareTo(BigDecimal.ZERO) < 0) {
                finalScore = BigDecimal.ZERO;
            }
            if (finalScore.compareTo(BigDecimal.valueOf(100)) > 0) {
                finalScore = BigDecimal.valueOf(100);
            }

            result.setBaseScore(baseScore);
            result.setFinalScore(finalScore);

            return result;

        } catch (Exception e) {
            System.err.println("AI 분석 실패: " + e.getMessage());
            e.printStackTrace();

            // AI 실패 시 기본 점수만 반환
            return createFallbackResult(financialData, profile);
        }
    }

    /**
     * AI 보정값 검증 및 범위 제한
     */
    private void validateAndClampAdjustments(AiAnalysisResult result) {
        // 점수 보정 -10 ~ +10 제한
        BigDecimal scoreAdj = result.getScoreAdjustment();
        if (scoreAdj.compareTo(BigDecimal.valueOf(-10)) < 0) {
            result.setScoreAdjustment(BigDecimal.valueOf(-10));
        }
        if (scoreAdj.compareTo(BigDecimal.valueOf(10)) > 0) {
            result.setScoreAdjustment(BigDecimal.valueOf(10));
        }

        // 가중치 보정도 각각 -10 ~ +10 제한
        AiWeightAdjustment weights = result.getWeightAdjustment();
        if (weights != null) {
            clampWeight(weights::getRevenueGrowth, weights::setRevenueGrowth);
            clampWeight(weights::getOperatingMargin, weights::setOperatingMargin);
            clampWeight(weights::getRoe, weights::setRoe);
            clampWeight(weights::getDebtRatio, weights::setDebtRatio);
            clampWeight(weights::getFcf, weights::setFcf);
            clampWeight(weights::getPer, weights::setPer);
            clampWeight(weights::getPbr, weights::setPbr);
        }
    }

    /**
     * 단일 가중치 값을 -10 ~ +10 범위로 제한
     */
    private void clampWeight(
            java.util.function.Supplier<BigDecimal> getter,
            java.util.function.Consumer<BigDecimal> setter) {
        BigDecimal value = getter.get();
        if (value == null) {
            setter.accept(BigDecimal.ZERO);
            return;
        }
        if (value.compareTo(BigDecimal.valueOf(-10)) < 0) {
            setter.accept(BigDecimal.valueOf(-10));
        }
        if (value.compareTo(BigDecimal.valueOf(10)) > 0) {
            setter.accept(BigDecimal.valueOf(10));
        }
    }

    /**
     * AI 실패 시 폴백 결과 생성
     */
    private AiAnalysisResult createFallbackResult(FinancialData financialData, InvestmentProfile profile) {
        AiAnalysisResult result = new AiAnalysisResult();

        BigDecimal baseScore = financialAnalysisService.calculateInvestmentScore(financialData, profile);
        result.setBaseScore(baseScore);
        result.setFinalScore(baseScore);
        result.setScoreAdjustment(BigDecimal.ZERO);
        result.setSummary("AI 분석을 사용할 수 없습니다. 기본 점수를 참고하세요.");
        result.setRiskLevel("MEDIUM");

        return result;
    }

    /**
     * 챗봇 대화 처리 (나중 확장용)
     */
    public String chat(String userMessage, String context) {
        try {
            String systemPrompt = "너는 투자 관련 질문에 답변하는 AI 챗봇이다. 구체적 투자 조언은 하지 마라. 재무 지표 설명, 용어 해설에 집중하라. 간결하고 명확하게 답변하라.";
            String userPrompt = String.format("컨텍스트: %s\n\n사용자 질문: %s", context, userMessage);
            return geminiClient.callGemini(systemPrompt, userPrompt);
        } catch (Exception e) {
            System.err.println("챗봇 응답 실패: " + e.getMessage());
            return "죄송합니다. 현재 응답할 수 없습니다.";
        }
    }
}
