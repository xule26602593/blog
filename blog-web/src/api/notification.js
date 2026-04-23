import request from '@/utils/request'

// 获取通知列表
export function getNotifications(type) {
  return request.get('/api/portal/notifications', { params: { type } })
}

// 获取未读数量
export function getUnreadCount() {
  return request.get('/api/portal/notifications/unread-count')
}

// 标记单条已读
export function markAsRead(id) {
  return request.put(`/api/portal/notifications/${id}/read`)
}

// 全部标记已读
export function markAllAsRead() {
  return request.put('/api/portal/notifications/read-all')
}
