import request from '@/utils/request'

// 获取会话列表
export function getConversations(params) {
  return request.get('/api/messages/conversations', { params })
}

// 获取会话消息
export function getMessages(conversationId, params) {
  return request.get(`/api/messages/${conversationId}`, { params })
}

// 发送私信
export function sendMessage(data) {
  return request.post('/api/messages', data)
}

// 标记消息已读
export function markAsRead(conversationId) {
  return request.put(`/api/messages/${conversationId}/read`)
}

// 获取未读消息数
export function getUnreadCount() {
  return request.get('/api/messages/unread-count')
}
