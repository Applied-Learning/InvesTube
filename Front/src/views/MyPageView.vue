<template>
  <div class="mypage-container">
    <PageHeader title="마이페이지" :showBack="false" />

    <div class="mypage-content">
      <!-- 프로필 섹션 -->
      <section class="profile-section">
        <div class="profile-header">
          <!-- 왼쪽: 아바타 + 닉네임 + 아이디 -->
          <div class="profile-main-info">
            <div class="profile-avatar-wrapper">
              <div class="profile-avatar">
                <img
                  v-if="myProfile && myProfile.profileImage"
                  :src="resolveImageUrl(myProfile.profileImage)"
                  :alt="authStore.nickname || authStore.id || '프로필'"
                />
                <span v-else>
                  {{ (authStore.nickname || authStore.id || '').charAt(0).toUpperCase() }}
                </span>
              </div>
              <button class="avatar-settings-btn" @click="openProfileEdit">⚙</button>
            </div>
            <div class="profile-info">
              <h2>{{ authStore.nickname }}</h2>
              <p class="user-id">@{{ authStore.id }}</p>
            </div>
          </div>

          <!-- 오른쪽: 팔로워 / 팔로잉 요약 -->
          <div class="profile-follow-summary" v-if="authStore.userId">
            <button class="summary-item" @click="openFollowers">
              <span class="summary-label">팔로워</span>
              <span class="summary-number">{{ followerCount }}</span>
            </button>
            <button class="summary-item" @click="openFollowings">
              <span class="summary-label">팔로잉</span>
              <span class="summary-number">{{ followingCount }}</span>
            </button>
          </div>
        </div>
      </section>

      <!-- 활동 섹션 -->
      <section class="menu-section">
        <h3 class="section-title">활동</h3>

        <div class="activity-tabs">
          <button
            class="activity-tab-btn"
            :class="{ active: activeActivityTab === 'videos' }"
            @click="activeActivityTab = 'videos'"
          >
            영상
          </button>
          <button
            class="activity-tab-btn"
            :class="{ active: activeActivityTab === 'boards' }"
            @click="activeActivityTab = 'boards'"
          >
            게시판
          </button>
        </div>

        <div v-if="activeActivityTab === 'videos'" class="activity-videos">
          <!-- 찜한 영상 -->
          <section class="activity-group">
            <div class="activity-group-header">
              <h4>찜한 영상</h4>
              <button class="activity-more-btn" @click="goAllWishedVideos">전체 보기</button>
            </div>
            <ul class="activity-list">
              <li
                v-for="video in previewWishedVideos"
                :key="'wish-' + video.videoId"
                class="activity-item"
                @click="goVideoDetail(video.videoId)"
              >
                <div class="activity-thumb">
                  <img :src="video.thumbnailUrl" :alt="video.title" />
                </div>
                <div class="activity-info">
                  <p class="activity-title">{{ video.title }}</p>
                  <p class="activity-meta">
                    조회수 {{ video.viewCount }} · 찜 {{ video.wishCount }}
                  </p>
                </div>
              </li>
              <li
                v-if="!activityLoading && previewWishedVideos.length === 0"
                class="activity-empty"
              >
                아직 찜한 영상이 없어요.
              </li>
            </ul>
          </section>

          <!-- 내가 등록한 영상 -->
          <section class="activity-group">
            <div class="activity-group-header">
              <h4>등록한 영상</h4>
              <button class="activity-more-btn" @click="goMyVideos">전체 보기</button>
            </div>
            <ul class="activity-list">
              <li
                v-for="video in previewUploadedVideos"
                :key="'upload-' + video.videoId"
                class="activity-item"
                @click="goVideoDetail(video.videoId)"
              >
                <div class="activity-thumb">
                  <img :src="video.thumbnailUrl" :alt="video.title" />
                </div>
                <div class="activity-info">
                  <p class="activity-title">{{ video.title }}</p>
                  <p class="activity-meta">
                    조회수 {{ video.viewCount }} · 찜 {{ video.wishCount }}
                  </p>
                </div>
              </li>
              <li
                v-if="!activityLoading && previewUploadedVideos.length === 0"
                class="activity-empty"
              >
                아직 등록한 영상이 없어요.
              </li>
            </ul>
          </section>

          <!-- 내가 리뷰 남긴 영상 (미리보기만) -->
          <section class="activity-group">
            <div class="activity-group-header">
              <h4>리뷰를 작성한 영상</h4>
              <button class="activity-more-btn" @click="goMyReviewVideos">전체 보기</button>
            </div>
            <ul class="activity-list">
              <li
                v-for="video in previewReviewedVideos"
                :key="'review-' + video.videoId"
                class="activity-item"
                @click="goVideoDetail(video.videoId)"
              >
                <div class="activity-thumb">
                  <img :src="video.thumbnailUrl" :alt="video.title" />
                </div>
                <div class="activity-info">
                  <p class="activity-title">{{ video.title }}</p>
                  <p class="activity-meta">
                    조회수 {{ video.viewCount }} · 찜 {{ video.wishCount }}
                  </p>
                </div>
              </li>
              <li
                v-if="!activityLoading && previewReviewedVideos.length === 0"
                class="activity-empty"
              >
                아직 리뷰를 작성한 영상이 없어요.
              </li>
            </ul>
          </section>
        </div>

        <div v-else class="activity-boards">
          <!-- 작성한 게시글 -->
          <section class="activity-group">
            <div class="activity-group-header">
              <h4>작성한 게시글</h4>
              <button class="activity-more-btn" @click="goMyBoards">전체 보기</button>
            </div>
            <ul class="activity-list">
              <li
                v-for="post in previewWrittenBoards"
                :key="'board-' + post.postId"
                class="activity-item"
                @click="goBoardDetail(post.postId)"
              >
                <div class="activity-info">
                  <p class="activity-title">{{ post.title }}</p>
                  <p class="activity-meta">
                    조회수 {{ post.viewCount }} · 댓글 {{ post.commentCount }}
                  </p>
                </div>
              </li>
              <li
                v-if="!boardActivityLoading && previewWrittenBoards.length === 0"
                class="activity-empty"
              >
                아직 작성한 게시글이 없어요.
              </li>
            </ul>
          </section>

          <!-- 내가 댓글 단 게시글 -->
          <section class="activity-group">
            <div class="activity-group-header">
              <h4>댓글 작성한 게시글</h4>
              <!-- 원하면 여기에도 전체보기 버튼 추가 가능 -->
              <button class="activity-more-btn" @click="goMyCommentedBoards">전체 보기</button>
            </div>
            <ul class="activity-list">
              <li
                v-for="post in previewCommentedBoards"
                :key="'commented-' + post.postId"
                class="activity-item"
                @click="goBoardDetail(post.postId)"
              >
                <div class="activity-info">
                  <!-- 글 제목 -->
                  <p class="activity-title">{{ post.title }}</p>
                  <!-- 내가 쓴 댓글 내용 -->
                  <p class="activity-meta">{{ post.commentContent }}</p>
                </div>
              </li>
              <li
                v-if="!boardActivityLoading && previewCommentedBoards.length === 0"
                class="activity-empty"
              >
                아직 댓글 단 게시글이 없어요.
              </li>
            </ul>
          </section>
        </div>
      </section>

      <!-- 설정 섹션 -->
      <section class="menu-section">
        <h3 class="section-title">설정</h3>
        <div class="menu-list">
          <button class="menu-item" @click="openNotificationModal">
            <span>알림 설정</span>
          </button>
          <button class="menu-item" @click="openPasswordModal">
            <span>비밀번호 변경</span>
          </button>
          <button class="menu-item menu-item--danger" @click="openDeleteModal">
            <span>회원 탈퇴</span>
          </button>
        </div>
      </section>
    </div>

    <!-- 팔로워 / 팔로잉 모달 -->
    <div v-if="showFollowModal" class="follow-modal-backdrop" @click.self="closeFollowModal">
      <div class="follow-modal">
        <div class="follow-modal-header">
          <h3>{{ activeFollowTab === 'followers' ? '팔로워' : '팔로잉' }}</h3>
          <button class="modal-close-btn" @click="closeFollowModal">×</button>
        </div>

        <div class="follow-modal-tabs">
          <button
            class="tab-btn"
            :class="{ active: activeFollowTab === 'followers' }"
            @click="activeFollowTab = 'followers'"
          >
            팔로워 {{ followerCount }}
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeFollowTab === 'followings' }"
            @click="activeFollowTab = 'followings'"
          >
            팔로잉 {{ followingCount }}
          </button>
        </div>

        <div class="follow-modal-body">
          <div v-if="followLoading" class="follow-loading">불러오는 중...</div>
          <div v-else-if="followError" class="follow-error">{{ followError }}</div>
          <ul v-else class="follow-list">
            <li
              v-for="user in activeFollowTab === 'followers' ? followerUsers : followingUsers"
              :key="user.userId + '-' + activeFollowTab"
              class="follow-item"
              @click="goToUserProfile(user.userId)"
            >
              <div class="follow-user">
                <div class="follow-avatar">
                  <img
                    v-if="user.profileImage"
                    :src="resolveImageUrl(user.profileImage)"
                    :alt="user.nickname || user.id || '프로필'"
                  />
                  <span v-else>{{ user.initial }}</span>
                </div>
                <div class="follow-user-info">
                  <p class="follow-user-name">
                    {{
                      user.nickname ||
                      (user.userId != null ? `사용자 ${user.userId}` : '알 수 없는 사용자')
                    }}
                  </p>
                  <p class="follow-user-sub" v-if="user.id">@{{ user.id }}</p>
                </div>
              </div>
            </li>
            <li
              v-if="
                (activeFollowTab === 'followers' ? followerUsers.length : followingUsers.length) ===
                0
              "
              class="follow-empty"
            >
              {{
                activeFollowTab === 'followers'
                  ? '아직 나를 팔로우한 사람이 없어요.'
                  : '아직 팔로우한 사람이 없어요.'
              }}
            </li>
          </ul>
        </div>
      </div>
    </div>
    <!-- 프로필 편집 모달 -->
    <div v-if="showProfileEditModal" class="follow-modal-backdrop" @click.self="closeProfileEdit">
      <div class="follow-modal profile-edit-modal">
        <div class="follow-modal-header">
          <h3>프로필 편집</h3>
          <button class="modal-close-btn" @click="closeProfileEdit">✕</button>
        </div>
        <div class="follow-modal-body profile-edit-body">
          <div class="form-field">
            <label>닉네임</label>
            <input v-model="editNickname" type="text" class="text-input" />
          </div>

          <div class="form-field">
            <label>프로필 이미지</label>
            <div class="profile-image-row">
              <div class="profile-image-preview">
                <img
                  v-if="previewProfileImage"
                  :src="
                    previewProfileImage.startsWith('blob:')
                      ? previewProfileImage
                      : resolveImageUrl(previewProfileImage)
                  "
                  alt="프로필 미리보기"
                />
              </div>
              <div class="profile-image-actions">
                <input type="file" accept="image/*" @change="onProfileImageChange" />
                <button type="button" class="small-btn" @click="markProfileImageDelete">
                  이미지 제거
                </button>
              </div>
            </div>
          </div>

          <div class="modal-actions">
            <button class="cancel-btn" @click="closeProfileEdit">취소</button>
            <button class="save-btn" @click="saveProfile" :disabled="savingProfile">저장</button>
          </div>
        </div>
      </div>
    </div>
    <!-- 비밀번호 변경 모달 -->
    <div v-if="showPasswordModal" class="follow-modal-backdrop" @click.self="closePasswordModal">
      <div class="follow-modal follow-modal--small">
        <div class="follow-modal-header">
          <h3>비밀번호 변경</h3>
          <button class="modal-close-btn" @click="closePasswordModal">✕</button>
        </div>
        <div class="follow-modal-body settings-modal-body">
          <div class="form-field">
            <label>새 비밀번호</label>
            <input v-model="newPassword" type="password" class="text-input settings-input" />
          </div>
          <div class="form-field">
            <label>새 비밀번호 확인</label>
            <input v-model="confirmPassword" type="password" class="text-input settings-input" />
          </div>
          <div class="settings-actions">
            <button class="cancel-btn" @click="closePasswordModal">취소</button>
            <button class="save-btn" @click="savePassword" :disabled="savingPassword">변경</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 회원 탈퇴 모달 -->
    <div v-if="showDeleteModal" class="follow-modal-backdrop" @click.self="closeDeleteModal">
      <div class="follow-modal follow-modal--small">
        <div class="follow-modal-header">
          <h3>회원 탈퇴</h3>
          <button class="modal-close-btn" @click="closeDeleteModal">✕</button>
        </div>
        <div class="follow-modal-body settings-modal-body">
          <p class="delete-warning">
            정말로 탈퇴하시겠어요?<br />
            탈퇴 시 계정과 데이터는 복구할 수 없어요.
          </p>
          <p class="delete-warning">
            아래 입력란에 <strong>탈퇴</strong> 를 입력하면 탈퇴가 진행됩니다.
          </p>
          <div class="form-field">
            <label>확인 입력</label>
            <input
              v-model="deleteConfirmText"
              type="text"
              class="text-input settings-input"
              placeholder="탈퇴"
            />
          </div>
          <div class="settings-actions">
            <button class="cancel-btn" @click="closeDeleteModal">취소</button>
            <button
              class="save-btn save-btn--danger"
              @click="confirmDelete"
              :disabled="deletingAccount"
            >
              탈퇴하기
            </button>
          </div>
        </div>
      </div>
    </div>
    <!-- 알림 설정 모달 -->
    <div
      v-if="showNotificationModal"
      class="follow-modal-backdrop"
      @click.self="closeNotificationModal"
    >
      <div class="follow-modal">
        <div class="follow-modal-header">
          <h3>알림 설정</h3>
          <button class="modal-close-btn" @click="closeNotificationModal">×</button>
        </div>

        <div class="follow-modal-body">
          <div class="notification-settings">
            <div class="notification-setting-item">
              <div class="notification-text">
                <p class="notification-title">영상 리뷰 알림</p>
                <p class="notification-desc">내 영상에 리뷰가 달리면 알려줘요</p>
              </div>
              <label class="toggle-switch">
                <input
                  type="checkbox"
                  v-model="notificationSettings.REVIEW"
                  @change="saveNotificationSettings"
                />
                <span class="toggle-track">
                  <span class="toggle-thumb"></span>
                </span>
              </label>
            </div>
            <div class="notification-setting-item">
              <div class="notification-text">
                <p class="notification-title">영상 찜 알림</p>
                <p class="notification-desc">내 영상이 찜되면 알려줘요</p>
              </div>
              <label class="toggle-switch">
                <input
                  type="checkbox"
                  v-model="notificationSettings.WISH"
                  @change="saveNotificationSettings"
                />
                <span class="toggle-track">
                  <span class="toggle-thumb"></span>
                </span>
              </label>
            </div>
            <div class="notification-setting-item">
              <div class="notification-text">
                <p class="notification-title">게시판 댓글 알림</p>
                <p class="notification-desc">내 게시글에 댓글이 달리면 알려줘요</p>
              </div>
              <label class="toggle-switch">
                <input
                  type="checkbox"
                  v-model="notificationSettings.COMMENT"
                  @change="saveNotificationSettings"
                />
                <span class="toggle-track">
                  <span class="toggle-thumb"></span>
                </span>
              </label>
            </div>
            <div class="notification-setting-item">
              <div class="notification-text">
                <p class="notification-title">팔로우 알림</p>
                <p class="notification-desc">누군가 나를 팔로우하면 알려줘요</p>
              </div>
              <label class="toggle-switch">
                <input
                  type="checkbox"
                  v-model="notificationSettings.FOLLOW"
                  @change="saveNotificationSettings"
                />
                <span class="toggle-track">
                  <span class="toggle-thumb"></span>
                </span>
              </label>
            </div>

            <p v-if="notificationSettingsError" class="notification-settings-error">
              {{ notificationSettingsError }}
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { useAuthStore } from '../stores/auth.js'
import { getFollowers, getFollowings } from '../api/follow.js'
import {
  getUserByUserId,
  getMyVideos,
  getMyReviews,
  getMyInfo,
  updateMyInfo,
  uploadProfileImage,
  deleteProfileImage,
  updatePassword,
  deleteMe,
} from '../api/user.js'
import { getWishedVideos, getVideoPreview } from '../api/video.js'
import { getBoardsByUser, getMyCommentedBoards } from '../api/board.js'
import { PREVIEW_LIMIT } from '../constants/ui.js'
import { getNotificationSettings, updateNotificationSettings } from '../api/notification.js'
import { resolveImageUrl } from '../utils/image.js'

