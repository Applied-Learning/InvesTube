package com.Investube.mvc.model.dto;

public class User {
	private int userId;
	private String username;
	private String password;
	private String email;
	private String nickname;
	private String createdAt;
	
	public User() {}
	
	public User(int userId, String username, String password, String email, String nickname, String createdAt) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.email = email;
		this.nickname = nickname;
		this.createdAt = createdAt;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", username=" + username + ", password=" + password + ", email=" + email
				+ ", nickname=" + nickname + ", createdAt=" + createdAt + "]";
	}
}
