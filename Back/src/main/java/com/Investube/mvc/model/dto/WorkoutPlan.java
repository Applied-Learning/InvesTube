package com.Investube.mvc.model.dto;

public class WorkoutPlan {
	private int planId;
	private int userId;
	private String planName;
	private String description;
	private String startDate;
	private String endDate;
	private String createdAt;
	
	public WorkoutPlan() {}
	
	public WorkoutPlan(int planId, int userId, String planName, String description, 
	                   String startDate, String endDate, String createdAt) {
		this.planId = planId;
		this.userId = userId;
		this.planName = planName;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdAt = createdAt;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "WorkoutPlan [planId=" + planId + ", userId=" + userId + ", planName=" + planName + ", description="
				+ description + ", startDate=" + startDate + ", endDate=" + endDate + ", createdAt=" + createdAt + "]";
	}
}
