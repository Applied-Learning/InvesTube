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
          <div class="header-actions">
            <button class="wish-btn" :class="{ active: isWished }" @click="toggleWish">
              <svg
                width="24"
                height="24"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M12 21L10.55 19.7051C5.4 15.1242 2 12.1029 2 8.39509C2 5.37384 4.42 3 7.5 3C9.24 3 10.91 3.81 12 5.08651C13.09 3.81 14.76 3 16.5 3C19.58 3 22 5.37384 22 8.39509C22 12.1029 18.6 15.1242 13.45 19.7051L12 21Z"
                  :fill="isWished ? 'currentColor' : 'none'"
                  stroke="currentColor"
                  stroke-width="2"
                />
              </svg>
            </button>
            <button
              v-if="isMyVideo"
              class="edit-video-btn"
              :class="{ active: editMode }"
              @click="editMode ? cancelEditVideo() : startEditVideo()"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M12 20h9"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                />
                <path
                  d="M16.5 3.5C17.3284 2.67157 18.6716 2.67157 19.5 3.5C20.3284 4.32843 20.3284 5.67157 19.5 6.5L8 18L4 19L5 15L16.5 3.5Z"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <span>{{ editMode ? '취소' : '수정' }}</span>
            </button>
            <button v-if="isMyVideo" class="delete-video-btn" @click="removeVideo">
              <svg
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2M10 11v6M14 11v6"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
              <span>영상 삭제</span>
            </button>
          </div>
        </div>

        <!-- 업로더 정보 -->
        <div class="uploader-section">
          <div class="uploader-info" @click="goUploaderProfile">
            <div class="uploader-avatar">
              <img
                v-if="video.uploaderProfileImage"
                :src="resolveImageUrl(video.uploaderProfileImage)"
                :alt="video.uploaderNickname"
              />
              <div v-else class="avatar-fallback">{{ uploaderInitial }}</div>
            </div>
            <span class="uploader-name">{{ uploaderDisplayName }}</span>
          </div>
          <button
            v-if="!isMyVideo && authStore.isAuthenticated"
            class="follow-btn"
            :class="{ following: isFollowing }"
            @click="toggleFollowStatus"
          >
            {{ isFollowing ? '팔로잉' : '팔로우' }}
          </button>
        </div>

        <div class="video-meta">
          <span class="meta-item">
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M8 14.6667C11.6819 14.6667 14.6667 11.6819 14.6667 8C14.6667 4.3181 11.6819 1.33333 8 1.33333C4.3181 1.33333 1.33333 4.3181 1.33333 8C1.33333 11.6819 4.3181 14.6667 8 14.6667Z"
                stroke="currentColor"
                stroke-width="1.5"
              />
              <path
                d="M8 4V8L10.6667 9.33333"
                stroke="currentColor"
                stroke-width="1.5"
                stroke-linecap="round"
              />
            </svg>
            조회수 {{ video.viewCount.toLocaleString() }}회
          </span>
          <span class="meta-item">
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M8 14L6.82 12.9533C3.4 10.06 1.33333 8.21333 1.33333 5.93333C1.33333 4.2 2.63333 2.9 4.36667 2.9C5.45333 2.9 6.5 3.44 7.16667 4.27333C7.83333 3.44 8.88 2.9 9.96667 2.9C11.7 2.9 13 4.2 13 5.93333C13 8.21333 10.9333 10.06 7.51333 12.96L8 14Z"
                stroke="currentColor"
                stroke-width="1.5"
              />
            </svg>
            {{ video.wishCount }}
          </span>
          <span class="meta-item">
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M8 1.33333L10.06 5.50667L14.6667 6.18L11.3333 9.42667L12.12 14.0133L8 11.8467L3.88 14.0133L4.66667 9.42667L1.33333 6.18L5.94 5.50667L8 1.33333Z"
                stroke="currentColor"
                stroke-width="1.5"
              />
            </svg>
            {{ video.avgRating ? video.avgRating.toFixed(1) : 'N/A' }} ({{ video.reviewCount }}개)
          </span>
          <span class="meta-item">{{ formatDate(video.createdAt) }}</span>
        </div>

        <div v-if="editMode" class="video-edit-form">
          <div class="form-row">
            <label for="editTitle" class="required">제목</label>
            <input
              id="editTitle"
              v-model="editForm.title"
              type="text"
              maxlength="200"
              placeholder="제목을 입력하세요"
            />
          </div>
          <div class="form-row">
            <label for="editDescription" class="required">설명</label>
            <textarea
              id="editDescription"
              v-model="editForm.description"
              rows="4"
              maxlength="1000"
              placeholder="설명을 입력하세요"
            ></textarea>
          </div>
          <div class="form-row">
            <label for="editCategory" class="required">카테고리</label>
            <select id="editCategory" v-model="editForm.categoryId">
              <option value="" disabled>카테고리를 선택하세요</option>
              <option v-for="(name, id) in categories" :key="id" :value="Number(id)">
                {{ name }}
              </option>
            </select>
          </div>
          <div class="form-row">
            <label for="editYoutubeId" class="required">YouTube 영상 ID</label>
            <input
              id="editYoutubeId"
              v-model="editForm.youtubeVideoId"
              type="text"
              placeholder="예) dQw4w9WgXcQ"
            />
          </div>
          <div class="form-row">
            <label for="editThumb">썸네일 URL (선택)</label>
            <input
              id="editThumb"
              v-model="editForm.thumbnailUrl"
              type="url"
              placeholder="https://example.com/thumb.jpg"
            />
          </div>
          <div class="edit-actions">
            <button class="save-btn" :disabled="editLoading" @click="submitEditVideo">
              {{ editLoading ? '수정 중...' : '저장' }}
            </button>
            <button class="cancel-btn" :disabled="editLoading" @click="cancelEditVideo">
              취소
            </button>
          </div>
        </div>
        <template v-else>
          <div class="video-description">
            <h3>설명</h3>
            <p>{{ video.description || '설명이 없습니다.' }}</p>
          </div>

          <!-- 카테고리 -->
          <div class="video-category">
            <span class="category-badge">{{ getCategoryName(video.categoryId) }}</span>
          </div>
        </template>
      </div>

      <!-- 리뷰 섹션 -->
      <div class="reviews-section">
        <h2>리뷰 ({{ reviews.length }})</h2>

        <!-- 리뷰 작성 폼 -->
        <div v-if="authStore.isAuthenticated" class="review-form">
          <h3>리뷰 작성하기</h3>
          <div class="rating-input">
            <label>평점:</label>
            <div class="star-rating">
              <button
                v-for="star in 5"
                :key="star"
                type="button"
                class="star-btn"
                :class="{ active: star <= newReview.rating }"
                @click="newReview.rating = star"
              >
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                    :fill="star <= newReview.rating ? 'currentColor' : 'none'"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                </svg>
              </button>
              <span class="rating-value">{{ newReview.rating }}</span>
            </div>
          </div>
          <textarea
            v-model="newReview.content"
            placeholder="이 영상에 대한 평가를 남겨주세요..."
            rows="4"
            class="review-textarea"
          ></textarea>
          <button @click="submitReview" class="submit-btn">리뷰 등록</button>
        </div>
        <div v-else class="login-prompt">
          <p>리뷰를 작성하려면 로그인이 필요합니다.</p>
        </div>

        <!-- 리뷰 목록 -->
        <div v-if="reviewsLoading" class="loading">리뷰 로딩 중...</div>
        <div v-else-if="reviews.length === 0" class="no-reviews">아직 작성된 리뷰가 없습니다.</div>
        <div v-else class="reviews-list">
          <div v-for="review in reviews" :key="review.reviewId" class="review-item">
            <!-- 수정 모드 -->
            <div
              v-if="editingReview && editingReview.reviewId === review.reviewId"
              class="review-edit"
            >
              <div class="rating-input">
                <label>평점:</label>
                <div class="star-rating">
                  <button
                    v-for="star in 5"
                    :key="star"
                    type="button"
                    class="star-btn"
                    :class="{ active: star <= editingReview.rating }"
                    @click="editingReview.rating = star"
                  >
                    <svg
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <path
                        d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                        :fill="star <= editingReview.rating ? 'currentColor' : 'none'"
                        stroke="currentColor"
                        stroke-width="2"
                      />
                    </svg>
                  </button>
                  <span class="rating-value">{{ editingReview.rating }}</span>
                </div>
              </div>
              <textarea v-model="editingReview.content" rows="4" class="review-textarea"></textarea>
              <div class="edit-actions">
                <button @click="submitEditReview" class="save-btn">저장</button>
                <button @click="cancelEditReview" class="cancel-btn">취소</button>
              </div>
            </div>

            <!-- 일반 표시 모드 -->
            <div v-else class="review-content">
              <div class="review-header">
                <div class="review-author">
                  <span class="author-name">{{
                    review.nickname || '사용자 ' + review.userId
                  }}</span>
                  <div class="review-rating">
                    <template v-for="star in 5" :key="star">
                      <svg
                        v-if="star <= Math.floor(review.rating)"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                          stroke="currentColor"
                          stroke-width="2"
                        />
                      </svg>
                      <svg
                        v-else-if="star === Math.ceil(review.rating) && review.rating % 1 !== 0"
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <defs>
                          <linearGradient :id="`half-${review.reviewId}-${star}`">
                            <stop offset="50%" stop-color="currentColor" />
                            <stop offset="50%" stop-color="transparent" />
                          </linearGradient>
                        </defs>
                        <path
                          d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                          :fill="`url(#half-${review.reviewId}-${star})`"
                          stroke="currentColor"
                          stroke-width="2"
                        />
                      </svg>
                      <svg
                        v-else
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                          stroke="currentColor"
                          stroke-width="2"
                        />
                      </svg>
                    </template>
                    <span class="rating-text">{{ review.rating }}</span>
                  </div>
                </div>
                <div class="review-meta">
                  <span class="review-date">{{ formatDate(review.createdAt) }}</span>
                  <div v-if="isMyReview(review)" class="review-actions">
                    <button @click="startEditReview(review)" class="action-btn edit-btn">
                      수정
                    </button>
                    <button @click="removeReview(review.reviewId)" class="action-btn delete-btn">
                      삭제
                    </button>
                  </div>
                </div>
              </div>
              <p class="review-text">{{ review.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import {
  getVideoDetail,
  checkWishStatus,
  toggleVideoWish,
  deleteVideo,
  updateVideo,
} from '../api/video.js'
import { getReviewsByVideoId, createReview, updateReview, deleteReview } from '../api/review.js'
import { checkFollowStatus, toggleFollow } from '../api/follow.js'
import { useAuthStore } from '../stores/auth.js'
import { resolveImageUrl } from '../utils/image.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const video = ref(null)
const loading = ref(false)
const error = ref(null)
const isWished = ref(false)
const isFollowing = ref(false)
const editMode = ref(false)
const editLoading = ref(false)
const editForm = ref({
  youtubeVideoId: '',
  title: '',
  description: '',
  categoryId: '',
  thumbnailUrl: '',
})

// 리뷰 관련 상태
const reviews = ref([])
const reviewsLoading = ref(false)
const newReview = ref({ content: '', rating: 5 })
const editingReview = ref(null)

const categories = {
  1: '금융',
  2: '기술',
  3: '투자',
}

const getCategoryName = (id) => {
  return categories[id] || '기타'
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

const goUploaderProfile = () => {
  if (!video.value || !video.value.userId) return
  const uploaderId = video.value.userId
  if (authStore.userId && parseInt(authStore.userId) === uploaderId) {
    router.push('/mypage')
  } else {
    router.push({ name: 'userProfile', params: { userId: uploaderId } })
  }
}

const uploaderDisplayName = computed(() => {
  if (!video.value) return ''
  return video.value.uploaderNickname || video.value.userId.toString()
})

const uploaderInitial = computed(() => {
  if (!video.value) return '?'
  if (video.value.uploaderNickname) {
    return video.value.uploaderNickname.trim().charAt(0).toUpperCase()
  }
  return video.value.userId
})

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

      // 팔로우 상태 확인 (본인이 아닌 경우만)
      if (video.value && video.value.userId !== parseInt(authStore.userId)) {
        try {
          const followResponse = await checkFollowStatus(video.value.userId)
          isFollowing.value = followResponse.data
        } catch (followErr) {
          if (followErr.response?.status !== 401) {
            console.error('팔로우 상태 확인 실패:', followErr)
          }
          isFollowing.value = false
        }
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

// 본인이 올린 영상인지 확인
const isMyVideo = computed(() => {
  return authStore.userId && video.value && video.value.userId === parseInt(authStore.userId)
})

// 영상 수정 모드 진입
const startEditVideo = () => {
  if (!video.value) return
  editForm.value = {
    youtubeVideoId: video.value.youtubeVideoId || '',
    title: video.value.title || '',
    description: video.value.description || '',
    categoryId: video.value.categoryId || '',
    thumbnailUrl: video.value.thumbnailUrl || '',
  }
  editMode.value = true
}

const cancelEditVideo = () => {
  editMode.value = false
}

const submitEditVideo = async () => {
  if (!isMyVideo.value) return
  if (!editForm.value.title.trim()) {
    alert('제목을 입력해주세요.')
    return
  }
  if (!editForm.value.description.trim()) {
    alert('설명을 입력해주세요.')
    return
  }
  if (!editForm.value.youtubeVideoId.trim()) {
    alert('YouTube 영상 ID를 입력해주세요.')
    return
  }

  editLoading.value = true
  try {
    const payload = {
      ...editForm.value,
      categoryId: editForm.value.categoryId ? Number(editForm.value.categoryId) : null,
    }
    await updateVideo(route.params.id, payload)
    await fetchVideoDetail()
    editMode.value = false
    alert('영상이 수정되었습니다.')
  } catch (err) {
    console.error('영상 수정 실패:', err)
    alert(err.response?.data?.message || '영상 수정에 실패했습니다.')
  } finally {
    editLoading.value = false
  }
}

// 영상 삭제
const removeVideo = async () => {
  if (!confirm('정말 이 영상을 삭제하시겠습니까?\n삭제된 영상은 복구할 수 없습니다.')) {
    return
  }

  try {
    await deleteVideo(video.value.videoId)
    alert('영상이 삭제되었습니다.')
    router.push('/videos')
  } catch (err) {
    console.error('영상 삭제 실패:', err)
    if (err.response?.status === 403) {
      alert('본인이 올린 영상만 삭제할 수 있습니다.')
    } else {
      alert('영상 삭제에 실패했습니다.')
    }
  }
}

// 팔로우 토글
const toggleFollowStatus = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다.')
    return
  }

  try {
    await toggleFollow(video.value.userId)
    isFollowing.value = !isFollowing.value
  } catch (err) {
    console.error('팔로우 토글 실패:', err)
    alert('팔로우 처리에 실패했습니다.')
  }
}

// 리뷰 목록 조회
const fetchReviews = async () => {
  reviewsLoading.value = true
  try {
    const response = await getReviewsByVideoId(route.params.id)
    reviews.value = response.data
  } catch (err) {
    console.error('리뷰 목록 조회 실패:', err)
  } finally {
    reviewsLoading.value = false
  }
}

// 리뷰 작성
const submitReview = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다.')
    return
  }

  if (!newReview.value.content.trim()) {
    alert('리뷰 내용을 입력해주세요.')
    return
  }

  try {
    await createReview(route.params.id, newReview.value)
    newReview.value = { content: '', rating: 5 }
    await fetchReviews()
    await fetchVideoDetail()
    alert('리뷰가 작성되었습니다.')
  } catch (err) {
    console.error('리뷰 작성 실패:', err)
    alert('리뷰 작성에 실패했습니다.')
  }
}

