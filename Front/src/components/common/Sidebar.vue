<template>
  <aside class="sidebar">
    <nav class="nav">
      <RouterLink
        to="/"
        class="nav-item"
        :class="{ 'nav-item--active': isVideoRoute }"
      >
        <span class="nav-icon" aria-hidden="true">
          <svg
            class="w-4 h-4 text-blue-600"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
            <polygon points="10 8 16 11 10 14 10 8"></polygon>
            <line x1="2" y1="17" x2="22" y2="17"></line>
            <line x1="2" y1="21" x2="22" y2="21"></line>
          </svg>
        </span>
        <span class="nav-label">영상</span>
      </RouterLink>

      <RouterLink
        to="/board"
        class="nav-item"
        :class="{ 'nav-item--active': isBoardRoute }"
      >
        <span class="nav-icon" aria-hidden="true">
          <svg
            class="w-4 h-4 text-green-600"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path
              d="M19 4H5a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z"
            ></path>
            <line x1="8" y1="9" x2="16" y2="9"></line>
            <line x1="8" y1="13" x2="16" y2="13"></line>
            <line x1="8" y1="17" x2="12" y2="17"></line>
            <path d="M17 17h.01"></path>
          </svg>
        </span>
        <span class="nav-label">커뮤니티</span>
      </RouterLink>

      <RouterLink
        to="/invest"
        class="nav-item"
        :class="{ 'nav-item--active': isInvestRoute }"
      >
        <span class="nav-icon" aria-hidden="true">
          <svg
            class="w-4 h-4 text-orange-500"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline>
            <polyline points="17 6 23 6 23 12"></polyline>
          </svg>
        </span>
        <span class="nav-label">투자 정보</span>
      </RouterLink>
    </nav>
  </aside>
</template>

<script setup>
import { RouterLink, useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const isVideoRoute = computed(() => {
  const names = ['home', 'videoDetail', 'videoCreate', 'wishlist', 'myVideos', 'myReviewVideos', 'myWishedVideos', 'userVideos']
  return names.includes(route.name)
})

const isBoardRoute = computed(() => {
  const names = ['board', 'boardCreate', 'boardEdit', 'boardDetail', 'myBoards', 'myCommentedBoards', 'userBoards']
  return names.includes(route.name)
})

const isInvestRoute = computed(() => {
  const names = ['invest', 'investmentSurvey', 'stockDetail', 'myWishedStocks']
  return names.includes(route.name)
})
</script>

<style scoped>
.sidebar {
  width: 160px;
  padding: 16px 8px 16px 16px;
  border-right: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 13px;
  color: #4b5563;
  text-decoration: none;
}

.nav-item--active {
  background-color: #e5edff;
  color: #1d4ed8;
  font-weight: 600;
}

.nav-icon {
  display: inline-flex;
  width: 18px;
  height: 18px;
  color: #6b7280;
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

.nav-label {
  font-size: 13px;
}
</style>
