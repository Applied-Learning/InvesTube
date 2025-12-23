<template>
  <div class="editorial-board-page">
    <!-- 이머시브 헤더 (배경) -->
    <div class="immersive-header">
      <div class="header-overlay"></div>
      <div class="header-pattern"></div>
      <div class="header-content">
        <h1 class="page-title">커뮤니티</h1>
        <p class="page-subtitle">다양한 투자 인사이트를 자유롭게 나누어보세요</p>
      </div>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="content-wrapper">
      <div class="board-action-bar">
        <!-- 검색 / 글쓰기 -->
        <div class="search-group">
          <div class="search-input-wrapper">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" class="search-icon">
              <path d="M21 21L15 15M17 10C17 13.866 13.866 17 10 17C6.13401 17 3 13.866 3 10C3 6.13401 6.13401 3 10 3C13.866 3 17 6.13401 17 10Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="관심있는 주제를 검색해보세요..."
              @keyup.enter="handleSearch"
              class="search-input"
            />
          </div>
          <button @click="handleSearch" class="action-btn search">검색</button>
        </div>

        <div class="right-actions">
           <!-- 정렬 -->
          <div class="sort-group">
            <button
              @click="changeSortBy('latest')"
              :class="['sort-pill', { active: sortBy === 'latest' }]"
            >
              최신순
            </button>
            <button
              @click="changeSortBy('views')"
              :class="['sort-pill', { active: sortBy === 'views' }]"
            >
              조회순
            </button>
          </div>
          <button v-if="authStore.isAuthenticated" @click="goToCreate" class="action-btn create">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 5v14M5 12h14" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            글쓰기
          </button>
        </div>
      </div>

      <!-- 게시글 목록 -->
      <div class="board-feed">
        <div v-if="loading" class="state-message loading">
          <div class="loader"></div>
          <span>로딩 중...</span>
        </div>
        <div v-else-if="error" class="state-message error">{{ error }}</div>
        <div v-else-if="posts.length === 0" class="state-message empty">
          <p>게시글이 없습니다.</p>
          <span>첫 번째 글을 작성해보세요!</span>
        </div>
        
        <div v-else class="post-grid">
          <article
            v-for="post in posts"
            :key="post.postId"
            class="article-card"
            @click="goToDetail(post.postId)"
          >
            <div class="card-content">
              <div class="card-meta-top">
                <span class="card-date">{{ formatDate(post.createdAt) }}</span>
                <span class="card-author">{{ post.authorNickname || 'Anonymous' }}</span>
              </div>
              
              <h3 class="card-title">{{ post.title }}</h3>
              <p class="card-preview">{{ getContentPreview(post.content) }}</p>
              
              <div class="card-footer">
                <div class="card-stats">
                  <span class="stat-item" title="조회수">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                    {{ post.viewCount || 0 }}
                  </span>
                  <!-- 댓글 수 필요하다면 API에서 제공되어야 함. 현재 없으면 생략 -->
                </div>
                <div class="read-more">Read More →</div>
              </div>
            </div>

            <div v-if="getFirstImageSrc(post.content)" class="card-thumbnail">
              <img :src="getFirstImageSrc(post.content)" alt="Thumbnail" />
            </div>
          </article>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination-nav">
        <button @click="changePage(currentPage - 1)" :disabled="currentPage === 0" class="nav-btn prev">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M15 18l-6-6 6-6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div class="page-pills">
          <button
            v-for="page in displayPages"
            :key="page"
            @click="changePage(page)"
            :class="['page-pill', { active: page === currentPage }]"
          >
            {{ page + 1 }}
          </button>
        </div>
        <button
          @click="changePage(currentPage + 1)"
          :disabled="currentPage >= totalPages - 1"
          class="nav-btn next"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
             <path d="M9 18l6-6-6-6" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getBoardList, getBoardsByUser } from '../api/board'
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

