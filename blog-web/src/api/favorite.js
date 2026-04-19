import request from '@/utils/request'

// 获取收藏列表
export function getFavorites(params) {
  return request.get('/api/portal/favorites', { params })
}

// 收藏/取消收藏文章（复用现有API）
export function favoriteArticle(id) {
  return request.post(`/api/portal/article/${id}/favorite`)
}
