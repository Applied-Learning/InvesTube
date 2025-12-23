<template>
  <div class="profile-page-container">
    <PageHeader title="찜한 영상" />

    <div class="profile-page-content">
      <div v-if="loading" class="profile-loading">불러오는 중...</div>
      <div v-else-if="error" class="profile-error">{{ error }}</div>
      <div v-else-if="videos.length === 0" class="profile-empty">아직 찜한 영상이 없어요.</div>
      <div v-else class="profile-videos-section">
        <div class="profile-video-list">
          <RouterLink
            v-for="video in videos"
            :key="video.videoId"
            class="profile-video-item"
            :to="{ name: 'videoDetail', params: { id: video.videoId } }"
          >
            <div class="profile-video-thumb">
              <img :src="video.thumbnailUrl" :alt="video.title" />
            </div>
            <div class="profile-video-info">
              <p class="profile-video-title">{{ video.title }}</p>
              <p class="profile-video-meta">
                조회수 {{ video.viewCount }} · 찜 {{ video.wishCount }}
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
import { getWishedVideos } from '../api/video.js'

const videos = ref([])
const loading = ref(false)
const error = ref(null)

const fetchWishedVideos = async () => {
  loading.value = true
  error.value = null

  try {
    const res = await getWishedVideos()
    // getWishedVideos likely returns { data: { videos: [...], ... } } or { data: [...] }
    // Based on VideoListView it seemed to return { data: { videos: [] } }
    // But let's check safety. VideoListView line 219: const wishedVideos = response.data.videos || []
    // But wait, VideoListView line 246: response = await getWishedVideos(params)
    // Then line 259: const videoList = response.data.videos || []
    // So it returns a paginated object.
    
    // However, MyVideosView uses getMyVideos which returns res.data directly as array (line 49).
    // Let's assume getWishedVideos returns standard pagination structure { videos: [], ... }
    
    // If I want to match MyVideosView style, I should handle array or paginated object.
    if (res.data.videos) {
      videos.value = res.data.videos
    } else if (Array.isArray(res.data)) {
      videos.value = res.data
    } else {
      videos.value = []
    }

  } catch (err) {
    console.error('찜한 영상 목록 조회 실패:', err)
    error.value = '찜한 영상 목록을 불러오지 못했어요.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchWishedVideos)
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
  gap: 12px;
  padding: 8px 0;
  text-decoration: none;
  color: inherit;
}

.profile-video-item:hover .profile-video-title {
  text-decoration: underline;
}

.profile-video-thumb {
  width: 120px;
  height: 68px;
  border-radius: 8px;
  overflow: hidden;
  background: #e5e7eb;
  flex-shrink: 0;
}

.profile-video-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-video-info {
  flex: 1;
}

.profile-video-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.profile-video-meta {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}
</style>
