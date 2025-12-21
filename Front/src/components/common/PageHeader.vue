<template>
  <div class="page-header-with-back">
    <div class="header-row" :class="{ 'header-row--no-back': !showBack }">
      <button v-if="showBack" class="back-button" @click="goBack" aria-label="뒤로 가기">
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
      <span v-if="iconSvg" class="section-icon">
        <component :is="iconSvg" class="w-5 h-5 text-current" />
      </span>
      <h2 v-if="title" class="page-title">{{ title }}</h2>
    </div>
    <slot></slot>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { computed, h } from 'vue'
// lucide icons (import locally if needed)
import { Video, TrendingUp } from 'lucide-vue-next'

// Inline Board SVG component to match sidebar icon
const BoardIcon = () =>
  h(
    'svg',
    {
      xmlns: 'http://www.w3.org/2000/svg',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round',
      class: 'w-5 h-5 text-current',
    },
    [
      h('path', { d: 'M19 4H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z' }),
      h('line', { x1: '8', y1: '9', x2: '16', y2: '9' }),
      h('line', { x1: '8', y1: '13', x2: '16', y2: '13' }),
      h('line', { x1: '8', y1: '17', x2: '12', y2: '17' }),
      h('path', { d: 'M17 17h.01' }),
    ],
  )

const props = defineProps({
  title: {
    type: String,
    default: '',
  },
  showBack: {
    type: Boolean,
    default: true,
  },
  icon: {
    type: String,
    default: '',
  },
})

const router = useRouter()

const ICONS = {
  video: Video,
  board: BoardIcon,
  invest: TrendingUp,
}

const iconSvg = computed(() => ICONS[props.icon] || null)

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
  font-size: 22px;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
}

.section-icon {
  display: inline-flex;
  width: 22px;
  height: 22px;
  color: #2563eb;
  margin-right: 8px;
}
</style>
