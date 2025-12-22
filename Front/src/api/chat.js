import http from './http'

/**
 * 챗봇 API
 */

/**
 * 종목 관련 질문
 * @param {string} stockCode - 종목 코드
 * @param {string} message - 사용자 질문
 * @returns {Promise} 챗봇 응답
 */
export const chatAboutStock = (stockCode, message) => {
  return http.post('/chat/stock', {
    stockCode,
    message
  })
}

export default {
  chatAboutStock
}
