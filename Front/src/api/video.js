import http from './http.js'

/**
 * 전체 비디오 목록 조회
 * @param {Object} params - { sortBy: 'latest'|'views'|'rating', page, size }
 * @returns {Promise}
 */
export const getVideos = (params = {}) => {
  return http.get('/videos', { params })
}

/**
 * 비디오 상세 조회
 * @param {number} videoId
 * @returns {Promise}
 */
export const getVideoDetail = (videoId) => {
  return http.get(`/videos/${videoId}`)
}

/**
 * 비디오 찜하기/취소
 * @param {number} videoId
 * @returns {Promise}
 */
export const toggleVideoWish = (videoId) => {
  return http.post(`/videos/${videoId}/wish`)
}

/**
 * 비디오 검색
 * @param {Object} params - { keyword, sortBy }
 * @returns {Promise}
 */
export const searchVideos = (params) => {
  return http.get('/videos/search', { params })
}
