package com.Investube.mvc.model.service;

import java.util.List;

import com.Investube.mvc.model.dto.Review;

public interface ReviewService {
	
	// 전체 리뷰 조회
	List<Review> getAllReviews();
	
	// 리뷰 ID로 조회
	Review getReview(int reviewId);
	
	// 비디오별 리뷰 조회
	List<Review> getReviewsByVideoId(int videoId);
	
	// 리뷰 등록
	boolean createReview(Review review);
	
	// 리뷰 수정
	boolean modifyReview(Review review);
	
	// 리뷰 삭제
	boolean removeReview(int reviewId);
}
