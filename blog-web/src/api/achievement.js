import request from '@/utils/request'

export function getAchievements() {
  return request.get('/api/portal/achievements')
}

export function getAchievementsByCategory(category) {
  return request.get(`/api/portal/achievements/category/${category}`)
}

export function getUserAchievements() {
  return request.get('/api/portal/achievements/my')
}

export function getUnlockedAchievements() {
  return request.get('/api/portal/achievements/my/unlocked')
}
