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
            placeholder="제목을 입력해 주세요."
            required
          />
        </div>

        <div class="form-group">
          <label for="content">내용 *</label>
          <RichTextEditor
            v-model="form.content"
            placeholder="내용을 입력해 주세요."
          />
        </div>

        <div class="form-actions">
          <button type="button" @click="goBack" class="cancel-btn">취소</button>
          <button type="submit" :disabled="loading" class="submit-btn">
            {{ loading ? '저장 중...' : isEdit ? '수정 완료' : '작성 완료' }}
          </button>
        </div>
      </form>
    </div>
  </Container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { createBoard, updateBoard, getBoardDetail } from '../api/board'
import Container from '../components/common/Container.vue'
import PageHeader from '../components/common/PageHeader.vue'
import RichTextEditor from '../components/common/RichTextEditor.vue'

const router = useRouter()
const route = useRoute()
const isEdit = ref(false)
const editingPostId = ref(null)

const form = ref({
  title: '',
  content: '',
})

const loading = ref(false)

onMounted(async () => {
  if (route.params.id) {
    isEdit.value = true
    editingPostId.value = route.params.id
    try {
      const resp = await getBoardDetail(editingPostId.value)
      const data = resp.data
      form.value.title = data.title || ''
      form.value.content = data.content || ''
    } catch (err) {
      console.error('게시글 불러오기 실패:', err)
      alert('게시글을 불러오는 중 오류가 발생했습니다.')
      router.push('/board')
    }
  }
})

const handleSubmit = async () => {
  if (!form.value.title.trim()) {
    alert('제목을 입력해 주세요.')
    return
  }

  if (!form.value.content.trim()) {
    alert('내용을 입력해 주세요.')
    return
  }

  loading.value = true

  try {
    if (isEdit.value && editingPostId.value) {
      const payload = {
        title: form.value.title,
        content: form.value.content,
      }
      await updateBoard(editingPostId.value, payload)
      alert('게시글이 수정되었습니다.')
      router.push(`/board/${editingPostId.value}`)
    } else {
      const formData = new FormData()
      formData.append('title', form.value.title)
      formData.append('content', form.value.content)

      const response = await createBoard(formData)
      alert('게시글이 작성되었습니다.')
      router.push(`/board/${response.data.postId}`)
    }
  } catch (err) {
    console.error('게시글 작성/수정 실패:', err)
    if (err.response?.status === 401) {
      alert('로그인이 필요합니다.')
      router.push('/login')
    } else {
      alert(isEdit.value ? '게시글 수정 중 오류가 발생했습니다.' : '게시글 작성 중 오류가 발생했습니다.')
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (confirm('작성 중인 내용을 취소하고 목록으로 돌아가시겠습니까?')) {
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

.form-group input[type='text'] {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group input[type='text']:focus {
  outline: none;
  border-color: #2563eb;
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