// 리뷰 수정 모드
const startEditReview = (review) => {
  editingReview.value = { ...review }
}

// 리뷰 수정 취소
const cancelEditReview = () => {
  editingReview.value = null
}

// 리뷰 수정 제출
const submitEditReview = async () => {
  if (!editingReview.value.content.trim()) {
    alert('리뷰 내용을 입력해주세요.')
    return
  }

  try {
    await updateReview(route.params.id, editingReview.value.reviewId, {
      content: editingReview.value.content,
      rating: editingReview.value.rating,
    })
    editingReview.value = null
    await fetchReviews()
    await fetchVideoDetail()
    alert('리뷰가 수정되었습니다.')
  } catch (err) {
    console.error('리뷰 수정 실패:', err)
    alert('리뷰 수정에 실패했습니다.')
  }
}

// 리뷰 삭제
const removeReview = async (reviewId) => {
  if (!confirm('정말 이 리뷰를 삭제하시겠습니까?')) {
    return
  }

  try {
    await deleteReview(route.params.id, reviewId)
    await fetchReviews()
    await fetchVideoDetail()
    alert('리뷰가 삭제되었습니다.')
  } catch (err) {
    console.error('리뷰 삭제 실패:', err)
    alert('리뷰 삭제에 실패했습니다.')
  }
}

