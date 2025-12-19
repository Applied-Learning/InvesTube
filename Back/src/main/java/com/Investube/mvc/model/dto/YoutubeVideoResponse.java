package com.Investube.mvc.model.dto;

public class YoutubeVideoResponse {
	private String videoId;
	private String title;
	private String description;
	private String channelTitle;
	private String thumbnailUrl;

	public YoutubeVideoResponse() {
	}

	public YoutubeVideoResponse(String videoId, String title, String description, String channelTitle,
			String thumbnailUrl) {
		this.videoId = videoId;
		this.title = title;
		this.description = description;
		this.channelTitle = channelTitle;
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getChannelTitle() {
		return channelTitle;
	}

	public void setChannelTitle(String channelTitle) {
		this.channelTitle = channelTitle;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
}
