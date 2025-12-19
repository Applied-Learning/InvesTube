<template>
  <div>
    <PageHeader v-if="isWishlistMode" title="찜한 영상" icon="video" />

    <div class="controls" v-if="!isWishlistMode">
      <!-- 검색바 -->
      <div class="search-bar">
        <div class="search-input-wrapper">
          <svg
            class="search-icon"
            width="20"
            height="20"
            viewBox="0 0 20 20"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M9 17A8 8 0 1 0 9 1a8 8 0 0 0 0 16zM18 18l-4-4"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="영상 검색..."
            @keyup.enter="handleSearch"
            class="search-input"
          />
          <button v-if="searchKeyword" class="clear-btn" @click.stop="searchKeyword = ''">
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M12 4L4 12M4 4l8 8"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
          </button>
        </div>
        <button class="search-btn" @click="handleSearch" type="button">검색</button>
      </div>

      <!-- 검색 결과 표시 -->
      <div v-if="isSearchMode" class="search-result-info">
        <span>"{{ searchKeyword }}" 검색 결과 ({{ totalCount }}개)</span>
        <button class="clear-search-btn" @click="clearSearch" type="button">검색 취소</button>
      </div>

      <!-- 카테고리 칩 (가로 스크롤) -->
      <div v-if="!isSearchMode" class="category-scroll">
        <button
          v-for="category in categories"
          :key="category.id"
          :class="['category-chip', { active: selectedCategory === category.id }]"
          @click="changeCategory(category.id)"
        >
          {{ category.name }}
        </button>
      </div>

      <!-- 정렬 탭 -->
      <div class="sort-tabs-wrapper">
        <div class="sort-tabs">
          <button
            v-for="option in sortOptions"
            :key="option.value"
            :class="['sort-tab', { active: sortBy === option.value && !isWishlistMode }]"
            @click="changeSortBy(option.value)"
          >
            {{ option.label }}
          </button>
          <button
            class="sort-tab"
            :class="{ active: isWishlistMode }"
            @click="$router.push('/wishlist')"
          >
            찜한 영상
          </button>
        </div>

        <!-- 영상 등록 버튼 (로그인 사용자만) -->
        <button v-if="authStore.isAuthenticated" class="create-video-btn" @click="goCreateVideo">
          <svg
            width="16"
            height="16"
            viewBox="0 0 16 16"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M8 3V13M3 8H13"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
            />
          </svg>
          영상 등록
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="videos.length === 0" class="empty">
      <p v-if="isWishlistMode">찜한 영상이 없습니다.</p>
      <p v-else>비디오가 없습니다.</p>
    </div>
    <div v-else>
      <div class="grid">
        <VideoCard
          v-for="video in videos"
          :key="video.id"
          v-bind="video"
          @click="goDetail(video.id)"
          @toggle-wish="toggleWish(video)"
        />
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="currentPage === 1" @click="changePage(1)">처음</button>
        <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">
          이전
        </button>

        <button
          v-for="page in getPageNumbers()"
          :key="page"
          :class="['page-number', { active: page === currentPage }]"
          @click="changePage(page)"
        >
          {{ page }}
        </button>

        <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="changePage(currentPage + 1)"
        >
          다음
        </button>
        <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="changePage(totalPages)"
        >
          마지막
        </button>
      </div>

      <!-- 페이지 정보 -->
      <div v-if="totalCount > 0" class="page-info">
        전체 {{ totalCount }}개 중 {{ (currentPage - 1) * pageSize + 1 }}-{{
          Math.min(currentPage * pageSize, totalCount)
        }}개
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VideoCard from '../components/video/VideoCard.vue'
import PageHeader from '../components/common/PageHeader.vue'
import {
  getVideos,
  getVideosByCategory,
  getWishedVideos,
  toggleVideoWish,
  searchVideos,
} from '../api/video.js'
import { useAuthStore } from '../stores/auth.js'
import { formatKSTDate } from '../utils/date.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const videos = ref([])
const loading = ref(false)
const error = ref(null)
const sortBy = ref('latest')
const selectedCategory = ref(null)
const isWishlistMode = computed(() => route.meta.isWishlist === true)
const wishedVideoIds = ref(new Set()) // 찜한 비디오 ID 목록