// 본인이 작성한 리뷰인지 확인
const isMyReview = (review) => {
  return authStore.userId && review.userId === parseInt(authStore.userId)
}

onMounted(() => {
  fetchVideoDetail()
  fetchReviews()
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

.header-actions {
  display: flex;
  gap: 8px;
}

.wish-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 10px;
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
  width: 22px;
  height: 22px;
}

.delete-video-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  color: #dc2626;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 600;
}

.delete-video-btn:hover {
  background: #fef2f2;
  border-color: #dc2626;
}

.delete-video-btn svg {
  width: 20px;
  height: 20px;
}

.edit-video-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: white;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  color: #2563eb;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 600;
}

.edit-video-btn:hover {
  background: #eff6ff;
  border-color: #2563eb;
}

.edit-video-btn.active {
  background: #dbeafe;
  border-color: #1d4ed8;
  color: #1d4ed8;
}

.edit-video-btn svg {
  width: 20px;
  height: 20px;
}

.uploader-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 16px;
}

.uploader-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.uploader-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
}

.uploader-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 20px;
  font-weight: 700;
}

.uploader-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.follow-btn {
  padding: 8px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.follow-btn:hover {
  background: #1d4ed8;
}

.follow-btn.following {
  background: white;
  color: #374151;
  border: 2px solid #e5e7eb;
}

.follow-btn.following:hover {
  background: #f3f4f6;
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

.video-edit-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-top: 12px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.form-row label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.form-row input,
.form-row textarea,
.form-row select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-row input:focus,
.form-row textarea:focus,
.form-row select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.reviews-section {
  padding: 24px;
  border-top: 1px solid #e5e7eb;
}

.reviews-section h2 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.reviews-section h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #374151;
}

/* 리뷰 작성 폼 */
.review-form {
  background: #f9fafb;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 32px;
}

.rating-input {
  margin-bottom: 16px;
}

.rating-input label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.star-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.star-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  color: #d1d5db;
  transition: color 0.2s;
}

