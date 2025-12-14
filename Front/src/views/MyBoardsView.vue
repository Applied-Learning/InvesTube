<template>
  <div class="profile-page-container">
    <PageHeader title="내가 작성한 게시글" />

    <div class="profile-page-content">
      <div v-if="loading" class="profile-loading">불러오는 중...</div>
      <div v-else-if="error" class="profile-error">{{ error }}</div>
      <div v-else-if="posts.length === 0" class="profile-empty">아직 작성한 게시글이 없어요.</div>
      <div v-else class="profile-boards-section">
        <div class="profile-board-list">
          <RouterLink
            v-for="post in posts"
            :key="post.postId"
            class="profile-board-item"
            :to="{ name: 'boardDetail', params: { id: post.postId } }"
          >
            <div class="profile-board-info">
              <p class="profile-board-title">{{ post.title }}</p>
              <p class="profile-board-meta">
                조회수 {{ post.viewCount ?? 0 }} · 댓글 {{ post.commentCount ?? 0 }} ·
                {{ post.createdAtDisplay }}
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
import { getBoardsByUser } from '../api/board.js'
import { useAuthStore } from '../stores/auth.js'

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
    createdAtDisplay: post.createdAt ? String(post.createdAt).slice(0, 10) : '',
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
</style>