const router = useRouter()
const authStore = useAuthStore()

// 팔로워 / 팔로잉
const followers = ref([])
const followings = ref([])
const followLoading = ref(false)
const followError = ref(null)
const userProfiles = ref({})
const showFollowModal = ref(false)
const activeFollowTab = ref('followers')

const followerUsers = computed(() =>
  followers.value.map((item) => {
    const userId = item.followerId ?? item.follower_id
    const profile = userProfiles.value[userId] || {}
    const nickname = profile.nickname
    const loginId = profile.id
    const profileImage = profile.profileImage
    const base = nickname || loginId || (userId != null ? String(userId) : '?')
    const initial = base.charAt(0).toUpperCase()
    return {
      userId,
      id: loginId,
      nickname,
      profileImage,
      initial,
    }
  }),
)

const followingUsers = computed(() =>
  followings.value.map((item) => {
    const userId = item.followingId ?? item.following_id
    const profile = userProfiles.value[userId] || {}
    const nickname = profile.nickname
    const loginId = profile.id
    const profileImage = profile.profileImage
    const base = nickname || loginId || (userId != null ? String(userId) : '?')
    const initial = base.charAt(0).toUpperCase()
    return {
      userId,
      id: loginId,
      nickname,
      profileImage,
      initial,
    }
  }),
)

