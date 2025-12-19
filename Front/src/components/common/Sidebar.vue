<template>
  <aside class="sidebar">
    <nav class="nav">
      <RouterLink
        to="/videos"
        class="nav-item"
        :class="{ 'nav-item--active': isVideoRoute }"
      >
        영상
      </RouterLink>
      <RouterLink to="/board" class="nav-item" active-class="nav-item--active">게시판</RouterLink>
      <RouterLink to="/invest" class="nav-item" active-class="nav-item--active">
        투자 정보
      </RouterLink>
    </nav>
  </aside>
</template>

<script setup>
import { RouterLink, useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const isVideoRoute = computed(() => {
  const videoNames = ['home', 'videoDetail', 'videoCreate', 'wishlist']
  return videoNames.includes(route.name)
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
  display: block;
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 14px;
  color: #4b5563;
  text-decoration: none;
}

.nav-item--active {
  background-color: #e5edff;
  color: #1d4ed8;
  font-weight: 600;
}
</style>
