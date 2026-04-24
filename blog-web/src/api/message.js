import request from '@/utils/request'

// 获取留言列表（公开）
export function getMessages(params) {
  return request.get('/api/portal/messages', { params })
}

// 提交留言
export function submitMessage(data) {
  return request.post('/api/portal/messages', data)
}

// 管理员获取留言列表
export function getAdminMessages(params) {
  return request.get('/api/admin/messages', { params })
}

// 审核留言
export function auditMessage(id, status) {
  return request.put(`/api/admin/messages/${id}/audit`, null, {
    params: { status }
  })
}

// 删除留言
export function deleteMessage(id) {
  return request.delete(`/api/admin/messages/${id}`)
}
