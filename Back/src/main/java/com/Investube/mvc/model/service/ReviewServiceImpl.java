package com.Investube.mvc.model.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.Investube.mvc.model.dao.ReviewDao;
import com.Investube.mvc.model.dto.Review;

@Service
public class ReviewServiceImpl implements ReviewService {
	
	private final ReviewDao reviewDao;
	
	public ReviewServiceImpl(ReviewDao reviewDao) {
		this.reviewDao = reviewDao;
	}

	@Override
	public List<Review> getAllReviews() {
		return reviewDao.selectAll();
	}

	@Override
	public Review getReview(int reviewId) {
		return reviewDao.selectOne(reviewId);
	}

	@Override
	public List<Review> getReviewsByVideoId(int videoId) {
		return reviewDao.selectByVideoId(videoId);
	}

	@Override
	public boolean createReview(Review review) {
		return reviewDao.insertReview(review) > 0;
	}

	@Override
	public boolean modifyReview(Review review) {
		return reviewDao.updateReview(review) > 0;
	}

	@Override
	public boolean removeReview(int reviewId) {
		return reviewDao.deleteReview(reviewId) > 0;
	}

	@Override
	public List<Review> getReviewsByUser(int userId) {
		return reviewDao.getReviewsByUser(userId);
	}
}