const followerCount = computed(() => followerUsers.value.length)
const followingCount = computed(() => followingUsers.value.length)

const fetchFollowData = async () => {
  if (!authStore.userId) return

  followLoading.value = true
  followError.value = null

  try {
    const [followersRes, followingsRes] = await Promise.all([
      getFollowers(authStore.userId),
      getFollowings(authStore.userId),
    ])

    const followersRaw = followersRes.data || []
    const followingsRaw = followingsRes.data || []

    followers.value = followersRaw
    followings.value = followingsRaw

    const ids = new Set()
    followersRaw.forEach((item) => {
      const id = item.followerId ?? item.follower_id
      if (id != null) ids.add(id)
    })
    followingsRaw.forEach((item) => {
      const id = item.followingId ?? item.following_id
      if (id != null) ids.add(id)
    })

    const missingIds = Array.from(ids).filter((id) => !userProfiles.value[id])
    if (missingIds.length) {
      const results = await Promise.allSettled(missingIds.map((id) => getUserByUserId(id)))
      results.forEach((res) => {
        if (res.status === 'fulfilled') {
          const user = res.value.data
          if (user && user.userId != null) {
            userProfiles.value[user.userId] = user
          }
        }
      })
    }
  } catch (error) {
    console.error('팔로워/팔로잉 정보 조회 실패:', error)
    followError.value = '팔로워 정보를 불러오지 못했어요.'
  } finally {
    followLoading.value = false
  }
}

