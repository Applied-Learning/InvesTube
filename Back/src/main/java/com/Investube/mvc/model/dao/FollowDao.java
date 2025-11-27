package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.Investube.mvc.model.dto.Follow;

@Mapper
public interface FollowDao {
	// 팔로워 조회
    List<Follow> findFollowersByUserId(int userId);

    // 팔로잉 조회
    List<Follow> findFollowingsByUserId(int userId);

    // 팔로우 여부 확인
    boolean existsByFollowerIdAndFollowingId(int followerId, int followingId);

    // 팔로우 관계 추가
    void addFollow(int followerId, int followingId);

    // 팔로우 관계 삭제 (언팔로우)
    void removeFollow(int followerId, int followingId);
}
