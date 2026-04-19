import request from '@/utils/request'

// 记录阅读历史
export function recordHistory(articleId) {
  return request.post(`/api/portal/history/${articleId}`)
}

// 获取阅读历史列表
export function getHistory(params) {
  return request.get('/api/portal/history', { params })
}

// 删除单条阅读历史
export function deleteHistory(articleId) {
  return request.delete(`/api/portal/history/${articleId}`)
}

// 清空阅读历史
export function clearHistory() {
  return request.delete('/api/portal/history')
}
