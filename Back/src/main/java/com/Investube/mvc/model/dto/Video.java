package com.Investube.mvc.model.dto;

public class Video {
	private int videoId;
	private String title;
	private String channelName;
	private String videoUrl;
	private String thumbnailUrl;
	private String category;
	private String part; // 운동 부위 (상체, 하체, 전신 등)
	private int viewCount;
	private String uploadDate;
	
	public Video() {}
	
	public Video(int videoId, String title, String channelName, String videoUrl, String thumbnailUrl, 
	             String category, String part, int viewCount, String uploadDate) {
		this.videoId = videoId;
		this.title = title;
		this.channelName = channelName;
		this.videoUrl = videoUrl;
		this.thumbnailUrl = thumbnailUrl;
		this.category = category;
		this.part = part;
		this.viewCount = viewCount;
		this.uploadDate = uploadDate;
	}

	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public String getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}

	@Override
	public String toString() {
		return "Video [videoId=" + videoId + ", title=" + title + ", channelName=" + channelName + ", videoUrl="
				+ videoUrl + ", thumbnailUrl=" + thumbnailUrl + ", category=" + category + ", part=" + part
				+ ", viewCount=" + viewCount + ", uploadDate=" + uploadDate + "]";
	}
}
