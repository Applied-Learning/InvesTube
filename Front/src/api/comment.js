import http from './http'

export const getCommentsByPostId = (postId) => {
  return http.get(`/board/${postId}/comments`)
}

export const createComment = (postId, payload) => {
  return http.post(`/board/${postId}/comments`, payload)
}

export const updateComment = (commentId, payload) => {
  return http.put(`/comments/${commentId}`, payload)
}

export const deleteComment = (commentId) => {
  return http.delete(`/comments/${commentId}`)
}
