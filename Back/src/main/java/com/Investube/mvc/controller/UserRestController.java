package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.User;
import com.Investube.mvc.model.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
	
	private final UserService userService;
	
	public UserRestController(UserService userService) {
		this.userService = userService;
	}
	
	// 전체 사용자 조회
	@GetMapping
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userService.getAllUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}
	
	// 사용자 ID로 조회
	@GetMapping("/{userId}")
	public ResponseEntity<User> getUser(@PathVariable int userId) {
		User user = userService.getUser(userId);
		if (user != null) {
			return new ResponseEntity<>(user, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// 사용자 등록 (회원가입)
	@PostMapping("/signup")
	public ResponseEntity<Void> signup(@RequestBody User user) {
		if (userService.createUser(user)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 로그인
	@PostMapping("/login")
	public ResponseEntity<User> login(@RequestBody User user) {
		User loginUser = userService.login(user.getUsername(), user.getPassword());
		if (loginUser != null) {
			return new ResponseEntity<>(loginUser, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
	// 사용자 정보 수정
	@PutMapping("/{userId}")
	public ResponseEntity<Void> updateUser(@PathVariable int userId, @RequestBody User user) {
		user.setUserId(userId);
		if (userService.modifyUser(user)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 사용자 삭제
	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteUser(@PathVariable int userId) {
		if (userService.removeUser(userId)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
