package com.Investube.mvc.controller;

import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.service.FinancialService;
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
}
