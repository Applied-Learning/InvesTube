package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.Follow;
import com.Investube.mvc.model.service.FollowService;
import com.Investube.mvc.util.JwtUtil;

@RestController
@RequestMapping("/follow")
public class FollowRestController {

	private final FollowService followService;
	private final JwtUtil jwtUtil;

	public FollowRestController(FollowService followService, JwtUtil jwtUtil) {
		this.followService = followService;
		this.jwtUtil = jwtUtil;
	}

	// 팔로워 목록
	@GetMapping("/{userId}/followers")
	public ResponseEntity<List<Follow>> getFollowers(@PathVariable("userId") int userId) {
		List<Follow> result = followService.getFollowers(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 팔로잉 목록
	@GetMapping("/{userId}/followings")
	public ResponseEntity<List<Follow>> getFollowings(@PathVariable("userId") int userId) {
		List<Follow> result = followService.getFollowings(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 팔로우 토글
	@PostMapping("toggle/{followingId}")
	public ResponseEntity<String> toggleFollow(@PathVariable("followingId") int followingId,
			@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
		// JWT 토큰에서 userId 추출
		String token = authorizationHeader.replace("Bearer ", "");
		Integer followerId = jwtUtil.getUserIdByToken(token); // JWT에서 userId 추출

		// 로그인되지 않은 사용자 처리
		if (followerId == null) {
	        return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);  // 로그인되지 않았으면 UNAUTHORIZED 응답
	    }

		// 팔로우/언팔로우 토글 수행
		String result = followService.toggleFollow(followerId, followingId);
		return new ResponseEntity<>(result, HttpStatus.OK); // 성공적으로 팔로우/언팔로우 처리됨
	}
}
