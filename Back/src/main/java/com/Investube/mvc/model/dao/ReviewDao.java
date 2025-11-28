package com.Investube.mvc.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.Investube.mvc.model.dto.Review;

@Mapper
public interface ReviewDao {
	
	// 전체 리뷰 조회
	List<Review> selectAll();
	
	// 리뷰 ID로 조회
	Review selectOne(int reviewId);
	
	// 비디오별 리뷰 조회
	List<Review> selectByVideoId(int videoId);
	
	// 리뷰 등록
	int insertReview(Review review);
	
	// 리뷰 수정
	int updateReview(Review review);
	
	// 리뷰 삭제
	int deleteReview(int reviewId);
	
	// 작성한 리뷰 조회
	List<Review> getReviewsByUser(@Param("userId") int userId);

}
