package com.Investube.mvc.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 업종별 리스크 판단 기준 유틸리티
 * 
 * 업종에 따라 재무 지표의 해석 기준이 다름:
 * - 금융업: 부채비율이 높은 것이 정상
 * - 바이오: 적자가 일반적
 * - 제조업: 일반적인 기준 적용
 * 
 * 실제 DB 업종명 기반:
 * IT서비스, 건설, 금속, 금융, 기계·장비, 기타, 기타금융, 기타제조, 농업,
 * 보험, 부동산, 비금속, 섬유·의류, 오락·문화, 운송·창고, 운송장비·부품,
 * 유통, 은행, 음식료·담배, 의료·정밀기기, 일반서비스, 전기·가스, 전기·가스·수도,
 * 전기·전자, 제약, 종이·목재, 증권, 출판·매체복제, 통신, 화학
 */
public class IndustryRiskCriteria {

    // 업종 카테고리 (확장)
    public enum IndustryCategory {
        FINANCE, // 금융, 은행, 보험, 증권, 기타금융
        BIO_PHARMA, // 제약, 의료·정밀기기
        IT_SOFTWARE, // IT서비스, 전기·전자
        MANUFACTURING, // 기계·장비, 기타제조, 금속, 비금속, 운송장비·부품, 종이·목재
        UTILITY, // 전기·가스, 전기·가스·수도, 통신
        RETAIL, // 유통
        CONSTRUCTION, // 건설, 부동산
        TRANSPORTATION, // 운송·창고
        FOOD_BEVERAGE, // 음식료·담배, 농업
        CHEMICAL, // 화학
        TEXTILE, // 섬유·의류
        ENTERTAINMENT, // 오락·문화, 출판·매체복제
        SERVICE, // 일반서비스
        DEFAULT // 기타
    }

    // 업종별 리스크 기준
    public static class RiskThresholds {
        public final BigDecimal debtRatioHigh;
        public final BigDecimal debtRatioLow;
        public final BigDecimal operatingMarginHigh;
        public final BigDecimal operatingMarginLow;
        public final BigDecimal roeHigh;
        public final BigDecimal roeLow;
        public final String description;

        public RiskThresholds(
                double debtRatioHigh, double debtRatioLow,
                double operatingMarginHigh, double operatingMarginLow,
                double roeHigh, double roeLow,
                String description) {
            this.debtRatioHigh = BigDecimal.valueOf(debtRatioHigh);
            this.debtRatioLow = BigDecimal.valueOf(debtRatioLow);
            this.operatingMarginHigh = BigDecimal.valueOf(operatingMarginHigh);
            this.operatingMarginLow = BigDecimal.valueOf(operatingMarginLow);
            this.roeHigh = BigDecimal.valueOf(roeHigh);
            this.roeLow = BigDecimal.valueOf(roeLow);
            this.description = description;
        }
    }

    private static final Map<IndustryCategory, RiskThresholds> THRESHOLDS = new HashMap<>();

