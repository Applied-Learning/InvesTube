<template>
  <div class="editorial-page">
    <!-- 로딩 인디케이터 -->
    <div v-if="loading" class="loading-overlay">
      <div class="loader"></div>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="error" class="error-banner">
      <p>{{ error }}</p>
      <button @click="goToList">목록으로</button>
    </div>

    <!-- 이머시브 헤더 (배경) -->
    <div v-if="post" class="immersive-header">
      <div class="header-overlay"></div>
      <div class="header-pattern"></div>
    </div>

    <div v-if="post" class="content-wrapper">
      <!-- 메인 컨텐츠 (통합 카드) -->
      <main class="unified-card">
        <!-- 헤더 영역 -->
        <header class="unified-header">
          <div class="header-left">
            <h1 class="article-title">{{ post.title }}</h1>
            <span class="date-tag">{{ formatDate(post.createdAt) }}</span>
          </div>
          
          <div class="header-right">
            <div class="author-row" @click="goAuthorProfile">
              <div class="author-avatar">
                <img 
                  v-if="post.authorProfileImage" 
                  :src="resolveImageUrl(post.authorProfileImage)" 
                  :alt="post.authorNickname"
                />
                <div v-else class="avatar-placeholder">
                  {{ getAuthorInitial(post.authorNickname) }}
                </div>
              </div>
              <div class="author-info">
                <span class="author-name">{{ post.authorNickname || '익명' }}</span>
                <span class="author-role">Contributor</span>
              </div>
              
              <div class="meta-divider"></div>
              
              <div class="view-count" title="조회수">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                {{ post.viewCount || 0 }}
              </div>
            </div>
          </div>
        </header>

        <hr class="section-divider" />

        <!-- 본문 -->
        <article class="article-body-inline">
          <div class="content theme-typography" v-html="post.content"></div>
        </article>
      </main>

      <!-- 사이드 플로팅 액션 바 (PC) -->
      <aside class="side-actions">
        <button @click="goToList" class="side-btn back" title="목록으로">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
        </button>
        
        <div v-if="isMyPost" class="owner-actions">
          <button @click="editPost" class="side-btn edit" title="수정">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
            </svg>
          </button>
          <button @click="confirmDelete" class="side-btn delete" title="삭제">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 6h18M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
            </svg>
          </button>
        </div>
      </aside>

      <!-- 댓글 섹션 -->
      <section class="comments-wrapper">
        <div class="comments-container">
          <div class="comments-header-row">
            <h3>Discussion <span class="count">{{ comments.length }}</span></h3>
          </div>

          <!-- 댓글 작성 -->
          <div v-if="authStore.isAuthenticated" class="comment-input-area">
            <textarea
              v-model="newComment.content"
              placeholder="게시글에 대한 생각을 남겨주세요..."
              class="minimal-input"
              rows="1"
              @input="adjustHeight"
            ></textarea>
            <button 
              @click="submitComment" 
              class="send-btn" 
              :class="{ active: newComment.content.trim() }"
              :disabled="!newComment.content.trim()"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" class="send-icon">
                <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <div v-else class="login-banner" @click="router.push({ name: 'login', query: { redirect: route.fullPath } })">
            로그인하고 토론에 참여하세요 →
          </div>

          <!-- 댓글 목록 -->
          <div class="comments-stream">
            <transition-group name="fade-slide">
              <div v-for="comment in comments" :key="comment.commentId" class="stream-item">
                
                <!-- 수정 모드 -->
                <div v-if="editingComment && editingComment.commentId === comment.commentId" class="edit-mode-box">
                  <textarea v-model="editingComment.content" rows="3" class="edit-textarea"></textarea>
                  <div class="edit-tools">
                    <button @click="cancelEditComment" class="cancel-text">취소</button>
                    <button @click="submitEditComment" class="save-pill">저장</button>
                  </div>
                </div>

                <!-- 뷰 모드 -->
                <div v-else class="view-mode-box">
                  <div class="stream-avatar" @click="goUserProfile(comment)">
                    <img v-if="comment.profileImage" :src="resolveImageUrl(comment.profileImage)" />
                    <div v-else class="char-avatar">{{ getAuthorInitial(comment.nickname) }}</div>
                  </div>
                  
                  <div class="stream-content">
                    <div class="stream-meta">
                      <span class="user-name" @click="goUserProfile(comment)">{{ comment.nickname }}</span>
                      <span class="time-stamp">{{ formatDate(comment.createdAt) }}</span>
                    </div>
                    <p class="comment-msg">{{ comment.content }}</p>
                    
                    <div v-if="isMyComment(comment)" class="stream-actions">
                      <button @click="startEditComment(comment)">수정</button>
                      <button @click="removeComment(comment.commentId)" class="del">삭제</button>
                    </div>
                  </div>
                </div>

              </div>
            </transition-group>
            
            <div v-if="!commentsLoading && comments.length === 0" class="empty-stream">
              <p>아직 댓글이 없습니다. 첫 번째 의견을 남겨보세요.</p>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- 이미지 모달 -->
    <Transition name="fade">
      <div v-if="showImageModal" class="modal-backdrop" @click="closeImageModal">
        <img :src="selectedImage" class="modal-img" />
        <button class="close-modal-btn" @click.stop="closeImageModal">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getCommentsByPostId, createComment, updateComment, deleteComment } from '../api/comment'