const openFollowers = () => {
  activeFollowTab.value = 'followers'
  showFollowModal.value = true
}

const openFollowings = () => {
  activeFollowTab.value = 'followings'
  showFollowModal.value = true
}

const closeFollowModal = () => {
  showFollowModal.value = false
}

const goToUserProfile = (userId) => {
  if (!userId) return
  if (authStore.userId && Number(authStore.userId) === Number(userId)) {
    router.push('/mypage')
  } else {
    router.push({ name: 'userProfile', params: { userId } })
  }
  closeFollowModal()
}

// 활동
const activeActivityTab = ref('videos')
const activityLoading = ref(false)
const previewWishedVideos = ref([])
const previewUploadedVideos = ref([])
const previewReviewedVideos = ref([])
const boardActivityLoading = ref(false)
const previewWrittenBoards = ref([])
const previewCommentedBoards = ref([])

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
  }
}

const fetchActivityPreviews = async () => {
  if (!authStore.isAuthenticated) return

  activityLoading.value = true

  try {
    // 찜한 영상
    const wishedParams = { page: 1, size: PREVIEW_LIMIT }
    const wishedRes = await getWishedVideos(wishedParams)
    const wishedList = wishedRes.data?.videos || []
    previewWishedVideos.value = wishedList.map(normalizeVideo).filter(Boolean)

    // 내가 등록한 영상
    const myVideosRes = await getMyVideos()
    const myVideosList = myVideosRes.data || []
    previewUploadedVideos.value = myVideosList
      .slice(0, PREVIEW_LIMIT)
      .map(normalizeVideo)
      .filter(Boolean)

    // 내가 리뷰 남긴 영상 (리뷰 -> 비디오 조회)
    const myReviewsRes = await getMyReviews()
    const reviews = myReviewsRes.data || []
    const videoIdSet = new Set()
    const orderedIds = []
    for (const r of reviews) {
      const vid = r.videoId ?? r.video_id
      if (vid != null && !videoIdSet.has(vid)) {
        videoIdSet.add(vid)
        orderedIds.push(vid)
      }
      if (orderedIds.length >= PREVIEW_LIMIT) break
    }

    const videoResults = await Promise.allSettled(orderedIds.map((id) => getVideoPreview(id)))
    const reviewedVideos = []
    videoResults.forEach((res) => {
      if (res.status === 'fulfilled') {
        const v = normalizeVideo(res.value.data)
        if (v) reviewedVideos.push(v)
      }
    })
    previewReviewedVideos.value = reviewedVideos
  } catch (err) {
    console.error('활동(영상) 정보 조회 실패:', err)
  } finally {
    activityLoading.value = false
  }
}

