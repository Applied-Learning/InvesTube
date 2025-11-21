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

@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {
	
	private final ReviewService reviewService;
	
	public ReviewRestController(ReviewService reviewService) {
		this.reviewService = reviewService;
	}
	
	// 전체 리뷰 조회
	@GetMapping
	public ResponseEntity<List<Review>> getAllReviews() {
		List<Review> reviews = reviewService.getAllReviews();
		return new ResponseEntity<>(reviews, HttpStatus.OK);
	}
	
	// 리뷰 ID로 조회
	@GetMapping("/{reviewId}")
	public ResponseEntity<Review> getReview(@PathVariable int reviewId) {
		Review review = reviewService.getReview(reviewId);
		if (review != null) {
			return new ResponseEntity<>(review, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
	// 비디오별 리뷰 조회
	@GetMapping("/video/{videoId}")
	public ResponseEntity<List<Review>> getReviewsByVideoId(@PathVariable int videoId) {
		List<Review> reviews = reviewService.getReviewsByVideoId(videoId);
		return new ResponseEntity<>(reviews, HttpStatus.OK);
	}
	
	// 리뷰 등록
	@PostMapping
	public ResponseEntity<Void> createReview(@RequestBody Review review) {
		if (reviewService.createReview(review)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 리뷰 수정
	@PutMapping("/{reviewId}")
	public ResponseEntity<Void> updateReview(@PathVariable int reviewId, @RequestBody Review review) {
		review.setReviewId(reviewId);
		if (reviewService.modifyReview(review)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	// 리뷰 삭제
	@DeleteMapping("/{reviewId}")
	public ResponseEntity<Void> deleteReview(@PathVariable int reviewId) {
		if (reviewService.removeReview(reviewId)) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
