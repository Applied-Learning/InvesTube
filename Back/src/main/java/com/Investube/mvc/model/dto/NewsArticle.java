package com.Investube.mvc.model.dto;

/**
 * Simple DTO representing a news article fetched from an external provider.
 */
public class NewsArticle {
    private String title;
    private String summary;
    private String link;
    private String originalLink;
    private String publishedAt;
    private String source;

    public NewsArticle() {
    }

    public NewsArticle(String title, String summary, String link, String originalLink, String publishedAt, String source) {
        this.title = title;
        this.summary = summary;
        this.link = link;
        this.originalLink = originalLink;
        this.publishedAt = publishedAt;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getOriginalLink() {
        return originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
