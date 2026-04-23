import request from '@/utils/request'

// ========== 门户端 ==========

// 分页查询系列列表
export function getSeries(params) {
  return request.get('/api/portal/series', { params })
}

// 获取系列详情
export function getSeriesDetail(id) {
  return request.get(`/api/portal/series/${id}`)
}

// 获取热门系列
export function getHotSeries(limit = 5) {
  return request.get('/api/portal/series/hot', { params: { limit } })
}

// ========== 管理后台 ==========

// 分页查询系列（管理）
export function getAdminSeries(params) {
  return request.get('/api/admin/series', { params })
}

// 获取系列详情（管理）
export function getAdminSeriesDetail(id) {
  return request.get(`/api/admin/series/${id}`)
}

// 保存或更新系列
export function saveSeries(data) {
  return request.post('/api/admin/series', data)
}

// 删除系列
export function deleteSeries(id) {
  return request.delete(`/api/admin/series/${id}`)
}

// 添加文章到系列
export function addArticlesToSeries(seriesId, articleIds) {
  return request.post(`/api/admin/series/${seriesId}/articles`, articleIds)
}

// 从系列移除文章
export function removeArticleFromSeries(seriesId, articleId) {
  return request.delete(`/api/admin/series/${seriesId}/articles/${articleId}`)
}

// 调整文章顺序
export function updateArticlesOrder(seriesId, articleIds) {
  return request.put(`/api/admin/series/${seriesId}/articles/order`, articleIds)
}
