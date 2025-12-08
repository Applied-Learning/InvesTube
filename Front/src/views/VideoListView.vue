<template>
  <div>
    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
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
import { ref, onMounted } from 'vue'
import VideoCard from '../components/video/VideoCard.vue'
import { getVideos, toggleVideoWish } from '../api/video.js'

const videos = ref([])
const loading = ref(false)
const error = ref(null)

// 비디오 목록 불러오기
const fetchVideos = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await getVideos({ sortBy: 'latest' })
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
      wished: video.wished || false,
    }))
  } catch (err) {
    console.error('비디오 목록 조회 실패:', err)
    error.value = '비디오 목록을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchVideos()
})

const goDetail = (id) => {
  console.log('go video detail', id)
}

const toggleWish = async (video) => {
  try {
    await toggleVideoWish(video.id)
    video.wished = !video.wished
  } catch (err) {
    console.error('찜하기 실패:', err)
    alert('찜하기에 실패했습니다.')
  }
}
</script>

<style scoped>
.title {
  margin: 0 0 12px;
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
</style>

