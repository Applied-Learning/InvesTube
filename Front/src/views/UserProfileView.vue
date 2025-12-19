<template>
  <div class="profile-page-container">
    <PageHeader :title="pageTitle" />

    <div class="profile-page-content">
      <div v-if="loading" class="profile-loading">불러오는 중...</div>
      <div v-else-if="error" class="profile-error">{{ error }}</div>

      <div v-else-if="user" class="profile-card">
        <div class="profile-card-header">
          <div class="profile-main-info">
            <div class="profile-card-avatar">
              <img
                v-if="user.profileImage"
                :src="resolveImageUrl(user.profileImage)"
                :alt="user.nickname || user.id || '프로필'"
              />
              <span v-else>{{ avatarInitial }}</span>
            </div>
            <div class="profile-card-info">
              <h2 class="profile-card-name">
                {{ user.nickname || user.id || `사용자 ${user.userId}` }}
              </h2>
              <p v-if="user.id" class="profile-card-id">@{{ user.id }}</p>
            </div>
          </div>
          <button
            v-if="!isMe && authStore.isAuthenticated"
            class="profile-follow-btn"
            :class="{ following: isFollowing }"
            @click="toggleFollowStatus"
          >
            {{ isFollowing ? '팔로잉' : '팔로우' }}
          </button>
        </div>

        <div v-if="isMe" class="profile-self-note">
          <p>본인 프로필입니다. 상세 설정은 마이페이지에서 변경할 수 있어요.</p>
          <button class="goto-mypage-btn" @click="goToMyPage">마이페이지로 이동</button>
        </div>
      </div>

      <!-- 업로드한 영상 목록 -->
      <div v-if="videos.length" class="profile-videos-section">
        <div class="profile-section-header">
          <h3 class="section-title">업로드한 영상</h3>
          <button class="section-more-btn" @click="goUserVideos">전체 보기</button>
        </div>
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

      <!-- 게시글 요약 -->
      <div class="profile-posts-section">
        <div class="profile-section-header">
          <h3 class="section-title">게시글</h3>
          <button class="section-more-btn" @click="goUserBoards">전체 보기</button>
        </div>
        <div v-if="boardLoading" class="profile-loading">불러오는 중...</div>
        <div v-else-if="boardError" class="profile-error">{{ boardError }}</div>
        <div v-else-if="userBoards.length === 0" class="profile-empty">
          아직 작성한 게시글이 없어요.
        </div>
        <div v-else>
          <ul class="profile-board-list">
            <li
              v-for="post in userBoards"
              :key="'board-' + post.postId"
              class="profile-board-item"
              @click="goUserBoardDetail(post.postId)"
            >
              <p class="profile-board-title">{{ post.title }}</p>
              <p class="profile-board-meta">
                조회수 {{ post.viewCount }} · 댓글 {{ post.commentCount }} ·
                {{ post.createdAtDisplay }}
              </p>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { resolveImageUrl } from '../utils/image.js'
import { getUserByUserId } from '../api/user.js'
import { getVideos } from '../api/video.js'
import { getBoardsByUser } from '../api/board.js'
import { PREVIEW_LIMIT } from '../constants/ui.js'
import { checkFollowStatus, toggleFollow } from '../api/follow.js'
import { formatKSTDate } from '../utils/date.js'
import { useAuthStore } from '../stores/auth.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const user = ref(null)
const videos = ref([])
const loading = ref(false)
const error = ref(null)
const isFollowing = ref(false)

const userBoards = ref([])
const boardLoading = ref(false)
const boardError = ref(null)

const isMe = computed(
  () => user.value && authStore.userId && user.value.userId === Number(authStore.userId),
)

const pageTitle = computed(() => {
  if (isMe.value) return '내 프로필'
  if (user.value?.nickname) return `${user.value.nickname}님의 프로필`
  return '사용자 프로필'
})

const avatarInitial = computed(() => {
  if (!user.value) return '?'
  const base = user.value.nickname || user.value.id || String(user.value.userId || '')
  return base.charAt(0).toUpperCase()
})

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

