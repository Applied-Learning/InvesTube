<template>
  <div class="board-list-page">
    <PageHeader title="댓글 단 게시글" />

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
        <button @click="fetchMyCommentedBoardsAll" class="retry-btn">다시 시도</button>
      </div>

      <!-- 빈 상태 -->
      <div v-else-if="items.length === 0" class="state-box empty">
        <span class="state-icon">💬</span>
        <p class="empty-title">댓글 단 게시글이 없어요</p>
        <p class="empty-desc">게시글에 댓글을 남겨보세요!</p>
      </div>

      <!-- 게시글 목록 -->
      <div v-else class="board-list">
        <RouterLink
          v-for="item in items"
          :key="item.commentId || item.postId"
          class="board-card"
          :to="{ name: 'boardDetail', params: { id: item.postId } }"
        >
          <div class="card-header">
            <span class="post-badge">💬</span>
            <span class="post-date">{{ item.createdAtDisplay }}</span>
          </div>
          <h3 class="post-title">{{ item.postTitle }}</h3>
          <div class="my-comment">
            <span class="comment-label">내 댓글</span>
            <p class="comment-text">{{ item.commentContent }}</p>
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
import { getMyCommentedBoards } from '../api/board.js'
import { formatKSTDate } from '../utils/date.js'

const items = ref([])
const loading = ref(false)
const error = ref(null)

const normalizeCommented = (item) => {
  if (!item) return null
  return {
    commentId: item.commentId,
    postId: item.postId,
    postTitle: item.postTitle,
    commentContent: item.content,
    createdAtDisplay: item.createdAt ? formatKSTDate(item.createdAt) : '',
  }
}

const fetchMyCommentedBoardsAll = async () => {
  loading.value = true
  error.value = null

  try {
    const res = await getMyCommentedBoards(100)
    const list = res.data || []
    items.value = list.map(normalizeCommented).filter(Boolean)
  } catch (err) {
    console.error('댓글 단 게시글 목록 조회 실패:', err)
    error.value = '댓글 단 게시글 목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchMyCommentedBoardsAll)
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
  border-top-color: #8b5cf6;
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
  background: #8b5cf6;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.retry-btn:hover {
  background: #7c3aed;
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
  border-color: #8b5cf6;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15);
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
}

/* 내 댓글 영역 */
.my-comment {
  background: #f5f3ff;
  border-radius: 8px;
  padding: 10px 12px;
  border-left: 3px solid #8b5cf6;
}

.comment-label {
  font-size: 11px;
  font-weight: 600;
  color: #8b5cf6;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.comment-text {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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