import { formatKSTDate } from '../utils/date.js'

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

// 댓글 단 게시글용 (백엔드에서 postTitle + commentContent 내려준다고 가정)
const normalizeCommentedPost = (item) => {
  if (!item) return null
  return {
    postId: item.postId,
    title: item.postTitle, // 글 제목
    commentContent: item.content, // 내가 단 댓글 내용
    createdAtDisplay: item.createdAt ? formatKSTDate(item.createdAt) : '',
  }
}

const fetchBoardActivity = async () => {
  if (!authStore.isAuthenticated || !authStore.userId) return

  boardActivityLoading.value = true
  try {
    // 작성한 게시글
    const res = await getBoardsByUser(authStore.userId, 0, PREVIEW_LIMIT)
    const posts = res.data?.posts || res.data || []
    previewWrittenBoards.value = posts.map(normalizeBoardPost).filter(Boolean)

    // 내가 댓글 단 게시글
    const commentedRes = await getMyCommentedBoards(PREVIEW_LIMIT)
    const commentedList = commentedRes.data || []
    previewCommentedBoards.value = commentedList.map(normalizeCommentedPost).filter(Boolean)
  } catch (err) {
    console.error('활동(게시판) 정보 조회 실패:', err)
    if (!previewWrittenBoards.value.length) previewWrittenBoards.value = []
    if (!previewCommentedBoards.value.length) previewCommentedBoards.value = []
  } finally {
    boardActivityLoading.value = false
  }
}

