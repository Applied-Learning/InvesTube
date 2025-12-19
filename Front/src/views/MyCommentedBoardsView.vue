<template>
  <div class="profile-page-container">
    <PageHeader title="댓글 단 게시글" />

    <div class="profile-page-content">
      <div v-if="loading" class="profile-loading">불러오는 중...</div>
      <div v-else-if="error" class="profile-error">{{ error }}</div>
      <div v-else-if="items.length === 0" class="profile-empty">아직 댓글 단 게시글이 없어요.</div>
      <div v-else class="profile-boards-section">
        <div class="profile-board-list">
          <RouterLink
            v-for="item in items"
            :key="item.commentId || item.postId"
            class="profile-board-item"
            :to="{ name: 'boardDetail', params: { id: item.postId } }"
          >
            <div class="profile-board-info">
              <p class="profile-board-title">{{ item.postTitle }}</p>
              <p class="profile-board-meta comment-content">
                {{ item.commentContent }}
              </p>
              <p class="profile-board-meta">
                {{ item.createdAtDisplay }}
              </p>
            </div>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { getMyCommentedBoards } from '../api/board.js'

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
    createdAtDisplay: item.createdAt ? String(item.createdAt).slice(0, 10) : '',
  }
}

const fetchMyCommentedBoardsAll = async () => {
  loading.value = true
  error.value = null

  try {
    // 충분히 큰 limit으로 전체 보기 대체
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
.profile-page-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.profile-page-content {
  margin-top: 20px;
}

.profile-loading,
.profile-error,
.profile-empty {
  padding: 40px 16px;
  text-align: center;
  font-size: 14px;
}

.profile-error {
  color: #b91c1c;
}

.profile-boards-section {
  margin-top: 8px;
}

.profile-board-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-board-item {
  display: block;
  padding: 10px 0;
  text-decoration: none;
  color: inherit;
  border-bottom: 1px solid #e5e7eb;
}

.profile-board-item:last-child {
  border-bottom: none;
}

.profile-board-item:hover .profile-board-title {
  text-decoration: underline;
}

.profile-board-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.profile-board-meta {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.comment-content {
  margin-top: 2px;
}
</style>
