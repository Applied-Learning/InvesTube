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
 * 비디오 등록
 * @param {Object} videoData - { youtubeVideoId, title, description, categoryId, thumbnailUrl }
 * @returns {Promise}
 */
export const createVideo = (videoData) => {
  return http.post('/videos', videoData)
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

/**
 * 카테고리별 비디오 조회
 * @param {number} categoryId
 * @param {Object} params - { page, size }
 * @returns {Promise}
 */
export const getVideosByCategory = (categoryId, params = {}) => {
  return http.get(`/videos/category/${categoryId}`, { params })
}

/**
 * 찜한 비디오 목록 조회
 * @param {Object} params - { page, size }
 * @returns {Promise}
 */
export const getWishedVideos = (params = {}) => {
  return http.get('/videos/wished', { params })
}
