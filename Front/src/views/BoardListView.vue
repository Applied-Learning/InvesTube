<template>
  <Container>
    <PageHeader title="게시판" :showBack="false" icon="board" />

    <div class="board-container">
      <!-- 검색 / 글쓰기 -->
      <div class="board-header">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="제목 또는 내용으로 검색..."
            @keyup.enter="handleSearch"
          />
          <button @click="handleSearch" class="search-btn">검색</button>
        </div>
        <button v-if="authStore.isAuthenticated" @click="goToCreate" class="create-btn">
          글쓰기
        </button>
      </div>

      <!-- 정렬 -->
      <div class="sort-options">
        <button
          @click="changeSortBy('latest')"
          :class="['sort-btn', { active: sortBy === 'latest' }]"
        >
          최신순
        </button>
        <button
          @click="changeSortBy('views')"
          :class="['sort-btn', { active: sortBy === 'views' }]"
        >
          조회순
        </button>
      </div>

      <!-- 게시글 목록 -->
      <div v-if="loading" class="loading">로딩 중...</div>
      <div v-else-if="error" class="error">{{ error }}</div>
      <div v-else-if="posts.length === 0" class="empty">게시글이 없습니다.</div>
      <div v-else class="post-list">
        <div
          v-for="post in posts"
          :key="post.postId"
          class="post-item"
          @click="goToDetail(post.postId)"
        >
          <!-- 위: 제목 + 요약 + (오른쪽) 썸네일 -->
          <div class="post-main">
            <div class="post-content">
              <h3 class="post-title">
                <span class="post-title-text">{{ post.title }}</span>
              </h3>
              <p class="post-preview">{{ getContentPreview(post.content) }}</p>
            </div>
            <div v-if="getFirstImageSrc(post.content)" class="post-thumb">
              <img :src="getFirstImageSrc(post.content)" alt="썸네일" />
            </div>
          </div>

          <!-- 아래: 왼쪽 작성자 / 오른쪽 조회/시간 -->
          <div class="post-meta">
            <div class="author-info">
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
              <span class="author-name">{{ post.authorNickname || '익명' }}</span>
            </div>
            <div class="post-stats">
              <span class="view-count">조회 {{ post.viewCount || 0 }}</span>
              <span class="post-date">{{ formatDate(post.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0" class="page-btn">
          이전
        </button>
        <div class="page-numbers">
          <button
            v-for="page in displayPages"
            :key="page"
            @click="changePage(page)"
            :class="['page-number', { active: page === currentPage }]"
          >
            {{ page + 1 }}
          </button>
        </div>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
          class="page-btn"
        >
          다음
        </button>
      </div>
    </div>
  </Container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getBoardList, getBoardsByUser } from '../api/board'
import Container from '../components/common/Container.vue'
import PageHeader from '../components/common/PageHeader.vue'
import { resolveImageUrl } from '../utils/image.js'
import { timeAgoKST } from '../utils/date.js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const posts = ref([])
const loading = ref(false)
const error = ref(null)
const searchKeyword = ref('')
const sortBy = ref('latest')
const currentPage = ref(0)
const pageSize = ref(10)
const totalCount = ref(0)
const totalPages = ref(0)

const displayPages = computed(() => {
  const pages = []
  const start = Math.max(0, currentPage.value - 2)
  const end = Math.min(totalPages.value, start + 5)
  for (let i = start; i < end; i++) {
    pages.push(i)
  }
  return pages
})

const isMyPostsOnly = computed(() => route.query.mine === 'true')

const fetchPosts = async () => {
  loading.value = true
  error.value = null

  try {
    if (isMyPostsOnly.value && authStore.userId) {
      const response = await getBoardsByUser(authStore.userId, currentPage.value, pageSize.value)
      const data = response.data
      posts.value = data.posts || []
      totalCount.value = data.totalCount ?? posts.value.length
      totalPages.value = data.totalPages ?? 1
    } else {
      const response = await getBoardList(
        searchKeyword.value,
        sortBy.value,
        currentPage.value,
        pageSize.value,
      )
      posts.value = response.data.posts
      totalCount.value = response.data.totalCount
      totalPages.value = response.data.totalPages
    }
  } catch (err) {
    console.error('게시글 조회 실패:', err)
    error.value = '게시글을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

const changeSortBy = (newSortBy) => {
  sortBy.value = newSortBy
  currentPage.value = 0
  fetchPosts()
}

const handleSearch = () => {
  currentPage.value = 0
  fetchPosts()
}

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    fetchPosts()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

const goToCreate = () => {
  router.push('/board/create')
}

const goToDetail = (postId) => {
  router.push(`/board/${postId}`)
}

const getFirstImageSrc = (content) => {
  if (!content) return null
  const match = content.match(/<img[^>]*src=['"]([^'"]+)['"][^>]*>/i)
  return match ? match[1] : null
}

const getContentPreview = (content) => {
  if (!content) return ''
  const withoutImg = content.replace(/<img[\s\S]*?>/gi, '')
  const textOnly = withoutImg.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
  return textOnly.length > 100 ? textOnly.substring(0, 100) + '...' : textOnly
}

const getAuthorInitial = (nickname) => {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}

const formatDate = (dateString) => {
  return timeAgoKST(dateString)
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.board-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
}

.search-box {
  display: flex;
  flex: 1;
  max-width: 500px;
  gap: 8px;
}

.search-box input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
}

.search-box input:focus {
  outline: none;
  border-color: #2563eb;
}

.search-btn {
  padding: 12px 24px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.search-btn:hover {
  background: #4b5563;
}

.create-btn {
  padding: 12px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.create-btn:hover {
  background: #1d4ed8;
}

.sort-options {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
}

.sort-btn {
  padding: 10px 20px;
  background: white;
  color: #6b7280;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.sort-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.sort-btn.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 48px;
  color: #6b7280;
  font-size: 16px;
}

.error {
  color: #ef4444;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 24px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.post-main {
  display: flex;
  justify-content: space-between;
  gap: 20px;
}

.post-content {
  flex: 1;
  min-width: 0;
}

.post-thumb {
  width: 120px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f3f4f6;
}

.post-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-title {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-title-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-preview {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  width: 32px;
  height: 32px;
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
  font-size: 14px;
  font-weight: 700;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.view-count {
  font-size: 13px;
  color: #6b7280;
}

.post-date {
  font-size: 13px;
  color: #9ca3af;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 32px;
}

.page-btn,
.page-number {
  padding: 8px 16px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled),
.page-number:hover {
  border-color: #2563eb;
  color: #2563eb;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-number.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

@media (max-width: 768px) {
  .board-header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: none;
  }

  .post-item {
    flex-direction: column-reverse;
  }
}
</style>