import { getBoardDetail, deleteBoard } from '../api/board'
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
    error.value = '게시글을 불러오는 중 오류가 발생했습니다.'
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
    comments.value = items.map((c) => ({
      ...c,
      profileImage:
        c.profileImage || c.authorProfileImage || c.userProfileImage || c.profile_image || null,
    }))
  } catch (err) {
    console.error('댓글 목록 조회 실패:', err)
  } finally {
    commentsLoading.value = false
  }
}

const adjustHeight = (e) => {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

const submitComment = async () => {
  if (!authStore.isAuthenticated) {
    alert('로그인이 필요합니다.')
    return
  }
  if (!newComment.value.content.trim()) return

  try {
    await createComment(route.params.id, { content: newComment.value.content })
    newComment.value.content = ''
    // Reset height
    const textarea = document.querySelector('.minimal-input')
    if (textarea) textarea.style.height = 'auto'
    
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
    alert('내용을 입력해주세요.')
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
  if (!confirm('댓글을 삭제하시겠습니까?')) return
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
    console.error('navigate failed', err)
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
  if (!confirm('게시글을 삭제하시겠습니까? 돌이킬 수 없습니다.')) return

  try {
    await deleteBoard(post.value.postId)
    alert('게시글이 삭제되었습니다.')
    router.push('/board')
  } catch (err) {
    console.error('삭제 실패:', err)
    alert('삭제할 수 없습니다.')
  }
}

const goToList = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/board')
  }
}

// Global Image Click Handler for Article Body
const handleImageClick = (e) => {
  if (e.target.tagName === 'IMG' && e.target.closest('.article-body')) {
    openImageModal(e.target.src)
  }
}

onMounted(() => {
  fetchPost()
  fetchComments()
  document.addEventListener('click', handleImageClick)
})
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Merriweather:ital,wght@0,300;0,400;0,700;0,900;1,300&family=Inter:wght@300;400;500;600;700&display=swap');

/* 기본 설정 */
.editorial-page {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  color: #111;
  background-color: #fcfcfc;
  min-height: 100vh;
  position: relative;
}

/* 로딩 & 에러 */
.loading-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(255,255,255,0.8);
  display: flex; justify-content: center; align-items: center; z-index: 50;
}
.loader {
  width: 40px; height: 40px; border: 3px solid #eee; border-top-color: #111; border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.error-banner {
  padding: 40px; text-align: center; color: #e11d48;
}

