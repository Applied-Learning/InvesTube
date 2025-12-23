import http from './http'

export default {
  // Stock list (includes latest price)
  getStocks() {
    return http.get('/stocks')
  },

  // Single stock detail
  getStock(stockCode) {
    return http.get(`/stocks/${stockCode}`)
  },

  // Related news for a stock
  getStockNews(stockCode, limit = 5) {
    return http.get(`/stocks/${stockCode}/news`, { params: { limit } })
  },

  // Stock price history
  getStockPrices(stockCode) {
    return http.get(`/stocks/${stockCode}/prices`)
  },

  // Stock price history by date range
  getStockPricesByDateRange(stockCode, startDate, endDate) {
    return http.get(`/stocks/${stockCode}/prices/range`, {
      params: { startDate, endDate },
    })
  },

  // Register a stock
  registerStock(stock) {
    return http.post('/stocks', stock)
  },

  // KRX indices
  getIndices() {
    return http.get('/stocks/indices')
  },

  // Register or update a stock price entry
  registerStockPrice(stockCode, stockPrice) {
    return http.post(`/stocks/${stockCode}/prices`, stockPrice)
  },

  // Update stock information
  updateStock(stockCode, stock) {
    return http.put(`/stocks/${stockCode}`, stock)
  },

  // Remove a stock
  deleteStock(stockCode) {
    return http.delete(`/stocks/${stockCode}`)
  },

  // Sync stocks from DART
  syncDartData() {
    return http.post('/stocks/sync/dart')
  },

  // Sync stock price from KRX
  syncKrxData(stockCode) {
    return http.post(`/stocks/sync/krx/${stockCode}`)
  },
}
