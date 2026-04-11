import request from '@/utils/request'

// 分页查询文章评论
export function getComments(articleId, params) {
  return request.get(`/api/portal/comments/article/${articleId}`, { params })
}

// 发表评论
export function addComment(data) {
  return request.post('/api/portal/comments', data)
}

// ========== 管理后台 ==========

// 分页查询评论（管理）
export function getAdminComments(params) {
  return request.get('/api/admin/comments', { params })
}

// 审核评论
export function auditComment(id, status) {
  return request.put(`/api/admin/comments/${id}/audit`, null, {
    params: { status }
  })
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/api/admin/comments/${id}`)
}

// 获取待审核数量
export function getPendingCount() {
  return request.get('/api/admin/comments/pending/count')
}
