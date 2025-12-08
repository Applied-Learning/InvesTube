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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "팔로우 API", description = "사용자 팔로우/언팔로우 기능")
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
	@Operation(summary = "팔로워 목록 조회", description = "특정 사용자의 팔로워 목록을 조회합니다")
	@ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공")
	@GetMapping("/{userId}/followers")
	public ResponseEntity<List<Follow>> getFollowers(@Parameter(description = "사용자 ID") @PathVariable("userId") int userId) {
		List<Follow> result = followService.getFollowers(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 팔로잉 목록
	@Operation(summary = "팔로잉 목록 조회", description = "특정 사용자의 팔로잉 목록을 조회합니다")
	@ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공")
	@GetMapping("/{userId}/followings")
	public ResponseEntity<List<Follow>> getFollowings(@Parameter(description = "사용자 ID") @PathVariable("userId") int userId) {
		List<Follow> result = followService.getFollowings(userId);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	// 팔로우 상태 확인
	@Operation(summary = "팔로우 상태 확인", description = "특정 사용자를 팔로우하고 있는지 확인합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "팔로우 상태 확인 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@GetMapping("status/{followingId}")
	public ResponseEntity<Boolean> checkFollowStatus(
			@Parameter(description = "확인할 사용자 ID") @PathVariable("followingId") int followingId,
			@Parameter(description = "JWT 토큰") @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
		// JWT 토큰에서 userId 추출
		String token = authorizationHeader.replace("Bearer ", "");
		Integer followerId = jwtUtil.getUserIdByToken(token);

		if (followerId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		boolean isFollowing = followService.isFollowing(followerId, followingId);
		return new ResponseEntity<>(isFollowing, HttpStatus.OK);
	}

	// 팔로우 토글
	@Operation(summary = "팔로우/언팔로우 토글", description = "사용자를 팔로우하거나 언팔로우합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "팔로우/언팔로우 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("toggle/{followingId}")
	public ResponseEntity<String> toggleFollow(
			@Parameter(description = "팔로우할 사용자 ID") @PathVariable("followingId") int followingId,
			@Parameter(description = "JWT 토큰") @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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
