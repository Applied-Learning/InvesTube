<template>
  <AppLayout>
    <router-view />
    <Toast />
    <!-- 플로팅 챗봇 (로그인 시에만 표시) -->
    <FloatingChatbot v-if="isLoggedIn" />
  </AppLayout>
</template>

<script setup>
import { computed } from 'vue'
import AppLayout from './layouts/AppLayout.vue'
import Toast from './components/common/Toast.vue'
import FloatingChatbot from './components/common/FloatingChatbot.vue'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
const isLoggedIn = computed(() => authStore.isAuthenticated)
</script>

<style scoped>
:global(body) {
  margin: 0;
  font-family:
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
  background-color: #f5f5f5;
}
</style>
