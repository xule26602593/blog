import request from '@/utils/request'

export function previewFormat(content, rules) {
  return request.post('/api/admin/format/preview', { content, rules })
}

export function applyFormat(content, rules) {
  return request.post('/api/admin/format/apply', { content, rules })
}

export function getFormatRules() {
  return request.get('/api/admin/format/rules')
}

export function updateRuleStatus(id, status) {
  return request.put(`/api/admin/format/rules/${id}`, { status })
}

export function checkLinks(content) {
  return request.post('/api/admin/format/check-links', { content })
}
