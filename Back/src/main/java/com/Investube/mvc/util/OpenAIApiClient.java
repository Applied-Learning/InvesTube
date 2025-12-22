package com.Investube.mvc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
     * OpenAI GPT API 호출 - JSON 응답 반환
     * 
     * @param prompt 프롬프트 텍스트
     * @return AI 응답 텍스트
     */
    public String callGPT(String prompt) {
        try {
            // OpenAI API 요청 포맷
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            
            Map<String, String> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);
            
            requestBody.put("messages", List.of(message));
            requestBody.put("temperature", 0.2); // 낮은 온도로 일관성 향상
            requestBody.put("max_tokens", 4096);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.POST, 
                requestEntity, 
                String.class
            );

            // OpenAI 응답에서 텍스트 추출
            String responseBody = response.getBody();
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> messageObj = (Map<String, Object>) choice.get("message");
                String content = (String) messageObj.get("content");
                return content.trim();
            }

            throw new RuntimeException("OpenAI API 응답에서 텍스트를 추출할 수 없습니다.");

        } catch (Exception e) {
            System.err.println("OpenAI API 호출 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OpenAI API 호출 중 오류 발생", e);
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
