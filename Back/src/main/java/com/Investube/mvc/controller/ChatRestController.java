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

            // 종목 코드가 언급되었는지 확인
            String stockCode = extractStockCode(userMessage);
            Stock stock = null;
            FinancialData financialData = null;
            Double baseScore = null;

            if (stockCode != null) {
                stock = stockService.getStockByCode(stockCode);
                if (stock != null) {
                    financialData = financialService.getFinancialDataWithScore(stockCode, null);
                    if (financialData != null && financialData.getTotalScore() != null) {
                        baseScore = financialData.getTotalScore().doubleValue();
                    }
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
    private String extractStockCode(String message) {
        // 6자리 숫자 종목코드 패턴
        java.util.regex.Pattern codePattern = java.util.regex.Pattern.compile("\\b(\\d{6})\\b");
        java.util.regex.Matcher matcher = codePattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 주요 종목명으로 검색
        String[] majorStocks = { "삼성전자", "SK하이닉스", "네이버", "카카오", "현대차", "LG에너지솔루션" };
        String[] majorCodes = { "005930", "000660", "035420", "035720", "005380", "373220" };

        for (int i = 0; i < majorStocks.length; i++) {
            if (message.contains(majorStocks[i])) {
                return majorCodes[i];
            }
        }

        return null;
    }
}
