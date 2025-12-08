package com.Investube.mvc.model.dto;

public class Video {
	private int videoId;
	private int userId;
	private String youtubeVideoId;
	private String title;
	private String thumbnailUrl;
	private String description;
	private int categoryId;
	private int viewCount;
	private int wishCount;
	private int reviewCount;
	private double avgRating;
	private String createdAt;
	private String updatedAt;
	
	// 사용자 정보 (JOIN용)
	private String uploaderNickname;
	private String uploaderProfileImage;
	
	public Video() {}
	
	public Video(int videoId, int userId, String youtubeVideoId, String title, String thumbnailUrl, 
	             String description, int categoryId, int viewCount, int wishCount, int reviewCount, 
	             double avgRating, String createdAt, String updatedAt) {
		this.videoId = videoId;
		this.userId = userId;
		this.youtubeVideoId = youtubeVideoId;
		this.title = title;
		this.thumbnailUrl = thumbnailUrl;
		this.description = description;
		this.categoryId = categoryId;
		this.viewCount = viewCount;
		this.wishCount = wishCount;
		this.reviewCount = reviewCount;
		this.avgRating = avgRating;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getYoutubeVideoId() {
		return youtubeVideoId;
	}

	public void setYoutubeVideoId(String youtubeVideoId) {
		this.youtubeVideoId = youtubeVideoId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getWishCount() {
		return wishCount;
	}

	public void setWishCount(int wishCount) {
		this.wishCount = wishCount;
	}

	public int getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}

	public double getAvgRating() {
		return avgRating;
	}

	public void setAvgRating(double avgRating) {
		this.avgRating = avgRating;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getUploaderNickname() {
		return uploaderNickname;
	}

	public void setUploaderNickname(String uploaderNickname) {
		this.uploaderNickname = uploaderNickname;
	}

	public String getUploaderProfileImage() {
		return uploaderProfileImage;
	}

	public void setUploaderProfileImage(String uploaderProfileImage) {
		this.uploaderProfileImage = uploaderProfileImage;
	}

	@Override
	public String toString() {
		return "Video [videoId=" + videoId + ", userId=" + userId + ", youtubeVideoId=" + youtubeVideoId 
				+ ", title=" + title + ", thumbnailUrl=" + thumbnailUrl + ", description=" + description 
				+ ", categoryId=" + categoryId + ", viewCount=" + viewCount + ", wishCount=" + wishCount 
				+ ", reviewCount=" + reviewCount + ", avgRating=" + avgRating + ", createdAt=" + createdAt 
				+ ", updatedAt=" + updatedAt + "]";
	}
}
