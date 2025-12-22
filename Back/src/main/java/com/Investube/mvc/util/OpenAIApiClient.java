package com.Investube.mvc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI GPT API 클라이언트 (HttpURLConnection 직접 사용)
 */
@Component
public class OpenAIApiClient {

    @Value("${gms.api.key}")
    private String apiKey;

    @Value("${gms.api.url:https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${gms.model:gpt-4.1-mini}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OpenAI GPT API 호출 (system/user 분리) - HttpURLConnection 직접 사용
     */
    public String callGPTWithRoles(String systemPrompt, String userPrompt) {
        HttpURLConnection connection = null;
        try {
            System.out.println("[OpenAIApiClient] API 호출 시작 (HttpURLConnection)");
            System.out.println("[OpenAIApiClient] API URL: " + apiUrl);
            System.out.println("[OpenAIApiClient] Model: " + model);
            System.out.println("[OpenAIApiClient] API Key 존재 여부: " + (apiKey != null && !apiKey.isEmpty()));

            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("GMS API 키가 설정되지 않았습니다.");
            }

            // 요청 바디 생성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 4096);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            System.out.println("[OpenAIApiClient] Request Body 길이: " + jsonBody.length());

            // HttpURLConnection 설정
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);

            // 헤더 설정 (curl과 동일하게)
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("User-Agent", "curl/7.68.0");

            System.out.println("[OpenAIApiClient] HTTP 요청 전송 중...");

            // 요청 바디 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            System.out.println("[OpenAIApiClient] 응답 코드: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 성공 응답 읽기
                StringBuilder response = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                }

                String responseBody = response.toString();
                System.out.println("[OpenAIApiClient] API 응답 수신 성공");

                // JSON 파싱
                Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                    String content = (String) messageObj.get("content");
                    System.out.println("[OpenAIApiClient] 응답 추출 완료");
                    return content.trim();
                }

                throw new RuntimeException("OpenAI API 응답에서 텍스트를 추출할 수 없습니다.");

            } else {
                // 에러 응답 읽기
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                }
                String errorBody = errorResponse.toString();
                System.err.println("[OpenAIApiClient] API 에러: " + responseCode + " - " + errorBody);
                throw new RuntimeException("API 호출 실패: " + responseCode + " - " + errorBody);
            }

        } catch (Exception e) {
            System.err.println("[OpenAIApiClient] API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OpenAI API 호출 중 오류 발생: " + e.getMessage(), e);
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
