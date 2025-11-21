package com.Investube.mvc.model.dto;

public class Review {
	private int reviewId;
	private int videoId;
	private String writer;
	private String title;
	private String content;
	private int rating;
	private String createdAt;
	private String updatedAt;
	
	public Review() {}
	
	public Review(int reviewId, int videoId, String writer, String title, String content, 
	              int rating, String createdAt, String updatedAt) {
		this.reviewId = reviewId;
		this.videoId = videoId;
		this.writer = writer;
		this.title = title;
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

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
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
		return "Review [reviewId=" + reviewId + ", videoId=" + videoId + ", writer=" + writer + ", title=" + title
				+ ", content=" + content + ", rating=" + rating + ", createdAt=" + createdAt + ", updatedAt="
				+ updatedAt + "]";
	}
}
