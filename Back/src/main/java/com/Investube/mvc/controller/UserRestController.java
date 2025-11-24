package com.Investube.mvc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

	// 업로더 정보 조회
	@GetMapping("/{userId}")
	public ResponseEntity<User> getUser(@PathVariable("userId") int userId) {
		User result = userService.getUserByUserId(userId);

		if (result == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 내 정보 조회
	@GetMapping("/me")
	public ResponseEntity<User> getMyInfo(@RequestParam int userId) {
		User result = userService.getMyInfo(userId);

		if (result == null) {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 내 정보 수정
	@PutMapping("/me")
	public ResponseEntity<Integer> updateMyInfo(@RequestParam int userId, @RequestBody User user) {

		// body에는 nickname, profileImage만 들어와 있다고 가정
		user.setUserId(userId);

		int result = userService.updateMyInfo(user);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 비밀번호 변경
	@PutMapping("/me/password")
	public ResponseEntity<Integer> updatePassword(@RequestParam int userId, @RequestParam String password) {
		int result = userService.updatePassword(userId, password);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 회원가입
	@PostMapping
	public ResponseEntity<Integer> register(@RequestBody User user) {
		int result = userService.register(user);

		return new ResponseEntity<>(result, HttpStatus.CREATED);
	}

	// 회원 탈퇴
	@DeleteMapping("/me")
	public ResponseEntity<Integer> deleteUser(@RequestParam int userId) {
		int result = userService.deleteUser(userId);

		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
