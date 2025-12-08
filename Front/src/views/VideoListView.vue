<template>
  <div>
    <PageHeader v-if="isWishlistMode" title="찜한 영상" />

    <div class="controls" v-if="!isWishlistMode">
      <!-- 카테고리 칩 (가로 스크롤) -->
      <div class="category-scroll">
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
    </div>

    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="videos.length === 0" class="empty">
      <p v-if="isWishlistMode">찜한 영상이 없습니다.</p>
      <p v-else>비디오가 없습니다.</p>
    </div>
    <div v-else class="grid">
      <VideoCard
        v-for="video in videos"
        :key="video.id"
        v-bind="video"
        @click="goDetail(video.id)"
        @toggle-wish="toggleWish(video)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import VideoCard from '../components/video/VideoCard.vue'
import PageHeader from '../components/common/PageHeader.vue'
import { getVideos, getVideosByCategory, getWishedVideos, toggleVideoWish } from '../api/video.js'

const route = useRoute()
const videos = ref([])
const loading = ref(false)
const error = ref(null)
const sortBy = ref('latest')
const selectedCategory = ref(null)
const isWishlistMode = computed(() => route.meta.isWishlist === true)
const wishedVideoIds = ref(new Set()) // 찜한 비디오 ID 목록

const categories = [
  { id: null, name: '전체' },
  { id: 1, name: '주식' },
  { id: 2, name: '부동산' },
  { id: 3, name: '채권' },
  { id: 4, name: '펀드' },
  { id: 5, name: 'ETF' },
  { id: 6, name: '암호화폐' },
  { id: 7, name: '경제 뉴스' },
  { id: 8, name: '재테크' },
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
    wishedVideoIds.value = new Set(wishedVideos.map(v => v.videoId))
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
    // 로그인 사용자의 찜 목록을 먼저 로드 (찜 목록 모드가 아닐 때만)
    if (!isWishlistMode.value) {
      await loadWishedVideoIds()
    }
    
    let response
    if (isWishlistMode.value) {
      // 찜 목록 조회
      response = await getWishedVideos()
    } else if (selectedCategory.value === null) {
      // 전체 조회
      response = await getVideos({ sortBy: sortBy.value })
    } else {
      // 카테고리별 조회
      response = await getVideosByCategory(selectedCategory.value)
    }
    
    // 백엔드 데이터를 VideoCard props 형식으로 변환
    const videoList = response.data.videos || []
    videos.value = videoList.map(video => ({
      id: video.videoId,
      youtubeVideoId: video.youtubeVideoId,
      title: video.title,
      thumbnailUrl: video.thumbnailUrl || `https://i.ytimg.com/vi/${video.youtubeVideoId}/hqdefault.jpg`,
      uploaderName: video.uploaderName || '익명',
      uploaderProfileImageUrl: video.uploaderProfileImageUrl || '',
      views: video.viewCount,
      createdAtText: video.createdAt ? new Date(video.createdAt).toLocaleDateString() : '',
      duration: video.duration || '',
      wished: isWishlistMode.value ? true : wishedVideoIds.value.has(video.videoId),
    }))
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
  fetchVideos()
}

// 정렬 기준 변경
const changeSortBy = (newSortBy) => {
  sortBy.value = newSortBy
  fetchVideos()
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchVideos()
})

// 라우트 변경 감지 (찜 목록 ↔ 일반 목록)
watch(() => route.path, () => {
  // 라우트 변경 시 필터 초기화 및 데이터 재로드
  selectedCategory.value = null
  sortBy.value = 'latest'
  fetchVideos()
})

const goDetail = (id) => {
  console.log('go video detail', id)
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

/* 정렬 탭 */
.sort-tabs {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0;
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
</style>

