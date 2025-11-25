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
    public User getUserByUserId(int userId) {
        return userDao.selectByUserId(userId);
    }

    @Override
    public User getUserById(String id) {
        return userDao.selectById(id);
    }

    @Override
    public int register(User user) {
        return userDao.insert(user);
    }

    @Override
    public int updateMyInfo(User user) {
        return userDao.updateMyInfo(user);
    }

    @Override
    public int updatePassword(int userId, String password) {
        return userDao.updatePassword(userId, password);
    }

    @Override
    public int deleteUser(int userId) {
        return userDao.delete(userId);
    }

    @Override
    public User getMyInfo(int userId) {
        return userDao.selectByUserId(userId);
    }
}
