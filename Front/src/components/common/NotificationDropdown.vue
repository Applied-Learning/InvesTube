<template>
  <div class="notification-root">
    <button
      type="button"
      class="nav-item nav-item--notification"
      @click="toggleNotifications"
      title="알림"
    >
      <svg
        width="20"
        height="20"
        viewBox="0 0 20 20"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          d="M10 2C8.343 2 7 3.343 7 5v.5C7 8.538 5.763 11.5 3.5 13.5V15h13v-1.5C14.237 11.5 13 8.538 13 5.5V5c0-1.657-1.343-3-3-3Z"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
        <path
          d="M8 15v.5A2 2 0 0 0 12 15.5V15"
          stroke="currentColor"
          stroke-width="1.5"
          stroke-linecap="round"
          stroke-linejoin="round"
        />
      </svg>
      <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
    </button>

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
          @click="onNotificationClick(notification)"
        >
          <div class="notification-content">
            <p class="notification-message">{{ notification.message }}</p>
            <span class="notification-time">{{ formatTime(notification.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth.js'
import {
  getNotifications,
  getUnreadCount,
  markAsRead as apiMarkAsRead,
  markAllAsRead as apiMarkAllAsRead,
} from '../../api/notification.js'
import { formatKST } from '../../utils/date.js'

const router = useRouter()
const authStore = useAuthStore()

const showNotifications = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const loadUnreadCount = async () => {
  if (!authStore.isAuthenticated) {
    unreadCount.value = 0
    return
  }
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (err) {
    console.error('unread count fetch failed', err)
  }
}

const loadNotifications = async () => {
  if (!authStore.isAuthenticated) {
    notifications.value = []
    return
  }
  try {
    const res = await getNotifications()
    const items = res.data || []
    // Show all unread notifications, and keep recent read notifications so
    // the dropdown always displays up to `KEEP_RECENT_READ` items in total.
    const KEEP_RECENT_READ = 5
    const unread = items.filter((it) => !it.isRead)
    if (unread.length >= KEEP_RECENT_READ) {
      notifications.value = unread
    } else {
      const readSorted = items
        .filter((it) => it.isRead)
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      const toKeep = readSorted.slice(0, Math.max(0, KEEP_RECENT_READ - unread.length))
      notifications.value = [...unread, ...toKeep]
    }
  } catch (err) {
    console.error('notifications fetch failed', err)
    notifications.value = []
  }
}

const toggleNotifications = async () => {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    await loadNotifications()
    await loadUnreadCount()
  }
}

const markAllAsRead = async () => {
  // optimistic: mark current visible notifications as read locally, set unread count to 0
  const prevNotifications = notifications.value.slice()
  const prevCount = unreadCount.value
  try {
    // mark visible ones as read locally
    notifications.value = notifications.value.map((n) => ({ ...n, isRead: true }))
    unreadCount.value = 0
    // call API to mark all as read on server
    await apiMarkAllAsRead()
    // refresh list to include most recent read notifications (keeps recent 5)
    await loadNotifications()
  } catch (err) {
    console.error('mark all as read failed', err)
    // restore on failure
    notifications.value = prevNotifications
    unreadCount.value = prevCount
    alert('모두 읽음 처리에 실패했습니다. 잠시 후 다시 시도하세요.')
  }
}

const onNotificationClick = async (notification) => {
  if (!notification.isRead) {
    // optimistic removal
    const prevNotifications = notifications.value.slice()
    const prevCount = unreadCount.value
    notifications.value = notifications.value.filter((n) => n.id !== notification.id)
    if (unreadCount.value > 0) unreadCount.value = Math.max(0, unreadCount.value - 1)
    try {
      await apiMarkAsRead(notification.id)
    } catch (err) {
      console.error('mark as read failed', err)
      // restore on failure
      notifications.value = prevNotifications
      unreadCount.value = prevCount
      alert('알림 읽음 처리에 실패했습니다. 잠시 후 다시 시도하세요.')
      return
    }
  }
  // remove regardless (covers case where notification was already read)
  notifications.value = notifications.value.filter((n) => n.id !== notification.id)

  try {
    if (notification.targetType === 'VIDEO') {
      router.push({ name: 'videoDetail', params: { id: notification.targetId } })
    } else if (notification.targetType === 'POST') {
      router.push({ name: 'boardDetail', params: { id: notification.targetId } })
    } else if (notification.targetType === 'USER') {
      if (authStore.userId && Number(authStore.userId) === Number(notification.targetId)) {
        router.push('/mypage')
      } else {
        router.push({ name: 'userProfile', params: { userId: notification.targetId } })
      }
    }
    showNotifications.value = false
  } catch (err) {
    console.error('navigation error', err)
  }
}

const formatTime = (dateString) => {
  return formatKST(dateString)
}

onMounted(() => {
  loadUnreadCount()
})
</script>

<style scoped>
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
  color: #ffffff;
}
.notification-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background: #ef4444;
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}
.notifications-dropdown {
  position: absolute;
  top: 60px;
  right: 20px;
  width: 360px;
  max-height: 500px;
  background: #ffffff;
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
</style>