const fetchUserVideos = async (userId) => {
  try {
    const res = await getVideos({ sortBy: 'latest', page: 1, size: PREVIEW_LIMIT })
    const list = (res.data?.videos || []).map(normalizeVideo).filter(Boolean)
    videos.value = list.filter((v) => v.userId === userId)
  } catch (err) {
    console.error('사용자 영상 목록 조회 실패:', err)
  }
}

const fetchUserBoards = async (userId) => {
  boardLoading.value = true
  boardError.value = null
  try {
    const res = await getBoardsByUser(userId, 0, PREVIEW_LIMIT)
    const data = res.data
    const list = data?.posts || data || []
    userBoards.value = list.map(normalizeBoardPost).filter(Boolean)
  } catch (err) {
    console.error('사용자 게시글 요약 조회 실패:', err)
    boardError.value = '게시글 정보를 불러오는 중 오류가 발생했습니다.'
  } finally {
    boardLoading.value = false
  }
}

const fetchUser = async () => {
  const rawId = route.params.userId
  const userId = Number(rawId)
  if (!userId || Number.isNaN(userId)) {
    error.value = '잘못된 사용자입니다.'
    return
  }

  // 본인 프로필이면 마이페이지로 이동
  if (authStore.userId && Number(authStore.userId) === userId) {
    router.replace('/mypage')
    return
  }

  loading.value = true
  error.value = null

  try {
    const res = await getUserByUserId(userId)
    user.value = res.data
    if (!user.value) {
      error.value = '사용자를 찾을 수 없습니다.'
      return
    }

    await fetchUserVideos(userId)
    await fetchUserBoards(userId)

    if (
      authStore.isAuthenticated &&
      authStore.userId &&
      user.value.userId !== Number(authStore.userId)
    ) {
      try {
        const followRes = await checkFollowStatus(user.value.userId)
        isFollowing.value = followRes.data
      } catch (followErr) {
        if (followErr.response?.status !== 401) {
          console.error('팔로우 상태 확인 실패:', followErr)
        }
        isFollowing.value = false
      }
    }
  } catch (err) {
    console.error('사용자 정보 조회 실패:', err)
    error.value = '사용자 정보를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

const goToMyPage = () => {
  router.push('/mypage')
}

const toggleFollowStatus = async () => {
  if (!authStore.isAuthenticated || !user.value) return

  try {
    await toggleFollow(user.value.userId)
    isFollowing.value = !isFollowing.value
  } catch (err) {
    console.error('팔로우 처리 실패:', err)
    alert('팔로우 처리 중 오류가 발생했습니다.')
  }
}

const goUserBoards = () => {
  if (!user.value) return
  router.push({ name: 'userBoards', params: { userId: user.value.userId } })
}

const goUserBoardDetail = (postId) => {
  if (!postId) return
  router.push({ name: 'boardDetail', params: { id: postId } })
}

const goUserVideos = () => {
  if (!user.value) return
  router.push({ name: 'userVideos', params: { userId: user.value.userId } })
}

onMounted(fetchUser)
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

.profile-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.profile-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.profile-card-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  overflow: hidden;
}

.profile-card-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-card-info {
  flex: 1;
}

.profile-card-name {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.profile-card-id {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.profile-self-note {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  font-size: 13px;
  color: #6b7280;
}

.goto-mypage-btn {
  margin-top: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  border: none;
  background: #2563eb;
  color: #ffffff;
  font-size: 13px;
  cursor: pointer;
}

.goto-mypage-btn:hover {
  background: #1d4ed8;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 700;
  color: #374151;
}

.profile-videos-section {
  margin-top: 24px;
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

.profile-posts-section {
  margin-top: 24px;
}

.profile-board-list {
  margin: 8px 0 0 0;
  padding: 0;
  list-style: none;
}

.profile-board-item {
  padding: 8px 0;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
}

.profile-board-item:last-child {
  border-bottom: none;
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

.profile-main-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-follow-btn {
  padding: 8px 20px;
  border-radius: 999px;
  border: none;
  background: #2563eb;
  color: #ffffff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.profile-follow-btn.following {
  background: #e5e7eb;
  color: #111827;
}

.profile-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.section-more-btn {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #374151;
  cursor: pointer;
}
.section-more-btn:hover {
  border-color: #2563eb;
  color: #2563eb;
}
</style>