.star-btn.active {
  color: #fbbf24;
}

.star-btn:hover {
  color: #fbbf24;
}

.rating-value {
  margin-left: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.review-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s;
  box-sizing: border-box;
  margin-bottom: 12px;
}

.review-textarea:focus {
  outline: none;
  border-color: #2563eb;
}

.submit-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover {
  background: #1d4ed8;
}

.login-prompt {
  background: #f9fafb;
  padding: 32px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 32px;
}

.login-prompt p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

/* 리뷰 목록 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background: #f9fafb;
  padding: 20px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 12px;
}

.review-author {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.author-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.review-rating {
  display: flex;
  align-items: center;
  gap: 2px;
  color: #fbbf24;
}

.rating-text {
  margin-left: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.review-date {
  font-size: 13px;
  color: #6b7280;
}

.review-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  background: none;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.edit-btn {
  color: #2563eb;
}

.edit-btn:hover {
  background: #dbeafe;
}

.delete-btn {
  color: #dc2626;
}

.delete-btn:hover {
  background: #fee2e2;
}

.review-text {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 리뷰 수정 모드 */
.review-edit {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.edit-actions {
  display: flex;
  gap: 8px;
}

.save-btn {
  background: #2563eb;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.save-btn:hover {
  background: #1d4ed8;
}

.cancel-btn {
  background: #6b7280;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.cancel-btn:hover {
  background: #4b5563;
}

.no-reviews {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}
</style>
