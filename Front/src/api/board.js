import http from './http'

// 게시글 목록 조회 (페이징, 검색, 정렬)
export const getBoardList = (keyword = '', sortBy = 'latest', page = 0, size = 10) => {
  return http.get('/boards', {
    params: { keyword, sortBy, page, size }
  })
}

// 게시글 상세 조회
export const getBoardDetail = (postId) => {
  return http.get(`/boards/${postId}`)
}

// 게시글 작성 (이미지 포함)
export const createBoard = (formData) => {
  return http.post('/boards', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 게시글 수정
export const updateBoard = (postId, data) => {
  return http.put(`/boards/${postId}`, data)
}

// 게시글 삭제
export const deleteBoard = (postId) => {
  return http.delete(`/boards/${postId}`)
}

// 사용자별 게시글 조회
export const getBoardsByUser = (userId, page = 0, size = 10) => {
  return http.get(`/boards/user/${userId}`, {
    params: { page, size }
  })
}
