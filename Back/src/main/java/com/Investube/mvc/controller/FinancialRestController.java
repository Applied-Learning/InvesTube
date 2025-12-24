package com.Investube.mvc.controller;

import com.Investube.mvc.model.dao.InvestmentProfileDao;
import com.Investube.mvc.model.dto.AiAnalysisResult;
import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.service.FinancialService;
import com.Investube.mvc.model.service.StockService;
import com.Investube.mvc.service.AIAnalysisService;
import com.Investube.mvc.service.FinancialAnalysisService;
import com.Investube.mvc.service.FinancialDataBatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 재무 분석 REST API 컨트롤러
 */
@RestController
@RequestMapping("/financial")
public class FinancialRestController {

    @Autowired
    private FinancialService financialService;

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @Autowired
    private InvestmentProfileDao profileDao;

    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    @Autowired
    private StockService stockService;

    @Autowired
    private FinancialDataBatchService batchService;

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
     * DART API에서 재무 데이터 동기화
     * POST /financial/sync/{stockCode}?year=2024
     */
    @PostMapping("/sync/{stockCode}")
    public ResponseEntity<?> syncFinancialData(
            @PathVariable String stockCode,
            @RequestParam(required = false, defaultValue = "2024") int year) {
        try {
            FinancialData data = financialService.syncFinancialData(stockCode, year);
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 기업의 최신 재무 데이터 조회 (점수 포함)
     * GET /financial/{stockCode}?profileId=1
     */
    @GetMapping("/{stockCode}")
    public ResponseEntity<?> getFinancialData(
            @PathVariable String stockCode,
            @RequestParam(required = false) Integer profileId) {
        try {
            FinancialData data = financialService.getFinancialDataWithScore(stockCode, profileId);
            if (data == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "재무 데이터가 없습니다. DART 동기화가 필요합니다.");
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 기업의 재무 데이터 이력 조회
     * GET /financial/{stockCode}/history
     */
    @GetMapping("/{stockCode}/history")
    public ResponseEntity<?> getFinancialHistory(@PathVariable String stockCode) {
        try {
            List<FinancialData> history = financialService.getFinancialDataHistory(stockCode);
            return new ResponseEntity<>(history, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 찜한 기업들의 재무 데이터 및 점수 조회 (인증 필요)
     * GET /financial/wished?profileId=1
     */
    @GetMapping("/wished")
    public ResponseEntity<?> getWishedStocksWithScores(
            HttpServletRequest request,
            @RequestParam(required = false) Integer profileId) {
        try {
            Integer userId = getUserIdFromRequest(request);
            if (userId == null) {
                return new ResponseEntity<>("인증이 필요합니다.", HttpStatus.UNAUTHORIZED);
            }

            List<FinancialData> wishedData = financialService.getWishedStocksWithScores(userId, profileId);
            return new ResponseEntity<>(wishedData, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * AI 기반 재무 분석
     * GET /financial/{stockCode}/ai-analysis?profileId=1
     * 
     * 기능:
     * 1. 기본 rule-based 점수 계산
     * 2. AI가 점수/가중치 보정 + 해석 제공
     * 3. 최종 점수 반환
     */
    @GetMapping("/{stockCode}/ai-analysis")
    public ResponseEntity<?> getAiAnalysis(
            @PathVariable String stockCode,
            @RequestParam(required = false) Integer profileId,
            HttpServletRequest request) {
        try {
            // 1. 재무 데이터 조회
            FinancialData financialData = financialService.getFinancialDataWithScore(stockCode, profileId);
            if (financialData == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "재무 데이터가 없습니다. DART 동기화가 필요합니다.");
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }

            // 2. 투자 프로필 조회
            InvestmentProfile profile;
            if (profileId != null) {
                profile = profileDao.getProfileById(profileId);
                if (profile == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "투자 성향 프로필을 찾을 수 없습니다.");
                    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
                }
            } else {
                // 기본 프로필 사용
                Integer userId = getUserIdFromRequest(request);
                if (userId != null) {
                    profile = profileDao.getDefaultProfile(userId);
                    if (profile == null) {
                        // 기본 균형형 프로필 생성
                        profile = financialAnalysisService.createDefaultProfile("균형형");
                        profile.setUserId(userId);
                        profile.setIsDefault(true);
                        try {
                            profileDao.insertProfile(profile);
                        } catch (Exception e) {
                            System.err.println("기본 프로필 생성 실패 (DB 저장 건너뜀): " + e.getMessage());
                            // DB 저장이 실패해도 메모리 상의 profile 객체로 분석은 가능하므로 진행
                        }
                    }
                } else {
                    // 비로그인 사용자는 임시 균형형 프로필 사용
                    profile = financialAnalysisService.createDefaultProfile("균형형");
                }
            }

            // 3. 종목 정보 조회 (업종 포함)
            Stock stock = stockService.getStockByCode(stockCode);
            String stockName = stock != null ? stock.getStockName() : stockCode;
            String industry = stock != null ? stock.getIndustry() : null;

            // 4. AI 분석 수행 (업종별 분석 + 동종업계 비교 포함)
            AiAnalysisResult aiResult = aiAnalysisService.analyzeFinancials(
                    stockCode,
                    stockName,
                    industry,
                    financialData,
                    profile);

            return new ResponseEntity<>(aiResult, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 스택 트레이스 출력
            Map<String, String> error = new HashMap<>();
            error.put("error", "AI 분석 실패 (" + e.getClass().getSimpleName() + "): " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== 일괄 동기화 API ====================

    /**
     * 전체 종목 재무 데이터 일괄 동기화 시작 (백그라운드)
     * POST /financial/sync-all
     */
    @PostMapping("/sync-all")
    public ResponseEntity<?> startSyncAll() {
        if (batchService.isSyncing()) {
            Map<String, Object> status = new HashMap<>();
            status.put("message", "이미 동기화가 진행 중입니다.");
            status.put("progress", batchService.getSyncProgress());
            status.put("total", batchService.getSyncTotal());
            return new ResponseEntity<>(status, HttpStatus.CONFLICT);
        }

        batchService.syncAllFinancialData();

        Map<String, String> response = new HashMap<>();
        response.put("message", "전체 동기화가 백그라운드에서 시작되었습니다. /financial/sync-status에서 진행 상황을 확인하세요.");
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    /**
     * 동기화 진행 상황 조회
     * GET /financial/sync-status
     */
    @GetMapping("/sync-status")
    public ResponseEntity<?> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("isSyncing", batchService.isSyncing());
        status.put("progress", batchService.getSyncProgress());
        status.put("total", batchService.getSyncTotal());
        status.put("status", batchService.getSyncStatus());
        status.put("hasCsvCache", batchService.hasCsvCache());

        if (!batchService.getSyncErrors().isEmpty()) {
            status.put("recentErrors", batchService.getSyncErrors().subList(0,
                    Math.min(10, batchService.getSyncErrors().size())));
            status.put("totalErrors", batchService.getSyncErrors().size());
        }

        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    /**
     * 현재 DB 데이터를 CSV로 내보내기
     * POST /financial/export-csv
     */
    @PostMapping("/export-csv")
    public ResponseEntity<?> exportToCsv() {
        try {
            batchService.exportToCsv();
            Map<String, String> response = new HashMap<>();
            response.put("message", "CSV 내보내기가 완료되었습니다.");
            response.put("path", "data/financial_data_cache.csv");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "CSV 내보내기 실패: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * CSV 파일에서 DB로 로드
     * POST /financial/load-csv
     */
    @PostMapping("/load-csv")
    public ResponseEntity<?> loadFromCsv() {
        try {
            int loadedCount = batchService.loadFromCsv();
            Map<String, Object> response = new HashMap<>();
            response.put("message", "CSV 로드가 완료되었습니다.");
            response.put("loadedCount", loadedCount);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "CSV 로드 실패: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