const goAllWishedVideos = () => {
  router.push({ name: 'myWishedVideos' })
}

const goMyVideos = () => {
  router.push({ name: 'myVideos' })
}

const goMyReviewVideos = () => {
  router.push({ name: 'myReviewVideos' })
}

const goVideoDetail = (videoId) => {
  if (!videoId) return
  router.push({ name: 'videoDetail', params: { id: videoId } })
}

const goBoardDetail = (postId) => {
  if (!postId) return
  router.push({ name: 'boardDetail', params: { id: postId } })
}

const goMyBoards = () => {
  router.push({ name: 'myBoards' })
}

const goMyCommentedBoards = () => {
  router.push({ name: 'myCommentedBoards' })
}

// 프로필 편집 모달 관련 상태
const notificationSettings = ref({
  COMMENT: true,
  REVIEW: true,
  FOLLOW: true,
  WISH: true,
})
const notificationSettingsError = ref('')
const showNotificationModal = ref(false)

const openNotificationModal = () => {
  showNotificationModal.value = true
}

const closeNotificationModal = () => {
  showNotificationModal.value = false
}

const loadNotificationSettings = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const res = await getNotificationSettings()
    const data = res.data || {}
    notificationSettings.value = {
      COMMENT: data.COMMENT ?? true,
      REVIEW: data.REVIEW ?? true,
      FOLLOW: data.FOLLOW ?? true,
      WISH: data.WISH ?? true,
    }
    notificationSettingsError.value = ''
  } catch (err) {
    console.error('알림 설정 조회 실패:', err)
    notificationSettingsError.value = '알림 설정을 불러오는 중 오류가 발생했습니다.'
  }
}

const saveNotificationSettings = async () => {
  if (!authStore.isAuthenticated) return
  try {
    await updateNotificationSettings(notificationSettings.value)
    notificationSettingsError.value = ''
  } catch (err) {
    console.error('알림 설정 저장 실패:', err)
    notificationSettingsError.value = '알림 설정 저장 중 오류가 발생했습니다.'
  }
}

const myProfile = ref(null)

const showProfileEditModal = ref(false)
const editNickname = ref('')
const previewProfileImage = ref('')
const profileImageFile = ref(null)
const deleteProfileImageFlag = ref(false)
const savingProfile = ref(false)

const fetchMyProfile = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const res = await getMyInfo()
    myProfile.value = res.data
    if (myProfile.value?.profileImage) {
      previewProfileImage.value = myProfile.value.profileImage
    }
  } catch (err) {
    console.error('내 프로필 조회 실패:', err)
  }
}

const openProfileEdit = () => {
  editNickname.value = myProfile.value?.nickname || authStore.nickname || ''
  previewProfileImage.value = myProfile.value?.profileImage || ''
  profileImageFile.value = null
  deleteProfileImageFlag.value = false
  showProfileEditModal.value = true
}

const closeProfileEdit = () => {
  showProfileEditModal.value = false
}

const onProfileImageChange = (e) => {
  const file = e.target.files[0]
  if (!file) return
  profileImageFile.value = file
  deleteProfileImageFlag.value = false
  previewProfileImage.value = URL.createObjectURL(file)
}

const markProfileImageDelete = () => {
  deleteProfileImageFlag.value = true
  profileImageFile.value = null
  previewProfileImage.value = ''
}

