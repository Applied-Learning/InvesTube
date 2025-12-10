import http from './http.js'

// 내부 userId(PK)로 사용자 정보 조회
export const getUserByUserId = (userId) => {
  return http.get(`/users/${userId}`)
}

// 내 계정으로 등록한 영상 목록 조회
export const getMyVideos = () => {
  return http.get('/users/me/videos')
}

// 내가 작성한 리뷰 목록 조회
export const getMyReviews = () => {
  return http.get('/users/me/reviews')
}

