import http from './http.js'

// YouTube 영상 검색
export const searchYoutubeVideos = (query, params = {}) => {
  return http.get('/youtube/search', {
    params: { query, ...params },
  })
}

// YouTube 영상 메타데이터 조회
export const getYoutubeVideo = (videoId) => {
  return http.get(`/youtube/videos/${videoId}`)
}
