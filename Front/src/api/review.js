import http from './http.js'

/**
 * 동영상 리뷰 목록 조회
 * @param {number} videoId
 * @returns {Promise}
 */
export const getReviewsByVideoId = (videoId) => {
  return http.get(`/reviews/video/${videoId}`)
}

/**
 * 리뷰 작성
 * @param {number} videoId
 * @param {Object} reviewData - { content, rating }
 * @returns {Promise}
 */
export const createReview = (videoId, reviewData) => {
  return http.post(`/reviews/video/${videoId}`, reviewData)
}

/**
 * 리뷰 수정
 * @param {number} videoId
 * @param {number} reviewId
 * @param {Object} reviewData - { content, rating }
 * @returns {Promise}
 */
export const updateReview = (videoId, reviewId, reviewData) => {
  return http.put(`/reviews/video/${videoId}/${reviewId}`, reviewData)
}

/**
 * 리뷰 삭제
 * @param {number} videoId
 * @param {number} reviewId
 * @returns {Promise}
 */
export const deleteReview = (videoId, reviewId) => {
  return http.delete(`/reviews/video/${videoId}/${reviewId}`)
}
