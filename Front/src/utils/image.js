import http from '../api/http.js'

const apiBaseUrl = http.defaults.baseURL?.replace(/\/$/, '') || ''

export const resolveImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  // 백엔드에서 "/uploads/..." 처럼 오는 경우
  return apiBaseUrl + (url.startsWith('/') ? url : '/' + url)
}
