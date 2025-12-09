import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, logout as apiLogout } from '../api/auth.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('accessToken') || null)
  const userId = ref(localStorage.getItem('userId') || null)
  const id = ref(localStorage.getItem('id') || localStorage.getItem('userId') || null)
  const nickname = ref(localStorage.getItem('nickname') || null)

  const isAuthenticated = computed(() => !!token.value)

  const login = async (credentials) => {
    try {
      const response = await apiLogin(credentials)
      const { token: newToken, userId: newUserId, nickname: newNickname, id: newId } = response.data

      token.value = newToken
      userId.value = newUserId
      id.value = newId || credentials.id || null
      nickname.value = newNickname

      localStorage.setItem('accessToken', newToken)
      localStorage.setItem('userId', newUserId)
      localStorage.setItem('id', id.value)
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
    id.value = null
    nickname.value = null

    apiLogout()
  }

  return {
    token,
    userId,
    id,
    nickname,
    isAuthenticated,
    login,
    logout,
  }
})
