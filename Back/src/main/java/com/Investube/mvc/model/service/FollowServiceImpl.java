package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.FollowDao;
import com.Investube.mvc.model.dto.Follow;

@Service
public class FollowServiceImpl implements FollowService {
	
	private final FollowDao followDao;
	
	public FollowServiceImpl(FollowDao followDao) {
        this.followDao = followDao;
    }

	@Override
	public List<Follow> getFollowers(int userId) {
		return followDao.findFollowersByUserId(userId);
	}

	@Override
	public List<Follow> getFollowings(int userId) {
		return followDao.findFollowingsByUserId(userId);
	}

	@Override
	public String toggleFollow(int followerId, int followingId) {
		boolean isFollowing = followDao.existsByFollowerIdAndFollowingId(followerId, followingId);
		
		if(isFollowing) {
			followDao.removeFollow(followerId, followingId); // 언팔
			return "Unfollowed";
		} else {
			followDao.addFollow(followerId, followingId); // 팔로우
			return "followed";
		}
	}

}
