package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.User;

public interface UserService {
	
	List<User> getAllUsers();

    User getUserByUserId(int userId);

    User getUserById(String id);

    int register(User user);

    int updateMyInfo(User user);

    int updatePassword(int userId, String password);

    int deleteUser(int userId);

    User getMyInfo(int userId);
}
