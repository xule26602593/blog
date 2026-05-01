import request from '@/utils/request'

// 获取话题列表
export function getTopics(params) {
  return request.get('/api/admin/topics', { params })
}

// 获取话题详情
export function getTopic(id) {
  return request.get(`/api/admin/topics/${id}`)
}

// 创建话题
export function createTopic(data) {
  return request.post('/api/admin/topics', data)
}

// 更新话题
export function updateTopic(id, data) {
  return request.put(`/api/admin/topics/${id}`, data)
}

// 删除话题
export function deleteTopic(id) {
  return request.delete(`/api/admin/topics/${id}`)
}

// 触发AI分析
export function analyzeTopic(id) {
  return request.post(`/api/admin/topics/${id}/analyze`)
}

// 更新话题状态
export function updateTopicStatus(id, status) {
  return request.put(`/api/admin/topics/${id}/status`, null, {
    params: { status }
  })
}

// 关联文章
export function linkArticle(topicId, articleId) {
  return request.post(`/api/admin/topics/${topicId}/link`, null, {
    params: { articleId }
  })
}
