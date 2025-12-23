<template>
  <div class="profile-page-container">
    <PageHeader title="리뷰한 영상" />

    <div class="profile-page-content">
      <div v-if="loading" class="profile-loading">불러오는 중...</div>
      <div v-else-if="error" class="profile-error">{{ error }}</div>
      <div v-else-if="reviews.length === 0" class="profile-empty">아직 작성한 리뷰가 없어요.</div>
      <div v-else class="profile-videos-section">
        <div class="profile-video-list">
          <RouterLink
            v-for="review in reviews"
            :key="review.reviewId"
            class="profile-video-item"
            :to="{ name: 'videoDetail', params: { id: review.videoId } }"
          >
            <div class="profile-video-thumb">
              <!-- video object might be nested or flattened. Adjust based on API -->
              <img 
                :src="review.videoThumbnail || review.message?.video?.thumbnailUrl || 'https://via.placeholder.com/120x68?text=No+Image'" 
                :alt="review.videoTitle" 
                @error="$event.target.src = 'https://via.placeholder.com/120x68?text=No+Image'"
              />
            </div>
            <div class="profile-video-info">
              <p class="profile-video-title">{{ review.videoTitle || '제목 없음' }}</p>
              <div class="review-meta">
                <span class="star-rating">★ {{ review.rating }}</span>
                <span class="review-date">{{ formatDate(review.createdAt) }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
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
import { getMyReviews } from '../api/user.js'
import { formatKSTDate } from '../utils/date.js'

const reviews = ref([])
const loading = ref(false)
const error = ref(null)

const formatDate = (date) => {
  return formatKSTDate(date)
}

const fetchMyReviews = async () => {
  loading.value = true
  error.value = null

  try {
    const res = await getMyReviews()
    // Assuming res.data is the list of reviews
    // We map it to a friendly structure if needed, or stick to raw data if it matches
    reviews.value = res.data || []
    
    // Debug log to help identify structure if it doesn't render correctly
    console.log('My Reviews:', reviews.value)
  } catch (err) {
    console.error('내 리뷰 목록 조회 실패:', err)
    error.value = '리뷰 목록을 불러오지 못했어요.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchMyReviews)
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

.profile-videos-section {
  margin-top: 8px;
}

.profile-video-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.profile-video-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  text-decoration: none;
  color: inherit;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.2s;
}

.profile-video-item:hover {
  border-color: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.profile-video-thumb {
  width: 140px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  background: #f3f4f6;
  flex-shrink: 0;
}

.profile-video-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-video-info {
  flex: 1;
  min-width: 0;
}

.profile-video-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.star-rating {
  color: #fbbf24;
  font-weight: 700;
}

.review-date {
  color: #6b7280;
}

.review-content {
  margin: 0;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
