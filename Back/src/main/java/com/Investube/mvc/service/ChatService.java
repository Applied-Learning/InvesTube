package com.Investube.mvc.service;

import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.util.OpenAIApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 챗봇 서비스
 * - 종목 재무 데이터 기반 설명용 챗봇
 * - 매수/매도 추천 없이 데이터 해석만 제공
 */
@Service
public class ChatService {

    @Autowired
    private OpenAIApiClient openaiClient;

    /**
     * 종목 정보 기반 챗봇 응답 생성
     */
    public String getChatResponse(
            Stock stock,
            FinancialData financialData,
            InvestmentProfile profile,
            Double baseScore,
            String userMessage) {
        try {
            System.out.println("[ChatService] 챗봇 응답 생성 시작");
            System.out.println("[ChatService] 사용자 질문: " + userMessage);

            // 1. 시스템 프롬프트 구성
            System.out.println("[ChatService] 시스템 프롬프트 구성 중...");
            String systemPrompt = buildSystemPrompt();

            // 2. Context + 질문 구성 (user message)
            System.out.println("[ChatService] User 메시지 구성 중...");
            String context = buildContext(stock, financialData, profile, baseScore);
            String userPrompt = context + "\n\n질문: " + userMessage;
            System.out.println("[ChatService] User 메시지 길이: " + userPrompt.length() + " 글자");

            // 프롬프트가 너무 길면 축약
            if (userPrompt.length() > 4000) {
                System.out.println("[ChatService] User 메시지가 너무 깁니다. 축약합니다.");
                userPrompt = buildShortUserMessage(stock, financialData, userMessage);
                System.out.println("[ChatService] 축약된 메시지 길이: " + userPrompt.length());
            }

            // 3. GMS OpenAI API 호출 (system/user 분리)
            System.out.println("[ChatService] GMS API 호출 중...");
            String response = openaiClient.callGPTWithRoles(systemPrompt, userPrompt);
            System.out.println("[ChatService] API 응답 수신 완료");

            return response;

        } catch (Exception e) {
            System.err.println("[ChatService] 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("챗봇 응답 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 짧은 사용자 메시지 생성 (토큰 제한 대응)
     */
    private String buildShortUserMessage(Stock stock, FinancialData financialData, String userMessage) {
        StringBuilder message = new StringBuilder();

        message.append("종목: ").append(stock.getStockName()).append("\n");

        if (financialData != null) {
            if (financialData.getTotalScore() != null) {
                message.append("투자점수: ").append(String.format("%.1f점\n", financialData.getTotalScore()));
            }
            if (financialData.getRevenueGrowthRate() != null) {
                message.append("매출성장: ").append(String.format("%.1f%%\n", financialData.getRevenueGrowthRate()));
            }
            if (financialData.getOperatingMargin() != null) {
                message.append("영업이익률: ").append(String.format("%.1f%%\n", financialData.getOperatingMargin()));
            }
            if (financialData.getRoe() != null) {
                message.append("ROE: ").append(String.format("%.1f%%\n", financialData.getRoe()));
            }
            if (financialData.getDebtRatio() != null) {
                message.append("부채비율: ").append(String.format("%.1f%%\n", financialData.getDebtRatio()));
            }
        }

        message.append("\n질문: ").append(userMessage);

        return message.toString();
    }

    /**
     * Context 구성 - 종목 정보와 재무 데이터
     */
    private String buildContext(
            Stock stock,
            FinancialData financialData,
            InvestmentProfile profile,
            Double baseScore) {
        StringBuilder context = new StringBuilder();

        // 종목 정보
        context.append("[종목 정보]\n");
        context.append("- 종목명: ").append(stock.getStockName()).append("\n");
        context.append("- 종목코드: ").append(stock.getStockCode()).append("\n");
        context.append("- 시장: ").append(stock.getMarket()).append("\n");
        if (stock.getIndustry() != null) {
            context.append("- 업종: ").append(stock.getIndustry()).append("\n");
        }

        if (baseScore != null) {
            context.append("- 기본 점수: ").append(String.format("%.1f", baseScore)).append("점\n");
        }

        if (profile != null) {
            context.append("- 투자 성향: ").append(profile.getProfileName()).append("\n");
        }

        // 재무 데이터
        if (financialData != null) {
            context.append("\n[재무 요약]\n");

            if (financialData.getRevenueGrowthRate() != null) {
                context.append("- 매출 성장률: ")
                        .append(String.format("%.2f", financialData.getRevenueGrowthRate()))
                        .append("%\n");
            }

            if (financialData.getOperatingMargin() != null) {
                context.append("- 영업이익률: ")
                        .append(String.format("%.2f", financialData.getOperatingMargin()))
                        .append("%\n");
            }

            if (financialData.getRoe() != null) {
                context.append("- ROE: ")
                        .append(String.format("%.2f", financialData.getRoe()))
                        .append("%\n");
            }

            if (financialData.getDebtRatio() != null) {
                context.append("- 부채비율: ")
                        .append(String.format("%.2f", financialData.getDebtRatio()))
                        .append("%\n");
            }

            if (financialData.getPerRatio() != null) {
                context.append("- PER: ")
                        .append(String.format("%.2f", financialData.getPerRatio()))
                        .append("\n");
            }

            if (financialData.getPbrRatio() != null) {
                context.append("- PBR: ")
                        .append(String.format("%.2f", financialData.getPbrRatio()))
                        .append("\n");
            }

            if (financialData.getTotalScore() != null) {
                context.append("- 투자 점수: ")
                        .append(String.format("%.1f", financialData.getTotalScore()))
                        .append("점\n");
            }
        }

        return context.toString();
    }

    /**
     * 시스템 프롬프트 - AI의 역할 정의
     */
    private String buildSystemPrompt() {
        return """
                당신은 투자 판단을 대신하지 않는 재무 설명 AI입니다.

                역할:
                - 주어진 재무 데이터를 쉽게 설명
                - 지표의 의미와 수치 해석 제공
                - 객관적인 사실 기반 정보만 제공

                금지사항:
                - 매수/매도 추천 금지
                - 수익 보장 금지
                - "반드시", "확실히" 등 단정적 표현 금지

                답변 형식:
                - 간결하고 이해하기 쉽게 (3-5문장)
                - 전문용어 사용 시 설명 추가
                - 존댓말 사용
                """;
    }

    /**
     * 가격 포맷팅
     */
    private String formatPrice(Integer price) {
        if (price == null)
            return "정보 없음";
        return String.format("%,d", price);
    }

    /**
     * 큰 숫자 포맷팅 (억/조 단위)
     */
    private String formatBigNumber(Long num) {
        if (num == null)
            return "정보 없음";
        long absNum = Math.abs(num);
        String sign = num < 0 ? "-" : "";

        if (absNum >= 1_000_000_000_000L) {
            return sign + String.format("%.1f조", absNum / 1_000_000_000_000.0);
        }
        if (absNum >= 100_000_000L) {
            return sign + String.format("%.0f억", absNum / 100_000_000.0);
        }
        if (absNum >= 10_000L) {
            return sign + String.format("%.0f만", absNum / 10_000.0);
        }
        return String.format("%,d", num);
    }

    /**
     * 일반 투자 관련 챗봇 응답 생성 (종목 없이 가능)
     */
    public String getGeneralChatResponse(
            Stock stock,
            FinancialData financialData,
            InvestmentProfile profile,
            Double baseScore,
            String userMessage) {
        try {
            // 시스템 프롬프트 (일반 투자 상담용)
            String systemPrompt = buildGeneralSystemPrompt(profile);

            // 사용자 메시지 구성
            StringBuilder userPrompt = new StringBuilder();

            if (stock != null) {
                userPrompt.append("[종목 정보]\n");
                userPrompt.append("- 종목명: ").append(stock.getStockName()).append("\n");
                userPrompt.append("- 시장: ").append(stock.getMarket()).append("\n");
                if (stock.getIndustry() != null) {
                    userPrompt.append("- 업종: ").append(stock.getIndustry()).append("\n");
                }

                if (financialData != null) {
                    userPrompt.append("\n[재무 데이터 (").append(financialData.getFiscalYear()).append("년)]\n");
                    if (financialData.getRevenue() != null) {
                        userPrompt.append("- 매출액: ").append(formatBigNumber(financialData.getRevenue())).append("원\n");
                    }
                    if (financialData.getOperatingProfit() != null) {
                        userPrompt.append("- 영업이익: ").append(formatBigNumber(financialData.getOperatingProfit()))
                                .append("원\n");
                    }
                    if (financialData.getOperatingMargin() != null) {
                        userPrompt.append("- 영업이익률: ")
                                .append(String.format("%.2f%%\n", financialData.getOperatingMargin()));
                    }
                    if (financialData.getRevenueGrowthRate() != null) {
                        userPrompt.append("- 매출 성장률: ")
                                .append(String.format("%.1f%%\n", financialData.getRevenueGrowthRate()));
                    }
                    if (financialData.getRoe() != null) {
                        userPrompt.append("- ROE: ")
                                .append(String.format("%.2f%%\n", financialData.getRoe()));
                    }
                    if (financialData.getDebtRatio() != null) {
                        userPrompt.append("- 부채비율: ")
                                .append(String.format("%.1f%%\n", financialData.getDebtRatio()));
                    }
                    if (baseScore != null) {
                        userPrompt.append("- 투자 점수: ")
                                .append(String.format("%.1f점\n", baseScore));
                    }
                } else {
                    userPrompt.append("\n[⚠️ 재무 데이터 없음]\n");
                    userPrompt.append("이 종목은 아직 재무 데이터가 동기화되지 않았습니다.\n");
                    userPrompt.append("사용자에게 '투자 상세 페이지에서 DART 데이터 가져오기 버튼을 클릭하면 재무 정보를 확인할 수 있다'고 안내해주세요.\n");
                }
            }

            userPrompt.append("\n질문: ").append(userMessage);

            // API 호출
            return openaiClient.callGPTWithRoles(systemPrompt, userPrompt.toString());

        } catch (Exception e) {
            System.err.println("일반 챗봇 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("챗봇 응답 생성 실패", e);
        }
    }

    /**
     * 일반 투자 상담용 시스템 프롬프트
     */
    private String buildGeneralSystemPrompt(InvestmentProfile profile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                당신은 친절한 투자 정보 제공 AI입니다.

                역할:
                - 투자 관련 일반 질문에 답변
                - 재무 지표 설명
                - 시장 동향 정보 제공

                금지사항:
                - 매수/매도 추천 금지
                - 수익 보장 금지
                - 미래 가격 예측 금지

                답변 형식:
                - 간결하게 (3-5문장)
                - 쉬운 용어 사용
                - 존댓말 사용
                """);

        if (profile != null) {
            prompt.append("\n사용자 투자 성향: ").append(profile.getProfileName()).append("\n");
        }

        return prompt.toString();
    }
}
