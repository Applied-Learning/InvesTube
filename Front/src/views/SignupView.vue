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
          <input
            id="password"
            v-model="formData.password"
            type="password"
            placeholder="비밀번호를 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="passwordConfirm">비밀번호 확인</label>
          <input
            id="passwordConfirm"
            v-model="passwordConfirm"
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            required
          />
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

.form-group input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #1976d2;
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
