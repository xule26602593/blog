import request from '@/utils/request'

// 分页查询文章评论（旧接口）
export function getComments(articleId, params) {
  return request.get(`/api/portal/comments/article/${articleId}`, { params })
}

// 获取文章评论列表（支持排序）
export function getCommentsList(articleId, params) {
  return request({
    url: `/api/portal/comments/article/${articleId}/list`,
    method: 'get',
    params: {
      sortBy: params.sortBy || 'hot',
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

// 发表评论
export function addComment(data) {
  return request.post('/api/portal/comments', data)
}

// 创建评论（新接口，返回评论对象）
export function createComment(data) {
  return request({
    url: '/api/portal/comments',
    method: 'post',
    data
  })
}

// 获取评论回复列表
export function getReplies(commentId, params) {
  return request({
    url: `/api/portal/comments/${commentId}/replies`,
    method: 'get',
    params: {
      sortBy: params.sortBy || 'hot',
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

// 点赞/取消点赞评论
export function toggleCommentLike(commentId) {
  return request({
    url: `/api/portal/comments/${commentId}/like`,
    method: 'post'
  })
}

// 获取评论点赞列表
export function getCommentLikes(commentId, params) {
  return request({
    url: `/api/portal/comments/${commentId}/likes`,
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

// 搜索用户（用于@提及）
export function searchUsers(keyword, size = 10) {
  return request({
    url: '/api/portal/users/search',
    method: 'get',
    params: { keyword, size }
  })
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
