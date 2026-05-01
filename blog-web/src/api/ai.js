import request from '@/utils/request'

function getAuthToken() {
  return localStorage.getItem('token') || ''
}

function processSSEEvent(eventText) {
  const lines = eventText.split('\n')
  let dataLine = ''
  
  for (const line of lines) {
    if (line.startsWith('data:')) {
      dataLine += line.slice(5)
    }
  }
  
  if (!dataLine.trim()) return null
  
  if (dataLine.trim() === '[DONE]') {
    return { type: 'done' }
  }
  
  try {
    return JSON.parse(dataLine)
  } catch {
    return { type: 'text', text: dataLine }
  }
}

export function generateSummary(data) {
  return request.post('/api/admin/ai/summary', data)
}

export function extractTags(data) {
  return request.post('/api/admin/ai/tags', data)
}

export function streamRequest(url, data, onMessage, onComplete, onError) {
  const controller = new AbortController()
  const token = getAuthToken()
  let buffer = ''

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
            if (buffer.trim()) {
              const parsed = processSSEEvent(buffer)
              if (parsed) handleParsed(parsed, onMessage, onComplete, onError)
            }
            onComplete?.()
            return
          }
          
          buffer += decoder.decode(value, { stream: true })
          
          // SSE事件以双换行分隔
          const events = buffer.split('\n\n')
          buffer = events.pop() || ''
          
          events.forEach(event => {
            if (event.trim()) {
              const parsed = processSSEEvent(event)
              if (parsed) handleParsed(parsed, onMessage, onComplete, onError)
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

function handleParsed(parsed, onMessage, onComplete, onError) {
  if (parsed.type === 'delta') {
    onMessage(parsed.text)
  } else if (parsed.type === 'error') {
    onError?.(new Error(parsed.message))
  } else if (parsed.type === 'done') {
    onComplete?.()
  } else if (parsed.text) {
    onMessage(parsed.text)
  }
}

export function getPromptTemplates(category) {
  return request.get('/api/admin/ai/prompts', { params: { category } })
}

export function savePromptTemplate(data) {
  return data.id
    ? request.put(`/api/admin/ai/prompts/${data.id}`, data)
    : request.post('/api/admin/ai/prompts', data)
}

export function deletePromptTemplate(id) {
  return request.delete(`/api/admin/ai/prompts/${id}`)
}

export function expandWriting(content, direction = '丰富内容细节', onMessage, onComplete, onError) {
  return streamRequest(
    '/api/admin/ai/writing/stream',
    { type: 'expand', content, direction },
    onMessage,
    onComplete,
    onError
  )
}

export function rewriteWriting(content, style = 'default', onMessage, onComplete, onError) {
  return streamRequest(
    '/api/admin/ai/writing/stream',
    { type: 'rewrite', content, style },
    onMessage,
    onComplete,
    onError
  )
}

export function proofreadContent(content) {
  return request.post('/api/admin/ai/writing/proofread', { content })
}
