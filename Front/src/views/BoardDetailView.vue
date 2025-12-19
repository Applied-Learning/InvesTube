<template>
  <Container>
    <div v-if="loading" class="loading">로딩 중...</div>
    <div v-else-if="error" class="error-container">
      <p class="error-message">{{ error }}</p>
      <button @click="goToList" class="back-btn">목록으로</button>
    </div>
    <div v-else-if="post" class="detail-container">
      <!-- 게시글 헤더 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div v-if="isMyPost" class="post-actions">
          <button @click="editPost" class="edit-btn">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <path
                d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            수정
          </button>
          <button @click="confirmDelete" class="delete-btn">
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            삭제
          </button>
        </div>
      </div>

      <!-- 메타 정보 -->
      <div class="post-meta">
        <div class="author-section" @click="goAuthorProfile">
          <div class="author-avatar">
            <img
              v-if="post.authorProfileImage"
              :src="resolveImageUrl(post.authorProfileImage)"
              :alt="post.authorNickname"
            />
            <div v-else class="avatar-fallback">
              {{ getAuthorInitial(post.authorNickname) }}
            </div>
          </div>
          <div class="author-details">
            <span class="author-name">{{ post.authorNickname || '익명' }}</span>
            <div class="meta-info">
              <span class="meta-item">
                <svg
                  width="14"
                  height="14"
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
                {{ formatDate(post.createdAt) }}
              </span>
              <span class="meta-divider">·</span>
              <span class="meta-item">
                <svg
                  width="14"
                  height="14"
                  viewBox="0 0 16 16"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M1.33333 8C1.33333 8 3.33333 3.33333 8 3.33333C12.6667 3.33333 14.6667 8 14.6667 8C14.6667 8 12.6667 12.6667 8 12.6667C3.33333 12.6667 1.33333 8 1.33333 8Z"
                    stroke="currentColor"
                    stroke-width="1.5"
                  />
                  <path
                    d="M8 10C9.10457 10 10 9.10457 10 8C10 6.89543 9.10457 6 8 6C6.89543 6 6 6.89543 6 8C6 9.10457 6.89543 10 8 10Z"
                    stroke="currentColor"
                    stroke-width="1.5"
                  />
                </svg>
                {{ post.viewCount || 0 }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 게시글 본문 -->
      <div class="post-body">
        <div class="post-content">
          {{ post.content }}
        </div>

        <!-- 이미지 갤러리 -->
        <div
          v-if="post.images && post.images.filter((i) => i.imageUrl).length > 0"
          class="image-gallery"
        >
          <img
            v-for="(image, index) in post.images.filter((i) => i.imageUrl)"
            :key="image.imageId"
            :src="resolveImageUrl(image.imageUrl)"
            :alt="`이미지 ${index + 1}`"
            @click="openImageModal(image.imageUrl)"
          />
        </div>
      </div>

      <!-- 댓글 섹션 -->
      <div class="comments-section">
        <h2>댓글 ({{ comments.length }})</h2>

        <div v-if="authStore.isAuthenticated" class="comment-form">
          <textarea
            v-model="newComment.content"
            rows="3"
            placeholder="댓글을 입력하세요..."
            class="comment-textarea"
          ></textarea>
          <div class="comment-actions">
            <button @click="submitComment" class="submit-btn">등록</button>
          </div>
        </div>
        <div v-else class="login-prompt">댓글을 작성하려면 로그인이 필요합니다.</div>

        <div v-if="commentsLoading" class="loading">댓글 로딩 중...</div>
        <div v-else-if="comments.length === 0" class="no-comments">
          아직 작성된 댓글이 없습니다.
        </div>
        <div v-else class="comments-list">
          <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
            <div
              v-if="editingComment && editingComment.commentId === comment.commentId"
              class="comment-edit"
            >
              <textarea
                v-model="editingComment.content"
                rows="2"
                class="comment-textarea"
              ></textarea>
              <div class="comment-actions">
                <button @click="submitEditComment" class="submit-btn">저장</button>
                <button @click="cancelEditComment" class="cancel-btn">취소</button>
              </div>
            </div>
            <div v-else class="comment-body">
              <div class="comment-meta">
                <div class="comment-author-block" @click="goUserProfile(comment)">
                  <div class="comment-author-avatar">
                    <img
                      v-if="comment.profileImage"
                      :src="resolveImageUrl(comment.profileImage)"
                      :alt="comment.nickname || '사용자'
                    "
                    />
                    <div v-else class="avatar-fallback-small">{{ getAuthorInitial(comment.nickname) }}</div>
                  </div>
                  <span class="comment-author-name">{{ comment.nickname || ('사용자 ' + comment.userId) }}</span>
                </div>
                <span class="comment-date">· {{ formatDate(comment.createdAt) }}</span>
                <div v-if="isMyComment(comment)" class="comment-controls">
                  <button @click="startEditComment(comment)" class="action-btn">수정</button>
                  <button @click="removeComment(comment.commentId)" class="action-btn delete-btn">
                    삭제
                  </button>
                </div>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 하단 버튼 -->
      <div class="footer-actions">
        <button @click="goToList" class="list-btn">
          <svg
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M19 12H5M12 19l-7-7 7-7"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
          목록으로
        </button>
      </div>
    </div>

    <!-- 이미지 모달 -->
    <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
      <img :src="selectedImage" alt="확대 이미지" />
    </div>
  </Container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getCommentsByPostId, createComment, updateComment, deleteComment } from '../api/comment'
