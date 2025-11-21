package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.User;

public interface UserService {
	
	// 전체 사용자 조회
	List<User> getAllUsers();
	
	// 사용자 ID로 조회
	User getUser(int userId);
	
	// 사용자명으로 조회
	User getUserByUsername(String username);
	
	// 사용자 등록
	boolean createUser(User user);
	
	// 사용자 정보 수정
	boolean modifyUser(User user);
	
	// 사용자 삭제
	boolean removeUser(int userId);
	
	// 로그인
	User login(String username, String password);
}
