import request from '@/utils/request'

export function getTemplates(category) {
  return request.get('/api/admin/writing-templates', { params: { category } })
}

export function getTemplatePage(params) {
  return request.get('/api/admin/writing-templates/page', { params })
}

export function getTemplate(id) {
  return request.get(`/api/admin/writing-templates/${id}`)
}

export function getTemplateCategories() {
  return request.get('/api/admin/writing-templates/categories')
}

export function createTemplate(data) {
  return request.post('/api/admin/writing-templates', data)
}

export function updateTemplate(id, data) {
  return request.put(`/api/admin/writing-templates/${id}`, data)
}

export function deleteTemplate(id) {
  return request.delete(`/api/admin/writing-templates/${id}`)
}

export function useTemplate(id) {
  return request.post(`/api/admin/writing-templates/${id}/use`)
}