import { getBoardDetail, deleteBoard } from '../api/board'
import Container from '../components/common/Container.vue'
import { resolveImageUrl } from '../utils/image.js'
import { formatKST } from '../utils/date.js'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const post = ref(null)
const loading = ref(false)
const error = ref(null)
const showImageModal = ref(false)
const selectedImage = ref('')

// comments
const comments = ref([])
const commentsLoading = ref(false)
const newComment = ref({ content: '' })
const editingComment = ref(null)

const isMyPost = computed(() => {
  return authStore.userId && post.value && post.value.userId === parseInt(authStore.userId)
})

const fetchPost = async () => {
  loading.value = true
  error.value = null

  try {
    const postId = route.params.id
    const response = await getBoardDetail(postId)
    post.value = response.data
  } catch (err) {
    console.error('게시글 조회 실패:', err)
    error.value = '게시글을 불러오는데 실패했습니다.'
  } finally {
    loading.value = false
  }
}

// comments
const fetchComments = async () => {
  commentsLoading.value = true
  try {
    const resp = await getCommentsByPostId(route.params.id)
    const items = resp.data || []
    // normalize possible profile image field names from backend
    comments.value = items.map((c) => ({
      ...c,
      profileImage: c.profileImage || c.authorProfileImage || c.userProfileImage || c.profile_image || null,
    }))
  } catch (err) {
    console.error('댓글 목록 조회 실패:', err)
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다.')
    return
  }
  if (!newComment.value.content.trim()) {
    alert('댓글 내용을 입력해주세요.')
    return
  }

  try {
    await createComment(route.params.id, { content: newComment.value.content })
    newComment.value.content = ''
    await fetchComments()
  } catch (err) {
    console.error('댓글 작성 실패:', err)
    alert('댓글 작성에 실패했습니다.')
  }
}

const startEditComment = (c) => {
  editingComment.value = { ...c }
}

const cancelEditComment = () => {
  editingComment.value = null
}

const submitEditComment = async () => {
  if (!editingComment.value.content.trim()) {
    alert('댓글 내용을 입력해주세요.')
    return
  }
  try {
    await updateComment(editingComment.value.commentId, { content: editingComment.value.content })
    editingComment.value = null
    await fetchComments()
  } catch (err) {
    console.error('댓글 수정 실패:', err)
    alert('댓글 수정에 실패했습니다.')
  }
}

