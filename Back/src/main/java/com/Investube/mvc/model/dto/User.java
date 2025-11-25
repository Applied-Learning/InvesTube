package com.Investube.mvc.model.dto;

import java.time.LocalDateTime;

public class User {
	private int userId;
	private String id;
	private String password;
	private String email;
	private String nickname;
	private String profileImage;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public User() {}
	
	public User(int userId, String id, String password, String email, String nickname, String profileImage,
			LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.userId = userId;
		this.id = id;
		this.password = password;
		this.email = email;
		this.nickname = nickname;
		this.profileImage = profileImage;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
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

	@Override
	public String toString() {
		return "User [userId=" + userId + ", id=" + id + ", password=" + password + ", email=" + email + ", nickname="
				+ nickname + ", profileImage=" + profileImage + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
				+ "]";
	}

}
