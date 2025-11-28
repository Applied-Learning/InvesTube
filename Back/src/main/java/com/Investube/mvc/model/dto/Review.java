package com.Investube.mvc.model.dto;

public class Review {
	private int reviewId;
	private int videoId;
	private int userId;
	private String content;
	private double rating;
	private String createdAt;
	private String updatedAt;
	
	public Review() {}
	
	public Review(int reviewId, int videoId, int userId, String content, 
	              double rating, String createdAt, String updatedAt) {
		this.reviewId = reviewId;
		this.videoId = videoId;
		this.userId = userId;
		this.content = content;
		this.rating = rating;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
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

	@Override
	public String toString() {
		return "Review [reviewId=" + reviewId + ", videoId=" + videoId + ", userId=" + userId
				+ ", content=" + content + ", rating=" + rating + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
}