const removeComment = async (commentId) => {
  if (!confirm('정말 이 댓글을 삭제하시겠습니까?')) return
  try {
    await deleteComment(commentId)
    await fetchComments()
  } catch (err) {
    console.error('댓글 삭제 실패:', err)
    alert('댓글 삭제에 실패했습니다.')
  }
}

const isMyComment = (c) => {
  return authStore.userId && c.userId === parseInt(authStore.userId)
}

const goUserProfile = (comment) => {
  try {
    if (authStore.userId && Number(authStore.userId) === Number(comment.userId)) {
      router.push('/mypage')
    } else {
      router.push({ name: 'userProfile', params: { userId: comment.userId } })
    }
  } catch (err) {
    console.error('navigate to user profile failed', err)
  }
}

const getAuthorInitial = (nickname) => {
  if (!nickname) return '?'
  return nickname.charAt(0).toUpperCase()
}

const formatDate = (dateString) => {
  return formatKST(dateString)
}

const openImageModal = (imageUrl) => {
  selectedImage.value = resolveImageUrl(imageUrl)
  showImageModal.value = true
}

const closeImageModal = () => {
  showImageModal.value = false
  selectedImage.value = ''
}

const editPost = () => {
  if (post.value && post.value.postId) {
    router.push(`/board/${post.value.postId}/edit`)
  } else {
    alert('게시글 정보를 불러오는 중입니다.')
  }
}

const goAuthorProfile = () => {
  if (!post.value || !post.value.userId) return
  const authorId = post.value.userId
  if (authStore.userId && parseInt(authStore.userId) === authorId) {
    router.push('/mypage')
  } else {
    router.push({ name: 'userProfile', params: { userId: authorId } })
  }
}

const confirmDelete = async () => {
  if (!confirm('정말 이 게시글을 삭제하시겠습니까?')) {
    return
  }

  try {
    await deleteBoard(post.value.postId)
    alert('게시글이 삭제되었습니다.')
    router.push('/board')
  } catch (err) {
    console.error('게시글 삭제 실패:', err)
    if (err.response?.status === 403) {
      alert('본인이 작성한 게시글만 삭제할 수 있습니다.')
    } else {
      alert('게시글 삭제에 실패했습니다.')
    }
  }
}

const goToList = () => {
  router.push('/board')
}

onMounted(() => {
  fetchPost()
  fetchComments()
})
</script>

<style scoped>
.loading {
  text-align: center;
  padding: 48px;
  color: #6b7280;
  font-size: 16px;
}

.error-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 48px 24px;
  text-align: center;
}

.error-message {
  color: #ef4444;
  font-size: 16px;
  margin-bottom: 24px;
}

