<template>
  <Container>
    <PageHeader title="게시글 작성" />
    
    <div class="create-container">
      <form @submit.prevent="handleSubmit" class="create-form">
        <div class="form-group">
          <label for="title">제목 *</label>
          <input
            id="title"
            v-model="form.title"
            type="text"
            placeholder="제목을 입력하세요"
            required
          />
        </div>

        <div class="form-group">
          <label for="content">내용 *</label>
          <textarea
            id="content"
            v-model="form.content"
            placeholder="내용을 입력하세요"
            rows="15"
            required
          ></textarea>
        </div>

        <div class="form-group">
          <label for="images">이미지 첨부 (최대 5개)</label>
          <input
            id="images"
            type="file"
            accept="image/*"
            multiple
            @change="handleFileChange"
            ref="fileInput"
          />
          <p class="file-hint">JPG, PNG, GIF 파일만 가능합니다.</p>
        </div>

        <!-- 이미지 미리보기 -->
        <div v-if="imagePreviews.length > 0" class="image-previews">
          <div
            v-for="(preview, index) in imagePreviews"
            :key="index"
            class="preview-item"
          >
            <img :src="preview" :alt="`미리보기 ${index + 1}`" />
            <button
              type="button"
              @click="removeImage(index)"
              class="remove-btn"
            >
              ✕
            </button>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="cancel-btn">
            취소
          </button>
          <button type="submit" :disabled="loading" class="submit-btn">
            {{ loading ? '작성 중...' : '작성 완료' }}
          </button>
        </div>
      </form>
    </div>
  </Container>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createBoard } from '../api/board'
import Container from '../components/common/Container.vue'
import PageHeader from '../components/common/PageHeader.vue'

const router = useRouter()

const form = ref({
  title: '',
  content: ''
})

const selectedFiles = ref([])
const imagePreviews = ref([])
const fileInput = ref(null)
const loading = ref(false)

const handleFileChange = (event) => {
  const files = Array.from(event.target.files)
  
  if (files.length + selectedFiles.value.length > 5) {
    alert('이미지는 최대 5개까지만 첨부할 수 있습니다.')
    return
  }

  files.forEach((file) => {
    if (!file.type.startsWith('image/')) {
      alert('이미지 파일만 첨부할 수 있습니다.')
      return
    }

    selectedFiles.value.push(file)

    const reader = new FileReader()
    reader.onload = (e) => {
      imagePreviews.value.push(e.target.result)
    }
    reader.readAsDataURL(file)
  })

  // input 초기화
  if (fileInput.value) {
    fileInput.value.value = ''
  }
}

const removeImage = (index) => {
  selectedFiles.value.splice(index, 1)
  imagePreviews.value.splice(index, 1)
}

const handleSubmit = async () => {
  if (!form.value.title.trim()) {
    alert('제목을 입력해주세요.')
    return
  }

  if (!form.value.content.trim()) {
    alert('내용을 입력해주세요.')
    return
  }

  loading.value = true

  try {
    const formData = new FormData()
    formData.append('title', form.value.title)
    formData.append('content', form.value.content)

    selectedFiles.value.forEach((file) => {
      formData.append('images', file)
    })

    const response = await createBoard(formData)
    alert('게시글이 작성되었습니다.')
    router.push(`/board/${response.data.postId}`)
  } catch (err) {
    console.error('게시글 작성 실패:', err)
    if (err.response?.status === 401) {
      alert('로그인이 필요합니다.')
      router.push('/login')
    } else {
      alert('게시글 작성에 실패했습니다.')
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (confirm('작성 중인 내용이 사라집니다. 취소하시겠습니까?')) {
    router.push('/board')
  }
}
</script>

<style scoped>
.create-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.create-form {
  background: white;
  padding: 32px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-group input[type='text'],
.form-group textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group input[type='text']:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #2563eb;
}

.form-group textarea {
  resize: vertical;
  line-height: 1.6;
}

.form-group input[type='file'] {
  width: 100%;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.file-hint {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
}

.image-previews {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.remove-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
}

.cancel-btn,
.submit-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f3f4f6;
  color: #374151;
}

.cancel-btn:hover {
  background: #e5e7eb;
}

.submit-btn {
  background: #2563eb;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background: #1d4ed8;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .create-form {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}
</style>
