import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import { getUnreadCount } from '@/api/notification'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const unreadNotificationCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)
  const roleCode = computed(() => userInfo.value?.roleCode || '')
  const isAdmin = computed(() => roleCode.value === 'ADMIN')

  // 登录
  async function login(loginData) {
    const res = await loginApi(loginData)
    token.value = res.data.token
    userInfo.value = {
      userId: res.data.userId,
      username: res.data.username,
      nickname: res.data.nickname,
      avatar: res.data.avatar,
      roleCode: res.data.roleCode
    }
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    return res
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const res = await getCurrentUser()
      // 保留 userId，因为后端返回的是 id
      userInfo.value = {
        ...userInfo.value,
        ...res.data,
        userId: res.data.id
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch (error) {
      logout()
    }
  }

  // 退出登录
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 更新用户信息
  function updateUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  // 获取未读通知数
  async function fetchUnreadCount() {
    if (!isLoggedIn.value) return
    try {
      const res = await getUnreadCount()
      unreadNotificationCount.value = res.data || 0
    } catch (e) {
      console.error('获取未读通知数失败', e)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roleCode,
    isAdmin,
    unreadNotificationCount,
    login,
    fetchUserInfo,
    logout,
    updateUserInfo,
    fetchUnreadCount
  }
})
