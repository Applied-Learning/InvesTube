package com.Investube.mvc.controller;

import com.Investube.mvc.util.GmsOpenAiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiTestController {

    private final GmsOpenAiClient gmsOpenAiClient;

    public AiTestController(GmsOpenAiClient gmsOpenAiClient) {
        this.gmsOpenAiClient = gmsOpenAiClient;
    }

    @GetMapping("/api/ai/test")
    public String test(@RequestParam(defaultValue = "주식 영상 관련성 테스트") String q) {
        return gmsOpenAiClient.chat(
                "Answer in Korean",
                "다음 문장을 한 줄로 요약해줘: " + q
        );
    }
}
