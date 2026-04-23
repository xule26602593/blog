import request from '@/utils/request'

// 获取已发布公告列表（前台）
export function getPublishedAnnouncements() {
  return request.get('/api/portal/announcements')
}

// 获取公告列表（管理端）
export function getAnnouncements() {
  return request.get('/api/admin/announcements')
}

// 获取公告详情
export function getAnnouncement(id) {
  return request.get(`/api/admin/announcements/${id}`)
}

// 创建公告
export function createAnnouncement(data) {
  return request.post('/api/admin/announcements', data)
}

// 更新公告
export function updateAnnouncement(id, data) {
  return request.put(`/api/admin/announcements/${id}`, data)
}

// 删除公告
export function deleteAnnouncement(id) {
  return request.delete(`/api/admin/announcements/${id}`)
}

// 发布公告
export function publishAnnouncement(id) {
  return request.put(`/api/admin/announcements/${id}/publish`)
}
