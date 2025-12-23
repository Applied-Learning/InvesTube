package com.Investube.mvc.model.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.Investube.mvc.model.dto.NewsArticle;

@Service
public class NewsService {

    private static final String NAVER_NEWS_URL = "https://openapi.naver.com/v1/search/news.json";

    @Value("${naver.api.client-id:}")
    private String clientId;

    @Value("${naver.api.client-secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<NewsArticle> searchNews(String stockName, String stockCode, int limit) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            return Collections.emptyList();
        }

        String query = buildQuery(stockName, stockCode);
        if (query.isBlank()) {
            return Collections.emptyList();
        }

        // 최신 기사 위주로 더 많이 받아서 필터링
        int display = Math.min(Math.max(limit * 3, limit), 20);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", clientId);
        headers.add("X-Naver-Client-Secret", clientSecret);

        URI uri = UriComponentsBuilder.fromHttpUrl(NAVER_NEWS_URL)
                .queryParam("query", query)
                .queryParam("display", display)
                // 최신순 우선
                .queryParam("sort", "date")
                .build()
                .encode()
                .toUri();

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
        } catch (Exception e) {
            System.err.println("Naver 뉴스 API 호출 실패: " + e.getMessage());
            return Collections.emptyList(); // 외부 API 오류는 빈 목록으로 대체
        }

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("items");
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        List<NewsArticle> articles = new ArrayList<>();
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        java.time.OffsetDateTime cutoff = now.minusDays(7);

        for (Map<String, Object> item : items) {
            String title = cleanText(item.get("title"));
            String description = cleanText(item.get("description"));
            String link = item.getOrDefault("link", "").toString();
            String originalLink = item.getOrDefault("originallink", link).toString();
            String publishedAt = parsePubDate(item.get("pubDate"));
            String source = extractSource(link);

            articles.add(new NewsArticle(title, description, link, originalLink, publishedAt, source));
        }

        // 최신순으로 정렬 및 최근 14일 이내 우선
        List<NewsArticle> recent = articles.stream()
                .filter(a -> {
                    java.time.OffsetDateTime t = parseToTime(a.getPublishedAt());
                    return t != java.time.OffsetDateTime.MIN && t.isAfter(cutoff);
                })
                .sorted((a, b) -> parseToTime(b.getPublishedAt()).compareTo(parseToTime(a.getPublishedAt())))
                .toList();

        return recent.stream()
                .limit(limit)
                .toList();
    }

    private String cleanText(Object textObj) {
        if (textObj == null) {
            return "";
        }
        String text = textObj.toString();
        String withoutTags = text.replaceAll("<[^>]*>", "");
        return HtmlUtils.htmlUnescape(withoutTags).trim();
    }

    private String parsePubDate(Object pubDateObj) {
        if (pubDateObj == null) {
            return null;
        }
        String pubDate = pubDateObj.toString();
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(pubDate, DateTimeFormatter.RFC_1123_DATE_TIME);
            return zdt.toOffsetDateTime().toString();
        } catch (Exception e) {
            return pubDate;
        }
    }

    private String extractSource(String link) {
        try {
            URI uri = URI.create(link);
            String host = uri.getHost();
            if (host == null) {
                return null;
            }
            if (host.startsWith("www.")) {
                return host.substring(4);
            }
            return host;
        } catch (Exception e) {
            return null;
        }
    }

    private String buildQuery(String stockName, String stockCode) {
        StringBuilder sb = new StringBuilder();
        if (stockName != null && !stockName.isBlank()) {
            sb.append("\"").append(stockName.trim()).append("\""); // 정확히 일치하는 종목명을 우선 검색
        }
        if (stockCode != null && !stockCode.isBlank()) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(stockCode.trim());
        }
        return sb.toString();
    }

    private java.time.OffsetDateTime parseToTime(String iso) {
        if (iso == null || iso.isBlank()) {
            return java.time.OffsetDateTime.MIN;
        }
        try {
            return java.time.OffsetDateTime.parse(iso);
        } catch (Exception e) {
            return java.time.OffsetDateTime.MIN;
        }
    }
}
