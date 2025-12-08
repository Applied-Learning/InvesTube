import http from './http.js'

/**
 * 팔로우 상태 확인
 * @param {number} userId
 * @returns {Promise<boolean>}
 */
export const checkFollowStatus = (userId) => {
  return http.get(`/follow/status/${userId}`)
}

/**
 * 팔로우/언팔로우 토글
 * @param {number} userId
 * @returns {Promise}
 */
export const toggleFollow = (userId) => {
  return http.post(`/follow/toggle/${userId}`)
}
