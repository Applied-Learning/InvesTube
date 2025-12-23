<template>
  <div class="board-list-page">
    <PageHeader title="내가 작성한 게시글" />

    <div class="board-list-content">
      <!-- 로딩 -->
      <div v-if="loading" class="state-box">
        <div class="loading-spinner"></div>
        <p>게시글을 불러오는 중...</p>
      </div>

      <!-- 에러 -->
      <div v-else-if="error" class="state-box error">
        <span class="state-icon">⚠️</span>
        <p>{{ error }}</p>
        <button @click="fetchMyBoards" class="retry-btn">다시 시도</button>
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="posts.length === 0" class="state-box empty">
        <span class="state-icon">📝</span>
        <p class="empty-title">아직 작성한 게시글이 없어요</p>
        <p class="empty-desc">커뮤니티에서 첫 글을 작성해보세요!</p>
      </div>

      <!-- 게시글 목록 -->
      <div v-else class="board-list">
        <RouterLink
          v-for="post in posts"
          :key="post.postId"
          class="board-card"
          :to="{ name: 'boardDetail', params: { id: post.postId } }"
        >
          <div class="card-header">
            <span class="post-date">{{ post.createdAtDisplay }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <div class="post-stats">
            <span class="stat">
              <span class="stat-icon">👁</span>
              {{ post.viewCount }}
            </span>
            <span class="stat">
              <span class="stat-icon">💬</span>
              {{ post.commentCount }}
            </span>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { getBoardsByUser } from '../api/board.js'
import { useAuthStore } from '../stores/auth.js'
import { formatKSTDate } from '../utils/date.js'

const authStore = useAuthStore()
const posts = ref([])
const loading = ref(false)
const error = ref(null)

const normalizeBoardPost = (post) => {
  if (!post) return null
  return {
    postId: post.postId,
    title: post.title,
    viewCount: post.viewCount ?? 0,
    commentCount: post.commentCount ?? 0,
    createdAtDisplay: post.createdAt ? formatKSTDate(post.createdAt) : '',
  }
}

const fetchMyBoards = async () => {
  if (!authStore.userId) {
    error.value = '로그인 후 이용해 주세요.'
    return
  }

  loading.value = true
  error.value = null

  try {
    const res = await getBoardsByUser(authStore.userId, 0, 50)
    const data = res.data
    const list = data?.posts || data || []
    posts.value = list.map(normalizeBoardPost).filter(Boolean)
  } catch (err) {
    console.error('작성한 게시글 목록 조회 실패:', err)
    error.value = '작성한 게시글 목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchMyBoards)
</script>

<style scoped>
.board-list-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.board-list-content {
  margin-top: 24px;
}

/* 상태 박스 (로딩/에러/빈 상태) */
.state-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  text-align: center;
  color: #6b7280;
}

.state-box.error p {
  color: #dc2626;
}

.state-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-desc {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.retry-btn {
  margin-top: 16px;
  padding: 10px 24px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: #2563eb;
}

/* 게시글 목록 */
.board-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.board-card {
  display: block;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px 20px;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s ease;
}

.board-card:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
  transform: translateX(4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.post-badge {
  font-size: 16px;
}

.post-date {
  font-size: 12px;
  color: #9ca3af;
}

.post-title {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-stats {
  display: flex;
  gap: 16px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #6b7280;
}

.stat-icon {
  font-size: 12px;
}

/* 반응형 */
@media (max-width: 640px) {
  .board-list-page {
    padding: 16px;
  }

  .board-card {
    padding: 14px 16px;
  }
}
</style>
