package com.Investube.mvc.model.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Investube.mvc.model.dao.FinancialDao;
import com.Investube.mvc.model.dao.InvestmentProfileDao;
import com.Investube.mvc.model.dao.StockWishDao;
import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.InvestmentProfile;
import com.Investube.mvc.model.dto.Stock;
import com.Investube.mvc.model.dto.StockWish;
import com.Investube.mvc.service.DartApiService;
import com.Investube.mvc.service.FinancialAnalysisService;

/**
 * 재무 분석 통합 서비스 구현체
 */
@Service
public class FinancialServiceImpl implements FinancialService {

    @Autowired
    private DartApiService dartApiService;

    @Autowired
    private FinancialAnalysisService financialAnalysisService;

    @Autowired
    private FinancialDao financialDao;

    @Autowired
    private InvestmentProfileDao investmentProfileDao;

    @Autowired
    private StockWishDao stockWishDao;

    @Autowired
    private StockService stockService;

    @Override
    @Transactional
    public FinancialData syncFinancialData(String stockCode, int year) throws Exception {
        // 주식 정보 조회
        Stock stock = stockService.getStockByCode(stockCode);
        if (stock == null) {
            throw new Exception("종목을 찾을 수 없습니다: " + stockCode);
        }

        // DART에서 corp_code 조회
        String corpCode = dartApiService.getCorpCode(stock.getStockName());
        if (corpCode == null) {
            throw new Exception("DART에서 기업 정보를 찾을 수 없습니다: " + stock.getStockName());
        }

        // 재무제표 데이터 조회 (연간 보고서)
        Map<String, Object> financialDataRaw = dartApiService.getFinancialStatement(corpCode, year, "11011");
        if (financialDataRaw == null || !Boolean.TRUE.equals(financialDataRaw.get("success"))) {
            String message = financialDataRaw != null ? (String) financialDataRaw.get("message") : "알 수 없는 오류";
            throw new Exception("DART " + year + "년 재무제표 없음 - " + message);
        }

        // 현금흐름표 데이터 조회
        Map<String, Object> cashFlowDataRaw = dartApiService.getCashFlowStatement(corpCode, year, "11011");
        if (cashFlowDataRaw == null || !Boolean.TRUE.equals(cashFlowDataRaw.get("success"))) {
            String message = cashFlowDataRaw != null ? (String) cashFlowDataRaw.get("message") : "알 수 없는 오류";
            throw new Exception("DART " + year + "년 현금흐름표 없음 - " + message);
        }

        // DartApiService가 반환하는 구조: { "success": true, "data": { "revenue": ..., ... } }
        // 따라서 "data" 키에서 실제 재무 데이터를 추출
        @SuppressWarnings("unchecked")
        Map<String, Object> financialData = (Map<String, Object>) financialDataRaw.get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> cashFlowData = (Map<String, Object>) cashFlowDataRaw.get("data");

        // FinancialData 객체 생성
        FinancialData data = new FinancialData();
        data.setStockCode(stockCode);
        data.setFiscalYear(year);
        data.setFiscalQuarter(4); // 연간 보고서는 Q4

        // DART API 응답: 모두 원 단위
        data.setRevenue(getLongFromMap(financialData, "revenue"));
        data.setOperatingProfit(getLongFromMap(financialData, "operating_profit"));
        data.setNetIncome(getLongFromMap(financialData, "net_income"));
        data.setTotalAssets(getLongFromMap(financialData, "total_assets"));
        data.setTotalEquity(getLongFromMap(financialData, "total_equity"));
        data.setTotalLiabilities(getLongFromMap(financialData, "total_liabilities"));

        // 현금흐름 데이터
        data.setCashFlowOperating(getLongFromMap(cashFlowData, "cash_flow_operating"));
        data.setCashFlowInvesting(getLongFromMap(cashFlowData, "cash_flow_investing"));
        data.setCashFlowFinancing(getLongFromMap(cashFlowData, "cash_flow_financing"));

        // 시장 데이터: KRX에서 시가총액 조회
        try {
            var latestPrice = stockService.getLatestPrice(stockCode);
            if (latestPrice != null && latestPrice.getMarketCap() != null) {
                data.setMarketCap(latestPrice.getMarketCap());
                data.setStockPrice(latestPrice.getClosePrice());
            } else {
                data.setMarketCap(0L);
                data.setStockPrice(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            System.err.println("시가총액 조회 실패: " + e.getMessage());
            data.setMarketCap(0L);
            data.setStockPrice(BigDecimal.ZERO);
        }
        data.setSharesOutstanding(0L);

        data.setDataSource("DART");
        data.setCreatedAt(LocalDateTime.now());

        // 디버그: 파싱된 재무 데이터 출력
        System.out.println("=== DART 재무 데이터 ===");
        System.out.println("기업: " + stock.getStockName() + " (" + stockCode + "), 연도: " + year);
        System.out.println("매출액: " + data.getRevenue());
        System.out.println("영업이익: " + data.getOperatingProfit());
        System.out.println("당기순이익: " + data.getNetIncome());
        System.out.println("자산총계: " + data.getTotalAssets());
        System.out.println("자본총계: " + data.getTotalEquity());
        System.out.println("부채총계: " + data.getTotalLiabilities());
        System.out.println("====================");

        // 전년도 데이터 조회 (매출 성장률 계산용)
        FinancialData previousData = financialDao.getFinancialData(stockCode, year - 1, 4);

        // 전년도 데이터가 DB에 없으면 자동 동기화 시도 (단, 최근 3년치까지만 재귀 허용)
        if (previousData == null && year >= java.time.LocalDate.now().getYear() - 3) {
            try {
                System.out.println("전년도(" + (year - 1) + ") 데이터 없음 → 자동 동기화 시도");
                previousData = this.syncFinancialData(stockCode, year - 1);
            } catch (Exception e) {
                System.err.println("전년도 데이터 동기화 실패: " + e.getMessage());
                // 실패해도 현재 연도 데이터는 저장해야 하므로 진행
            }
        }

        // 재무 지표 계산
        data = financialAnalysisService.calculateFinancialMetrics(data, previousData);

        // DB에 저장
        FinancialData existing = financialDao.getFinancialData(stockCode, year, 4);
        if (existing != null) {
            data.setFinancialId(existing.getFinancialId());
            financialDao.updateFinancialData(data);
        } else {
            financialDao.insertFinancialData(data);
        }

        return data;
    }

    /**
     * Map에서 Long 값 추출 헬퍼 메서드
     */
    private Long getLongFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public FinancialData getLatestFinancialData(String stockCode) {
        return financialDao.getLatestFinancialData(stockCode);
    }

    @Override
    public FinancialData getFinancialDataWithScore(String stockCode, Integer profileId) {
        FinancialData data = financialDao.getLatestFinancialData(stockCode);
        if (data == null) {
            return null;
        }

        // 투자 프로필 조회
        InvestmentProfile profile;
        if (profileId != null) {
            profile = investmentProfileDao.getProfileById(profileId);
        } else {
            // 기본 프로필 사용 (균형형)
            profile = financialAnalysisService.createDefaultProfile("균형형");
        }

        // 점수 계산
        BigDecimal score = financialAnalysisService.calculateInvestmentScore(data, profile);
        data.setTotalScore(score);

        return data;
    }

    @Override
    public List<FinancialData> getWishedStocksWithScores(int userId, Integer profileId) {
        // 찜한 기업 목록 조회
        List<StockWish> wishedStocks = stockWishDao.getWishedStocksByUserId(userId);

        List<FinancialData> result = new ArrayList<>();

        for (StockWish wish : wishedStocks) {
            FinancialData data = getFinancialDataWithScore(wish.getStockCode(), profileId);
            if (data != null) {
                // 기업명 등 추가 정보 설정
                data.setStockName(wish.getStockName());
                result.add(data);
            }
        }

        return result;
    }

    @Override
    public List<FinancialData> getFinancialDataHistory(String stockCode) {
        return financialDao.getFinancialDataList(stockCode, 10); // 최근 10개
    }
}
