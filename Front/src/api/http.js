import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  withCredentials: true,
})

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // 토큰 만료/인증 실패 시 클라이언트 상태 정리 후 로그인 페이지로 이동
      localStorage.removeItem('accessToken')
      localStorage.removeItem('userId')
      localStorage.removeItem('id')
      localStorage.removeItem('nickname')

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }

    console.error('API Error:', error)
    return Promise.reject(error)
  },
)

export default http