    static {
        // 금융/은행/보험: 부채비율이 높은 것이 정상 (예대업 특성)
        THRESHOLDS.put(IndustryCategory.FINANCE, new RiskThresholds(
                800, 300, -30, 5, -20, 8,
                "금융업은 예대업 특성상 부채비율이 높음. 자기자본비율·순이자마진 중시"));

        // 바이오/제약/의료: R&D 투자로 적자가 일반적
        THRESHOLDS.put(IndustryCategory.BIO_PHARMA, new RiskThresholds(
                200, 80, -50, 5, -30, 5,
                "바이오/제약은 R&D 투자로 초기 적자 일반적. 파이프라인·현금소진율 중시"));

        // IT/전자: 성장 중시
        THRESHOLDS.put(IndustryCategory.IT_SOFTWARE, new RiskThresholds(
                150, 50, -20, 10, -20, 10,
                "IT/전자는 성장률 중시. 기술력, 시장점유율 중요"));

        // 제조업: 일반적인 기준
        THRESHOLDS.put(IndustryCategory.MANUFACTURING, new RiskThresholds(
                150, 50, -10, 10, -20, 10,
                "제조업은 재고회전율·가동률·원가율 중요. 일반적인 재무 기준 적용"));

        // 유틸리티/통신: 안정적 수익 기대
        THRESHOLDS.put(IndustryCategory.UTILITY, new RiskThresholds(
                250, 80, 0, 15, -10, 8,
                "유틸리티/통신은 안정적 수익·배당 중시. 인프라 투자로 부채비율 다소 높음"));

        // 유통/소매: 박리다매 특성
        THRESHOLDS.put(IndustryCategory.RETAIL, new RiskThresholds(
                180, 50, -5, 5, -15, 8,
                "유통/소매는 마진이 낮고 재고회전율 중요. 박리다매 특성 고려"));

        // 건설/부동산: 프로젝트 기반, 부채 변동 큼
        THRESHOLDS.put(IndustryCategory.CONSTRUCTION, new RiskThresholds(
                250, 80, -15, 8, -20, 8,
                "건설/부동산은 프로젝트 기반으로 부채 변동 큼. 수주잔고·분양률 중요"));

        // 운송/물류: 자본 집약적
        THRESHOLDS.put(IndustryCategory.TRANSPORTATION, new RiskThresholds(
                200, 60, -10, 8, -15, 8,
                "운송/물류는 자본 집약적. 유가·환율 영향 큼"));

        // 음식료/담배: 안정적 현금흐름
        THRESHOLDS.put(IndustryCategory.FOOD_BEVERAGE, new RiskThresholds(
                150, 40, -5, 8, -10, 10,
                "음식료는 안정적 현금흐름. 브랜드 가치·시장점유율 중요"));

        // 화학: 경기 민감
        THRESHOLDS.put(IndustryCategory.CHEMICAL, new RiskThresholds(
                180, 50, -15, 10, -20, 10,
                "화학은 경기 민감 업종. 원자재 가격·수급 영향 큼"));

        // 섬유/의류: 재고 리스크
        THRESHOLDS.put(IndustryCategory.TEXTILE, new RiskThresholds(
                150, 50, -10, 8, -15, 8,
                "섬유/의류는 재고 리스크 존재. 브랜드·트렌드 반영 중요"));

        // 엔터테인먼트/미디어: 콘텐츠 투자
        THRESHOLDS.put(IndustryCategory.ENTERTAINMENT, new RiskThresholds(
                150, 50, -20, 10, -20, 10,
                "엔터/미디어는 콘텐츠 투자 특성. 흥행 변동성 큼"));

        // 서비스: 인적 자원 중심
        THRESHOLDS.put(IndustryCategory.SERVICE, new RiskThresholds(
                150, 50, -10, 10, -15, 10,
                "서비스업은 인적 자원 중심. 인건비 비중 높음"));

        // 기타/기본값
        THRESHOLDS.put(IndustryCategory.DEFAULT, new RiskThresholds(
                200, 50, -10, 10, -20, 10,
                "일반적인 재무 분석 기준 적용"));
    }

    /**
     * 업종명을 카테고리로 변환 (실제 DB 업종명 기반)
     */
    public static IndustryCategory categorize(String industry) {
        if (industry == null || industry.isEmpty()) {
            return IndustryCategory.DEFAULT;
        }

        String lower = industry.toLowerCase().replace(" ", "");

        // 금융/은행/보험/증권
        if (containsAny(lower, "금융", "은행", "보험", "증권", "카드", "캐피탈", "저축", "투자", "기타금융")) {
            return IndustryCategory.FINANCE;
        }

        // 제약/의료
        if (containsAny(lower, "제약", "바이오", "의약", "헬스케어", "의료", "정밀기기", "진단", "셀", "젠")) {
            return IndustryCategory.BIO_PHARMA;
        }

        // IT/전자
        if (containsAny(lower, "it", "소프트웨어", "인터넷", "게임", "전기·전자", "전기전자", "전자", "반도체", "디지털", "클라우드", "플랫폼")) {
            return IndustryCategory.IT_SOFTWARE;
        }

        // 유틸리티/통신
        if (containsAny(lower, "통신", "전기·가스", "전기가스", "전력", "가스", "수도", "에너지", "유틸리티")) {
            return IndustryCategory.UTILITY;
        }

        // 건설/부동산
        if (containsAny(lower, "건설", "부동산", "건축", "시공")) {
            return IndustryCategory.CONSTRUCTION;
        }

        // 운송/물류
        if (containsAny(lower, "운송", "창고", "물류", "항공", "해운", "택배", "배송")) {
            return IndustryCategory.TRANSPORTATION;
        }

        // 유통/소매
        if (containsAny(lower, "유통", "소매", "백화점", "마트", "홈쇼핑", "편의점", "이커머스", "도매")) {
            return IndustryCategory.RETAIL;
        }

        // 음식료/담배/농업
        if (containsAny(lower, "음식료", "담배", "식품", "음료", "농업", "축산", "수산")) {
            return IndustryCategory.FOOD_BEVERAGE;
        }

        // 화학
        if (containsAny(lower, "화학", "석유화학", "정유")) {
            return IndustryCategory.CHEMICAL;
        }

        // 섬유/의류
        if (containsAny(lower, "섬유", "의류", "패션", "의복", "직물")) {
            return IndustryCategory.TEXTILE;
        }

        // 엔터테인먼트/미디어
        if (containsAny(lower, "오락", "문화", "출판", "매체", "방송", "엔터", "미디어", "영화", "게임")) {
            return IndustryCategory.ENTERTAINMENT;
        }

        // 서비스
        if (containsAny(lower, "일반서비스", "서비스")) {
            return IndustryCategory.SERVICE;
        }

        // 제조업 (기계, 금속, 운송장비 등)
        if (containsAny(lower, "제조", "기계", "장비", "금속", "비금속", "철강", "운송장비", "부품", "조선", "자동차", "종이", "목재")) {
            return IndustryCategory.MANUFACTURING;
        }

        // 기타
        if (containsAny(lower, "기타")) {
            return IndustryCategory.DEFAULT;
        }

        return IndustryCategory.DEFAULT;
    }

