import http from './http'

export default {
  // 전체 주식 목록 조회
  getStocks() {
    return http.get('/stocks')
  },

  // 특정 주식 상세 조회
  getStock(stockCode) {
    return http.get(`/stocks/${stockCode}`)
  },

  // 주식 가격 이력 조회
  getStockPrices(stockCode) {
    return http.get(`/stocks/${stockCode}/prices`)
  },

  // 특정 기간 주식 가격 조회
  getStockPricesByDateRange(stockCode, startDate, endDate) {
    return http.get(`/stocks/${stockCode}/prices/range`, {
      params: { startDate, endDate }
    })
  },

  // 주식 등록
  registerStock(stock) {
    return http.post('/stocks', stock)
  },
  
  // KRX 지수 정보 조회
  getIndices() {
    return http.get('/stocks/indices')
  },

  // 주식 가격 등록/업데이트
  registerStockPrice(stockCode, stockPrice) {
    return http.post(`/stocks/${stockCode}/prices`, stockPrice)
  },

  // 주식 수정
  updateStock(stockCode, stock) {
    return http.put(`/stocks/${stockCode}`, stock)
  },

  // 주식 삭제
  deleteStock(stockCode) {
    return http.delete(`/stocks/${stockCode}`)
  },

  // DART 데이터 동기화
  syncDartData() {
    return http.post('/stocks/sync/dart')
  },

  // KRX 데이터 동기화
  syncKrxData(stockCode) {
    return http.post(`/stocks/sync/krx/${stockCode}`)
  }
}
