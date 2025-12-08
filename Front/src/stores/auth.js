import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout } from '../api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('accessToken') || null)
  const userId = ref(localStorage.getItem('userId') || null)
  const nickname = ref(localStorage.getItem('nickname') || null)

  const isAuthenticated = computed(() => !!token.value)

  const login = async (credentials) => {
    try {
      const response = await apiLogin(credentials)
      const { token: newToken, userId: newUserId, nickname: newNickname } = response.data

      token.value = newToken
      userId.value = newUserId
      nickname.value = newNickname

      localStorage.setItem('accessToken', newToken)
      localStorage.setItem('userId', newUserId)
      localStorage.setItem('nickname', newNickname)

      return true
    } catch (error) {
      console.error('로그인 실패:', error)
      return false
    }
  }

  const logout = () => {
    token.value = null
    userId.value = null
    nickname.value = null

    apiLogout()
  }

  return {
    token,
    userId,
    nickname,
    isAuthenticated,
    login,
    logout,
  }
})
