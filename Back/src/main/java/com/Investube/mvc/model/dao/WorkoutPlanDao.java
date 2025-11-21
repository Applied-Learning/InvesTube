package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.Investube.mvc.model.dto.WorkoutPlan;

@Mapper
public interface WorkoutPlanDao {
	
	// 전체 운동 계획 조회
	List<WorkoutPlan> selectAll();
	
	// 운동 계획 ID로 조회
	WorkoutPlan selectOne(int planId);
	
	// 사용자별 운동 계획 조회
	List<WorkoutPlan> selectByUserId(int userId);
	
	// 운동 계획 등록
	int insertWorkoutPlan(WorkoutPlan workoutPlan);
	
	// 운동 계획 수정
	int updateWorkoutPlan(WorkoutPlan workoutPlan);
	
	// 운동 계획 삭제
	int deleteWorkoutPlan(int planId);
}
