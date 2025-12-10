<template>
  <div class="video-create-container">
    <PageHeader title="영상 등록" />

    <form @submit.prevent="handleSubmit" class="create-form">
      <div class="form-group">
        <label for="youtubeVideoId" class="required">YouTube 영상 ID</label>
        <input
          id="youtubeVideoId"
          v-model="formData.youtubeVideoId"
          type="text"
          placeholder="예: dQw4w9WgXcQ"
          required
        />
        <p class="help-text">YouTube URL에서 youtube.be/ 뒤의 ID를 입력하세요</p>
      </div>

      <div class="form-group">
        <label for="title" class="required">제목</label>
        <input
          id="title"
          v-model="formData.title"
          type="text"
          placeholder="영상 제목을 입력하세요"
          required
          maxlength="200"
        />
      </div>

      <div class="form-group">
        <label for="description" class="required">설명</label>
        <textarea
          id="description"
          v-model="formData.description"
          placeholder="영상에 대한 설명을 입력하세요"
          rows="5"
          required
          maxlength="1000"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="categoryId" class="required">카테고리</label>
        <select id="categoryId" v-model="formData.categoryId" required>
          <option value="" disabled>카테고리를 선택하세요</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="thumbnailUrl">썸네일 URL (선택)</label>
        <input
          id="thumbnailUrl"
          v-model="formData.thumbnailUrl"
          type="url"
          placeholder="https://example.com/thumbnail.jpg"
        />
        <p class="help-text">입력하지 않으면 YouTube 기본 썸네일이 사용됩니다</p>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="goBack">취소</button>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '등록 중...' : '등록하기' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { createVideo } from '../api/video.js'

const router = useRouter()
const loading = ref(false)
const error = ref(null)

const formData = ref({
  youtubeVideoId: '',
  title: '',
  description: '',
  categoryId: '',
  thumbnailUrl: '',
})

const categories = [
  { id: 1, name: '금융' },
  { id: 2, name: '기술' },
  { id: 3, name: '투자' },
]

const handleSubmit = async () => {
  loading.value = true
  error.value = null

  try {
    // categoryId를 숫자로 변환
    const videoData = {
      ...formData.value,
      categoryId: parseInt(formData.value.categoryId),
    }

    await createVideo(videoData)
    alert('영상이 성공적으로 등록되었습니다!')
    router.push('/')
  } catch (err) {
    console.error('영상 등록 실패:', err)
    error.value = err.response?.data?.message || '영상 등록에 실패했습니다. 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.video-create-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.create-form {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.form-group label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.help-text {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.error-message {
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
  margin-bottom: 24px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 32px;
}

.btn-cancel,
.btn-submit {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-submit {
  background: #2563eb;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
