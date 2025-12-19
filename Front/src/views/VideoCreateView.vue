<template>
  <div class="video-create-container">
    <PageHeader title="영상 등록" />

    <form @submit.prevent="handleSubmit" class="create-form">
      <div class="form-group">
        <label class="required">YouTube 검색 / 링크</label>
        <div class="search-row">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="영상 제목으로 검색하거나 YouTube 링크를 붙여넣기"
            @keyup.enter="handleSearch"
          />
          <button type="button" class="btn-secondary" :disabled="searchLoading" @click="handleSearch">
            {{ searchLoading ? '검색 중...' : '검색' }}
          </button>
        </div>
        <p class="help-text">
          키워드 검색 후 영상을 선택하거나, 영상의 링크/ID를 입력하세요.
        </p>
        <div v-if="searchError" class="error-message inline-error">{{ searchError }}</div>
        <div v-if="searchResults.length" class="search-results">
          <div
            v-for="item in searchResults"
            :key="item.videoId"
            class="search-result-item"
            @click="selectSearchResult(item)"
          >
            <div class="search-thumb" v-if="item.thumbnailUrl">
              <img :src="item.thumbnailUrl" :alt="item.title" />
            </div>
            <div class="search-info">
              <p class="search-title">{{ item.title }}</p>
              <p class="search-sub">{{ item.channelTitle }}</p>
            </div>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label class="required">선택한 영상</label>
        <!-- 숨김 필드로 ID를 유지 -->
        <input type="hidden" v-model="formData.youtubeVideoId" />
        <div v-if="selectedPreview && selectedPreview.videoId" class="video-preview thumb-only">
          <img
            :src="
              selectedPreview.thumbnailUrl ||
              (selectedPreview.videoId ? `https://i.ytimg.com/vi/${selectedPreview.videoId}/hqdefault.jpg` : '')
            "
            alt="선택한 영상 썸네일"
          />
        </div>
        <div v-else class="activity-placeholder">검색/링크로 영상을 선택하면 썸네일이 표시됩니다.</div>
        <div class="meta-actions">
          <button
            type="button"
            class="btn-secondary"
            :disabled="metaLoading || !formData.youtubeVideoId"
            @click="fetchAndFillMetadata"
          >
            {{ metaLoading ? '메타데이터 불러오는 중...' : '데이터 자동 채우기' }}
          </button>
          <p class="help-text compact">
            영상 선택 후 버튼을 누르면 데이터를 불러옵니다. 필요하면 수정하세요.
          </p>
        </div>
      </div>

      <div class="form-group">
        <label for="title" class="required">제목</label>
        <input
          id="title"
          v-model="formData.title"
          type="text"
          placeholder="영상 제목을 입력하세요"
          required
          maxlength="200"
        />
      </div>

      <div class="form-group">
        <label for="description" class="required">설명</label>
        <textarea
          id="description"
          v-model="formData.description"
          placeholder="영상에 대한 설명을 입력하세요"
          rows="5"
          required
          maxlength="1000"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="categoryId" class="required">카테고리</label>
        <select id="categoryId" v-model="formData.categoryId" required>
          <option value="" disabled>카테고리를 선택하세요</option>
          <option v-for="category in categories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <div class="form-actions">
        <button type="button" class="btn-cancel" @click="goBack">취소</button>
        <button type="submit" class="btn-submit" :disabled="loading">
          {{ loading ? '등록 중...' : '등록하기' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import PageHeader from '../components/common/PageHeader.vue'
import { createVideo } from '../api/video.js'
import { searchYoutubeVideos, getYoutubeVideo } from '../api/youtube.js'

const router = useRouter()
const loading = ref(false)
const error = ref(null)
const searchLoading = ref(false)
const searchError = ref('')
const metaLoading = ref(false)
const selectedPreview = ref(null)

const formData = ref({
  youtubeVideoId: '',
  title: '',
  description: '',
  categoryId: '',
  thumbnailUrl: '',
})

const searchQuery = ref('')
const searchResults = ref([])

const categories = [
  { id: 1, name: '금융' },
  { id: 2, name: '기술' },
  { id: 3, name: '투자' },
]

const extractYoutubeId = (input) => {
  if (!input) return ''
  const trimmed = input.trim()
  // 이미 ID 형식(11자)인 경우
  if (/^[0-9A-Za-z_-]{11}$/.test(trimmed)) return trimmed
  // URL에서 추출
  const match = trimmed.match(
    /(?:v=|v\/|vi=|vi\/|youtu\.be\/|embed\/)([0-9A-Za-z_-]{11})/
  )
  return match ? match[1] : ''
}

const updatePreview = (payload = {}) => {
  const vid = payload.videoId || formData.value.youtubeVideoId || ''
  selectedPreview.value = {
    videoId: vid,
    title: payload.title || selectedPreview.value?.title || '',
    channelTitle: payload.channelTitle || selectedPreview.value?.channelTitle || '',
    thumbnailUrl:
      payload.thumbnailUrl ||
      selectedPreview.value?.thumbnailUrl ||
      (vid ? `https://i.ytimg.com/vi/${vid}/hqdefault.jpg` : ''),
  }
}

const resetMetadataFields = () => {
  formData.value.title = ''
  formData.value.description = ''
  formData.value.thumbnailUrl = ''
}

const handleSearch = async () => {
  searchError.value = ''
  searchResults.value = []

  const parsedId = extractYoutubeId(searchQuery.value)
  if (parsedId) {
    resetMetadataFields()
    formData.value.youtubeVideoId = parsedId
    updatePreview({ videoId: parsedId })
    return
  }

  if (!searchQuery.value || searchQuery.value.trim().length < 2) {
    searchError.value = '두 글자 이상 입력 후 검색해주세요.'
    return
  }

  searchLoading.value = true
  try {
    const res = await searchYoutubeVideos(searchQuery.value.trim())
    const list = res.data?.items || res.data || []
    searchResults.value = list.map((item) => ({
      videoId: item.videoId || item.id?.videoId || item.id,
      title: item.title || item.snippet?.title || '',
      channelTitle: item.channelTitle || item.snippet?.channelTitle || '',
      thumbnailUrl:
        item.thumbnailUrl ||
        item.snippet?.thumbnails?.medium?.url ||
        item.snippet?.thumbnails?.high?.url ||
        item.snippet?.thumbnails?.default?.url,
    }))
  } catch (err) {
    console.error('YouTube 검색 실패:', err)
    searchError.value = '검색에 실패했습니다. 다시 시도해주세요.'
  } finally {
    searchLoading.value = false
  }
}

const selectSearchResult = (item) => {
  if (!item?.videoId) return
  resetMetadataFields()
  formData.value.youtubeVideoId = item.videoId
  updatePreview({
    videoId: item.videoId,
    title: item.title,
    channelTitle: item.channelTitle,
    thumbnailUrl: item.thumbnailUrl,
  })
  searchResults.value = []
}

const fetchAndFillMetadata = async () => {
  const id = extractYoutubeId(formData.value.youtubeVideoId)
  if (!id) {
    searchError.value = '올바른 YouTube 영상 ID 또는 링크를 입력해주세요.'
    return
  }

  formData.value.youtubeVideoId = id
  metaLoading.value = true
  searchError.value = ''
  try {
    const res = await getYoutubeVideo(id)
    const data = res.data || {}
    formData.value.title = data.title || formData.value.title
    formData.value.description = data.description || formData.value.description
    formData.value.thumbnailUrl = data.thumbnailUrl || formData.value.thumbnailUrl
    updatePreview({
      videoId: id,
      title: data.title,
      channelTitle: data.channelTitle,
      thumbnailUrl: data.thumbnailUrl,
    })
  } catch (err) {
    console.error('YouTube 메타데이터 불러오기 실패:', err)
    searchError.value = '메타데이터를 불러오지 못했습니다.'
  } finally {
    metaLoading.value = false
  }
}

const handleSubmit = async () => {
  loading.value = true
  error.value = null

  try {
    const parsedId = extractYoutubeId(formData.value.youtubeVideoId)
    if (!parsedId) {
      error.value = '올바른 YouTube 영상 ID를 입력해주세요.'
      return
    }
    formData.value.youtubeVideoId = parsedId

    // categoryId를 숫자로 변환
    const videoData = {
      ...formData.value,
      categoryId: parseInt(formData.value.categoryId),
    }

    await createVideo(videoData)
    alert('영상이 성공적으로 등록되었습니다!')
    router.push('/')
  } catch (err) {
    console.error('영상 등록 실패:', err)
    error.value = err.response?.data?.message || '영상 등록에 실패했습니다. 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.video-create-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.create-form {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.form-group label.required::after {
  content: '*';
  color: #ef4444;
  margin-left: 4px;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.search-row {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.search-row input {
  flex: 1;
  min-width: 240px;
}

.btn-secondary {
  padding: 10px 16px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #f3f4f6;
  color: #374151;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background: #e5e7eb;
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.search-results {
  margin-top: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
  max-height: 280px;
  overflow-y: auto;
}

.search-result-item {
  display: flex;
  gap: 12px;
  padding: 10px 12px;
  cursor: pointer;
  align-items: center;
  transition: background 0.15s;
}

.search-result-item + .search-result-item {
  border-top: 1px solid #f3f4f6;
}

.search-result-item:hover {
  background: #f9fafb;
}

.search-thumb {
  width: 72px;
  height: 44px;
  border-radius: 8px;
  overflow: hidden;
  background: #e5e7eb;
  flex-shrink: 0;
}

.search-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.search-info {
  flex: 1;
  min-width: 0;
}

.search-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.search-sub {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.inline-error {
  margin-top: 8px;
}

.video-preview {
  display: flex;
  gap: 12px;
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #f9fafb;
}

.preview-thumb {
  width: 96px;
  height: 54px;
  border-radius: 8px;
  overflow: hidden;
  background: #e5e7eb;
  flex-shrink: 0;
}

.preview-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-info {
  flex: 1;
  min-width: 0;
}

.preview-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-sub {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
}

.video-preview.thumb-only {
  justify-content: flex-start;
  padding: 0;
  border: none;
  background: transparent;
  gap: 0;
}

.video-preview.thumb-only img {
  width: 160px;
  height: 90px;
  border-radius: 10px;
  object-fit: cover;
}

.meta-actions {
  margin-top: 10px;
}

.activity-placeholder {
  margin-top: 8px;
  font-size: 13px;
  color: #6b7280;
}

.help-text {
  margin-top: 6px;
  font-size: 13px;
  color: #6b7280;
}

.error-message {
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
  margin-bottom: 24px;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 32px;
}

.btn-cancel,
.btn-submit {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f3f4f6;
  color: #374151;
}

.btn-cancel:hover {
  background: #e5e7eb;
}

.btn-submit {
  background: #2563eb;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