const saveProfile = async () => {
  if (!authStore.isAuthenticated) return
  savingProfile.value = true

  try {
    // 닉네임 변경
    if (editNickname.value && editNickname.value !== authStore.nickname) {
      await updateMyInfo({ nickname: editNickname.value })
      authStore.nickname = editNickname.value
      localStorage.setItem('nickname', editNickname.value)
    }

    // 이미지 삭제
    if (deleteProfileImageFlag.value) {
      await deleteProfileImage()
    }

    // 새 이미지 업로드
    if (profileImageFile.value) {
      await uploadProfileImage(profileImageFile.value)
    }

    await fetchMyProfile()
    // notify header and other components to refresh profile
    try {
      window.dispatchEvent(new CustomEvent('profile-updated'))
    } catch (e) {
      console.warn('profile-updated event dispatch failed', e)
    }
    alert('프로필이 저장되었습니다.')
    closeProfileEdit()
  } catch (err) {
    console.error('프로필 저장 실패:', err)
    alert('프로필 저장에 실패했어요.')
  } finally {
    savingProfile.value = false
  }
}

// 비밀번호 변경 모달 상태
const showPasswordModal = ref(false)
const newPassword = ref('')
const confirmPassword = ref('')
const savingPassword = ref(false)

const openPasswordModal = () => {
  newPassword.value = ''
  confirmPassword.value = ''
  showPasswordModal.value = true
}

const closePasswordModal = () => {
  showPasswordModal.value = false
}

const savePassword = async () => {
  if (!newPassword.value) {
    alert('새 비밀번호를 입력하세요.')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    alert('비밀번호 확인이 일치하지 않습니다.')
    return
  }

  savingPassword.value = true
  try {
    await updatePassword(newPassword.value)
    alert('비밀번호가 변경되었습니다.')
    closePasswordModal()
  } catch (err) {
    console.error('비밀번호 변경 실패:', err)
    alert('비밀번호 변경에 실패했어요.')
  } finally {
    savingPassword.value = false
  }
}

const showDeleteModal = ref(false)
const deleteConfirmText = ref('')
const deletingAccount = ref(false)

const openDeleteModal = () => {
  deleteConfirmText.value = ''
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
}

const confirmDelete = async () => {
  if (deleteConfirmText.value.trim() !== '탈퇴') {
    alert("확인 입력란에 '탈퇴'를 정확히 입력해야 합니다.")
    return
  }

  if (!window.confirm('정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    return
  }

  deletingAccount.value = true
  try {
    await deleteMe()
    alert('회원 탈퇴가 완료되었어요.')
    authStore.logout()
    router.push('/')
  } catch (err) {
    console.error('회원 탈퇴 실패:', err)
    alert('회원 탈퇴에 실패했어요.')
  } finally {
    deletingAccount.value = false
  }
}

onMounted(() => {
  fetchMyProfile()
  fetchFollowData()
  fetchActivityPreviews()
  fetchBoardActivity()
  loadNotificationSettings()
})
</script>

<style scoped>
.mypage-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.mypage-content {
  margin-top: 20px;
}

.profile-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.profile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.profile-main-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
}

