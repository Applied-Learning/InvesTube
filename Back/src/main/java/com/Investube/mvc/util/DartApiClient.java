package com.Investube.mvc.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
public class DartApiClient {
    
    @Value("${dart.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String DART_API_BASE_URL = "https://opendart.fss.or.kr/api";
    
    /**
     * 상장 기업 목록 조회
     * @return API 응답 결과
     */
    public String getCorpCodeList() {
        String url = DART_API_BASE_URL + "/corpCode.xml";
        
        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("crtfc_key", apiKey)
                .build()
                .toUri();
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("DART API 호출 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 기업 개황 조회
     * @param corpCode 기업 고유 코드
     * @return API 응답 결과
     */
    public String getCompanyInfo(String corpCode) {
        String url = DART_API_BASE_URL + "/company.json";
        
        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("crtfc_key", apiKey)
                .queryParam("corp_code", corpCode)
                .build()
                .toUri();
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("DART API 호출 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 주요 재무 정보 조회
     * @param corpCode 기업 고유 코드
     * @param year 조회 년도
     * @return API 응답 결과
     */
    public String getFinancialStatement(String corpCode, String year) {
        String url = DART_API_BASE_URL + "/fnlttSinglAcnt.json";
        
        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("crtfc_key", apiKey)
                .queryParam("corp_code", corpCode)
                .queryParam("bsns_year", year)
                .queryParam("reprt_code", "11011") // 사업보고서
                .build()
                .toUri();
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            return response.getBody();
        } catch (Exception e) {
            System.err.println("DART API 호출 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
}
