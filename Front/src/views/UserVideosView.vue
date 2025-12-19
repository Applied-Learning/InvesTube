<template>
  <div class="profile-page-container">
    <PageHeader :title="pageTitle" />

    <div class="profile-page-content">
      <div v-if="loading" class="profile-loading">불러오는 중...</div>
      <div v-else-if="error" class="profile-error">{{ error }}</div>
      <div v-else-if="videos.length === 0" class="profile-empty">아직 업로드한 영상이 없어요.</div>
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
                조회수 {{ video.viewCount ?? 0 }} · 찜 {{ video.wishCount ?? 0 }}
              </p>
            </div>
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
import { getVideos } from '../api/video.js'
import { getUserByUserId } from '../api/user.js'

const route = useRoute()

const videos = ref([])
const loading = ref(false)
const error = ref(null)
const userName = ref('')

const pageTitle = computed(() =>
  userName.value ? `${userName.value}님의 업로드한 영상` : '사용자 영상',
)

const normalizeVideo = (video) => {
  if (!video) return null
  return {
    videoId: video.videoId,
    title: video.title,
    thumbnailUrl:
      video.thumbnailUrl ||
      (video.youtubeVideoId ? `https://i.ytimg.com/vi/${video.youtubeVideoId}/hqdefault.jpg` : ''),
    viewCount: video.viewCount ?? 0,
    wishCount: video.wishCount ?? 0,
    userId: video.userId,
  }
}

const fetchUserVideos = async () => {
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

    // 영상 목록
    const res = await getVideos({ sortBy: 'latest' })
    const list = (res.data?.videos || []).map(normalizeVideo).filter(Boolean)
    videos.value = list.filter((v) => v.userId === userId)
  } catch (err) {
    console.error('사용자 영상 전체 목록 조회 실패:', err)
    error.value = '영상 목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchUserVideos)
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
  border-bottom: 1px solid #e5e7eb;
}

.profile-video-item:last-child {
  border-bottom: none;
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
