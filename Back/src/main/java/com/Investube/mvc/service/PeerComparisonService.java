package com.Investube.mvc.service;

import com.Investube.mvc.model.dao.FinancialDao;
import com.Investube.mvc.model.dto.FinancialData;
import com.Investube.mvc.model.dto.PeerStats;
import com.Investube.mvc.util.IndustryRiskCriteria;
import com.Investube.mvc.util.IndustryRiskCriteria.IndustryCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 동종업계 비교 서비스
 * 업종 카테고리 기반으로 재무 지표를 비교 분석
 */
@Service
public class PeerComparisonService {

    @Autowired
    private FinancialDao financialDao;

    // 카테고리별 업종 매핑 (실제 DB 업종명)
    private static final Map<IndustryCategory, List<String>> CATEGORY_INDUSTRIES = new HashMap<>();

    static {
        CATEGORY_INDUSTRIES.put(IndustryCategory.FINANCE,
                Arrays.asList("금융", "은행", "보험", "증권", "기타금융"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.BIO_PHARMA,
                Arrays.asList("제약", "의료·정밀기기"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.IT_SOFTWARE,
                Arrays.asList("IT 서비스", "전기·전자"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.MANUFACTURING,
                Arrays.asList("기계·장비", "기타제조", "금속", "비금속", "운송장비·부품", "종이·목재"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.UTILITY,
                Arrays.asList("전기·가스", "전기·가스·수도", "통신"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.RETAIL,
                Arrays.asList("유통"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.CONSTRUCTION,
                Arrays.asList("건설", "부동산"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.TRANSPORTATION,
                Arrays.asList("운송·창고"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.FOOD_BEVERAGE,
                Arrays.asList("음식료·담배", "농업"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.CHEMICAL,
                Arrays.asList("화학"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.TEXTILE,
                Arrays.asList("섬유·의류"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.ENTERTAINMENT,
                Arrays.asList("오락·문화", "출판·매체복제"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.SERVICE,
                Arrays.asList("일반서비스"));
        CATEGORY_INDUSTRIES.put(IndustryCategory.DEFAULT,
                Arrays.asList("기타"));
    }

    /**
     * 동종업계 통계 및 현재 기업 순위 계산 (카테고리 기반)
     * 
     * @param stockCode   현재 분석 대상 종목 코드
     * @param industry    업종명
     * @param currentData 현재 기업의 재무 데이터
     * @return 동종업계 통계
     */
    public PeerStats getPeerStats(String stockCode, String industry, FinancialData currentData) {
        PeerStats stats = new PeerStats();

        if (industry == null || industry.isEmpty()) {
            stats.setPeerCount(0);
            return stats;
        }

        // 업종을 카테고리로 변환
        IndustryCategory category = IndustryRiskCriteria.categorize(industry);
        System.out.println("[PeerComparison] 원래 업종: " + industry + " → 카테고리: " + category);

        // 같은 카테고리에 속하는 모든 업종 가져오기
        List<String> categoryIndustries = CATEGORY_INDUSTRIES.getOrDefault(category,
                Arrays.asList(industry));
        System.out.println("[PeerComparison] 조회할 업종 리스트: " + categoryIndustries);

        // 카테고리명 설정 (한글)
        String categoryName = getCategoryDisplayName(category);
        stats.setIndustry(categoryName);

        // 동종업계 재무 데이터 조회 (카테고리 내 모든 업종)
        List<FinancialData> peerDataList = financialDao.getFinancialDataByIndustries(categoryIndustries);

        System.out.println("[PeerComparison] 조회된 기업 수: " + (peerDataList != null ? peerDataList.size() : 0));

        if (peerDataList == null || peerDataList.isEmpty()) {
            stats.setPeerCount(0);
            return stats;
        }

        stats.setPeerCount(peerDataList.size());

        // 평균 계산
        calculateAverages(stats, peerDataList);

        // 중앙값 계산
        calculateMedians(stats, peerDataList);

        // 현재 기업의 백분위 순위 계산
        if (currentData != null) {
            calculatePercentiles(stats, peerDataList, currentData);
        }

        return stats;
    }

    /**
     * 카테고리 표시명 반환
     */
    private String getCategoryDisplayName(IndustryCategory category) {
        switch (category) {
            case FINANCE:
                return "금융";
            case BIO_PHARMA:
                return "바이오/제약";
            case IT_SOFTWARE:
                return "IT/전자";
            case MANUFACTURING:
                return "제조업";
            case UTILITY:
                return "유틸리티/통신";
            case RETAIL:
                return "유통";
            case CONSTRUCTION:
                return "건설/부동산";
            case TRANSPORTATION:
                return "운송/물류";
            case FOOD_BEVERAGE:
                return "음식료";
            case CHEMICAL:
                return "화학";
            case TEXTILE:
                return "섬유/의류";
            case ENTERTAINMENT:
                return "엔터/미디어";
            case SERVICE:
                return "서비스";
            default:
                return "기타";
        }
    }

    /**
     * 평균 지표 계산
     */
    private void calculateAverages(PeerStats stats, List<FinancialData> peerDataList) {
        List<BigDecimal> operatingMargins = new ArrayList<>();
        List<BigDecimal> roes = new ArrayList<>();
        List<BigDecimal> debtRatios = new ArrayList<>();
        List<BigDecimal> pers = new ArrayList<>();
        List<BigDecimal> pbrs = new ArrayList<>();
        List<BigDecimal> revenueGrowthRates = new ArrayList<>();

        for (FinancialData data : peerDataList) {
            if (data.getOperatingMargin() != null)
                operatingMargins.add(data.getOperatingMargin());
            if (data.getRoe() != null)
                roes.add(data.getRoe());
            if (data.getDebtRatio() != null)
                debtRatios.add(data.getDebtRatio());
            if (data.getPerRatio() != null && data.getPerRatio().compareTo(BigDecimal.ZERO) > 0)
                pers.add(data.getPerRatio());
            if (data.getPbrRatio() != null && data.getPbrRatio().compareTo(BigDecimal.ZERO) > 0)
                pbrs.add(data.getPbrRatio());
            if (data.getRevenueGrowthRate() != null)
                revenueGrowthRates.add(data.getRevenueGrowthRate());
        }

        stats.setAvgOperatingMargin(average(operatingMargins));
        stats.setAvgRoe(average(roes));
        stats.setAvgDebtRatio(average(debtRatios));
        stats.setAvgPer(average(pers));
        stats.setAvgPbr(average(pbrs));
        stats.setAvgRevenueGrowthRate(average(revenueGrowthRates));
    }

    /**
     * 중앙값 지표 계산
     */
    private void calculateMedians(PeerStats stats, List<FinancialData> peerDataList) {
        List<BigDecimal> operatingMargins = peerDataList.stream()
                .map(FinancialData::getOperatingMargin)
                .filter(v -> v != null)
                .sorted()
                .collect(Collectors.toList());

        List<BigDecimal> roes = peerDataList.stream()
                .map(FinancialData::getRoe)
                .filter(v -> v != null)
                .sorted()
                .collect(Collectors.toList());

        List<BigDecimal> debtRatios = peerDataList.stream()
                .map(FinancialData::getDebtRatio)
                .filter(v -> v != null)
                .sorted()
                .collect(Collectors.toList());

        List<BigDecimal> pers = peerDataList.stream()
                .map(FinancialData::getPerRatio)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .collect(Collectors.toList());

        List<BigDecimal> pbrs = peerDataList.stream()
                .map(FinancialData::getPbrRatio)
                .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                .sorted()
                .collect(Collectors.toList());

        stats.setMedianOperatingMargin(median(operatingMargins));
        stats.setMedianRoe(median(roes));
        stats.setMedianDebtRatio(median(debtRatios));
        stats.setMedianPer(median(pers));
        stats.setMedianPbr(median(pbrs));
    }

    /**
     * 현재 기업의 백분위 순위 계산
     * 예: 상위 20% = 100개 중 20번째 이상
     */
    private void calculatePercentiles(PeerStats stats, List<FinancialData> peerDataList,
            FinancialData currentData) {
        // 영업이익률 (높을수록 좋음) - 상위 %
        if (currentData.getOperatingMargin() != null) {
            List<BigDecimal> values = peerDataList.stream()
                    .map(FinancialData::getOperatingMargin)
                    .filter(v -> v != null)
                    .sorted(Collections.reverseOrder()) // 내림차순
                    .collect(Collectors.toList());
            stats.setOperatingMarginPercentile(
                    calculatePercentileRank(values, currentData.getOperatingMargin(), true));
        }

        // ROE (높을수록 좋음) - 상위 %
        if (currentData.getRoe() != null) {
            List<BigDecimal> values = peerDataList.stream()
                    .map(FinancialData::getRoe)
                    .filter(v -> v != null)
                    .sorted(Collections.reverseOrder())
                    .collect(Collectors.toList());
            stats.setRoePercentile(
                    calculatePercentileRank(values, currentData.getRoe(), true));
        }

        // 부채비율 (낮을수록 좋음) - 하위 %
        if (currentData.getDebtRatio() != null) {
            List<BigDecimal> values = peerDataList.stream()
                    .map(FinancialData::getDebtRatio)
                    .filter(v -> v != null)
                    .sorted() // 오름차순
                    .collect(Collectors.toList());
            stats.setDebtRatioPercentile(
                    calculatePercentileRank(values, currentData.getDebtRatio(), false));
        }

        // PER (낮을수록 좋음) - 하위 %
        if (currentData.getPerRatio() != null) {
            List<BigDecimal> values = peerDataList.stream()
                    .map(FinancialData::getPerRatio)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                    .sorted()
                    .collect(Collectors.toList());
            stats.setPerPercentile(
                    calculatePercentileRank(values, currentData.getPerRatio(), false));
        }

        // PBR (낮을수록 좋음) - 하위 %
        if (currentData.getPbrRatio() != null) {
            List<BigDecimal> values = peerDataList.stream()
                    .map(FinancialData::getPbrRatio)
                    .filter(v -> v != null && v.compareTo(BigDecimal.ZERO) > 0)
                    .sorted()
                    .collect(Collectors.toList());
            stats.setPbrPercentile(
                    calculatePercentileRank(values, currentData.getPbrRatio(), false));
        }
    }

    /**
     * 백분위 순위 계산
     * 
     * @param sortedValues   정렬된 값 리스트
     * @param value          현재 값
     * @param higherIsBetter true면 높을수록 좋음 (내림차순), false면 낮을수록 좋음 (오름차순)
     * @return 백분위 (1~100)
     */
    private Integer calculatePercentileRank(List<BigDecimal> sortedValues, BigDecimal value,
            boolean higherIsBetter) {
        if (sortedValues.isEmpty() || value == null) {
            return null;
        }

        int rank = 0;
        for (int i = 0; i < sortedValues.size(); i++) {
            if (higherIsBetter) {
                // 내림차순: 값보다 큰 것이 몇 개인지
                if (sortedValues.get(i).compareTo(value) >= 0) {
                    rank = i + 1;
                } else {
                    break;
                }
            } else {
                // 오름차순: 값보다 작은 것이 몇 개인지
                if (sortedValues.get(i).compareTo(value) <= 0) {
                    rank = i + 1;
                } else {
                    break;
                }
            }
        }

        // 백분위 계산 (1~100)
        int percentile = (int) Math.ceil((double) rank / sortedValues.size() * 100);
        return Math.max(1, Math.min(100, percentile));
    }

    /**
     * 평균 계산
     */
    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * 중앙값 계산
     */
    private BigDecimal median(List<BigDecimal> sortedValues) {
        if (sortedValues.isEmpty()) {
            return null;
        }
        int size = sortedValues.size();
        if (size % 2 == 0) {
            return sortedValues.get(size / 2 - 1)
                    .add(sortedValues.get(size / 2))
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            return sortedValues.get(size / 2);
        }
    }
}
