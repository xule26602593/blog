<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="ai-writing-panel">
      <!-- 标题栏 -->
      <div class="panel-header">
        <span class="title">AI 写作助手</span>
        <van-icon name="cross" @click="visible = false" />
      </div>

      <!-- 功能标签页 -->
      <van-tabs v-model:active="activeTab">
        <van-tab title="大纲" name="outline">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              label="标题"
              placeholder="请输入文章标题"
            />
            <van-field
              v-model="description"
              label="说明"
              type="textarea"
              rows="2"
              placeholder="补充说明（可选）"
            />
            <van-radio-group v-model="outlineStyle" direction="horizontal">
              <van-radio name="tech">技术文章</van-radio>
              <van-radio name="tutorial">教程文章</van-radio>
            </van-radio-group>
          </div>
        </van-tab>

        <van-tab title="续写" name="continue">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入上下文内容"
            />
            <van-radio-group v-model="direction" direction="horizontal">
              <van-radio name="继续写">继续写</van-radio>
              <van-radio name="向下写">向下写</van-radio>
            </van-radio-group>
          </div>
        </van-tab>

        <van-tab title="润色" name="polish">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入需要润色的内容"
            />
            <van-radio-group v-model="polishStyle" direction="horizontal">
              <van-radio name="formal">正式风格</van-radio>
              <van-radio name="casual">轻松风格</van-radio>
            </van-radio-group>
          </div>
        </van-tab>

        <van-tab title="标题" name="titles">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入文章内容，AI将生成标题建议"
            />
          </div>
        </van-tab>
      </van-tabs>

      <!-- 操作按钮 -->
      <div class="action-bar">
        <van-button v-if="!streamState.isStreaming.value" type="primary" block @click="handleGenerate">
          生成
        </van-button>
        <van-button v-else type="danger" block @click="handleCancel">
          取消
        </van-button>
      </div>

      <!-- 输出区域 -->
      <div class="output-area">
        <div class="output-header">
          <span>生成结果</span>
          <div class="output-actions">
            <van-button v-if="streamState.isComplete.value" size="small" @click="handleCopy">
              复制
            </van-button>
            <van-button v-if="streamState.isComplete.value" type="primary" size="small" @click="handleApply">
              应用到编辑器
            </van-button>
          </div>
        </div>
        <div class="output-content">
          <template v-if="streamState.isLoading.value">
            <van-loading size="24px">生成中...</van-loading>
          </template>
          <template v-else-if="streamState.content.value">
            <pre>{{ streamState.content.value }}</pre>
            <span v-if="streamState.isStreaming.value" class="cursor">█</span>
          </template>
          <template v-else>
            <span class="placeholder">生成结果将在这里显示</span>
          </template>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { showToast } from 'vant'
import { streamRequest } from '@/api/ai'
import { useStreamState } from '@/composables/useStreamState'

const props = defineProps({
  show: Boolean,
  articleTitle: String,
  articleContent: String
})

const emit = defineEmits(['apply-content', 'update:show'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const streamState = useStreamState()
const activeTab = ref('outline')
const inputText = ref('')
const description = ref('')
const outlineStyle = ref('tech')
const direction = ref('继续写')
const polishStyle = ref('formal')

let abortController = null

// 从文章标题自动填充
watch(() => props.show, (show) => {
  if (show && props.articleTitle && activeTab.value === 'outline') {
    inputText.value = props.articleTitle
  }
})

const handleGenerate = () => {
  streamState.start()

  const data = {
    type: activeTab.value
  }

  if (activeTab.value === 'outline') {
    data.title = inputText.value
    data.description = description.value
    data.style = outlineStyle.value
    // 使用 GET 请求
    generateOutline(data)
  } else {
    if (activeTab.value === 'continue') {
      data.context = inputText.value
      data.direction = direction.value
    } else if (activeTab.value === 'polish') {
      data.content = inputText.value
      data.style = polishStyle.value
    } else if (activeTab.value === 'titles') {
      data.content = inputText.value
      data.count = 5
    }

    abortController = streamRequest(
      '/api/admin/ai/writing/stream',
      data,
      (chunk) => {
        if (streamState.isLoading.value) streamState.firstByte()
        streamState.append(chunk)
      },
      () => streamState.complete(),
      (error) => streamState.error(error.message)
    )
  }
}

const generateOutline = async (data) => {
  // 大纲使用 GET 请求
  const url = `/api/admin/ai/writing/outline?title=${encodeURIComponent(data.title)}&description=${encodeURIComponent(data.description || '')}&style=${data.style}`
  const token = localStorage.getItem('token') || ''

  abortController = new AbortController()

  fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    },
    signal: abortController.signal
  })
    .then(response => {
      const reader = response.body.getReader()
      const decoder = new TextDecoder()

      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            streamState.complete()
            return
          }
          const text = decoder.decode(value, { stream: true })
          if (streamState.isLoading.value) streamState.firstByte()
          const lines = text.split('\n')
          lines.forEach(line => {
            if (line.startsWith('data:')) {
              const content = line.slice(5).trim()
              if (content) streamState.append(content)
            }
          })
          read()
        })
      }
      read()
    })
    .catch(error => {
      if (error.name !== 'AbortError') {
        streamState.error(error.message)
      }
    })
}

const handleCancel = () => {
  if (abortController) {
    abortController.abort()
  }
  streamState.cancel()
}

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(streamState.content.value)
    showToast({ type: 'success', message: '已复制' })
  } catch {
    showToast('复制失败')
  }
}

const handleApply = () => {
  emit('apply-content', streamState.content.value)
  visible.value = false
}
</script>

<style scoped>
.ai-writing-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 16px;
  font-weight: 500;
}

.tab-content {
  padding: 16px;
}

.action-bar {
  padding: 16px;
  border-top: 1px solid #eee;
}

.output-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #f7f8fa;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.output-actions {
  display: flex;
  gap: 8px;
}

.output-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: white;
  border-radius: 4px;
}

.output-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.placeholder {
  color: #999;
}
</style>
