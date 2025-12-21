import http from './http'

// 기업 찜 관련 API

// 찜한 기업 목록 조회
export const getWishedStocks = () => {
  return http.get('/stocks/wished')
}

// 찜 여부 확인
export const isStockWished = (stockCode) => {
  return http.get(`/stocks/${stockCode}/wished`)
}

// 찜 추가
export const addStockWish = (stockCode) => {
  return http.post(`/stocks/${stockCode}/wish`)
}

// 찜 삭제
export const removeStockWish = (stockCode) => {
  return http.delete(`/stocks/${stockCode}/wish`)
}

// 기업 찜 개수 조회
export const getStockWishCount = (stockCode) => {
  return http.get(`/stocks/${stockCode}/wish-count`)
}
