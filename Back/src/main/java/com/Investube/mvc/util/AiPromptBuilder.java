package com.Investube.mvc.util;

import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.model.dto.PeerStats;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 프롬프트 빌더
 * 재무 데이터를 AI가 이해할 수 있는 프롬프트로 변환
 */
@Component
public class AiPromptBuilder {

  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * 재무 분석용 프롬프트 생성 (system/user 분리) - 업종별 분석 포함
   * 
   * @param stockCode     종목 코드
   * @param stockName     종목명
   * @param industry      업종명
   * @param financialData 재무 데이터
   * @param baseScore     기본 점수
   * @param profile       투자 성향 프로필
   * @param peerStats     동종업계 통계 (null 가능)
   */
  public String[] buildFinancialAnalysisPromptWithRoles(
      String stockCode,
      String stockName,
      String industry,
      FinancialData financialData,
      BigDecimal baseScore,
      InvestmentProfile profile,
      PeerStats peerStats) {
    try {
      // 재무 지표를 Map으로 변환
      Map<String, Object> financials = new HashMap<>();
      financials.put("revenueGrowthRate", formatValue(financialData.getRevenueGrowthRate()));
      financials.put("operatingProfitGrowthRate", formatValue(financialData.getOperatingProfitGrowthRate()));
      financials.put("operatingMargin", formatValue(financialData.getOperatingMargin()));
      financials.put("roe", formatValue(financialData.getRoe()));
      financials.put("debtRatio", formatValue(financialData.getDebtRatio()));
      financials.put("per", formatValue(financialData.getPerRatio()));
      financials.put("pbr", formatValue(financialData.getPbrRatio()));

      String financialsJson = objectMapper.writeValueAsString(financials);

      // 업종별 리스크 기준 가져오기
      String riskCriteriaPrompt = IndustryRiskCriteria.getRiskCriteriaPrompt(industry);

      // System 프롬프트 (AI의 역할과 규칙 정의) - 업종별 기준 포함
      String systemPrompt = String.format(
          "너는 투자 분석 보조 AI다. 너의 역할은 점수를 계산하는 것이 아니라, 이미 계산된 기본 점수를 해석하고 보정하는 것이다. " +
              "규칙: 1. 절대 최종 점수를 직접 계산하지 마라. 2. 보정 점수는 -10 ~ +10 범위만 허용된다. " +
              "3. 가중치 보정은 각 항목당 -10 ~ +10 사이 정수만 가능하다. 4. 출력은 반드시 JSON 형식이어야 한다. " +
              "5. 감정적 표현, 투자 권유 문구 사용 금지. 6. 요약은 2줄 이내로 작성하되, 동종업계 비교 결과를 포함하라. " +
              "7. %s " +
              "8. JSON만 출력하고 다른 텍스트는 포함하지 마라.",
          riskCriteriaPrompt);

      // Pre-revenue 기업 컨텍스트 (매출 1억 미만)
      String preRevenueContext = "";
      if (financialData.getRevenue() == null || financialData.getRevenue() < 100_000_000L) {
        preRevenueContext = " [주의: Pre-revenue 기업 - 매출 1억 미만으로 성장률 지표 없음. 기술력, R&D, 자금 상황 중심으로 분석 필요.]";
      }

      // 동종업계 비교 정보
      String peerComparisonContext = "";
      if (peerStats != null && peerStats.getPeerCount() >= 2) {
        peerComparisonContext = " " + peerStats.toPeerComparisonPrompt(financialData);
      }

      // User 프롬프트 (실제 분석할 데이터)
      String userPrompt = String.format(
          "기업: %s (%s) 업종: %s 회계 연도: %s 투자 성향: %s 기본 점수: %.2f%s%s 재무 지표: %s " +
              "위 정보를 바탕으로 다음 JSON 형식으로 응답하라: " +
              "{\"scoreAdjustment\": 0, \"weightAdjustment\": {\"revenueGrowth\": 0, \"operatingMargin\": 0, " +
              "\"roe\": 0, \"debtRatio\": 0, \"per\": 0, \"pbr\": 0}, " +
              "\"summary\": \"재무 분석 요약 (동종업계 비교 포함, 2줄 이내)\", \"riskLevel\": \"MEDIUM\"}",
          stockName,
          stockCode,
          industry != null ? industry : "미분류",
          financialData.getFiscalYear(),
          profile.getProfileName(),
          baseScore,
          preRevenueContext,
          peerComparisonContext,
          financialsJson);
      return new String[] { systemPrompt, userPrompt };

    } catch (Exception e) {
      throw new RuntimeException("프롬프트 생성 실패", e);
    }
  }

