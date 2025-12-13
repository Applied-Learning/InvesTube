import http from './http'

export function getNotifications() {
  return http.get('/notifications')
}

export function getUnreadCount() {
  return http.get('/notifications/unread-count')
}

export function markAsRead(id) {
  return http.patch(`/notifications/${id}/read`)
}

export function markAllAsRead() {
  return http.patch('/notifications/mark-all-read')
}

export function getNotificationSettings() {
  return http.get('/notifications/settings')
}

export function updateNotificationSettings(settings) {
  return http.put('/notifications/settings', settings)
}
