package com.Investube.mvc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Google Gemini 2.5 Flash API 클라이언트
 */
@Component
public class GeminiApiClient {

    @Value("${gms.api.key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gemini API 호출 (system/user prompt)
     */
    public String callGemini(String systemPrompt, String userPrompt) {
        HttpURLConnection connection = null;
        try {
            System.out.println("[GeminiApiClient] API 호출 시작");
            System.out.println("[GeminiApiClient] API Key 존재 여부: " + (apiKey != null && !apiKey.isEmpty()));

            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("GMS API 키가 설정되지 않았습니다.");
            }

            // Gemini 요청 바디 생성
            // system prompt + user prompt를 하나의 텍스트로 합침
            String combinedPrompt = systemPrompt + "\n\n" + userPrompt;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", combinedPrompt)))));

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            System.out.println("[GeminiApiClient] Request Body 길이: " + jsonBody.length());

            // URL에 API 키 추가
            String fullUrl = GEMINI_URL + "?key=" + apiKey;
            URL url = new URL(fullUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(120000); // Gemini는 좀 더 여유롭게

            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            System.out.println("[GeminiApiClient] HTTP 요청 전송 중...");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("[GeminiApiClient] 응답 코드: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                String responseBody = response.toString();
                System.out.println("[GeminiApiClient] API 응답 수신 성공");

                // Gemini 응답 파싱
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> candidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) candidate.get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

                    if (parts != null && !parts.isEmpty()) {
                        String text = (String) parts.get(0).get("text");
                        System.out.println("[GeminiApiClient] 응답 추출 완료");
                        return text.trim();
                    }
                }

                throw new RuntimeException("Gemini API 응답에서 텍스트를 추출할 수 없습니다.");

            } else {
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                String errorBody = errorResponse.toString();
                System.err.println("[GeminiApiClient] API 에러: " + responseCode + " - " + errorBody);
                throw new RuntimeException("Gemini API 호출 실패: " + responseCode + " - " + errorBody);
            }

        } catch (Exception e) {
            System.err.println("[GeminiApiClient] API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Gemini API 호출 중 오류 발생: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * JSON 응답 파싱 헬퍼
     */
    public String extractJsonFromResponse(String response) {
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        return response.trim();
    }
}
