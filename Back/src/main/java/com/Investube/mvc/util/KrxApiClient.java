package com.Investube.mvc.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class KrxApiClient {
    
    @Value("${krx.api.key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String KRX_API_BASE_URL = "http://data.krx.co.kr/comm/bldAttendant/getJsonData.cmd";
    
    /**
     * 주식 시세 정보 조회
     * @param stockCode 종목 코드
     * @param date 조회 날짜 (YYYYMMDD)
     * @return API 응답 결과
     */
    public String getStockPrice(String stockCode, String date) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("User-Agent", "Mozilla/5.0");
            
            String url = KRX_API_BASE_URL;
            URI uri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
                    .queryParam("trdDd", date)
                    .queryParam("isuCd", stockCode)
                    .build()
                    .toUri();
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            
            return response.getBody();
        } catch (Exception e) {
            System.err.println("KRX API 호출 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 일별 시세 조회
     * @param stockCode 종목 코드
     * @return API 응답 결과
     */
    public String getDailyStockPrice(String stockCode) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return getStockPrice(stockCode, dateStr);
    }
    
    /**
     * KOSPI/KOSDAQ 전체 종목 시세 조회
     * @param market "STK" (KOSPI) 또는 "KSQ" (KOSDAQ)
     * @param date 조회 날짜 (YYYYMMDD)
     * @return API 응답 결과
     */
    public String getAllStockPrices(String market, String date) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("User-Agent", "Mozilla/5.0");
            
            String url = KRX_API_BASE_URL;
            URI uri = UriComponentsBuilder.fromUriString(url)
                    .queryParam("bld", "dbms/MDC/STAT/standard/MDCSTAT01501")
                    .queryParam("mktId", market)
                    .queryParam("trdDd", date)
                    .build()
                    .toUri();
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            
            return response.getBody();
        } catch (Exception e) {
            System.err.println("KRX API 호출 중 오류 발생: " + e.getMessage());
            return null;
        }
    }
}
