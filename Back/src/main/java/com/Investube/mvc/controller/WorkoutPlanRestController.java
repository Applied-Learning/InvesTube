package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.WorkoutPlan;
import com.Investube.mvc.model.service.WorkoutPlanService;

@RestController
@RequestMapping("/api/workout-plans")
public class WorkoutPlanRestController {
	
	private final WorkoutPlanService workoutPlanService;
	
	public WorkoutPlanRestController(WorkoutPlanService workoutPlanService) {
		this.workoutPlanService = workoutPlanService;
	}
	
	// 전체 운동 계획 조회
	@GetMapping
	public ResponseEntity<List<WorkoutPlan>> getAllWorkoutPlans() {
		List<WorkoutPlan> plans = workoutPlanService.getAllWorkoutPlans();
		return new ResponseEntity<>(plans, HttpStatus.OK);
	}
	
	// 운동 계획 ID로 조회
	@GetMapping("/{planId}")
	public ResponseEntity<WorkoutPlan> getWorkoutPlan(@PathVariable int planId) {
		WorkoutPlan plan = workoutPlanService.getWorkoutPlan(planId);
		if (plan != null) {
			return new ResponseEntity<>(plan, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// 사용자별 운동 계획 조회
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<WorkoutPlan>> getWorkoutPlansByUserId(@PathVariable int userId) {
		List<WorkoutPlan> plans = workoutPlanService.getWorkoutPlansByUserId(userId);
		return new ResponseEntity<>(plans, HttpStatus.OK);
	}
	
	// 운동 계획 등록
	@PostMapping
	public ResponseEntity<Void> createWorkoutPlan(@RequestBody WorkoutPlan workoutPlan) {
		if (workoutPlanService.createWorkoutPlan(workoutPlan)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 운동 계획 수정
	@PutMapping("/{planId}")
	public ResponseEntity<Void> updateWorkoutPlan(@PathVariable int planId, @RequestBody WorkoutPlan workoutPlan) {
		workoutPlan.setPlanId(planId);
		if (workoutPlanService.modifyWorkoutPlan(workoutPlan)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 운동 계획 삭제
	@DeleteMapping("/{planId}")
	public ResponseEntity<Void> deleteWorkoutPlan(@PathVariable int planId) {
		if (workoutPlanService.removeWorkoutPlan(planId)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
