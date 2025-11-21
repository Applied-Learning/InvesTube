package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.WorkoutPlanDao;
import com.Investube.mvc.model.dto.WorkoutPlan;

@Service
public class WorkoutPlanServiceImpl implements WorkoutPlanService {
	
	private final WorkoutPlanDao workoutPlanDao;
	
	public WorkoutPlanServiceImpl(WorkoutPlanDao workoutPlanDao) {
		this.workoutPlanDao = workoutPlanDao;
	}

	@Override
	public List<WorkoutPlan> getAllWorkoutPlans() {
		return workoutPlanDao.selectAll();
	}

	@Override
	public WorkoutPlan getWorkoutPlan(int planId) {
		return workoutPlanDao.selectOne(planId);
	}

	@Override
	public List<WorkoutPlan> getWorkoutPlansByUserId(int userId) {
		return workoutPlanDao.selectByUserId(userId);
	}

	@Override
	public boolean createWorkoutPlan(WorkoutPlan workoutPlan) {
		return workoutPlanDao.insertWorkoutPlan(workoutPlan) > 0;
	}

	@Override
	public boolean modifyWorkoutPlan(WorkoutPlan workoutPlan) {
		return workoutPlanDao.updateWorkoutPlan(workoutPlan) > 0;
	}

	@Override
	public boolean removeWorkoutPlan(int planId) {
		return workoutPlanDao.deleteWorkoutPlan(planId) > 0;
	}
}
