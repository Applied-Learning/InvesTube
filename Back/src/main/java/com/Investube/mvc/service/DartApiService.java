package com.Investube.mvc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * DART Open API 연동 서비스
 * - 기업 재무제표 데이터 조회
 * - DART API 문서:
 * https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS001&apiId=2019018
 */
@Service
public class DartApiService {

    @Value("${dart.api.key}")
    private String dartApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 기업 고유번호 캐시 (메모리)
    private Map<String, String> corpCodeCache = new HashMap<>();
    // 우선주 표기 등을 제거한 이름으로도 찾을 수 있도록 정규화 캐시
    private Map<String, String> normalizedCorpCodeCache = new HashMap<>();
    private boolean isCacheLoaded = false;

    private static final String DART_API_BASE_URL = "https://opendart.fss.or.kr/api";

    public DartApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 기업 고유번호 목록 다운로드 및 캐싱
     */
    private void loadCorpCodeList() {
        if (isCacheLoaded) {
            return;
        }

        try {
            System.out.println("DART 기업 고유번호 목록 다운로드 중...");

            String url = UriComponentsBuilder
                    .fromHttpUrl(DART_API_BASE_URL + "/corpCode.xml")
                    .queryParam("crtfc_key", dartApiKey)
                    .toUriString();

            // ZIP 파일 다운로드
            byte[] zipData = restTemplate.getForObject(url, byte[].class);

            if (zipData == null) {
                throw new Exception("ZIP 파일 다운로드 실패");
            }

            // ZIP 압축 해제
            try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipData))) {
                ZipEntry entry = zis.getNextEntry();
                if (entry != null) {
                    // XML 파싱
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(zis);

                    NodeList lists = doc.getElementsByTagName("list");
                    for (int i = 0; i < lists.getLength(); i++) {
                        Element list = (Element) lists.item(i);
                        String corpCode = getElementText(list, "corp_code");
                        String corpName = getElementText(list, "corp_name");

                        if (corpCode != null && corpName != null) {
                            corpCodeCache.put(corpName, corpCode);
                            String normalized = normalizeCorpName(corpName);
                            if (normalized != null && !normalized.isBlank()) {
                                normalizedCorpCodeCache.put(normalized, corpCode);
                            }
                        }
                    }

                    isCacheLoaded = true;
                    System.out.println("DART 기업 고유번호 " + corpCodeCache.size() + "개 로드 완료");
                }
            }
        } catch (Exception e) {
            System.err.println("기업 고유번호 목록 로드 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * XML Element에서 텍스트 추출
     */
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }

    /**
     * 기업 고유번호 조회
     * 
     * @param corpName 기업명
     * @return 기업 고유번호 (corp_code)
     */
    public String getCorpCode(String corpName) {
        try {
            // 캐시 로드
            if (!isCacheLoaded) {
                loadCorpCodeList();
            }

            // 정확한 매칭 시도
            if (corpCodeCache.containsKey(corpName)) {
                System.out.println("기업명 정확 매칭: " + corpName);
                return corpCodeCache.get(corpName);
            }

            // 우선주 표기를 제거한 정규화 이름 매칭 시도
            String normalized = normalizeCorpName(corpName);
            if (normalized != null && normalizedCorpCodeCache.containsKey(normalized)) {
                System.out.println("기업명 정규화 매칭: " + corpName + " -> " + normalized);
                return normalizedCorpCodeCache.get(normalized);
            }

            // 유사 매칭 시도 (㈜, (주) 등 포함된 이름)
            for (Map.Entry<String, String> entry : corpCodeCache.entrySet()) {
                if (entry.getKey().contains(corpName)) {
                    System.out.println("기업명 유사 매칭: " + corpName + " -> " + entry.getKey());
                    return entry.getValue();
                }
                if (normalized != null && entry.getKey().contains(normalized)) {
                    System.out.println("기업명 유사 매칭(정규화): " + corpName + " -> " + entry.getKey());
                    return entry.getValue();
                }
            }

            System.err.println("DART에서 기업을 찾을 수 없음: " + corpName);
            return null;
        } catch (Exception e) {
            System.err.println("기업 고유번호 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 우선주 표기(우, 우B, 우(전환) 등)와 공백을 제거한 이름 반환
     */
    private String normalizeCorpName(String corpName) {
        if (corpName == null) {
            return null;
        }
        String normalized = corpName.trim().replaceAll("\\s+", "");
        // 끝의 우/우B/우C/우(전환) 등 제거
        normalized = normalized.replaceAll("(우([A-Za-z]|[0-9]+)?(\\([^)]*\\))?)$", "");
        return normalized;
    }

    /**
     * 재무제표 조회 (단일회사 전체재무제표)
     * 
     * @param corpCode   기업 고유번호
     * @param year       회계연도
     * @param reportCode 보고서 코드 (11011: 사업보고서, 11012: 반기보고서, 11013: 1분기, 11014: 3분기)
     * @return 재무제표 데이터
     */
    public Map<String, Object> getFinancialStatement(String corpCode, int year, String reportCode) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(DART_API_BASE_URL + "/fnlttSinglAcntAll.json")
                    .queryParam("crtfc_key", dartApiKey)
                    .queryParam("corp_code", corpCode)
                    .queryParam("bsns_year", year)
                    .queryParam("reprt_code", reportCode)
                    .queryParam("fs_div", "CFS") // CFS: 연결재무제표, OFS: 개별재무제표
                    .toUriString();

            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            Map<String, Object> result = new HashMap<>();

            if (root.has("status") && "000".equals(root.get("status").asText())) {
                JsonNode list = root.get("list");
                if (list != null && list.isArray()) {
                    // 재무제표 항목별로 파싱
                    Map<String, Long> financialData = parseFinancialData(list);
                    result.put("success", true);
                    result.put("data", financialData);
                } else {
                    result.put("success", false);
                    result.put("message", "재무제표 데이터가 없습니다.");
                }
            } else {
                result.put("success", false);
                result.put("message", root.has("message") ? root.get("message").asText() : "API 호출 실패");
            }

            return result;
        } catch (Exception e) {
            System.err.println("재무제표 조회 실패: " + e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", e.getMessage());
            return errorResult;
        }
    }

    /**
     * 재무제표 JSON 데이터 파싱
     * 
     * @param jsonList 재무제표 항목 리스트
     * @return 주요 재무 지표 맵
     */
    private Map<String, Long> parseFinancialData(JsonNode jsonList) {
        Map<String, Long> data = new HashMap<>();

        // 디버그: 모든 계정과목 출력
        System.out.println("=== DART 계정과목 목록 ===");
        for (JsonNode item : jsonList) {
            String accountName = item.get("account_nm").asText();
            System.out.println("- " + accountName);
        }
        System.out.println("========================");

        for (JsonNode item : jsonList) {
            String accountName = item.get("account_nm").asText();
            String amountStr = item.get("thstrm_amount").asText().replace(",", "");

            try {
                Long amount = Long.parseLong(amountStr);

                // contains 기반 유연한 매칭 (우선순위 순서, 한 번만 할당)
                // 매출액
                if (!data.containsKey("revenue") &&
                        (accountName.contains("매출액") || accountName.contains("영업수익") || accountName.equals("매출"))) {
                    data.put("revenue", amount);
                }
                // 영업이익
                else if (!data.containsKey("operating_profit") &&
                        (accountName.contains("영업이익") || accountName.contains("영업손익"))) {
                    data.put("operating_profit", amount);
                }
                // 당기순이익 (여러 변형 지원)
                else if (!data.containsKey("net_income") &&
                        (accountName.equals("당기순이익") ||
                                accountName.equals("당기순이익(손실)") ||
                                accountName.contains("지배기업의 소유주에게 귀속되는 당기순이익") ||
                                accountName.contains("지배기업 소유주지분 당기순이익") ||
                                (accountName.equals("지배기업 소유주지분") && accountName.length() < 15))) {
                    data.put("net_income", amount);
                }
                // 자산총계 (정확 매칭만)
                else if (!data.containsKey("total_assets") && accountName.equals("자산총계")) {
                    data.put("total_assets", amount);
                }
                // 자본총계 (엄격한 정확 매칭만! - 우선순위 순서)
                else if (!data.containsKey("total_equity") &&
                        (accountName.equals("지배기업의 소유주에게 귀속되는 자본총계") ||
                                accountName.equals("자본총계"))) {
                    data.put("total_equity", amount);
                }
                // 부채총계 (정확 매칭만)
                else if (!data.containsKey("total_liabilities") && accountName.equals("부채총계")) {
                    data.put("total_liabilities", amount);
                }
                // 영업활동현금흐름
                else if (!data.containsKey("cash_flow_operating") &&
                        (accountName.contains("영업활동") && accountName.contains("현금흐름"))) {
                    data.put("cash_flow_operating", amount);
                }
                // 투자활동현금흐름
                else if (!data.containsKey("cash_flow_investing") &&
                        (accountName.contains("투자활동") && accountName.contains("현금흐름"))) {
                    data.put("cash_flow_investing", amount);
                }
                // 재무활동현금흐름
                else if (!data.containsKey("cash_flow_financing") &&
                        (accountName.contains("재무활동") && accountName.contains("현금흐름"))) {
                    data.put("cash_flow_financing", amount);
                }
            } catch (NumberFormatException e) {
                // 숫자 변환 실패 시 무시
            }
        }

        // 검증 로직
        Long assets = data.get("total_assets");
        Long liabilities = data.get("total_liabilities");
        Long equity = data.get("total_equity");

        if (assets != null && liabilities != null && assets < liabilities) {
            System.err.println("⚠️ 검증 실패: 자산총계 < 부채총계");
        }
        if (assets != null && equity != null && assets < equity) {
            System.err.println("⚠️ 검증 실패: 자산총계 < 자본총계 (자본총계 매칭 오류 가능성)");
            // 자본총계가 이상하면 제거
            data.remove("total_equity");
        }
        if (assets != null && liabilities != null && equity != null) {
            long sum = liabilities + equity;
            long diff = Math.abs(assets - sum);
            // 10% 이상 차이나면 경고
            if (diff > assets * 0.1) {
                System.err.println("⚠️ 검증 경고: 자산 ≠ 부채 + 자본 (차이: " + diff + ")");
            }
        }

        return data;
    }

    /**
     * 현금흐름표 조회
     * 
     * @param corpCode   기업 고유번호
     * @param year       회계연도
     * @param reportCode 보고서 코드
     * @return 현금흐름 데이터
     */

    public Map<String, Object> getCashFlowStatement(String corpCode, int year, String reportCode) {
        try {
            // 재무제표 조회 API 사용 (현금흐름표도 포함)
            Map<String, Object> result = getFinancialStatement(corpCode, year, reportCode);

            if (result.get("success").equals(true)) {
                @SuppressWarnings("unchecked")
                Map<String, Long> data = (Map<String, Long>) result.get("data");

                // 현금흐름 관련 데이터만 추출
                Map<String, Long> cashFlowData = new HashMap<>();
                cashFlowData.put("cash_flow_operating", data.getOrDefault("operating_cash_flow", 0L));
                cashFlowData.put("cash_flow_investing", data.getOrDefault("investing_cash_flow", 0L));
                cashFlowData.put("cash_flow_financing", data.getOrDefault("financing_cash_flow", 0L));

                result.put("data", cashFlowData);
            }

            return result;
        } catch (Exception e) {
            System.err.println("현금흐름표 조회 실패: " + e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", e.getMessage());
            return errorResult;
        }
    }
}
