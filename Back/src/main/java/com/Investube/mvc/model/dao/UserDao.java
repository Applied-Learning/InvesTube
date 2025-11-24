package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.Investube.mvc.model.dto.User;

@Mapper
public interface UserDao {

	List<User> selectAll();

	User selectByUserId(int userId);

	User selectById(String id);

	int insert(User user);

	int updateMyInfo(User user);

	int updatePassword(@Param("userId") int userId, @Param("password") String password);

	int delete(int userId);
}
