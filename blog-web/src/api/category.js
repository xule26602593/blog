import request from '@/utils/request'

// 获取分类列表
export function getCategories() {
  return request.get('/api/portal/categories')
}

// ========== 管理后台 ==========

// 分页查询分类
export function getAdminCategories(params) {
  return request.get('/api/admin/categories/page', { params })
}

// 获取所有分类（管理）
export function getAdminAllCategories() {
  return request.get('/api/admin/categories')
}

// 保存或更新分类
export function saveCategory(data) {
  return request.post('/api/admin/categories', data)
}

// 删除分类
export function deleteCategory(id) {
  return request.delete(`/api/admin/categories/${id}`)
}
