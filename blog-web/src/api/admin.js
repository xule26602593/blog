import request from '@/utils/request'

// 获取仪表盘数据
export function getDashboard() {
  return request.get('/api/admin/dashboard')
}

// 上传图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/api/admin/upload/image', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