.profile-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.user-id {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.profile-follow-summary {
  display: flex;
  gap: 16px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4px 0;
  border: none;
  background: transparent;
  min-width: 80px;
  cursor: pointer;
}

.summary-item:hover .summary-label {
  color: #4b5563;
}

.summary-item:hover .summary-number {
  color: #111827;
}

.summary-label {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 2px;
}

.summary-number {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.menu-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 700;
  color: #374151;
}

.menu-list {
  display: flex;
  flex-direction: column;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  background: none;
  border: none;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
  text-align: left;
  font-size: 14px;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 활동 탭 */
.activity-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.activity-tab-btn {
  flex: 1;
  padding: 8px 0;
  border-radius: 999px;
  border: none;
  background: #f3f4f6;
  font-size: 13px;
  cursor: pointer;
  color: #6b7280;
}

.activity-tab-btn.active {
  background: #2563eb;
  color: #ffffff;
}

.activity-group {
  margin-bottom: 20px;
}

.activity-group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.activity-group-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.activity-more-btn {
  border: none;
  background: none;
  font-size: 12px;
  color: #2563eb;
  cursor: pointer;
}

.activity-more-btn:disabled {
  color: #9ca3af;
  cursor: default;
}

.activity-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.activity-item + .activity-item {
  border-top: 1px solid #f3f4f6;
}

.activity-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  cursor: pointer;
}

.activity-item:hover .activity-title {
  text-decoration: underline;
}

.activity-thumb {
  width: 96px;
  height: 54px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #e5e7eb;
}

.activity-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-info {
  flex: 1;
}

.activity-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.activity-meta {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.activity-empty {
  padding: 8px 0;
  font-size: 13px;
  color: #9ca3af;
}

.activity-placeholder {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #6b7280;
}

/* 팔로워 / 팔로잉 모달 스타일 */
.follow-modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.follow-modal {
  width: 100%;
  max-width: 420px;
  max-height: 80vh;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.follow-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.follow-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.modal-close-btn {
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
  color: #9ca3af;
}

.modal-close-btn:hover {
  color: #4b5563;
}

.follow-modal-tabs {
  display: flex;
  padding: 8px;
  gap: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.tab-btn {
  flex: 1;
  padding: 8px 0;
  border: none;
  border-radius: 999px;
  background: #f3f4f6;
  font-size: 13px;
  cursor: pointer;
  color: #6b7280;
}

.tab-btn.active {
  background: #2563eb;
  color: #ffffff;
}

.follow-modal-body {
  padding: 12px 16px 16px;
  overflow-y: auto;
}

.notification-settings {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #f3f4f6;
  border-radius: 12px;
  background: #f9fafb;
}

.notification-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.notification-desc {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.toggle-switch {
  position: relative;
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.toggle-switch input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-track {
  width: 42px;
  height: 24px;
  background: #e5e7eb;
  border-radius: 999px;
  position: relative;
  transition: background 0.2s;
  display: inline-flex;
  align-items: center;
}

.toggle-thumb {
  width: 18px;
  height: 18px;
  background: #ffffff;
  border-radius: 50%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  transform: translateX(3px);
  transition: transform 0.2s;
}

.toggle-switch input:checked + .toggle-track {
  background: #2563eb;
}

.toggle-switch input:checked + .toggle-track .toggle-thumb {
  transform: translateX(21px);
}

.follow-loading,
.follow-error,
.follow-empty {
  padding: 24px 8px;
  text-align: center;
  font-size: 14px;
  color: #6b7280;
}

.follow-error {
  color: #b91c1c;
}

.follow-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.follow-item + .follow-item {
  border-top: 1px solid #f3f4f6;
}

.follow-item {
  padding: 12px 0;
  cursor: pointer;
}

.follow-item:hover {
  background: #f9fafb;
}

.follow-user {
  display: flex;
  align-items: center;
  gap: 12px;
}

.follow-avatar {
  width: 36px;
  height: 36px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  overflow: hidden;
}

.follow-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 999px;
}

.follow-user-info {
  flex: 1;
}

.follow-user-name {
  margin: 0 0 2px 0;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.follow-user-sub {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.profile-avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
}

.profile-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  overflow: hidden;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-settings-btn {
  position: absolute;
  right: -4px;
  bottom: -4px;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  border: none;
  background: #ffffff;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  cursor: pointer;
}

/* 프로필 편집 모달 공통 폼 스타일 */
.profile-edit-body .form-field {
  margin-bottom: 12px;
}

.text-input {
  width: 100%;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  font-size: 14px;
}

.profile-image-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile-image-preview {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  background: #e5e7eb;
  flex-shrink: 0;
}

.profile-image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-image-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.small-btn {
  padding: 4px 8px;
  font-size: 12px;
  border-radius: 6px;
  border: 1px solid #d1d5db;
  background: #ffffff;
  cursor: pointer;
}

.modal-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.cancel-btn,
.save-btn {
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn {
  background: #e5e7eb;
  color: #111827;
}

.save-btn {
  background: #2563eb;
  color: #ffffff;
}

.follow-modal--small {
  max-width: 360px;
  max-height: none;
}

.follow-modal--small .follow-modal-body {
  max-height: none;
  overflow-y: visible;
}
.password-modal-body {
  max-width: 340px;
  margin: 0 auto;
}

.password-modal-body .text-input {
  width: 100%;
  max-width: 340px;
}

.menu-item--danger {
  color: #b91c1c;
}

.save-btn--danger {
  background: #b91c1c;
}

.save-btn--danger:hover {
  background: #991b1b;
}

.delete-warning {
  font-size: 14px;
  color: #374151;
  margin: 0 0 8px 0;
}

.input-narrow {
  width: 220px;
  max-width: 220px;
  display: block;
  margin: 0 auto;
}

.follow-modal--small {
  max-width: 360px;
}

/* 비밀번호 / 탈퇴 공통 레이아웃 */
.settings-modal-body {
  max-width: 300px;
  margin: 0 auto;
}

.settings-input {
  width: 100%;
  max-width: 300px;
  display: block;
}

.settings-actions {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  gap: 8px;
}
</style>