  /**
   * 재무 분석용 프롬프트 생성 (system/user 분리) - 기존 호환용
   */
  public String[] buildFinancialAnalysisPromptWithRoles(
      String stockCode,
      String stockName,
      FinancialData financialData,
      BigDecimal baseScore,
      InvestmentProfile profile) {
    // 업종 없이 기본 기준 사용
    return buildFinancialAnalysisPromptWithRoles(
        stockCode, stockName, null, financialData, baseScore, profile, null);
  }

  /**
   * 재무 분석용 프롬프트 생성
   */
  public String buildFinancialAnalysisPrompt(
      String stockCode,
      String stockName,
      FinancialData financialData,
      BigDecimal baseScore,
      InvestmentProfile profile) {
    try {
      // 재무 지표를 Map으로 변환
      Map<String, Object> financials = new HashMap<>();
      financials.put("revenueGrowthRate", formatValue(financialData.getRevenueGrowthRate()));
      financials.put("operatingProfitGrowthRate", formatValue(financialData.getOperatingProfitGrowthRate()));
      financials.put("operatingMargin", formatValue(financialData.getOperatingMargin()));
      financials.put("roe", formatValue(financialData.getRoe()));
      financials.put("debtRatio", formatValue(financialData.getDebtRatio()));
      financials.put("per", formatValue(financialData.getPerRatio()));
      financials.put("pbr", formatValue(financialData.getPbrRatio()));

      String financialsJson = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(financials);

      // 시스템 프롬프트 + 데이터
      return String.format("""
          너는 투자 분석 보조 AI다.
          너의 역할은 점수를 계산하는 것이 아니라,
          이미 계산된 기본 점수를 해석하고 보정하는 것이다.

          규칙:
          1. 절대 최종 점수를 직접 계산하지 마라.
          2. 보정 점수는 -10 ~ +10 범위만 허용된다.
          3. 가중치 보정은 각 항목당 -10 ~ +10 사이 정수만 가능하다.
          4. 출력은 반드시 JSON 형식이어야 한다.
          5. 감정적 표현, 투자 권유 문구 사용 금지.
          6. 요약은 2줄 이내로 작성하라.

          기업: %s (%s)
          회계 연도: %s
          투자 성향: %s
          기본 점수: %.2f

          재무 지표:
          %s

          위 정보를 바탕으로 다음 JSON 형식으로 응답하라:
          {
            "scoreAdjustment": -5,
            "weightAdjustment": {
              "revenueGrowth": -5,
              "operatingMargin": 0,
              "roe": -5,
              "debtRatio": 5,
              "per": 0,
              "pbr": 0
            },
            "summary": "2줄 이내 요약",
            "riskLevel": "HIGH"
          }

          riskLevel 판단 기준:
          - HIGH: 부채비율 > 200% OR 영업이익률 < -10% OR ROE < -20%
          - LOW: 부채비율 < 50% AND 영업이익률 > 10% AND ROE > 10%
          - MEDIUM: 위 조건에 해당하지 않는 경우
          JSON만 출력하고 다른 텍스트는 포함하지 마라.
          """,
          stockName,
          stockCode,
          financialData.getFiscalYear(),
          profile.getProfileName(),
          baseScore,
          financialsJson);

    } catch (Exception e) {
      throw new RuntimeException("프롬프트 생성 실패", e);
    }
  }

  /**
   * BigDecimal을 문자열로 포맷 (null 처리)
   */
  private String formatValue(BigDecimal value) {
    return value != null ? value.toString() : "null";
  }

  /**
   * 챗봇용 프롬프트 생성 (나중 확장용)
   */
  public String buildChatPrompt(String userMessage, String context) {
    return String.format("""
        너는 투자 관련 질문에 답변하는 AI 챗봇이다.

        규칙:
        1. 구체적 투자 조언은 하지 마라.
        2. 재무 지표 설명, 용어 해설에 집중하라.
        3. 간결하고 명확하게 답변하라.

        컨텍스트:
        %s

        사용자 질문: %s

        답변:
        """,
        context,
        userMessage);
  }
}
