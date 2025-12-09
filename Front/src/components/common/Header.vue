<template>
  <header class="header">
    <Container>
      <div class="header-inner">
        <RouterLink to="/" class="logo-link">
          <h1 class="logo">Investube</h1>
        </RouterLink>
        <nav class="nav">
          <template v-if="authStore.isAuthenticated">
            <button 
              type="button" 
              class="nav-item nav-item--notification"
              @click="toggleNotifications"
              title="알림"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M10 2C8.34315 2 7 3.34315 7 5V5.5C7 8.53757 5.76256 11.5 3.5 13.5V15H16.5V13.5C14.2374 11.5 13 8.53757 13 5.5V5C13 3.34315 11.6569 2 10 2Z" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M8 15V15.5C8 16.8807 8.89543 18 10 18C11.1046 18 12 16.8807 12 15.5V15" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
              <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
            </button>
            <button 
              type="button" 
              class="nav-item nav-item--avatar"
              @click="toggleUserMenu"
            >
              <div class="avatar-circle">
                {{ authStore.nickname?.charAt(0).toUpperCase() }}
              </div>
            </button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="nav-item nav-item--primary">
              로그인
            </RouterLink>
          </template>
        </nav>
      </div>
      
      <!-- 알림 드롭다운 -->
      <div v-if="showNotifications && authStore.isAuthenticated" class="notifications-dropdown">
        <div class="notifications-header">
          <h3>알림</h3>
          <button v-if="notifications.length > 0" @click="markAllAsRead" class="mark-all-read">
            모두 읽음
          </button>
        </div>
        <div class="notifications-body">
          <div v-if="notifications.length === 0" class="no-notifications">
            새로운 알림이 없습니다.
          </div>
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            :class="['notification-item', { unread: !notification.isRead }]"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-content">
              <p class="notification-message">{{ notification.message }}</p>
              <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
            </div>
            <button 
              v-if="!notification.isRead" 
              class="mark-read-btn"
              @click.stop="markAsRead(notification.id)"
              title="읽음으로 표시"
            >
              ✓
            </button>
          </div>
        </div>
      </div>

      <!-- 유저 메뉴 드롭다운 -->
      <div v-if="showUserMenu && authStore.isAuthenticated" class="user-menu-dropdown">
        <div class="user-menu-header">
          <div class="avatar-circle avatar-circle--large">
            {{ authStore.nickname?.charAt(0).toUpperCase() }}
          </div>
          <div class="user-info">
            <p class="user-nickname">{{ authStore.nickname }}</p>
            <p class="user-id">{{ authStore.id }}</p>
          </div>
        </div>
        <div class="user-menu-body">
          <RouterLink to="/mypage" class="user-menu-item" @click="closeUserMenu">
            마이페이지
          </RouterLink>
          <button type="button" class="user-menu-item user-menu-item--danger" @click="handleLogoutFromMenu">
            로그아웃
          </button>
        </div>
      </div>
    </Container>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import Container from './Container.vue'
import { useAuthStore } from '../../stores/auth.js'

const router = useRouter()
const authStore = useAuthStore()
const showNotifications = ref(false)
const showUserMenu = ref(false)
const notifications = ref([
  // TODO: API 연동 예정
])

const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.isRead).length
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}

const handleLogoutFromMenu = () => {
  showUserMenu.value = false
  handleLogout()
}

const toggleNotifications = () => {
  showNotifications.value = !showNotifications.value
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const handleNotificationClick = (notification) => {
  markAsRead(notification.id)
  // 추후 알림 클릭 시 이동 로직 추가 가능
}

const markAsRead = (id) => {
  const notification = notifications.value.find(n => n.id === id)
  if (notification) {
    notification.isRead = true
  }
}

const markAllAsRead = () => {
  notifications.value.forEach(n => { n.isRead = true })
}

const formatTime = (dateString) => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (hours < 24) return `${hours}시간 전`
  if (days < 7) return `${days}일 전`
  return date.toLocaleDateString()
}

const closeUserMenu = () => {
  showUserMenu.value = false
}

// 드롭다운 외부 클릭 시 닫기
const handleClickOutside = (event) => {
  const notificationsDropdown = document.querySelector('.notifications-dropdown')
  const notificationsButton = document.querySelector('.nav-item--notification')
  const userMenuDropdown = document.querySelector('.user-menu-dropdown')
  const userMenuButton = document.querySelector('.nav-item--avatar')

  if (
    notificationsDropdown &&
    !notificationsDropdown.contains(event.target) &&
    notificationsButton &&
    !notificationsButton.contains(event.target)
  ) {
    showNotifications.value = false
  }

  if (
    userMenuDropdown &&
    !userMenuDropdown.contains(event.target) &&
    userMenuButton &&
    !userMenuButton.contains(event.target)
  ) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.header {
  background-color: #111827;
  color: #f9fafb;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
}

.logo-link {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid transparent;
  text-decoration: none;
}

.nav-item--primary {
  background-color: #2563eb;
  color: #ffffff;
}

.nav-item--primary:hover {
  background-color: #1d4ed8;
}

.nav-item--notification {
  position: relative;
  background-color: transparent;
  color: #e5e7eb;
  border: none;
  padding: 8px;
  transition: all 0.2s;
}

.nav-item--notification:hover {
  background-color: #374151;
  color: white;
}

.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

/* 알림 드롭다운 */
.notifications-dropdown {
  position: absolute;
  top: 60px;
  right: 20px;
  width: 360px;
  max-height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.notifications-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
}

.notifications-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.mark-all-read {
  background: none;
  border: none;
  color: #2563eb;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.2s;
}

.mark-all-read:hover {
  background: #eff6ff;
}

.notifications-body {
  max-height: 400px;
  overflow-y: auto;
}

.no-notifications {
  padding: 40px 20px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f9fafb;
}

.notification-item.unread {
  background: #eff6ff;
}

.notification-item.unread:hover {
  background: #dbeafe;
}

.notification-content {
  flex: 1;
}

.notification-message {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #111827;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  color: #6b7280;
}

.mark-read-btn {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: background 0.2s;
}

.mark-read-btn:hover {
  background: #1d4ed8;
}

/* 유저 메뉴 */
.nav-item--avatar {
  padding: 0;
  background: transparent;
  border: none;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
}

.avatar-circle--large {
  width: 40px;
  height: 40px;
  font-size: 18px;
}

.user-menu-dropdown {
  position: absolute;
  top: 60px;
  right: 20px;
  width: 220px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  overflow: hidden;
}

.user-menu-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-nickname {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.user-id {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #6b7280;
}

.user-menu-body {
  display: flex;
  flex-direction: column;
}

.user-menu-item {
  padding: 10px 16px;
  background: transparent;
  border: none;
  text-align: left;
  font-size: 14px;
  color: #111827;
  cursor: pointer;
  text-decoration: none;
}

.user-menu-item:hover {
  background: #f3f4f6;
}

.user-menu-item--danger {
  color: #dc2626;
}
</style>
