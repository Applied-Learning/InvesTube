<template>
  <div class="signup-container">
    <div class="signup-card">
      <h1 class="title">Investube</h1>
      <p class="subtitle">회원가입</p>

      <form @submit.prevent="handleSignup" class="signup-form">
        <div class="form-group">
          <label for="userId">아이디</label>
          <input
            id="userId"
            v-model="formData.id"
            type="text"
            placeholder="아이디를 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <div class="password-wrapper">
            <input
              id="password"
              v-model="formData.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="비밀번호를 입력하세요"
              required
            />
            <button
              type="button"
              class="toggle-password-btn"
              @click="togglePasswordVisibility"
              tabindex="-1"
            >
              <svg
                v-if="!showPassword"
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M10 4.16667C5.83333 4.16667 2.275 6.73333 0.833333 10.4167C2.275 14.1 5.83333 16.6667 10 16.6667C14.1667 16.6667 17.725 14.1 19.1667 10.4167C17.725 6.73333 14.1667 4.16667 10 4.16667Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M10 13.75C12.0711 13.75 13.75 12.0711 13.75 10C13.75 7.92893 12.0711 6.25 10 6.25C7.92893 6.25 6.25 7.92893 6.25 10C6.25 12.0711 7.92893 13.75 10 13.75Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <svg
                v-else
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M14.95 14.95C13.5255 16.0358 11.7909 16.6374 10 16.6667C5.83333 16.6667 2.275 14.1 0.833333 10.4167C1.86953 8.0609 3.58333 6.07722 5.75 4.76667M8.25 3.53333C8.82321 3.39907 9.41073 3.33195 10 3.33333C14.1667 3.33333 17.725 5.9 19.1667 9.58333C18.6609 10.7857 17.9577 11.8963 17.0833 12.8667M11.7667 11.7667C11.5378 12.0123 11.2617 12.2093 10.9546 12.3459C10.6474 12.4825 10.3156 12.556 9.97885 12.562C9.64213 12.568 9.30781 12.5063 8.99597 12.3805C8.68414 12.2547 8.40096 12.0674 8.16307 11.8295C7.92518 11.5916 7.73791 11.3084 7.6121 10.9966C7.48629 10.6848 7.42464 10.3504 7.43061 10.0137C7.43659 9.67699 7.51008 9.34525 7.64667 9.0381C7.78327 8.73095 7.98026 8.45479 8.22583 8.22583"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M0.833333 0.833333L19.1667 19.1667"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="passwordConfirm">비밀번호 확인</label>
          <div class="password-wrapper">
            <input
              id="passwordConfirm"
              v-model="passwordConfirm"
              :type="showPasswordConfirm ? 'text' : 'password'"
              placeholder="비밀번호를 다시 입력하세요"
              required
            />
            <button
              type="button"
              class="toggle-password-btn"
              @click="togglePasswordConfirmVisibility"
              tabindex="-1"
            >
              <svg
                v-if="!showPasswordConfirm"
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M10 4.16667C5.83333 4.16667 2.275 6.73333 0.833333 10.4167C2.275 14.1 5.83333 16.6667 10 16.6667C14.1667 16.6667 17.725 14.1 19.1667 10.4167C17.725 6.73333 14.1667 4.16667 10 4.16667Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M10 13.75C12.0711 13.75 13.75 12.0711 13.75 10C13.75 7.92893 12.0711 6.25 10 6.25C7.92893 6.25 6.25 7.92893 6.25 10C6.25 12.0711 7.92893 13.75 10 13.75Z"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <svg
                v-else
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M14.95 14.95C13.5255 16.0358 11.7909 16.6374 10 16.6667C5.83333 16.6667 2.275 14.1 0.833333 10.4167C1.86953 8.0609 3.58333 6.07722 5.75 4.76667M8.25 3.53333C8.82321 3.39907 9.41073 3.33195 10 3.33333C14.1667 3.33333 17.725 5.9 19.1667 9.58333C18.6609 10.7857 17.9577 11.8963 17.0833 12.8667M11.7667 11.7667C11.5378 12.0123 11.2617 12.2093 10.9546 12.3459C10.6474 12.4825 10.3156 12.556 9.97885 12.562C9.64213 12.568 9.30781 12.5063 8.99597 12.3805C8.68414 12.2547 8.40096 12.0674 8.16307 11.8295C7.92518 11.5916 7.73791 11.3084 7.6121 10.9966C7.48629 10.6848 7.42464 10.3504 7.43061 10.0137C7.43659 9.67699 7.51008 9.34525 7.64667 9.0381C7.78327 8.73095 7.98026 8.45479 8.22583 8.22583"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
                <path
                  d="M0.833333 0.833333L19.1667 19.1667"
                  stroke="currentColor"
                  stroke-width="1.5"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="nickname">닉네임</label>
          <input
            id="nickname"
            v-model="formData.nickname"
            type="text"
            placeholder="닉네임을 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="email">이메일</label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            placeholder="이메일을 입력하세요"
            required
          />
        </div>

        <button type="submit" class="signup-button" :disabled="loading">
          {{ loading ? '회원가입 중...' : '회원가입' }}
        </button>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      </form>

      <div class="login-link">
        <p>이미 계정이 있으신가요? <router-link to="/login">로그인</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { signup } from '../api/auth.js'

const router = useRouter()

const formData = ref({
  id: '',
  password: '',
  nickname: '',
  email: '',
})

const passwordConfirm = ref('')
const loading = ref(false)
const errorMessage = ref('')
const showPassword = ref(false)
const showPasswordConfirm = ref(false)

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value
}

const togglePasswordConfirmVisibility = () => {
  showPasswordConfirm.value = !showPasswordConfirm.value
}

const handleSignup = async () => {
  loading.value = true
  errorMessage.value = ''

  // 비밀번호 확인
  if (formData.value.password !== passwordConfirm.value) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.'
    loading.value = false
    return
  }

  try {
    await signup(formData.value)
    alert('회원가입이 완료되었습니다. 로그인해주세요.')
    router.push('/login')
  } catch (error) {
    console.error('회원가입 실패:', error)
    errorMessage.value = error.response?.data?.message || '회원가입 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.signup-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.signup-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
}

.title {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  margin: 0 0 8px;
  color: #1976d2;
}

.subtitle {
  text-align: center;
  color: #666;
  margin: 0 0 32px;
  font-size: 18px;
  font-weight: 500;
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #333;
}

.password-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.password-wrapper input {
  flex: 1;
  padding-right: 40px;
}

.form-group input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
  width: 100%;
}

.form-group input:focus {
  outline: none;
  border-color: #1976d2;
}

.toggle-password-btn {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  transition: color 0.2s;
}

.toggle-password-btn:hover {
  color: #1976d2;
}

.signup-button {
  padding: 12px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 8px;
}

.signup-button:hover:not(:disabled) {
  background-color: #1565c0;
}

.signup-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: #d32f2f;
  text-align: center;
  margin: 0;
  font-size: 14px;
}

.login-link {
  text-align: center;
  margin-top: 24px;
}

.login-link p {
  color: #666;
  margin: 0;
}

.login-link a {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