// Preview text with clean length limit
const getContentPreview = (content) => {
  if (!content) return ''
  const withoutImg = content.replace(/<img[\s\S]*?>/gi, '')
  const textOnly = withoutImg
    .replace(/<[^>]+>/g, '') // strip html
    .replace(/&nbsp;/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return textOnly.length > 120 ? textOnly.substring(0, 120) + '...' : textOnly || '내용 없음'
}

const formatDate = (dateString) => {
  return timeAgoKST(dateString)
}

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;0,900;1,300&family=Inter:wght@300;400;500;600;700;800&display=swap');

.editorial-board-page {
  font-family: 'Inter', sans-serif;
  color: #0f172a;
  background-color: #f8fafc;
  min-height: 100vh;
  position: relative;
}

/* Header */
.immersive-header {
  position: relative;
  width: 100%; height: 320px;
  background: linear-gradient(135deg, #e0e7ff 0%, #fae8ff 50%, #fce7f3 100%);
  display: flex; align-items: center; justify-content: center;
  text-align: center;
  overflow: hidden;
}

.header-pattern {
  position: absolute; inset: 0;
  opacity: 0.4;
  background-image: radial-gradient(#6366f1 1px, transparent 1px);
  background-size: 32px 32px;
}

.header-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to bottom, transparent 0%, #f8fafc 100%);
}

.header-content {
  position: relative; z-index: 2;
  margin-top: -40px;
}

.page-title {
  font-size: 48px; font-weight: 900; letter-spacing: -0.03em;
  background: linear-gradient(to right, #0f172a, #334155);
  -webkit-background-clip: text; -webkit-text-fill-color: transparent;
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 16px; color: #64748b; font-weight: 500;
}

/* Content */
.content-wrapper {
  position: relative; z-index: 5;
  max-width: 1000px; margin: -60px auto 0;
  padding: 0 24px 120px;
}

/* Action Bar */
.board-action-bar {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 32px;
  background: white; padding: 16px 24px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.04);
}

.search-group {
  display: flex; gap: 12px; flex: 1; max-width: 480px;
  min-width: 0; /* Allow shrinking if necessary */
}

.search-input-wrapper {
  position: relative; flex: 1;
}

.search-icon {
  position: absolute; left: 16px; top: 50%; transform: translateY(-50%);
  color: #94a3b8;
}

.search-input {
  width: 100%; height: 48px;
  padding: 0 16px 0 48px;
  border: 1px solid #e2e8f0; border-radius: 100px;
  background: #f8fafc; font-size: 15px; color: #0f172a;
  transition: all 0.2s;
  font-family: 'Inter', sans-serif;
  box-sizing: border-box; /* Fix width overflow */
}

.search-input:focus {
  outline: none; border-color: #6366f1; background: white;
  box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
}

.action-btn {
  height: 48px; padding: 0 24px; border-radius: 100px;
  font-weight: 600; font-size: 14px;
  cursor: pointer; transition: all 0.2s; border: none;
  flex-shrink: 0; /* Prevent button from being cut off */
  white-space: nowrap;
  position: relative; z-index: 5; /* Ensure button stays on top */
}

.action-btn.search {
  background: #0f172a; color: white;
}
.action-btn.search:hover { background: #1e293b; transform: translateY(-1px); }

.action-btn.create {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white;
  display: flex; align-items: center; gap: 8px;
  padding: 0 28px;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}
.action-btn.create:hover {
  transform: translateY(-2px); box-shadow: 0 8px 16px rgba(99, 102, 241, 0.4);
}

.right-actions {
  display: flex; gap: 24px; align-items: center;
}

.sort-group {
  display: flex; gap: 6px;
}

.sort-pill {
  padding: 8px 16px; border-radius: 100px;
  font-size: 13px; font-weight: 600; color: #64748b;
  background: transparent; border: 1px solid transparent;
  cursor: pointer; transition: all 0.2s;
}

.sort-pill:hover { color: #0f172a; background: #f1f5f9; }
.sort-pill.active {
  background: #f1f5f9; color: #0f172a; border-color: #e2e8f0;
}

/* Feed List */
.post-grid {
  display: flex; flex-direction: column; gap: 20px;
}

.article-card {
  background: white;
  border-radius: 20px;
  padding: 32px;
  transition: all 0.3s ease;
  cursor: pointer;
  border: 1px solid transparent;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); /* very subtle default */
  
  display: flex; align-items: stretch; justify-content: space-between; gap: 32px;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px -8px rgba(0,0,0,0.1), 0 4px 8px -4px rgba(0,0,0,0.05);
  border-color: rgba(99, 102, 241, 0.1);
}

.card-content {
  flex: 1; display: flex; flex-direction: column;
}

.card-meta-top {
  display: flex; align-items: center; gap: 10px; margin-bottom: 12px;
  font-size: 12px; font-weight: 600; color: #6366f1;
}

.card-date { text-transform: uppercase; letter-spacing: 0.5px; }
.card-author { color: #94a3b8; font-weight: 500; }

.card-title {
  font-size: 20px; font-weight: 800; color: #0f172a; 
  margin: 0 0 12px; line-height: 1.4;
  transition: color 0.2s;
}

.article-card:hover .card-title { color: #6366f1; }

.card-preview {
  font-size: 15px; line-height: 1.6; color: #64748b;
  margin-bottom: 20px;
  flex: 1;
}

.card-footer {
  display: flex; justify-content: space-between; align-items: center;
  margin-top: auto;
}

.card-stats {
  display: flex; gap: 16px; color: #94a3b8; font-size: 13px; font-weight: 500;
}
.stat-item { display: flex; align-items: center; gap: 6px; }

.read-more {
  font-size: 13px; font-weight: 700; color: #0f172a; 
  opacity: 0; transform: translateX(-10px); transition: all 0.3s;
}
.article-card:hover .read-more {
  opacity: 1; transform: translateX(0);
}

.card-thumbnail {
  flex-shrink: 0; width: 180px; height: 180px;
  border-radius: 12px; overflow: hidden; background: #f1f5f9;
}
.card-thumbnail img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.5s; }
.article-card:hover .card-thumbnail img { transform: scale(1.05); }

/* States */
.state-message {
  text-align: center; padding: 80px 0; color: #94a3b8;
  display: flex; flex-direction: column; align-items: center; gap: 16px;
}
.loader {
  width: 40px; height: 40px; border: 3px solid #e2e8f0; 
  border-top-color: #6366f1; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Pagination */
.pagination-nav {
  display: flex; justify-content: center; align-items: center; gap: 20px;
  margin-top: 60px;
}

.nav-btn {
  width: 44px; height: 44px; border-radius: 50%;
  border: 1px solid #e2e8f0; background: white;
  display: flex; align-items: center; justify-content: center;
  color: #64748b; cursor: pointer; transition: all 0.2s;
}
.nav-btn:hover:not(:disabled) { border-color: #0f172a; color: #0f172a; }
.nav-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.page-pills { display: flex; gap: 8px; }

.page-pill {
  width: 40px; height: 40px; border-radius: 12px;
  border: none; background: transparent;
  color: #64748b; font-weight: 600; cursor: pointer;
  transition: all 0.2s;
}
.page-pill:hover { background: #f1f5f9; color: #0f172a; }
.page-pill.active { background: #0f172a; color: white; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.3); }

/* Mobile */
@media (max-width: 900px) {
  .board-action-bar { flex-direction: column; gap: 20px; padding: 24px; }
  .search-group { width: 100%; max-width: none; }
  .right-actions { width: 100%; justify-content: space-between; }
  .article-card { flex-direction: column; gap: 20px; }
  .card-thumbnail { width: 100%; height: 200px; order: -1; }
  .page-title { font-size: 32px; }
  .immersive-header { height: 260px; }
}
</style>
