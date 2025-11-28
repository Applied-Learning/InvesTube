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

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/reviews")
public class ReviewRestController {
	
	private final ReviewService reviewService;
	
	public ReviewRestController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}
	
	// 비디오별 리뷰 목록 조회
	@GetMapping("/video/{videoId}")
	public ResponseEntity<List<Review>> getVideoReviews(@PathVariable int videoId) {
		List<Review> reviews = reviewService.getReviewsByVideoId(videoId);
		return new ResponseEntity<>(reviews, HttpStatus.OK);
	}
	
	// 리뷰 작성 (인증 필요)
	@PostMapping("/video/{videoId}")
	public ResponseEntity<Void> createReview(@PathVariable int videoId, @RequestBody Review review, HttpServletRequest request) {
		Integer userId = getUserIdFromRequest(request);
		if (userId == null) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		// videoId와 userId 설정
		review.setVideoId(videoId);
		review.setUserId(userId);
		
		if (reviewService.createReview(review)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 리뷰 수정 (인증 + 작성자 검증)
	@PutMapping("/video/{videoId}/{reviewId}")
	public ResponseEntity<Void> updateReview(@PathVariable int videoId, @PathVariable int reviewId, 
	                                         @RequestBody Review review, HttpServletRequest request) {
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
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 리뷰 삭제 (인증 + 작성자 검증)
	@DeleteMapping("/video/{videoId}/{reviewId}")
	public ResponseEntity<Void> deleteReview(@PathVariable int videoId, @PathVariable int reviewId, HttpServletRequest request) {
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
