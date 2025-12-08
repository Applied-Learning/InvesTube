import http from './http.js'

/**
 * 로그인
 * @param {Object} credentials - { userId, password }
 * @returns {Promise}
 */
export const login = (credentials) => {
  return http.post('/auth/login', credentials)
}

/**
 * 회원가입
 * @param {Object} userData - { userId, password, nickname, email }
 * @returns {Promise}
 */
export const signup = (userData) => {
  return http.post('/auth/signup', userData)
}

/**
 * 로그아웃 (클라이언트 측)
 */
export const logout = () => {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('userId')
  localStorage.removeItem('nickname')
}
