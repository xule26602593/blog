import request from '@/utils/request'

// 分页查询文章列表
export function getArticles(params) {
  return request.get('/api/portal/articles', { params })
}

// 获取文章详情
export function getArticle(id) {
  return request.get(`/api/portal/article/${id}`)
}

// 获取热门文章
export function getHotArticles(limit = 5) {
  return request.get('/api/portal/articles/hot', { params: { limit } })
}

// 获取置顶文章
export function getTopArticles() {
  return request.get('/api/portal/articles/top')
}

// 搜索文章
export function searchArticles(keyword, pageNum = 1, pageSize = 10) {
  return request.get('/api/portal/articles/search', {
    params: { keyword, pageNum, pageSize }
  })
}

// 获取归档列表
export function getArchiveList() {
  return request.get('/api/portal/articles/archive')
}

// 按分类查询文章
export function getArticlesByCategory(categoryId, pageNum = 1, pageSize = 10) {
  return request.get(`/api/portal/articles/category/${categoryId}`, {
    params: { pageNum, pageSize }
  })
}

// 按标签查询文章
export function getArticlesByTag(tagId, pageNum = 1, pageSize = 10) {
  return request.get(`/api/portal/articles/tag/${tagId}`, {
    params: { pageNum, pageSize }
  })
}

// 点赞文章
export function likeArticle(id) {
  return request.post(`/api/portal/article/${id}/like`)
}

// 收藏文章
export function favoriteArticle(id) {
  return request.post(`/api/portal/article/${id}/favorite`)
}

// ========== 管理后台 ==========

// 分页查询文章（管理）
export function getAdminArticles(params) {
  return request.get('/api/admin/articles', { params })
}

// 保存或更新文章
export function saveArticle(data) {
  return request.post('/api/admin/articles', data)
}

// 删除文章
export function deleteArticle(id) {
  return request.delete(`/api/admin/articles/${id}`)
}

// 更新文章状态
export function updateArticleStatus(id, status) {
  return request.put(`/api/admin/articles/${id}/status`, null, {
    params: { status }
  })
}

// 切换置顶状态
export function toggleArticleTop(id) {
  return request.put(`/api/admin/articles/${id}/top`)
}
