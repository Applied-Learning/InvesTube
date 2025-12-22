package com.Investube.mvc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI GPT API 클라이언트
 */
@Component
public class OpenAIApiClient {

    @Value("${gms.api.key}")
    private String apiKey;

    @Value("${gms.api.url:https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${gms.model:gpt-4.1-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OpenAI GPT API 호출 (system/user 분리)
     * 
     * @param systemPrompt 시스템 프롬프트
     * @param userPrompt 사용자 프롬프트
     * @return AI 응답 텍스트
     */
    public String callGPTWithRoles(String systemPrompt, String userPrompt) {
        try {
            System.out.println("[OpenAIApiClient] API 호출 시작");
            System.out.println("[OpenAIApiClient] API URL: " + apiUrl);
            System.out.println("[OpenAIApiClient] Model: " + model);
            System.out.println("[OpenAIApiClient] API Key 존재 여부: " + (apiKey != null && !apiKey.isEmpty()));
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("GMS API 키가 설정되지 않았습니다. 환경변수 GMS_KEY를 확인하세요.");
            }
            
            // OpenAI API 요청 포맷 - system과 user 분리
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            // System 메시지
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            // User 메시지
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.add(userMessage);
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 4096);

            System.out.println("[OpenAIApiClient] Request Body: " + objectMapper.writeValueAsString(requestBody));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            System.out.println("[OpenAIApiClient] HTTP 요청 전송 중...");
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );

            System.out.println("[OpenAIApiClient] API 응답 수신: " + response.getStatusCode());
            
            // OpenAI 응답에서 텍스트 추출
            String responseBody = response.getBody();
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

        } catch (Exception e) {
            System.err.println("[OpenAIApiClient] API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OpenAI API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * OpenAI GPT API 호출 - JSON 응답 반환
     * 
     * @param prompt 프롬프트 텍스트
     * @return AI 응답 텍스트
     */
    public String callGPT(String prompt) {
        try {
            System.out.println("[OpenAIApiClient] API 호출 시작");
            System.out.println("[OpenAIApiClient] API URL: " + apiUrl);
            System.out.println("[OpenAIApiClient] Model: " + model);
            System.out.println("[OpenAIApiClient] API Key 존재 여부: " + (apiKey != null && !apiKey.isEmpty()));
            
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("GMS API 키가 설정되지 않았습니다. 환경변수 GMS_KEY를 확인하세요.");
            }
            
            // OpenAI API 요청 포맷
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("messages", List.of(message));
            requestBody.put("temperature", 0.2); // 낮은 온도로 일관성 향상
            requestBody.put("max_tokens", 4096);

            System.out.println("[OpenAIApiClient] Request Body: " + objectMapper.writeValueAsString(requestBody));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            System.out.println("[OpenAIApiClient] HTTP 요청 전송 중...");
            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );

            System.out.println("[OpenAIApiClient] API 응답 수신: " + response.getStatusCode());
            
            // OpenAI 응답에서 텍스트 추출
            String responseBody = response.getBody();
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

        } catch (Exception e) {
            System.err.println("[OpenAIApiClient] API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OpenAI API 호출 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * JSON 응답 파싱 헬퍼
     * GPT가 ```json ... ``` 형태로 응답할 경우 추출
     */
    public String extractJsonFromResponse(String response) {
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        // 이미 순수 JSON이면 그대로 반환
        return response.trim();
    }
}
