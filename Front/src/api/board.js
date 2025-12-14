import http from './http.js'

// 게시글 목록 조회 (페이징, 검색, 정렬)
export const getBoardList = (keyword = '', sortBy = 'latest', page = 0, size = 10) => {
  return http.get('/boards', {
    params: { keyword, sortBy, page, size },
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
      'Content-Type': 'multipart/form-data',
    },
  })
}

// 게시글에 이미지 추가 (편집 중)
export const addBoardImages = (postId, formData) => {
  return http.post(`/boards/${postId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
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
    params: { page, size },
  })
}

// 게시글 이미지 삭제
export const deleteBoardImage = (imageId) => {
  return http.delete(`/boards/images/${imageId}`)
}

// 내가 댓글 단 게시글 (상위 N개)
export const getMyCommentedBoards = (limit = 5) => {
  return http.get('/boards/me/commented', {
    params: { limit },
  })
}