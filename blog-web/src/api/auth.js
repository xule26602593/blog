import request from '@/utils/request'

// 登录
export function login(data) {
  return request.post('/api/auth/login', data)
}

// 注册
export function register(data) {
  return request.post('/api/auth/register', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get('/api/auth/current')
}

// 更新当前用户信息
export function updateCurrentUser(data) {
  return request.put('/api/auth/current', data)
}

// 修改密码
export function updatePassword(oldPassword, newPassword) {
  return request.put('/api/auth/password', null, {
    params: { oldPassword, newPassword }
  })
}