// 페이징 상태
const currentPage = ref(1)
const pageSize = ref(12)
const totalPages = ref(0)
const totalCount = ref(0)

// 검색 관련
const searchKeyword = ref('')
const isSearchMode = ref(false)

const categories = [
  { id: null, name: '전체' },
  { id: 1, name: '금융' },
  { id: 2, name: '기술' },
  { id: 3, name: '투자' },
]

const sortOptions = [
  { value: 'latest', label: '최신순' },
  { value: 'views', label: '조회수순' },
  { value: 'rating', label: '평점순' },
]

// 찜 목록 로드 (로그인 사용자만)
const loadWishedVideoIds = async () => {
  try {
    const response = await getWishedVideos()
    const wishedVideos = response.data.videos || []
    wishedVideoIds.value = new Set(wishedVideos.map((v) => v.videoId))
  } catch (err) {
    // 로그인하지 않았거나 에러 발생 시 빈 Set
    wishedVideoIds.value = new Set()
  }
}

// 비디오 목록 불러오기
const fetchVideos = async () => {
  loading.value = true
  error.value = null
  try {
    // 로그인 사용자의 찜 목록을 먼저 로드 (찜 목록 모드가 아닐 때만, 첫 로드시)
    if (!isWishlistMode.value && currentPage.value === 1) {
      await loadWishedVideoIds()
    }

    // 페이징 파라미터
    const params = {
      page: currentPage.value,
      size: pageSize.value,
    }

    let response
    if (isWishlistMode.value) {
      // 찜 목록 조회
      response = await getWishedVideos(params)
    } else if (isSearchMode.value && searchKeyword.value.trim()) {
      // 검색 모드
      response = await searchVideos({ ...params, keyword: searchKeyword.value.trim() })
    } else if (selectedCategory.value === null) {
      // 전체 조회
      response = await getVideos({ ...params, sortBy: sortBy.value })
    } else {
      // 카테고리별 조회
      response = await getVideosByCategory(selectedCategory.value, params)
    }

    // 백엔드 데이터를 VideoCard props 형식으로 변환
    const videoList = response.data.videos || []
    videos.value = videoList.map((video) => ({
      id: video.videoId,
      youtubeVideoId: video.youtubeVideoId,
      title: video.title,
      thumbnailUrl:
        video.thumbnailUrl || `https://i.ytimg.com/vi/${video.youtubeVideoId}/hqdefault.jpg`,
      uploaderName: video.uploaderNickname || `사용자 ${video.userId}`,
      uploaderProfileImageUrl: video.uploaderProfileImage || '',
      views: video.viewCount,
      createdAtText: video.createdAt ? formatKSTDate(video.createdAt) : '',
      duration: video.duration || '',
      wished: isWishlistMode.value ? true : wishedVideoIds.value.has(video.videoId),
    }))

    // 페이징 정보 업데이트
    totalPages.value = response.data.totalPages || 0
    totalCount.value = response.data.totalCount || 0
  } catch (err) {
    console.error('비디오 목록 조회 실패:', err)
    error.value = '비디오 목록을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

// 카테고리 변경
const changeCategory = (categoryId) => {
  selectedCategory.value = categoryId
  currentPage.value = 1 // 첫 페이지로 리셋
  fetchVideos()
}

// 정렬 기준 변경
const changeSortBy = (newSortBy) => {
  sortBy.value = newSortBy
  isSearchMode.value = false // 검색 모드 해제
  searchKeyword.value = '' // 검색어 초기화
  currentPage.value = 1 // 첫 페이지로 리셋
  fetchVideos()
}

// 페이지 변경
const changePage = (page) => {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return
  currentPage.value = page
  fetchVideos()
  // 페이지 변경 시 상단으로 스크롤
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 페이지네이션 번호 배열 생성
const getPageNumbers = () => {
  const pages = []
  const maxVisible = 5 // 보여줄 최대 페이지 번호 개수

  if (totalPages.value <= maxVisible) {
    // 전체 페이지가 적으면 모두 표시
    for (let i = 1; i <= totalPages.value; i++) {
      pages.push(i)
    }
  } else {
    // 현재 페이지 중심으로 표시
    let start = Math.max(1, currentPage.value - 2)
    let end = Math.min(totalPages.value, start + maxVisible - 1)

    if (end - start < maxVisible - 1) {
      start = Math.max(1, end - maxVisible + 1)
    }

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
  }

  return pages
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchVideos()
})

// 라우트 변경 감지 (찜 목록 ↔ 일반 목록)
watch(
  () => route.path,
  () => {
    // 라우트 변경 시 필터 초기화 및 데이터 재로드
    selectedCategory.value = null
    sortBy.value = 'latest'
    searchKeyword.value = ''
    isSearchMode.value = false
    currentPage.value = 1
    fetchVideos()
  },
)

const goDetail = (id) => {
  router.push(`/video/${id}`)
}

const goCreateVideo = () => {
  router.push('/video/create')
}

// 검색 처리
const handleSearch = () => {
  const keyword = searchKeyword.value.trim()

  if (keyword) {
    isSearchMode.value = true
    selectedCategory.value = null
    currentPage.value = 1
    fetchVideos()
  } else {
    alert('검색어를 입력해주세요')
  }
}

// 검색 취소
const clearSearch = () => {
  searchKeyword.value = ''
  isSearchMode.value = false
  currentPage.value = 1
  fetchVideos()
}

const toggleWish = async (video) => {
  try {
    const response = await toggleVideoWish(video.id)
    video.wished = response.data // 백엔드가 새로운 상태 반환

    // wishedVideoIds 업데이트
    if (video.wished) {
      wishedVideoIds.value.add(video.id)
    } else {
      wishedVideoIds.value.delete(video.id)
    }

    // 찜 목록 모드에서 찜 해제하면 목록에서 제거
    if (isWishlistMode.value && !video.wished) {
      fetchVideos()
    }
  } catch (err) {
    console.error('찜하기 실패:', err)
    alert('찜하기에 실패했습니다.')
  }
}
</script>

<style scoped>
.controls {
  margin-bottom: 24px;
}

/* 검색바 */
.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: #9ca3af;
  pointer-events: none;
}

.search-input {
  width: 100%;
  padding: 12px 40px 12px 40px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.clear-btn {
  position: absolute;
  right: 12px;
  padding: 4px;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.clear-btn:hover {
  color: #6b7280;
}

.search-btn {
  padding: 12px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.search-btn:hover {
  background: #1d4ed8;
}

/* 검색 결과 정보 */
.search-result-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #1e40af;
}

.clear-search-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid #bfdbfe;
  border-radius: 6px;
  font-size: 13px;
  color: #2563eb;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-search-btn:hover {
  background: #dbeafe;
}

/* 카테고리 가로 스크롤 */
.category-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 12px 0;
  margin-bottom: 16px;
  scrollbar-width: thin;
  scrollbar-color: #cbd5e1 #f1f5f9;
}

.category-scroll::-webkit-scrollbar {
  height: 6px;
}

.category-scroll::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.category-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.category-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.category-chip {
  flex-shrink: 0;
  padding: 8px 16px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.category-chip:hover {
  background: #e5e7eb;
}

.category-chip.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

/* 정렬 탭 wrapper */
.sort-tabs-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  border-bottom: 1px solid #e5e7eb;
}

/* 정렬 탭 */
.sort-tabs {
  display: flex;
  gap: 8px;
}

.sort-tab {
  padding: 12px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  color: #6b7280;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
  display: inline-block;
}

.sort-tab:hover {
  color: #374151;
}

.sort-tab.active {
  color: #2563eb;
  border-bottom-color: #2563eb;
}

/* 영상 등록 버튼 */
.create-video-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
}

.create-video-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.create-video-btn:active {
  transform: translateY(0);
}

.create-video-btn svg {
  width: 16px;
  height: 16px;
}

.title {
  margin: 0 0 12px;
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.loading,
.error,
.empty {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error {
  color: #d32f2f;
}

.empty {
  color: #6b7280;
}

/* 페이지네이션 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 32px;
  padding: 20px 0;
}

.page-btn,
.page-number {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
  background: white;
  color: #374151;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled),
.page-number:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-number.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.page-info {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
  margin-top: 12px;
}
</style>
