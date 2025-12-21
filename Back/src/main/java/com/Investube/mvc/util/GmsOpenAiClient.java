package com.Investube.mvc.util;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GmsOpenAiClient {

    @Value("${GMS_OPENAI_URL}")
    private String baseUrl;

    @Value("${GMS_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String chat(String systemContent, String userContent) {
        String url = baseUrl + "/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "curl/8.7.1");

        String escapedUserContent = userContent.replace("\"", "\\\"");

        String body = """
        {
          "model": "gpt-4.1-mini",
          "messages": [
            {
              "role": "system",
              "content": "Answer in Korean"
            },
            {
              "role": "user",
              "content": "%s"
            }
          ],
          "max_tokens": 4096,
          "temperature": 0.3
        }
        """.formatted(escapedUserContent);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response =
                restTemplate.postForEntity(url, entity, String.class);

        return response.getBody();
    }

}
