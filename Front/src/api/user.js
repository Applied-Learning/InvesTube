import http from './http.js'

// userId(PK)로 공개 사용자 정보 조회
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

// 내 기본 프로필 정보 조회
export const getMyInfo = () => http.get('/users/me')

// 내 프로필 정보 수정 (예: 닉네임 변경)
export const updateMyInfo = (data) => http.put('/users/me', data)

// 비밀번호 변경 (쿼리 파라미터 password 사용)
export const updatePassword = (password) =>
  http.put('/users/me/password', null, { params: { password } })

// 회원 탈퇴
export const deleteMe = () => http.delete('/users/me')

// 프로필 이미지 업로드
export const uploadProfileImage = (file) => {
  const formData = new FormData()
  formData.append('image', file)
  return http.post('/users/me/profile-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 프로필 이미지 삭제
export const deleteProfileImage = () => http.delete('/users/me/profile-image')
