import { showToast as vantShowToast } from 'vant'

export function showToast(message, type = 'text') {
  if (typeof message === 'object' && message !== null) {
    return vantShowToast(message)
  }
  return vantShowToast({ type, message })
}

export function showSuccess(message) {
  return vantShowToast({ type: 'success', message })
}

export function showFail(message) {
  return vantShowToast({ type: 'fail', message })
}

export function showLoading(message = '加载中...') {
  return vantShowToast({
    type: 'loading',
    message,
    forbidClick: true,
    duration: 0
  })
}