/* 이머시브 헤더 (배경) */
.immersive-header {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 500px;
  background: linear-gradient(135deg, #e0e7ff 0%, #fae8ff 50%, #fce7f3 100%);
  z-index: 0;
  overflow: hidden;
}

.header-pattern {
  position: absolute; inset: 0;
  opacity: 0.4;
  background-image: radial-gradient(#6366f1 1px, transparent 1px);
  background-size: 32px 32px;
}

.header-overlay {
  position: absolute; inset: 0;
  background: linear-gradient(to bottom, transparent 0%, #fcfcfc 100%);
}

/* 메인 컨텐츠 */
.content-wrapper {
  position: relative; z-index: 1;
  max-width: 1024px; margin: 0 auto;
  padding-top: 100px;
  padding-bottom: 120px;
}

/* 통합 카드 (Unified Card) */
.unified-card {
  max-width: 800px; margin: 0 auto;
  background: white;
  border-radius: 24px;
  box-shadow: 0 12px 32px rgba(0,0,0,0.06), 0 2px 8px rgba(0,0,0,0.03);
  padding: 40px;
  border: 1px solid rgba(0,0,0,0.02);
}

.unified-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 24px;
  text-align: left;
}

.header-left {
  flex: 1;
}

.header-right {
  flex-shrink: 0;
}

.article-title {
  font-family: 'Inter', sans-serif;
  font-size: 26px; font-weight: 800; line-height: 1.3;
  color: #0f172a; margin: 0 0 8px;
  letter-spacing: -0.01em;
}

.date-tag {
  display: block;
  font-size: 13px; color: #64748b; font-weight: 500;
}

.author-row {
  display: inline-flex; align-items: center; gap: 16px;
  padding: 8px 24px 8px 8px;
  border-radius: 100px;
  background: #f8fafc;
  cursor: pointer; transition: all 0.2s;
}

.author-row:hover {
  background: #f1f5f9;
}

.author-avatar {
  width: 40px; height: 40px; border-radius: 50%; overflow: hidden;
}
.author-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-placeholder {
  width: 100%; height: 100%; background: #111; color: white;
  display: flex; align-items: center; justify-content: center; font-weight: 700;
}

.author-info { text-align: left; }
.author-name { display: block; font-size: 14px; font-weight: 700; color: #0f172a; }
.author-role { display: block; font-size: 11px; color: #64748b; text-transform: uppercase; }

.meta-divider { width: 1px; height: 24px; background: #e2e8f0; }

.view-count {
  display: flex; align-items: center; gap: 6px;
  font-size: 13px; color: #64748b; font-weight: 500;
}

/* 구분선 */
.section-divider {
  border: none;
  border-top: 1px solid #f1f5f9;
  margin: 32px 0;
}

/* 본문 (Inline) */
.article-body-inline {
  /* background styles removed as it acts as part of unified card */
}

.content {
  font-family: 'Inter', sans-serif;
  font-size: 17px; line-height: 1.8; color: #1f2937;
}

.content :deep(p) { margin-bottom: 24px; }
.content :deep(img) {
  width: 100%; border-radius: 12px; margin: 40px 0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  cursor: zoom-in;
}

/* 사이드 액션 바 */
.side-actions {
  position: fixed; right: 40px; top: 50%; transform: translateY(-50%);
  display: flex; flex-direction: column; gap: 16px; 
  z-index: 10;
}

.side-btn {
  width: 48px; height: 48px; border-radius: 50%;
  background: white; border: 1px solid #e2e8f0;
  display: flex; align-items: center; justify-content: center;
  color: #64748b; cursor: pointer; transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}

.side-btn:hover {
  transform: scale(1.1); color: #111; box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}

.side-btn.delete:hover { color: #e11d48; border-color: #fecaca; background: #fff1f2; }

@media (max-width: 1200px) {
  .side-actions { 
    position: static; transform: none; 
    flex-direction: row; justify-content: center; margin-top: 60px; 
  }
}

/* 댓글 섹션 */
.comments-wrapper {
  margin-top: 60px;
  /* background removed to blend with page or keep separated? Layout suggests separation. */
}

.comments-container {
  max-width: 720px; margin: 0 auto;
}

.comments-header-row h3 {
  font-family: 'Inter', sans-serif;
  font-size: 20px; font-weight: 800; color: #0f172a; margin-bottom: 32px;
  display: flex; align-items: center; gap: 12px;
}

.count {
  font-size: 13px; color: #fff; background: #0f172a; 
  padding: 2px 10px; border-radius: 100px; font-weight: 600;
}

/* 댓글 작성 */
.comment-input-area {
  position: relative; margin-bottom: 60px;
}

.minimal-input {
  width: 100%; border: none; border-bottom: 2px solid #e2e8f0;
  padding: 16px 60px 16px 0;
  font-size: 16px; background: transparent;
  transition: border-color 0.3s;
  resize: none; overflow: hidden;
  font-family: 'Inter', sans-serif;
  box-sizing: border-box;
}

.minimal-input:focus {
  outline: none; border-color: #0f172a;
}

.send-btn {
  position: absolute; right: 0; bottom: 16px;
  background: none; border: none; color: #cbd5e1;
  transition: all 0.3s; cursor: pointer;
}

.send-btn.active {
  color: #4f46e5; transform: rotate(-45deg);
}

.send-btn:disabled { cursor: not-allowed; }

.login-banner {
  padding: 32px; background: #f8fafc; border-radius: 16px;
  text-align: center; color: #64748b; font-weight: 500; cursor: pointer;
  margin-bottom: 60px; transition: background 0.2s;
}
.login-banner:hover { background: #f1f5f9; color: #4f46e5; }

/* 댓글 목록 */
.comments-stream {
  display: flex; flex-direction: column; gap: 40px;
}

.stream-item {
  position: relative;
}

.view-mode-box {
  display: flex; gap: 20px;
}

.stream-avatar {
  flex-shrink: 0; width: 48px; height: 48px; border-radius: 12px; overflow: hidden; cursor: pointer;
  background: #f1f5f9;
}
.stream-avatar img { width: 100%; height: 100%; object-fit: cover; }
.char-avatar { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-weight: 700; color: #64748b; }

.stream-content { flex: 1; }

.stream-meta {
  display: flex; align-items: baseline; gap: 12px; margin-bottom: 8px;
}

.user-name { font-weight: 700; font-size: 15px; color: #0f172a; cursor: pointer; }
.time-stamp { font-size: 12px; color: #94a3b8; }

.comment-msg {
  font-size: 15px; line-height: 1.6; color: #334155; margin: 0;
}

.stream-actions {
  margin-top: 12px; display: flex; gap: 16px; 
}

.stream-actions button {
  background: none; border: none; padding: 0;
  font-size: 12px; font-weight: 600; color: #94a3b8; cursor: pointer;
}

.stream-actions button:hover { color: #0f172a; }
.stream-actions button.del:hover { color: #e11d48; }

/* 댓글 수정 모드 */
.edit-mode-box {
  background: #fff; border: 1px solid #e2e8f0; padding: 20px; border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.03);
}
.edit-textarea {
  width: 100%; border: none; font-family: inherit; font-size: 15px; resize: none; margin-bottom: 12px;
  outline: none;
}
.edit-tools { display: flex; justify-content: flex-end; gap: 12px; }
.cancel-text { background: none; border: none; color: #64748b; font-weight: 600; cursor: pointer; }
.save-pill {
  background: #0f172a; color: white; border: none; padding: 6px 16px; border-radius: 100px;
  font-size: 13px; font-weight: 600; cursor: pointer;
}

.empty-stream { text-align: center; color: #94a3b8; padding: 40px; }

/* 애니메이션 */
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.4s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-20px); }

/* 모바일 */
@media (max-width: 768px) {
  .unified-card {
    padding: 32px 20px;
    margin: 0 20px;
    border-radius: 20px;
  }
  .article-title { font-size: 30px; }
  .content-wrapper { padding-top: 80px; }
  .side-actions { display: none; }
}
</style>
