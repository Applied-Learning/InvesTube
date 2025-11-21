package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.WorkoutPlan;

public interface WorkoutPlanService {
	
	// 전체 운동 계획 조회
	List<WorkoutPlan> getAllWorkoutPlans();
	
	// 운동 계획 ID로 조회
	WorkoutPlan getWorkoutPlan(int planId);
	
	// 사용자별 운동 계획 조회
	List<WorkoutPlan> getWorkoutPlansByUserId(int userId);
	
	// 운동 계획 등록
	boolean createWorkoutPlan(WorkoutPlan workoutPlan);
	
	// 운동 계획 수정
	boolean modifyWorkoutPlan(WorkoutPlan workoutPlan);
	
	// 운동 계획 삭제
	boolean removeWorkoutPlan(int planId);
}
