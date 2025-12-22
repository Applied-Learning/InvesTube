import http from './http'

export default {
  // 설문 질문 조회
  getSurveyQuestions() {
    return http.get('/profiles/survey/questions')
  },

  // 설문 결과 제출
  submitSurvey(survey) {
    return http.post('/profiles/survey/submit', survey)
  },

  // 사용자 프로필 목록 조회
  getProfiles() {
    return http.get('/profiles')
  },

  // 기본 프로필 조회
  getDefaultProfile() {
    return http.get('/profiles/default')
  },

  // 특정 프로필 조회
  getProfile(profileId) {
    return http.get(`/profiles/${profileId}`)
  },

  // 프로필 생성
  createProfile(profile) {
    return http.post('/profiles', profile)
  },

  // 프로필 수정
  updateProfile(profileId, profile) {
    return http.put(`/profiles/${profileId}`, profile)
  },

  // 프로필 삭제
  deleteProfile(profileId) {
    return http.delete(`/profiles/${profileId}`)
  },

  // 기본 프로필 5개 생성
  createDefaultProfiles() {
    return http.post('/profiles/defaults')
  }
}