.back-btn {
  padding: 12px 32px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.back-btn:hover {
  background: #4b5563;
}

.detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

/* 헤더 */
.post-header {
  background: white;
  padding: 40px 32px 32px;
  border-radius: 16px 16px 0 0;
  border: 1px solid #e5e7eb;
  border-bottom: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.post-title {
  flex: 1;
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  color: #111827;
  line-height: 1.3;
  letter-spacing: -0.02em;
}

/* 메타 정보 섹션 */
.post-meta {
  background: linear-gradient(to bottom, #ffffff 0%, #f9fafb 100%);
  padding: 20px 32px;
  border-left: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  padding: 8px;
  margin: -8px;
  border-radius: 12px;
}

.author-section:hover {
  background: rgba(37, 99, 235, 0.05);
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
}

.author-section:hover .author-avatar {
  border-color: #2563eb;
  transform: scale(1.05);
}

.author-avatar img {
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
  font-size: 18px;
  font-weight: 700;
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.author-name {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  transition: color 0.2s;
}

.author-section:hover .author-name {
  color: #2563eb;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-item svg {
  color: #9ca3af;
}

.meta-divider {
  color: #d1d5db;
}

.post-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.edit-btn,
.delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.edit-btn {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
}

.edit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.edit-btn:active {
  transform: translateY(0);
}

.edit-btn svg {
  width: 16px;
  height: 16px;
}

.delete-btn {
  background: white;
  color: #ef4444;
  border: 1.5px solid #fecaca;
}

.delete-btn:hover {
  background: #fef2f2;
  border-color: #ef4444;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(239, 68, 68, 0.2);
}

.delete-btn:active {
  transform: translateY(0);
}

.delete-btn svg {
  width: 16px;
  height: 16px;
}

/* 본문 */
.post-body {
  background: white;
  padding: 40px 32px;
  border-left: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
  border-bottom: 1px solid #e5e7eb;
  border-radius: 0 0 16px 16px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.04);
}

.post-content {
  font-size: 17px;
  line-height: 1.8;
  color: #1f2937;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 0;
  letter-spacing: -0.01em;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid #f3f4f6;
}

.image-gallery img {
  width: 100%;
  height: 240px;
  object-fit: cover;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.image-gallery img:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

/* 하단 버튼 */
.footer-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.list-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: white;
  color: #374151;
  border: 1.5px solid #e5e7eb;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.list-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.list-btn:active {
  transform: translateY(0);
}

.list-btn svg {
  width: 16px;
  height: 16px;
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  cursor: pointer;
}

.image-modal img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

/* 반응형 */
/* 댓글 섹션 */
.comments-section {
  max-width: 800px;
  margin: 32px auto 0;
  padding: 0 24px;
}

.comments-section h2 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 700;
  color: #111827;
}

.comment-form {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.comment-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.6;
  resize: vertical;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.comment-textarea:focus {
  outline: none;
  border-color: #2563eb;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

.submit-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(37, 99, 235, 0.3);
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.4);
}

.submit-btn:active {
  transform: translateY(0);
}

.cancel-btn {
  padding: 10px 24px;
  background: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #e5e7eb;
}

.login-prompt {
  text-align: center;
  padding: 32px;
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 12px;
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 24px;
}

.no-comments {
  text-align: center;
  padding: 48px 24px;
  color: #9ca3af;
  font-size: 14px;
  background: #f9fafb;
  border-radius: 12px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.comment-item:hover {
  border-color: #d1d5db;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}

.comment-edit {
  width: 100%;
}

.comment-edit .comment-textarea {
  margin-bottom: 0;
}

.comment-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-author-block {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.comment-author-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: #fff;
  font-weight: 700;
}

.comment-author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback-small {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.comment-author-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.comment-author {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.comment-date {
  font-size: 13px;
  color: #9ca3af;
}

.comment-controls {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  background: transparent;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #374151;
}

.action-btn.delete-btn {
  color: #ef4444;
  border-color: #fecaca;
}

.action-btn.delete-btn:hover {
  background: #fef2f2;
  border-color: #ef4444;
}

.comment-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 768px) {
  .detail-container {
    padding: 12px;
  }

  .post-header {
    padding: 24px 20px 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .post-title {
    font-size: 24px;
  }

  .post-actions {
    width: 100%;
    display: flex;
    gap: 8px;
  }

  .edit-btn,
  .delete-btn {
    flex: 1;
    font-size: 13px;
    padding: 10px 16px;
  }

  .post-meta {
    padding: 20px;
  }

  .author-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .author-details {
    flex-direction: row;
    align-items: center;
  }

  .meta-info {
    margin-top: 12px;
    padding-left: 0;
  }

  .post-body {
    padding: 24px 20px;
  }

  .post-content {
    font-size: 16px;
  }

  .footer-actions {
    margin-top: 20px;
  }

  .list-btn {
    padding: 12px 24px;
    font-size: 13px;
  }

  .image-gallery {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .image-gallery img {
    height: 200px;
  }

  .comments-section {
    padding: 0 12px;
  }

  .comment-form {
    padding: 16px;
  }

  .comment-item {
    padding: 16px;
  }

  .comment-controls {
    margin-left: 0;
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
