import request from '@/utils/request'

export default {
  // 全文搜索
  search(params) {
    return request.get('/api/portal/search', { params })
  },

  // 获取搜索建议
  getSuggestions(prefix) {
    return request.get('/api/portal/search/suggestions', {
      params: { prefix }
    })
  },

  // 获取搜索历史
  getHistory() {
    return request.get('/api/portal/search/history')
  },

  // 获取热门搜索
  getHotKeywords() {
    return request.get('/api/portal/search/hot')
  }
}
