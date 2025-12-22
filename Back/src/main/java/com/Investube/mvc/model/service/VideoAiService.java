package com.Investube.mvc.model.service;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dto.AiVideoDecision;
import com.Investube.mvc.model.dto.Video;
import com.Investube.mvc.model.dto.YoutubeVideoResponse;
import com.Investube.mvc.util.GmsOpenAiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class VideoAiService {

    private static final Logger log = LoggerFactory.getLogger(VideoAiService.class);

    private static final Map<Integer, String> CATEGORY_MAP = Map.of(
            1, "금융",
            2, "기술",
            3, "투자"
    );

    /**
     * SYSTEM PROMPT
     * - JSON 스키마 고정 (다중 추천 허용, 첫 번째가 대표)
     * - allowed 의미 명확화
     * - 추측 금지
     * - YouTube 메타데이터와 사용자 입력을 모두 참고하여 일관성 확인
     */
    private static final String SYSTEM_PROMPT = """
너는 투자/재테크 커뮤니티 'InvesTube'의 콘텐츠 자동 검수 AI이다.

판단 기준:
- allowed는 영상 주제가 금융/기술/투자 카테고리와 충분히 관련 있으면 true이다.
- 위험성, 투자 조언 여부, 합법성 판단으로 차단하지 않는다.
- 제공된 텍스트(제목, 설명, 링크)만을 기반으로 판단하며 영상 내용을 추측하지 않는다.
- YouTube 메타데이터와 사용자 입력이 크게 어긋나면 보수적으로 판단한다.

카테고리:
1: 금융 (주식, 채권, 재테크, 경제 분석, 시장 동향)
2: 기술 (IT, AI, 블록체인, 개발, 기술 트렌드, 핀테크)
3: 투자 (자산 배분, 투자 전략, 리스크 관리, 부동산/대체 투자)

 출력 규칙:
 - 반드시 JSON 한 덩어리만 출력한다.
 - 코드펜스(```), 주석, 추가 설명을 절대 포함하지 않는다.
 - confidence는 0.0 ~ 1.0 사이의 숫자이다.
 - 추천 카테고리는 최대 3개까지 배열로 제공하며, 첫 번째가 대표 추천이다.

반환 JSON 스키마:
 {
   "allowed": boolean,
   "reason": string,
   "recommendedCategoryId": 1 | 2 | 3,
   "recommendedCategoryIds": [1,2,3],
   "confidence": number
 }
 """;

    private final GmsOpenAiClient gmsOpenAiClient;
    private final YoutubeService youtubeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VideoAiService(GmsOpenAiClient gmsOpenAiClient, YoutubeService youtubeService) {
        this.gmsOpenAiClient = gmsOpenAiClient;
        this.youtubeService = youtubeService;
    }

    public AiVideoDecision inspectVideo(Video video) {
        return inspectVideo(video, null);
    }

    public AiVideoDecision inspectVideo(Video video, String modelOverride) {
        String youtubeId = Optional.ofNullable(video.getYoutubeVideoId()).orElse("").trim();
        YoutubeVideoResponse meta = youtubeId.isBlank() ? null : youtubeService.getVideo(youtubeId);

        String title = Optional.ofNullable(meta != null ? meta.getTitle() : null)
                .filter(t -> !t.isBlank())
                .orElse(Optional.ofNullable(video.getTitle()).orElse("(제목 없음)"));

        String description = Optional.ofNullable(meta != null ? meta.getDescription() : null)
                .filter(d -> !d.isBlank())
                .orElse(Optional.ofNullable(video.getDescription()).orElse("(설명 없음)"));

        String channel = Optional.ofNullable(meta != null ? meta.getChannelTitle() : null)
                .filter(c -> !c.isBlank())
                .orElse("(채널 정보 없음)");

        String prompt = """
사용자가 등록하려는 영상 정보를 검토하라.
가능하면 YouTube 원본 메타데이터(snippet)를 우선 사용해 판단하라.

YouTube 제목: %s
YouTube 설명: %s
YouTube 채널: %s
YouTube ID 또는 링크: %s
""".formatted(title, description, channel, youtubeId.isBlank() ? "(youtube id 없음)" : youtubeId);

        String raw = null;
        try {
            raw = gmsOpenAiClient.chat(SYSTEM_PROMPT, prompt, modelOverride);
            AiVideoDecision decision = parseDecision(raw);
            return normalizeDecision(decision);
        } catch (Exception e) {
            log.error("AI 호출/파싱 실패. raw={}", nullSafe(raw), e);
            return fallbackDecision("AI 분석 중 오류가 발생했습니다.");
        }
    }

    /**
     * AI 응답 파싱 (wrapper/순수 JSON 모두 대응)
     */
    private AiVideoDecision parseDecision(String raw) throws Exception {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("AI raw 응답이 비어있습니다.");
        }

        JsonNode root = objectMapper.readTree(raw);
        String content;

        if (root.has("choices")) {
            content = root.path("choices").path(0).path("message").path("content").asText();
        } else {
            content = raw;
        }

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("AI 응답 content가 비어있습니다.");
        }

        String jsonOnly = extractFirstJsonObject(content);
        return objectMapper.readValue(jsonOnly, AiVideoDecision.class);
    }

    /**
     * JSON만 안전하게 추출
     */
    private String extractFirstJsonObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalStateException("응답에서 JSON 객체를 찾을 수 없습니다. content=" + text);
        }
        return text.substring(start, end + 1);
    }

    /**
     * AI 결과 보정 및 방어 로직
     */
    private AiVideoDecision normalizeDecision(AiVideoDecision decision) {
        if (decision == null) {
            return fallbackDecision("AI 응답을 받지 못했습니다.");
        }

        // 다중 추천이 오면 첫 번째를 대표로 사용
        if (decision.getRecommendedCategoryIds() != null && !decision.getRecommendedCategoryIds().isEmpty()) {
            decision.setRecommendedCategoryId(decision.getRecommendedCategoryIds().get(0));
        }

        Integer categoryId = decision.getRecommendedCategoryId();
        if (categoryId == null || !CATEGORY_MAP.containsKey(categoryId)) {
            decision.setAllowed(false);
            decision.setRecommendedCategoryId(null);
            decision.setRecommendedCategoryName(null);
            decision.setRecommendedCategoryIds(null);
            decision.setRecommendedCategoryNames(null);
            decision.setConfidence(0.0);
            decision.setReason("AI 분석 결과, 해당 영상은 InvesTube의 주요 주제와의 연관성이 낮은 것으로 판단되었습니다.");
            return decision;
        }

        decision.setRecommendedCategoryName(CATEGORY_MAP.get(categoryId));
        if (decision.getRecommendedCategoryIds() != null) {
            java.util.List<String> names = decision.getRecommendedCategoryIds().stream()
                    .filter(CATEGORY_MAP::containsKey)
                    .map(CATEGORY_MAP::get)
                    .toList();
            decision.setRecommendedCategoryNames(names);
        }

        if (decision.getConfidence() == null) {
            decision.setConfidence(0.0);
        }

        if (decision.getReason() == null || decision.getReason().isBlank()) {
            decision.setReason(decision.isAllowed()
                    ? "카테고리가 적합합니다."
                    : "카테고리가 부적합합니다.");
        }

        return decision;
    }

    private AiVideoDecision fallbackDecision(String reason) {
        AiVideoDecision fallback = new AiVideoDecision();
        fallback.setAllowed(false);
        fallback.setConfidence(0.0);
        fallback.setReason(reason);
        return fallback;
    }

    private String nullSafe(String s) {
        return s == null ? "(null)" : s;
    }
}
