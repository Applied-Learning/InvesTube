<template>
  <div class="video-detail-container">
    <PageHeader title="영상 상세" />

    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="video" class="video-content">
      <!-- YouTube 플레이어 -->
      <div class="video-player">
        <iframe
          :src="`https://www.youtube.com/embed/${video.youtubeVideoId}`"
          frameborder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
          allowfullscreen
        ></iframe>
      </div>

      <!-- 비디오 정보 -->
      <div class="video-info">
        <div class="video-header">
          <h1 class="video-title">{{ video.title }}</h1>
          <button 
            class="wish-btn"
            :class="{ active: isWished }"
            @click="toggleWish"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 21L10.55 19.7051C5.4 15.1242 2 12.1029 2 8.39509C2 5.37384 4.42 3 7.5 3C9.24 3 10.91 3.81 12 5.08651C13.09 3.81 14.76 3 16.5 3C19.58 3 22 5.37384 22 8.39509C22 12.1029 18.6 15.1242 13.45 19.7051L12 21Z" :fill="isWished ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"/>
            </svg>
            <span>{{ isWished ? '찜 해제' : '찜하기' }}</span>
          </button>
        </div>

        <div class="video-meta">
          <span class="meta-item">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 14.6667C11.6819 14.6667 14.6667 11.6819 14.6667 8C14.6667 4.3181 11.6819 1.33333 8 1.33333C4.3181 1.33333 1.33333 4.3181 1.33333 8C1.33333 11.6819 4.3181 14.6667 8 14.6667Z" stroke="currentColor" stroke-width="1.5"/>
              <path d="M8 4V8L10.6667 9.33333" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            조회수 {{ video.viewCount.toLocaleString() }}회
          </span>
          <span class="meta-item">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 14L6.82 12.9533C3.4 10.06 1.33333 8.21333 1.33333 5.93333C1.33333 4.2 2.63333 2.9 4.36667 2.9C5.45333 2.9 6.5 3.44 7.16667 4.27333C7.83333 3.44 8.88 2.9 9.96667 2.9C11.7 2.9 13 4.2 13 5.93333C13 8.21333 10.9333 10.06 7.51333 12.96L8 14Z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            {{ video.wishCount }}
          </span>
          <span class="meta-item">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 1.33333L10.06 5.50667L14.6667 6.18L11.3333 9.42667L12.12 14.0133L8 11.8467L3.88 14.0133L4.66667 9.42667L1.33333 6.18L5.94 5.50667L8 1.33333Z" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            {{ video.avgRating ? video.avgRating.toFixed(1) : 'N/A' }} ({{ video.reviewCount }}개)
          </span>
          <span class="meta-item">{{ formatDate(video.createdAt) }}</span>
        </div>

        <div class="video-description">
          <h3>설명</h3>
          <p>{{ video.description || '설명이 없습니다.' }}</p>
        </div>

        <!-- 카테고리 -->
        <div class="video-category">
          <span class="category-badge">{{ getCategoryName(video.categoryId) }}</span>
        </div>
      </div>

      <!-- 리뷰 섹션 (추후 구현) -->
      <div class="reviews-section">
        <h2>리뷰</h2>
        <div class="no-reviews">
          리뷰 기능은 준비 중입니다.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { getVideoDetail, checkWishStatus, toggleVideoWish } from '../api/video.js'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const authStore = useAuthStore()
const video = ref(null)
const loading = ref(false)
const error = ref(null)
const isWished = ref(false)

const categories = {
  1: '금융',
  2: '기술',
  3: '투자'
}

const getCategoryName = (id) => {
  return categories[id] || '기타'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

const fetchVideoDetail = async () => {
  loading.value = true
  error.value = null

  try {
    const videoId = route.params.id
    const response = await getVideoDetail(videoId)
    video.value = response.data

    // 로그인된 사용자만 찜 상태 확인
    if (authStore.isAuthenticated) {
      try {
        const wishResponse = await checkWishStatus(videoId)
        isWished.value = wishResponse.data
      } catch (wishErr) {
        // 401 에러는 무시 (로그인 필요)
        if (wishErr.response?.status !== 401) {
          console.error('찜 상태 확인 실패:', wishErr)
        }
        isWished.value = false
      }
    }
  } catch (err) {
    console.error('비디오 상세 조회 실패:', err)
    error.value = '비디오 정보를 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

const toggleWish = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다.')
    return
  }

  try {
    const response = await toggleVideoWish(video.value.videoId)
    isWished.value = response.data
    
    // 찜 수 업데이트
    if (isWished.value) {
      video.value.wishCount++
    } else {
      video.value.wishCount--
    }
  } catch (err) {
    console.error('찜하기 실패:', err)
    alert('찜하기에 실패했습니다.')
  }
}

onMounted(() => {
  fetchVideoDetail()
})
</script>

<style scoped>
.video-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading,
.error {
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.error {
  color: #d32f2f;
}

.video-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.video-player {
  position: relative;
  padding-bottom: 56.25%; /* 16:9 aspect ratio */
  height: 0;
  overflow: hidden;
  background: #000;
}

.video-player iframe {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.video-info {
  padding: 24px;
}

.video-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.video-title {
  flex: 1;
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.wish-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 600;
}

.wish-btn:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.wish-btn.active {
  background: #fef2f2;
  border-color: #ef4444;
  color: #ef4444;
}

.wish-btn svg {
  width: 20px;
  height: 20px;
}

.video-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #6b7280;
}

.meta-item svg {
  color: #9ca3af;
}

.video-description h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 700;
  color: #374151;
}

.video-description p {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
  white-space: pre-wrap;
}

.video-category {
  margin-top: 20px;
}

.category-badge {
  display: inline-block;
  padding: 6px 12px;
  background: #eff6ff;
  color: #2563eb;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
}

.reviews-section {
  padding: 24px;
  border-top: 1px solid #e5e7eb;
}

.reviews-section h2 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.no-reviews {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
