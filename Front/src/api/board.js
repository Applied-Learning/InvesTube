import http from './http.js'

// 게시글 목록 조회 (페이지, 검색, 정렬)
export const getBoardList = (keyword = '', sortBy = 'latest', page = 0, size = 10) => {
  return http.get('/boards', {
    params: { keyword, sortBy, page, size },
  })
}

// 게시글 상세 조회
export const getBoardDetail = (postId) => {
  return http.get(`/boards/${postId}`)
}

// 게시글 생성 (multipart/form-data)
export const createBoard = (formData) => {
  return http.post('/boards', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

// (기존) 게시글 이미지 추가 API - 현재는 사용 안 할 수 있음
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

// 내가 댓글 단 게시글 목록 (상위 N개)
export const getMyCommentedBoards = (limit = 5) => {
  return http.get('/boards/me/commented', {
    params: { limit },
  })
}

// 에디터용 단일 이미지 업로드
export const uploadBoardImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/boards/images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

