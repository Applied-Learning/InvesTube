<template>
  <header class="header">
    <Container>
      <div class="header-inner">
        <RouterLink to="/" class="logo-link">
          <h1 class="logo">Investube</h1>
        </RouterLink>

        <nav class="nav">
          <template v-if="authStore.isAuthenticated">
            <NotificationDropdown />

            <!-- 프로필 아바타 -->
            <button type="button" class="nav-item nav-item--avatar" @click="toggleUserMenu">
              <div class="avatar-circle">
                <img
                  v-if="myProfile && myProfile.profileImage"
                  :src="resolveImageUrl(myProfile.profileImage)"
                  :alt="authStore.nickname || authStore.id || '프로필'"
                />
                <span v-else>
                  {{ authStore.nickname?.charAt(0).toUpperCase() }}
                </span>
              </div>
            </button>
          </template>

          <template v-else>
            <RouterLink to="/login" class="nav-item nav-item--primary"> 로그인 </RouterLink>
          </template>
        </nav>
      </div>

      <!-- Notification dropdown moved to NotificationDropdown component -->

      <!-- 유저 메뉴 드롭다운 -->
      <div v-if="showUserMenu && authStore.isAuthenticated" class="user-menu-dropdown">
        <div class="user-menu-header">
          <div class="avatar-circle avatar-circle--large">
            <img
              v-if="myProfile && myProfile.profileImage"
              :src="resolveImageUrl(myProfile.profileImage)"
              :alt="authStore.nickname || authStore.id || '프로필'"
            />
            <span v-else>
              {{ authStore.nickname?.charAt(0).toUpperCase() }}
            </span>
          </div>
          <div class="user-info">
            <p class="user-nickname">{{ authStore.nickname }}</p>
            <p class="user-id">@{{ authStore.id }}</p>
          </div>
        </div>
        <div class="user-menu-body">
          <RouterLink to="/mypage" class="user-menu-item" @click="closeUserMenu">
            마이페이지
          </RouterLink>
          <button
            type="button"
            class="user-menu-item user-menu-item--danger"
            @click="handleLogoutFromMenu"
          >
            로그아웃
          </button>
        </div>
      </div>
    </Container>
  </header>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import Container from './Container.vue'
import { useAuthStore } from '../../stores/auth.js'
import { getMyInfo } from '../../api/user.js'
import { resolveImageUrl } from '../../utils/image.js'
import NotificationDropdown from './NotificationDropdown.vue'

const router = useRouter()
const authStore = useAuthStore()

const showUserMenu = ref(false)
const myProfile = ref(null)

// notifications state moved to NotificationDropdown component

const handleLogout = () => {
  authStore.logout()
  myProfile.value = null
  router.push('/login')
}

const handleLogoutFromMenu = () => {
  showUserMenu.value = false
  handleLogout()
}


const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

// notification handlers moved into NotificationDropdown component

const formatTime = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('ko-KR')
}

const fetchMyProfile = async () => {
  if (!authStore.isAuthenticated) {
    myProfile.value = null
    return
  }
  try {
    const res = await getMyInfo()
    myProfile.value = res.data
  } catch (err) {
    console.error('헤더 프로필 조회 실패:', err)
    myProfile.value = null
  }
}

const handleClickOutside = (event) => {
  const headerEl = document.querySelector('.header')
  if (headerEl && !headerEl.contains(event.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  fetchMyProfile()
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

watch(
  () => authStore.isAuthenticated,
  (isAuth) => {
    if (isAuth) {
      fetchMyProfile()
    } else {
      myProfile.value = null
    }
  },
)
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
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 700;
  overflow: hidden;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-circle--large {
  width: 40px;
  height: 40px;
  font-size: 18px;
}

/* notification styles moved to NotificationDropdown.vue */

.user-menu-dropdown {
  position: absolute;
  top: 60px;
  right: 20px;
  width: 220px;
  background: #ffffff;
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
