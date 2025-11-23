package com.Investube.mvc.model.dto;

public class VideoWish {
	private int userId;
	private int videoId;
	private String createdAt;
	
	public VideoWish() {}
	
	public VideoWish(int userId, int videoId, String createdAt) {
		this.userId = userId;
		this.videoId = videoId;
		this.createdAt = createdAt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getVideoId() {
		return videoId;
	}

	public void setVideoId(int videoId) {
		this.videoId = videoId;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "VideoWish [userId=" + userId + ", videoId=" + videoId + ", createdAt=" + createdAt + "]";
	}
}
