import request from '@/utils/request'

// 获取标签列表
export function getTags() {
  return request.get('/api/portal/tags')
}

// ========== 管理后台 ==========

// 分页查询标签
export function getAdminTags(params) {
  return request.get('/api/admin/tags/page', { params })
}

// 获取所有标签（管理）
export function getAdminAllTags() {
  return request.get('/api/admin/tags')
}

// 保存或更新标签
export function saveTag(data) {
  return request.post('/api/admin/tags', data)
}

// 删除标签
export function deleteTag(id) {
  return request.delete(`/api/admin/tags/${id}`)
}
