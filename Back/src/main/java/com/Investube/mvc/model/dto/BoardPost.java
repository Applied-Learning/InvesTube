package com.Investube.mvc.model.dto;

import java.time.LocalDateTime;

public class BoardPost {
	private int postId;
    private int userId;
    private String title;
    private String content;
    private int viewCount;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 작성자 정보
    private String authorNickname;
    private String authorProfileImage;

	public int getPostId() {
		return postId;
	}

	public void setPostId(int postId) {
		this.postId = postId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getAuthorNickname() {
		return authorNickname;
	}

	public void setAuthorNickname(String authorNickname) {
		this.authorNickname = authorNickname;
	}

	public String getAuthorProfileImage() {
		return authorProfileImage;
	}

	public void setAuthorProfileImage(String authorProfileImage) {
		this.authorProfileImage = authorProfileImage;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
}

