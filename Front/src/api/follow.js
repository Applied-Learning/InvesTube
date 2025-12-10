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
 * 팔로우 / 언팔로우 토글
 * @param {number} userId
 * @returns {Promise}
 */
export const toggleFollow = (userId) => {
  return http.post(`/follow/toggle/${userId}`)
}

/**
 * 특정 사용자의 팔로워 목록 조회
 * @param {number} userId
 * @returns {Promise}
 */
export const getFollowers = (userId) => {
  return http.get(`/follow/${userId}/followers`)
}

/**
 * 특정 사용자의 팔로잉 목록 조회
 * @param {number} userId
 * @returns {Promise}
 */
export const getFollowings = (userId) => {
  return http.get(`/follow/${userId}/followings`)
}
