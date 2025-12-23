package com.Investube.mvc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Component
public class GmsOpenAiClient {

    @Value("${GMS_ENDPOINT_URL}")
    private String baseUrl;

    @Value("${GMS_API_KEY}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model:gpt-4.1-mini}")
    private String defaultModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String chat(String systemContent, String userContent) {
        return chat(systemContent, userContent, null);
    }

    public String chat(String systemContent, String userContent, String modelOverride) {
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String url = normalizedBase.contains("api.openai.com")
                ? normalizedBase + "/v1/chat/completions"
                : normalizedBase + "/api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "curl/8.7.1");

        String sanitizedSystem = systemContent == null ? "" : systemContent.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ");
        String sanitizedUser = userContent == null ? "" : userContent.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ");

        String chosenModel = (modelOverride == null || modelOverride.isBlank()) ? defaultModel : modelOverride;

        Map<String, Object> payload = Map.of(
                "model", chosenModel,
                "messages", List.of(
                        Map.of("role", "system", "content", sanitizedSystem),
                        Map.of("role", "user", "content", sanitizedUser)),
                "max_completion_tokens", 4096);

        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize OpenAI request body", e);
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            return response.getBody();
        } catch (RestClientResponseException e) {
            // up-stream 오류 내용을 그대로 올려보내 디버깅을 돕는다
            throw new IllegalStateException(
                    "AI call failed: status=" + e.getRawStatusCode() + " body=" + e.getResponseBodyAsString(), e);
        }
    }

}
