import request from '@/utils/request'

// 关注用户
export function followUser(userId) {
  return request.post(`/api/portal/follow/${userId}`)
}

// 取关用户
export function unfollowUser(userId) {
  return request.delete(`/api/portal/follow/${userId}`)
}

// 检查是否已关注
export function checkFollow(userId) {
  return request.get(`/api/portal/follow/check/${userId}`)
}

// 获取关注列表
export function getFollowing(userId) {
  return request.get(`/api/portal/following/${userId}`)
}

// 获取粉丝列表
export function getFollowers(userId) {
  return request.get(`/api/portal/followers/${userId}`)
}
