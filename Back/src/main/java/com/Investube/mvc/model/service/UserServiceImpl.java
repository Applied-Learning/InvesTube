package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.UserDao;
import com.Investube.mvc.model.dto.User;

@Service
public class UserServiceImpl implements UserService {
	
	private final UserDao userDao;
	
	public UserServiceImpl(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public List<User> getAllUsers() {
		return userDao.selectAll();
	}

	@Override
	public User getUser(int userId) {
		return userDao.selectOne(userId);
	}

	@Override
	public User getUserByUsername(String username) {
		return userDao.selectByUsername(username);
	}

	@Override
	public boolean createUser(User user) {
		return userDao.insertUser(user) > 0;
	}

	@Override
	public boolean modifyUser(User user) {
		return userDao.updateUser(user) > 0;
	}

	@Override
	public boolean removeUser(int userId) {
		return userDao.deleteUser(userId) > 0;
	}

	@Override
	public User login(String username, String password) {
		User user = userDao.selectByUsername(username);
		if (user != null && user.getPassword().equals(password)) {
			return user;
		}
		return null;
	}
}
