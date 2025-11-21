package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.Investube.mvc.model.dto.User;

@Mapper
public interface UserDao {
	
	// 전체 사용자 조회
	List<User> selectAll();
	
	// 사용자 ID로 조회
	User selectOne(int userId);
	
	// 사용자명으로 조회
	User selectByUsername(String username);
	
	// 사용자 등록
	int insertUser(User user);
	
	// 사용자 정보 수정
	int updateUser(User user);
	
	// 사용자 삭제
	int deleteUser(int userId);
}
