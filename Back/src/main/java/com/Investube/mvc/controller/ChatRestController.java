package com.Investube.mvc.controller;

import com.Investube.mvc.model.dao.InvestmentProfileDao;
import com.Investube.mvc.model.dto.*;
import com.Investube.mvc.model.service.FinancialService;
import com.Investube.mvc.model.service.StockService;
import com.Investube.mvc.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 챗봇 REST API 컨트롤러
 */
@RestController
@RequestMapping("/chat")
public class ChatRestController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private StockService stockService;

    @Autowired
    private FinancialService financialService;

    @Autowired
    private InvestmentProfileDao profileDao;

    /**
     * userId 추출 헬퍼 메서드
     */
    private Integer getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj instanceof Integer) {
            return (Integer) userIdObj;
        }
        return null;
    }

    /**
     * 종목 관련 챗봇 질문 처리
     * 
     * POST /chat/stock
     * Request Body: { "stockCode": "005930", "message": "이 종목 점수 왜 이래?" }
     */
    @PostMapping("/stock")
    public ResponseEntity<?> chatAboutStock(
            @RequestBody ChatRequest chatRequest,
            HttpServletRequest request) {
        try {
            System.out.println("=== 챗봇 요청 시작 ===");
            String stockCode = chatRequest.getStockCode();
            String userMessage = chatRequest.getMessage();
            System.out.println("종목코드: " + stockCode + ", 메시지: " + userMessage);

            if (stockCode == null || stockCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ChatResponse("종목 코드가 필요합니다.", false));
            }

            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ChatResponse("질문을 입력해주세요.", false));
            }

            // 1. 종목 정보 조회
            System.out.println("1. 종목 정보 조회 중...");
            Stock stock = stockService.getStockByCode(stockCode);
            if (stock == null) {
                System.out.println("종목을 찾을 수 없음: " + stockCode);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ChatResponse("종목을 찾을 수 없습니다.", false));
            }
            System.out.println("종목 조회 성공: " + stock.getStockName());

            // 2. 재무 데이터 조회
            System.out.println("2. 재무 데이터 조회 중...");
            FinancialData financialData = financialService.getFinancialDataWithScore(stockCode, null);
            System.out.println("재무 데이터: " + (financialData != null ? "있음" : "없음"));

            // 3. 사용자 투자 성향 조회 (로그인한 경우)
            System.out.println("3. 사용자 투자 성향 조회 중...");
            Integer userId = getUserIdFromRequest(request);
            InvestmentProfile profile = null;
            if (userId != null) {
                profile = profileDao.getDefaultProfile(userId);
                System.out.println("투자 성향: " + (profile != null ? profile.getProfileName() : "없음"));
            } else {
                System.out.println("로그인하지 않은 사용자");
            }

            // 4. 기본 점수 계산 (재무 데이터가 있는 경우)
            System.out.println("4. 기본 점수 계산 중...");
            Double baseScore = null;
            if (financialData != null && financialData.getTotalScore() != null) {
                baseScore = financialData.getTotalScore().doubleValue();
                System.out.println("기본 점수: " + baseScore);
            }

            // 5. 챗봇 응답 생성
            System.out.println("5. 챗봇 응답 생성 중...");
            String response = chatService.getChatResponse(
                    stock,
                    financialData,
                    profile,
                    baseScore,
                    userMessage);
            System.out.println("챗봇 응답 생성 완료");
            System.out.println("=== 챗봇 요청 완료 ===");

            return ResponseEntity.ok(new ChatResponse(response, true));

        } catch (Exception e) {
            System.err.println("=== 챗봇 오류 발생 ===");
            System.err.println("오류 메시지: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("챗봇 응답 생성 중 오류가 발생했습니다: " + e.getMessage(), false));
        }
    }

    /**
     * 일반 투자 관련 챗봇 질문 처리 (종목 없이)
     * 
     * POST /chat/general
     * Request Body: { "message": "오늘 시장 상황은?" }
     */
    @PostMapping("/general")
    public ResponseEntity<?> generalChat(
            @RequestBody ChatRequest chatRequest,
            HttpServletRequest request) {
        try {
            String userMessage = chatRequest.getMessage();

            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ChatResponse("질문을 입력해주세요.", false));
            }

            // 사용자 투자 성향 조회 (로그인한 경우)
            Integer userId = getUserIdFromRequest(request);
            InvestmentProfile profile = null;
            if (userId != null) {
                profile = profileDao.getDefaultProfile(userId);
            }

            // 종목 정보 추출 (종목코드 또는 종목명으로 DB 검색)
            Stock stock = extractStockFromMessage(userMessage);
            FinancialData financialData = null;
            Double baseScore = null;

            if (stock != null) {
                String stockCode = stock.getStockCode();
                financialData = financialService.getFinancialDataWithScore(stockCode, null);

                // 재무 데이터가 없으면 DART에서 자동 동기화 시도
                if (financialData == null) {
                    System.out.println("[챗봇] " + stock.getStockName() + " 재무 데이터 없음 → 자동 동기화 시도");
                    try {
                        int currentYear = java.time.LocalDate.now().getYear() - 1; // 작년 데이터
                        financialService.syncFinancialData(stockCode, currentYear);
                        financialData = financialService.getFinancialDataWithScore(stockCode, null);
                        System.out.println("[챗봇] " + stock.getStockName() + " 재무 데이터 동기화 완료");
                    } catch (Exception syncErr) {
                        System.err.println("[챗봇] DART 동기화 실패: " + syncErr.getMessage());
                        // 동기화 실패해도 계속 진행 (기본 정보만 제공)
                    }
                }

                if (financialData != null && financialData.getTotalScore() != null) {
                    baseScore = financialData.getTotalScore().doubleValue();
                }
            }

            // 챗봇 응답 생성
            String response = chatService.getGeneralChatResponse(
                    stock,
                    financialData,
                    profile,
                    baseScore,
                    userMessage);

            return ResponseEntity.ok(new ChatResponse(response, true));

        } catch (Exception e) {
            System.err.println("일반 챗봇 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ChatResponse("응답 생성 중 오류가 발생했습니다.", false));
        }
    }

    /**
     * 메시지에서 종목 코드 또는 종목명 추출
     */
    private Stock extractStockFromMessage(String message) {
        // 1. 6자리 숫자 종목코드 패턴 먼저 확인
        java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile("\\b(\\d{6})\\b");
        java.util.regex.Matcher matcher = codePattern.matcher(message);
        if (matcher.find()) {
            String stockCode = matcher.group(1);
            Stock stock = stockService.getStockByCode(stockCode);
            if (stock != null) {
                return stock;
            }
        }

        // 2. 메시지에서 한글 단어 추출하여 종목명으로 검색
        java.util.regex.Pattern korPattern = java.util.regex.Pattern.compile("[가-힣]{2,}");
        java.util.regex.Matcher korMatcher = korPattern.matcher(message);

        while (korMatcher.find()) {
            String keyword = korMatcher.group();
            // 제외할 일반 단어들
            if (keyword.equals("어때") || keyword.equals("회사") || keyword.equals("종목") ||
                    keyword.equals("주식") || keyword.equals("지금") || keyword.equals("오늘") ||
                    keyword.equals("투자") || keyword.equals("분석") || keyword.equals("추천")) {
                continue;
            }

            // DB에서 종목명 검색
            Stock stock = stockService.getStockByName(keyword);
            if (stock != null) {
                return stock;
            }
        }

        return null;
    }
}
