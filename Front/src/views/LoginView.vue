<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">Investube</h1>
      <p class="subtitle">투자 영상 플랫폼</p>

      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="userId">아이디</label>
          <input
            id="userId"
            v-model="credentials.id"
            type="text"
            placeholder="아이디를 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            placeholder="비밀번호를 입력하세요"
            required
          />
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
      </form>

      <div class="signup-link">
        <p>계정이 없으신가요? <router-link to="/signup">회원가입</router-link></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()

const credentials = ref({
  id: '',
  password: '',
})

const loading = ref(false)
const errorMessage = ref('')

const handleLogin = async () => {
  loading.value = true
  errorMessage.value = ''

  try {
    const success = await authStore.login(credentials.value)
    
    if (success) {
      router.push('/')
    } else {
      errorMessage.value = '아이디 또는 비밀번호가 올바르지 않습니다.'
    }
  } catch (error) {
    errorMessage.value = '로그인 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 20px;
}

.login-card {
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
}

.login-form {
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

.login-button {
  padding: 12px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover:not(:disabled) {
  background-color: #1565c0;
}

.login-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.error-message {
  color: #d32f2f;
  text-align: center;
  margin: 0;
  font-size: 14px;
}

.signup-link {
  text-align: center;
  margin-top: 24px;
}

.signup-link p {
  color: #666;
  margin: 0;
}

.signup-link a {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
}

.signup-link a:hover {
  text-decoration: underline;
}
</style>
