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

	private static final Map<Integer, String> CATEGORY_MAP = Map.ofEntries(
			Map.entry(1, "투자 교육"),
			Map.entry(2, "종목 분석"),
			Map.entry(3, "경제 동향"),
			Map.entry(11, "기초 교육"),
			Map.entry(12, "분석 방법"),
			Map.entry(13, "투자 전략"),
			Map.entry(21, "재무 분석"),
			Map.entry(22, "산업 분석"),
			Map.entry(23, "종목 추천"),
			Map.entry(31, "국내 경제"),
			Map.entry(32, "국제 경제"));

	/**
	 * SYSTEM PROMPT - JSON 스키마 고정 (다중 추천 허용, 첫 번째가 대표) - allowed 의미 명확화 - 추측 금지 -
	 * YouTube 메타데이터와 사용자 입력을 모두 참고하여 일관성 확인
	 */
	private static final String SYSTEM_PROMPT = """
			You are the content classification AI for 'InvesTube'.

			Guidelines:
			- allowed = true only if the video topic fits the categories below.
			- Do not allow illegal/harmful/misinformation content.
			- Use only the given metadata (title/description/channel/tags/id) for judgment.

			Categories:
			1: 투자 교육 (parent)
			11: 기초 교육 (주식 기본 개념, 투자 시작 방법)
			12: 분석 방법 (기술적 분석, 재무 분석)
			13: 투자 전략 (장기/단기 투자 원칙, 특정 종목 추천/매수·매도 시점 제시는 여기 아님)
			2: 종목 분석 (parent)
			21: 재무 분석 (재무제표 분석, 재무 비율 분석)
			22: 산업 분석 (산업 성장성 분석, 산업 경쟁력 분석)
			23: 종목 추천 (개별 기업 매수/매도 아이디어, 급등 예상, 목표가 제시)
			3: 경제 동향 (parent)
			31: 국내 경제 (한국 경제, 고용, 정부 경제 정책)
			32: 국제 경제 (글로벌 경제 동향, 주요 국가 경제 현황)

			Output rules:
			- 모든 설명/이유는 한국어로 작성한다.
			- allowed는 위 카테고리 중 하나에 적합하면 true로 설정한다. 종목 추천/급등 예상/매수·매도 시점 언급만으로 차단하지 말고, 불법/허위/유해 콘텐츠일 때만 false로 한다. 종목 추천 영상은 23번으로 허용한다.
			- reason은 항상 한 문장으로 작성한다. allowed=false이면 차단 사유를, allowed=true이면 추천 사유를 간결히 적는다. (예: "투자와 무관한 일상 브이로그입니다.")
			- "연관성이 낮다" 같은 모호한 표현은 사용하지 말고, 왜 카테고리에 부적합/차단인지 구체적 근거를 한 문장으로 제시한다.
			- 특정 종목 추천, 급등 예상, 매수/매도 시점 언급, 포트폴리오 제시는 모두 23(종목 분석)으로 분류한다. 13(투자 전략)은 종목/시점 언급 없는 방법론일 때만 사용한다.
			- reason에는 카테고리 번호를 직접 적지 말고, 내용만 설명한다.
			- Return JSON only. No code fences, no extra text.
			- confidence: 0.0 ~ 1.0
			- recommendedCategoryIds: use only the category ids above, up to 3, no duplicates. The first is the main recommendation.

			Response JSON format:
			{
			  "allowed": boolean,
			  "reason": string,
			  "recommendedCategoryId": 11 | 12 | 13 | 21 | 22 | 23 | 31 | 32,
			  "recommendedCategoryIds": [11,12,13,21,22,23,31,32],
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

		String title = Optional.ofNullable(meta != null ? meta.getTitle() : null).filter(t -> !t.isBlank())
				.orElse(Optional.ofNullable(video.getTitle()).orElse("(제목 없음)"));

		String description = Optional.ofNullable(meta != null ? meta.getDescription() : null).filter(d -> !d.isBlank())
				.orElse(Optional.ofNullable(video.getDescription()).orElse("(설명 없음)"));

		String channel = Optional.ofNullable(meta != null ? meta.getChannelTitle() : null).filter(c -> !c.isBlank())
				.orElse("(채널 정보 없음)");

		String tags = (meta != null && meta.getTags() != null && !meta.getTags().isEmpty())
				? String.join(", ", meta.getTags())
				: "(태그 없음)";

		String prompt = """
				사용자가 등록하려는 영상 정보를 검토하라.
				가능하면 YouTube 원본 메타데이터(snippet)를 우선 사용해 판단하라.

				YouTube 제목: %s
				YouTube 설명: %s
				YouTube 채널: %s
				YouTube ID 또는 링크: %s
				YouTube 태그: %s
				""".formatted(title, description, channel, youtubeId.isBlank() ? "(youtube id 없음)" : youtubeId, tags);

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
					.filter(CATEGORY_MAP::containsKey).map(CATEGORY_MAP::get).toList();
			decision.setRecommendedCategoryNames(names);
		}

		if (decision.getConfidence() == null) {
			decision.setConfidence(0.0);
		}

		if (decision.getReason() == null || decision.getReason().isBlank()) {
			decision.setReason(decision.isAllowed() ? "카테고리가 적합합니다." : "카테고리가 부적합합니다.");
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
