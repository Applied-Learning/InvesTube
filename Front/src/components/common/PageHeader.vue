<template>
  <div class="page-header-with-back">
    <div class="header-row" :class="{ 'header-row--no-back': !showBack }">
      <button
        v-if="showBack"
        class="back-button"
        @click="goBack"
        aria-label="뒤로 가기"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
        >
          <path d="M19 12H5M12 19l-7-7 7-7" />
        </svg>
      </button>
      <h2 v-if="title" class="page-title">{{ title }}</h2>
    </div>
    <slot></slot>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  title: {
    type: String,
    default: '',
  },
  showBack: {
    type: Boolean,
    default: true,
  },
})

const router = useRouter()

const goBack = () => {
  // 히스토리가 있으면 뒤로, 없으면 홈으로
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.page-header-with-back {
  margin-bottom: 24px;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-row--no-back {
  padding-left: 44px;
}

.back-button {
  padding: 6px;
  background: transparent;
  border: none;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}

.back-button:hover {
  color: #2563eb;
  background: #f3f4f6;
}

.back-button svg {
  width: 24px;
  height: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
}
</style>
