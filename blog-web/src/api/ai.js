import request from '@/utils/request'

/**
 * 获取认证令牌
 */
function getAuthToken() {
  return localStorage.getItem('token') || ''
}

/**
 * 生成摘要
 */
export function generateSummary(data) {
  return request.post('/api/admin/ai/summary', data)
}

/**
 * 提取标签
 */
export function extractTags(data) {
  return request.post('/api/admin/ai/tags', data)
}

/**
 * 流式请求（使用 fetch + ReadableStream）
 */
export function streamRequest(url, data, onMessage, onComplete, onError) {
  const controller = new AbortController()
  const token = getAuthToken()

  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data),
    signal: controller.signal
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            onComplete?.()
            return
          }
          const text = decoder.decode(value, { stream: true })
          const lines = text.split('\n')
          lines.forEach(line => {
            if (line.startsWith('data:')) {
              const content = line.slice(5).trim()
              if (content) {
                onMessage(content)
              }
            }
          })
          read()
        }).catch(error => {
          if (error.name !== 'AbortError') {
            onError?.(error)
          }
        })
      }
      read()
    })
    .catch(error => {
      if (error.name !== 'AbortError') {
        onError?.(error)
      }
    })

  return controller
}

/**
 * 获取 Prompt 模板列表
 */
export function getPromptTemplates(category) {
  return request.get('/api/admin/ai/prompts', { params: { category } })
}

/**
 * 保存 Prompt 模板
 */
export function savePromptTemplate(data) {
  return data.id
    ? request.put(`/api/admin/ai/prompts/${data.id}`, data)
    : request.post('/api/admin/ai/prompts', data)
}

/**
 * 删除 Prompt 模板
 */
export function deletePromptTemplate(id) {
  return request.delete(`/api/admin/ai/prompts/${id}`)
}
