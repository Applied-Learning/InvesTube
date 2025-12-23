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

import com.Investube.mvc.model.dto.Review;
import com.Investube.mvc.model.service.ReviewService;
import com.Investube.mvc.model.service.VideoService;
import com.Investube.mvc.model.service.NotificationService;
import com.Investube.mvc.model.service.UserService;
import com.Investube.mvc.model.dto.Notification;
import com.Investube.mvc.model.dto.Video;

import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "리뷰 API", description = "동영상 리뷰 작성, 조회, 수정, 삭제")
@RestController
@RequestMapping("/reviews")
public class ReviewRestController {
	
	private final ReviewService reviewService;
	private final VideoService videoService;
	private final NotificationService notificationService;
	private final UserService userService;
    
	public ReviewRestController(ReviewService reviewService, VideoService videoService, NotificationService notificationService, UserService userService) {
		this.reviewService = reviewService;
		this.videoService = videoService;
		this.notificationService = notificationService;
		this.userService = userService;
	}
	
	// 비디오별 리뷰 목록 조회
	@Operation(summary = "동영상 리뷰 목록 조회", description = "특정 동영상의 모든 리뷰를 조회합니다")
	@ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공")
	@GetMapping("/video/{videoId}")
	public ResponseEntity<List<Review>> getVideoReviews(@Parameter(description = "동영상 ID") @PathVariable int videoId) {
		List<Review> reviews = reviewService.getReviewsByVideoId(videoId);
		return new ResponseEntity<>(reviews, HttpStatus.OK);
	}
	
	// 리뷰 작성 (인증 필요)
	@Operation(summary = "리뷰 작성", description = "동영상에 새로운 리뷰를 작성합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	@PostMapping("/video/{videoId}")
	public ResponseEntity<Void> createReview(
			@Parameter(description = "동영상 ID") @PathVariable int videoId, 
			@Parameter(description = "리뷰 정보") @RequestBody Review review, 
			HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		// videoId와 userId 설정
		review.setVideoId(videoId);
		review.setUserId(userId);
		
		if (reviewService.upsertReviewByUserAndVideo(review)) {
			videoService.updateRatingStats(videoId);
			// create notification to video owner (if reviewer is not the owner)
			try {
				Video video = videoService.getVideo(videoId);
				if (video != null && video.getUserId() != userId) {
					Notification n = new Notification();
					n.setRecipientId(video.getUserId());
					n.setActorId(userId);
					n.setType("REVIEW");
					n.setTargetType("VIDEO");
					n.setTargetId(videoId);
					try {
						String nickname = null;
						var actor = userService.getUserByUserId(userId);
						if (actor != null && actor.getNickname() != null && !actor.getNickname().isBlank()) {
							nickname = actor.getNickname();
						}
						if (nickname != null) {
							n.setMessage(nickname + "님이 당신의 영상에 리뷰를 남겼습니다.");
						} else {
							n.setMessage("사용자 " + userId + "님이 당신의 영상에 리뷰를 남겼습니다.");
						}
					} catch (Exception e) {
						n.setMessage("사용자 " + userId + "님이 당신의 영상에 리뷰를 남겼습니다.");
					}
					if (notificationService.isNotificationEnabled(video.getUserId(), "REVIEW")) {
						notificationService.createNotification(n);
					}
				}
			} catch (Exception e) {
				// notification failure should not block review creation
			}
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 리뷰 수정 (인증 + 작성자 검증)
	@Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다. 본인이 작성한 리뷰만 수정 가능합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
	})
	@PutMapping("/video/{videoId}/{reviewId}")
	public ResponseEntity<Void> updateReview(
			@Parameter(description = "동영상 ID") @PathVariable int videoId, 
			@Parameter(description = "리뷰 ID") @PathVariable int reviewId, 
	        @Parameter(description = "수정할 리뷰 정보") @RequestBody Review review, 
	        HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		// 기존 리뷰 확인
		Review existingReview = reviewService.getReview(reviewId);
		if (existingReview == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		// 비디오 ID 일치 확인
		if (existingReview.getVideoId() != videoId) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// 작성자 확인 (본인이 작성한 리뷰인지 확인)
		if (existingReview.getUserId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		review.setReviewId(reviewId);
		review.setVideoId(videoId);
		review.setUserId(userId);
		
		if (reviewService.modifyReview(review)) {
			videoService.updateRatingStats(videoId);
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 리뷰 삭제 (인증 + 작성자 검증)
	@Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다. 본인이 작성한 리뷰만 삭제 가능합니다")
	@SecurityRequirement(name = "bearerAuth")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "401", description = "인증 필요"),
		@ApiResponse(responseCode = "403", description = "권한 없음"),
		@ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
	})
	@DeleteMapping("/video/{videoId}/{reviewId}")
	public ResponseEntity<Void> deleteReview(
			@Parameter(description = "동영상 ID") @PathVariable int videoId, 
			@Parameter(description = "리뷰 ID") @PathVariable int reviewId, 
			HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		// 기존 리뷰 확인
		Review existingReview = reviewService.getReview(reviewId);
		if (existingReview == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		// 비디오 ID 일치 확인
		if (existingReview.getVideoId() != videoId) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		// 작성자 확인
		if (existingReview.getUserId() != userId) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		
		if (reviewService.removeReview(reviewId)) {
			videoService.updateRatingStats(videoId);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// request attribute에서 userId 안전하게 추출
	private Integer getUserIdFromRequest(HttpServletRequest request) {
		Object attr = request.getAttribute("userId");
		if (attr instanceof Integer) {
			return (Integer) attr;
		}
		return null;
	}
}
