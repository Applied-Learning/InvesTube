import http from './http'

// 재무 분석 관련 API

/**
 * DART API에서 재무 데이터 동기화
 * @param {string} stockCode 종목 코드
 * @param {number} year 회계 연도 (기본값: 2024)
 */
export const syncFinancialData = (stockCode, year = 2024) => {
  return http.post(`/financial/sync/${stockCode}`, null, {
    params: { year }
  })
}

/**
 * 특정 기업의 최신 재무 데이터 조회 (점수 포함)
 * @param {string} stockCode 종목 코드
 * @param {number|null} profileId 투자 프로필 ID (null이면 기본 프로필 사용)
 */
export const getFinancialData = (stockCode, profileId = null) => {
  return http.get(`/financial/${stockCode}`, {
    params: profileId ? { profileId } : {}
  })
}

/**
 * 특정 기업의 재무 데이터 이력 조회
 * @param {string} stockCode 종목 코드
 */
export const getFinancialHistory = (stockCode) => {
  return http.get(`/financial/${stockCode}/history`)
}

/**
 * 찜한 기업들의 재무 데이터 및 점수 조회 (인증 필요)
 * @param {number|null} profileId 투자 프로필 ID
 */
export const getWishedStocksWithScores = (profileId = null) => {
  return http.get('/financial/wished', {
    params: profileId ? { profileId } : {}
  })
}

// 투자 프로필 관련 API

/**
 * 사용자의 투자 프로필 목록 조회
 */
export const getProfiles = () => {
  return http.get('/profiles')
}

/**
 * 기본 투자 프로필 조회
 */
export const getDefaultProfile = () => {
  return http.get('/profiles/default')
}

/**
 * 특정 프로필 조회
 * @param {number} profileId 프로필 ID
 */
export const getProfile = (profileId) => {
  return http.get(`/profiles/${profileId}`)
}

/**
 * 투자 프로필 생성
 * @param {object} profile 프로필 데이터
 */
export const createProfile = (profile) => {
  return http.post('/profiles', profile)
}

/**
 * 투자 프로필 수정
 * @param {number} profileId 프로필 ID
 * @param {object} profile 프로필 데이터
 */
export const updateProfile = (profileId, profile) => {
  return http.put(`/profiles/${profileId}`, profile)
}

/**
 * 투자 프로필 삭제
 * @param {number} profileId 프로필 ID
 */
export const deleteProfile = (profileId) => {
  return http.delete(`/profiles/${profileId}`)
}

/**
 * 기본 투자 프로필 5가지 생성 (안정형, 성장형, 균형형, 가치형, 현금흐름형)
 */
export const createDefaultProfiles = () => {
  return http.post('/profiles/defaults')
}
