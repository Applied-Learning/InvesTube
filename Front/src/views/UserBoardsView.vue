<template>
  <div class="profile-page-container">
    <PageHeader :title="pageTitle" />

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
            <p class="profile-board-title">{{ post.title }}</p>
            <p class="profile-board-meta">
              조회수 {{ post.viewCount ?? 0 }} · 댓글 {{ post.commentCount ?? 0 }} ·
              {{ post.createdAtDisplay }}
            </p>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { formatKSTDate } from '../utils/date.js'
import { getBoardsByUser } from '../api/board.js'
import { getUserByUserId } from '../api/user.js'

const route = useRoute()

const posts = ref([])
const loading = ref(false)
const error = ref(null)
const userName = ref('')

const pageTitle = computed(() =>
  userName.value ? `${userName.value}님의 게시글` : '사용자 게시글',
)

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

const fetchUserBoards = async () => {
  const rawId = route.params.userId
  const userId = Number(rawId)
  if (!userId || Number.isNaN(userId)) {
    error.value = '잘못된 사용자입니다.'
    return
  }

  loading.value = true
  error.value = null

  try {
    // 사용자 이름
    const userRes = await getUserByUserId(userId)
    if (userRes.data) {
      userName.value = userRes.data.nickname || userRes.data.id || `사용자 ${userId}`
    }

    // 게시글
    const res = await getBoardsByUser(userId, 0, 50)
    const data = res.data
    const list = data?.posts || data || []
    posts.value = list.map(normalizeBoardPost).filter(Boolean)
  } catch (err) {
    console.error('사용자 게시글 전체 목록 조회 실패:', err)
    error.value = '게시글을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchUserBoards)
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
