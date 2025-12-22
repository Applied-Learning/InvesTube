package com.Investube.mvc.model.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.Investube.mvc.model.dto.YoutubeVideoResponse;

@Service
public class YoutubeService {

	@Value("${youtube.api.key:}")
	private String youtubeApiKey;

	private final RestTemplate restTemplate;

	public YoutubeService() {
		this.restTemplate = new RestTemplate();
	}

	public List<YoutubeVideoResponse> searchVideos(String query, int maxResults) {
		if (youtubeApiKey == null || youtubeApiKey.isBlank()) {
			return Collections.emptyList();
		}
		if (query == null || query.isBlank()) {
			return Collections.emptyList();
		}
		if (maxResults <= 0 || maxResults > 25) {
			maxResults = 10;
		}

		String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/search")
				.queryParam("part", "snippet").queryParam("type", "video").queryParam("maxResults", maxResults)
				.queryParam("q", query).queryParam("key", youtubeApiKey).build().encode().toUriString();

		ResponseEntity<Map> response = restTemplate.getForEntity(URI.create(url), Map.class);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			return Collections.emptyList();
		}

		List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
		if (items == null) {
			return Collections.emptyList();
		}

		List<YoutubeVideoResponse> results = new ArrayList<>();
		for (Map<String, Object> item : items) {
			String videoId = null;
			Map<String, Object> idMap = (Map<String, Object>) item.get("id");
			if (idMap != null && idMap.get("videoId") != null) {
				videoId = idMap.get("videoId").toString();
			} else if (item.get("videoId") != null) {
				videoId = item.get("videoId").toString();
			}

			Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
			String title = snippet != null ? (String) snippet.getOrDefault("title", "") : "";
			String description = snippet != null ? (String) snippet.getOrDefault("description", "") : "";
			String channelTitle = snippet != null ? (String) snippet.getOrDefault("channelTitle", "") : "";
			String thumbnailUrl = null;
			if (snippet != null && snippet.get("thumbnails") instanceof Map) {
				Map<String, Object> thumbs = (Map<String, Object>) snippet.get("thumbnails");
				thumbnailUrl = extractThumbnail(thumbs);
			}

			if (videoId != null) {
				results.add(new YoutubeVideoResponse(videoId, title, description, channelTitle, thumbnailUrl, null));
			}
		}

		return results;
	}

	public YoutubeVideoResponse getVideo(String videoId) {
		if (youtubeApiKey == null || youtubeApiKey.isBlank()) {
			return null;
		}
		if (videoId == null || videoId.isBlank()) {
			return null;
		}

		String url = UriComponentsBuilder.fromHttpUrl("https://www.googleapis.com/youtube/v3/videos")
				.queryParam("part", "snippet,statistics,contentDetails").queryParam("id", videoId)
				.queryParam("key", youtubeApiKey).build().encode().toUriString();

		ResponseEntity<Map> response = restTemplate.getForEntity(URI.create(url), Map.class);
		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			return null;
		}

		List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
		if (items == null || items.isEmpty()) {
			return null;
		}

		Map<String, Object> item = items.get(0);
		Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
		String title = snippet != null ? (String) snippet.getOrDefault("title", "") : "";
		String description = snippet != null ? (String) snippet.getOrDefault("description", "") : "";
		String channelTitle = snippet != null ? (String) snippet.getOrDefault("channelTitle", "") : "";
		java.util.List<String> tags = null;
		if (snippet != null && snippet.get("tags") instanceof java.util.List<?>) {
			tags = new java.util.ArrayList<>();
			for (Object o : (java.util.List<?>) snippet.get("tags")) {
				if (o != null) {
					tags.add(o.toString());
				}
			}
		}
		String thumbnailUrl = null;
		if (snippet != null && snippet.get("thumbnails") instanceof Map) {
			Map<String, Object> thumbs = (Map<String, Object>) snippet.get("thumbnails");
			thumbnailUrl = extractThumbnail(thumbs);
		}

		return new YoutubeVideoResponse(videoId, title, description, channelTitle, thumbnailUrl, tags);
	}

	private String extractThumbnail(Map<String, Object> thumbs) {
		if (thumbs == null)
			return null;
		// high > medium > default
		if (thumbs.get("high") instanceof Map) {
			Object url = ((Map<?, ?>) thumbs.get("high")).get("url");
			if (url != null)
				return url.toString();
		}
		if (thumbs.get("medium") instanceof Map) {
			Object url = ((Map<?, ?>) thumbs.get("medium")).get("url");
			if (url != null)
				return url.toString();
		}
		if (thumbs.get("default") instanceof Map) {
			Object url = ((Map<?, ?>) thumbs.get("default")).get("url");
			if (url != null)
				return url.toString();
		}
		return null;
	}
}
