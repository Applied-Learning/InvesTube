<template>
  <Container>
    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error-container">
      <p class="error-message">{{ error }}</p>
      <button @click="goToList" class="back-btn">목록으로</button>
    </div>
    <div v-else-if="post" class="detail-container">
      <!-- 게시글 헤더 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-info">
          <div class="author-section">
            <div class="author-avatar">
              <img
                v-if="post.authorProfileImage"
                :src="resolveImageUrl(post.authorProfileImage)"
                :alt="post.authorNickname"
              />
              <div v-else class="avatar-fallback">
                {{ getAuthorInitial(post.authorNickname) }}
              </div>
            </div>
            <div class="author-details">
              <span class="author-name">{{ post.authorNickname || '익명' }}</span>
              <span class="post-date">{{ formatDate(post.createdAt) }}</span>
            </div>
          </div>
          <div v-if="isMyPost" class="post-actions">
            <button @click="editPost" class="edit-btn">수정</button>
            <button @click="confirmDelete" class="delete-btn">삭제</button>
          </div>
        </div>
      </div>

      <!-- 게시글 본문 -->
      <div class="post-body">
        <div class="post-content">
          {{ post.content }}
        </div>

        <!-- 이미지 갤러리 -->
        <div v-if="post.images && post.images.length > 0" class="image-gallery">
          <img
            v-for="(image, index) in post.images"
            :key="image.imageId"
            :src="resolveImageUrl(image.imageUrl)"
            :alt="`이미지 ${index + 1}`"
            @click="openImageModal(image.imageUrl)"
          />
        </div>
      </div>

      <!-- 하단 버튼 -->
      <div class="footer-actions">
        <button @click="goToList" class="list-btn">목록으로</button>
      </div>
    </div>

    <!-- 이미지 모달 -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <img :src="selectedImage" alt="확대 이미지" />
    </div>
  </Container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getBoardDetail, deleteBoard } from '../api/board'
import Container from '../components/common/Container.vue'
import { resolveImageUrl } from '../utils/image.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const post = ref(null)
const loading = ref(false)
const error = ref(null)
const showImageModal = ref(false)
const selectedImage = ref('')

const isMyPost = computed(() => {
  return authStore.userId && post.value && post.value.userId === parseInt(authStore.userId)
})

const fetchPost = async () => {
  loading.value = true
  error.value = null

  try {
    const postId = route.params.id
    const response = await getBoardDetail(postId)
    post.value = response.data
  } catch (err) {
    console.error('게시글 조회 실패:', err)
    error.value = '게시글을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const getAuthorInitial = (nickname) => {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const openImageModal = (imageUrl) => {
  selectedImage.value = resolveImageUrl(imageUrl)
  showImageModal.value = true
}

const closeImageModal = () => {
  showImageModal.value = false
  selectedImage.value = ''
}

const editPost = () => {
  if (post.value && post.value.postId) {
    router.push(`/board/${post.value.postId}/edit`)
  } else {
    alert('게시글 정보를 불러오는 중입니다.')
  }
}

const confirmDelete = async () => {
  if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) {
    return
  }

  try {
    await deleteBoard(post.value.postId)
    alert('게시글이 삭제되었습니다.')
    router.push('/board')
  } catch (err) {
    console.error('게시글 삭제 실패:', err)
    if (err.response?.status === 403) {
      alert('본인이 작성한 게시글만 삭제할 수 있습니다.')
    } else {
      alert('게시글 삭제에 실패했습니다.')
    }
  }
}

const goToList = () => {
  router.push('/board')
}

onMounted(() => {
  fetchPost()
})
</script>

<style scoped>
.loading {
  text-align: center;
  padding: 48px;
  color: #6b7280;
  font-size: 16px;
}

.error-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 48px 24px;
  text-align: center;
}

.error-message {
  color: #ef4444;
  font-size: 16px;
  margin-bottom: 24px;
}

.back-btn {
  padding: 12px 32px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #4b5563;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.post-header {
  background: white;
  padding: 32px;
  border-radius: 12px 12px 0 0;
  border: 1px solid #e5e7eb;
  border-bottom: none;
}

.post-title {
  margin: 0 0 24px 0;
  font-size: 28px;
  font-weight: 700;
  color: #111827;
  line-height: 1.4;
}

.post-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 20px;
  font-weight: 700;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.post-date {
  font-size: 14px;
  color: #6b7280;
}

.post-actions {
  display: flex;
  gap: 8px;
}

.edit-btn,
.delete-btn {
  padding: 8px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn {
  background: white;
  color: #374151;
}

.edit-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.delete-btn {
  background: white;
  color: #ef4444;
}

.delete-btn:hover {
  border-color: #ef4444;
  background: #fef2f2;
}

.post-body {
  background: white;
  padding: 32px;
  border: 1px solid #e5e7eb;
  border-top: none;
  border-bottom: none;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 32px;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-top: 24px;
}

.image-gallery img {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.image-gallery img:hover {
  transform: scale(1.02);
}

.footer-actions {
  background: white;
  padding: 24px 32px;
  border-radius: 0 0 12px 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}

.list-btn {
  padding: 12px 32px;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.list-btn:hover {
  background: #e5e7eb;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  cursor: pointer;
}

.image-modal img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

@media (max-width: 768px) {
  .post-header,
  .post-body,
  .footer-actions {
    padding: 20px;
  }

  .post-title {
    font-size: 24px;
  }

  .post-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .image-gallery {
    grid-template-columns: 1fr;
  }
}
</style>