    /**
     * 업종에 맞는 리스크 기준 조회
     * 
     * @param industry 업종명
     * @return 리스크 기준
     */
    public static RiskThresholds getThresholds(String industry) {
        IndustryCategory category = categorize(industry);
        return THRESHOLDS.get(category);
    }

    /**
     * 리스크 레벨 판단
     * 
     * @param industry        업종명
     * @param debtRatio       부채비율 (%)
     * @param operatingMargin 영업이익률 (%)
     * @param roe             ROE (%)
     * @return "HIGH", "MEDIUM", "LOW"
     */
    public static String determineRiskLevel(String industry, BigDecimal debtRatio,
            BigDecimal operatingMargin, BigDecimal roe) {
        RiskThresholds thresholds = getThresholds(industry);

        // null 체크
        boolean debtHigh = debtRatio != null && debtRatio.compareTo(thresholds.debtRatioHigh) > 0;
        boolean marginHigh = operatingMargin != null && operatingMargin.compareTo(thresholds.operatingMarginHigh) < 0;
        boolean roeHigh = roe != null && roe.compareTo(thresholds.roeHigh) < 0;

        boolean debtLow = debtRatio != null && debtRatio.compareTo(thresholds.debtRatioLow) < 0;
        boolean marginLow = operatingMargin != null && operatingMargin.compareTo(thresholds.operatingMarginLow) > 0;
        boolean roeLow = roe != null && roe.compareTo(thresholds.roeLow) > 0;

        // HIGH: 하나라도 위험 기준 충족
        if (debtHigh || marginHigh || roeHigh) {
            return "HIGH";
        }

        // LOW: 모든 지표가 안전 기준 충족
        if (debtLow && marginLow && roeLow) {
            return "LOW";
        }

        return "MEDIUM";
    }

    /**
     * 업종별 리스크 기준 설명 (AI 프롬프트용)
     * 
     * @param industry 업종명
     * @return 프롬프트에 포함할 기준 설명
     */
    public static String getRiskCriteriaPrompt(String industry) {
        RiskThresholds thresholds = getThresholds(industry);
        IndustryCategory category = categorize(industry);

        return String.format(
                "riskLevel 판단 기준 (업종: %s):\n" +
                        "- HIGH: 부채비율 > %.0f%% OR 영업이익률 < %.0f%% OR ROE < %.0f%%\n" +
                        "- LOW: 부채비율 < %.0f%% AND 영업이익률 > %.0f%% AND ROE > %.0f%%\n" +
                        "- MEDIUM: 위 조건에 해당하지 않는 경우\n" +
                        "업종 특성: %s",
                category.name(),
                thresholds.debtRatioHigh, thresholds.operatingMarginHigh, thresholds.roeHigh,
                thresholds.debtRatioLow, thresholds.operatingMarginLow, thresholds.roeLow,
                thresholds.description);
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
