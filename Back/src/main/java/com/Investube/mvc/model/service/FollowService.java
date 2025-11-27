package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.Follow;

public interface FollowService {
	
	public List<Follow> getFollowers(int userId);
	
	public List<Follow> getFollowings(int userId);
	
	public String toggleFollow(int followerId, int followingId);
}
