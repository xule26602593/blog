<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="ai-writing-panel">
      <div class="panel-header">
        <span class="title">AI 写作助手</span>
        <van-icon name="cross" @click="visible = false" />
      </div>

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

        <van-tab title="扩写" name="expand">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入需要扩写的内容"
            />
            <van-radio-group v-model="expandDirection" direction="horizontal">
              <van-radio name="丰富内容细节">丰富细节</van-radio>
              <van-radio name="增加实例说明">增加实例</van-radio>
              <van-radio name="深入分析阐述">深入分析</van-radio>
            </van-radio-group>
          </div>
        </van-tab>

        <van-tab title="改写" name="rewrite">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入需要改写的内容"
            />
            <van-radio-group v-model="rewriteStyle" direction="horizontal">
              <van-radio name="default">通俗易懂</van-radio>
              <van-radio name="formal">正式专业</van-radio>
              <van-radio name="casual">轻松活泼</van-radio>
              <van-radio name="concise">简洁精炼</van-radio>
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

        <van-tab title="纠错" name="proofread">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入需要纠错的内容"
            />
          </div>
        </van-tab>
      </van-tabs>

      <div class="action-bar">
        <van-button v-if="!streamState.isStreaming.value" type="primary" block @click="handleGenerate">
          生成
        </van-button>
        <van-button v-else type="danger" block @click="handleCancel">
          取消
        </van-button>
      </div>

      <div class="output-area">
        <div class="output-header">
          <span>生成结果</span>
          <div class="output-actions">
            <van-button v-if="streamState.isComplete.value || proofreadResult" size="small" @click="handleCopy">
              复制
            </van-button>
            <van-button v-if="streamState.isComplete.value || proofreadResult" type="primary" size="small" @click="handleApply">
              应用到编辑器
            </van-button>
          </div>
        </div>
        <div class="output-content">
          <template v-if="streamState.isLoading.value">
            <van-loading size="24px">生成中...</van-loading>
          </template>
          <template v-else-if="activeTab === 'proofread' && proofreadResult">
            <div v-if="proofreadResult.errors?.length > 0" class="error-list">
              <div v-for="(error, index) in proofreadResult.errors" :key="index" class="error-item">
                <van-tag :type="getErrorTagType(error.type)">{{ error.type }}</van-tag>
                <span class="error-original">{{ error.original }}</span>
                <van-icon name="arrow" />
                <span class="error-corrected">{{ error.suggestion }}</span>
              </div>
            </div>
            <div v-else class="no-error">
              <van-icon name="passed" color="#07c160" />
              <span>未发现明显错误</span>
            </div>
            <div v-if="proofreadResult.correctedText" class="corrected-text">
              <div class="corrected-label">修改后文本：</div>
              <pre>{{ proofreadResult.correctedText }}</pre>
            </div>
          </template>
          <template v-else-if="streamState.content.value">
            <div ref="outputRef" class="markdown-body" v-html="renderedContent"></div>
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
import { ref, computed, watch, nextTick } from 'vue'
import { showToast, showSuccess } from '@/utils/toast'
import { marked } from 'marked'
import { streamRequest, proofreadContent } from '@/api/ai'
import { useStreamState } from '@/composables/useStreamState'

const props = defineProps({
  show: Boolean,
  articleTitle: String,
  articleContent: String,
  initialTab: String
})

const emit = defineEmits(['apply-content', 'update:show', 'update:initial-tab'])

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
const expandDirection = ref('丰富内容细节')
const rewriteStyle = ref('default')
const proofreadResult = ref(null)
const outputRef = ref(null)

const renderedContent = computed(() => {
  if (!streamState.content.value) return ''
  return marked(streamState.content.value)
})

watch(streamState.content, () => {
  if (streamState.isStreaming.value && outputRef.value) {
    nextTick(() => {
      outputRef.value.scrollIntoView({ behavior: 'smooth', block: 'end' })
    })
  }
})

let abortController = null

const resetState = () => {
  inputText.value = ''
  description.value = ''
  streamState.reset()
  proofreadResult.value = null
  if (abortController) {
    abortController.abort()
    abortController = null
  }
}

watch(() => props.show, (show) => {
  if (show) {
    resetState()
    if (props.articleTitle && activeTab.value === 'outline') {
      inputText.value = props.articleTitle
    }
    if (props.articleContent && ['continue', 'polish', 'titles', 'expand', 'rewrite', 'proofread'].includes(activeTab.value)) {
      inputText.value = props.articleContent
    }
  }
})

watch(activeTab, () => {
  inputText.value = ''
  description.value = ''
  streamState.reset()
  proofreadResult.value = null
})

watch(() => props.initialTab, (newTab) => {
  if (newTab && ['outline', 'continue', 'polish', 'titles', 'expand', 'rewrite', 'proofread'].includes(newTab)) {
    activeTab.value = newTab
    emit('update:initial-tab', null)
  }
})

const handleGenerate = async () => {
  if (!inputText.value.trim()) {
    const placeholderMap = {
      outline: '请输入文章标题',
      continue: '请输入上下文内容',
      polish: '请输入需要润色的内容',
      titles: '请输入文章内容',
      expand: '请输入需要扩写的内容',
      rewrite: '请输入需要改写的内容',
      proofread: '请输入需要纠错的内容'
    }
    showToast(placeholderMap[activeTab.value] || '请输入内容')
    return
  }

  if (activeTab.value === 'proofread') {
    streamState.start()
    try {
      const res = await proofreadContent(inputText.value)
      streamState.firstByte()
      streamState.append(res.data.correctedText || '')
      proofreadResult.value = res.data
      streamState.complete()
    } catch (error) {
      streamState.error(error.message || '纠错失败')
    }
    return
  }

  streamState.start()

  const data = {
    type: activeTab.value
  }

  if (activeTab.value === 'outline') {
    data.title = inputText.value
    data.description = description.value
    data.style = outlineStyle.value
  } else if (activeTab.value === 'continue') {
    data.context = inputText.value
    data.direction = direction.value
  } else if (activeTab.value === 'polish') {
    data.content = inputText.value
    data.style = polishStyle.value
  } else if (activeTab.value === 'titles') {
    data.content = inputText.value
    data.count = 5
  } else if (activeTab.value === 'expand') {
    data.content = inputText.value
    data.direction = expandDirection.value
  } else if (activeTab.value === 'rewrite') {
    data.content = inputText.value
    data.style = rewriteStyle.value
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

const handleCancel = () => {
  if (abortController) {
    abortController.abort()
  }
  streamState.cancel()
}

const handleCopy = async () => {
  const textToCopy = activeTab.value === 'proofread' && proofreadResult.value
    ? proofreadResult.value.correctedText
    : streamState.content.value
  try {
    await navigator.clipboard.writeText(textToCopy)
    showSuccess('已复制')
  } catch {
    showToast('复制失败')
  }
}

const handleApply = () => {
  const contentToApply = activeTab.value === 'proofread' && proofreadResult.value
    ? proofreadResult.value.correctedText
    : streamState.content.value
  emit('apply-content', contentToApply)
  visible.value = false
}

const getErrorTagType = (type) => {
  const typeMap = {
    '拼写': 'danger',
    '语法': 'warning',
    '标点': 'primary',
    '表达': 'success'
  }
  return typeMap[type] || 'default'
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

.output-content::-webkit-scrollbar {
  width: 6px;
}

.output-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.output-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

@media (max-width: 768px) {
  .output-content::-webkit-scrollbar {
    width: 10px;
  }
  
  .output-content::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 5px;
  }
  
  .output-content::-webkit-scrollbar-thumb {
    background: #b0b0b0;
    border-radius: 5px;
  }
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

.error-list {
  margin-bottom: 16px;
}

.error-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #fff7e6;
  border-radius: 4px;
  margin-bottom: 8px;
}

.error-original {
  color: #d4380d;
  text-decoration: line-through;
}

.error-corrected {
  color: #389e0d;
  font-weight: 500;
}

.no-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  justify-content: center;
  color: #52c41a;
}

.corrected-text {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.corrected-label {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.markdown-body {
  line-height: 1.6;

  h1, h2, h3, h4, h5, h6 {
    margin: 16px 0 8px;
    font-weight: 600;
  }

  h1 { font-size: 1.5em; }
  h2 { font-size: 1.3em; }
  h3 { font-size: 1.1em; }

  ul, ol {
    padding-left: 20px;
    margin: 8px 0;
  }

  li {
    margin: 4px 0;
  }

  p {
    margin: 8px 0;
  }

  code {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 0.9em;
  }

  pre {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;

    code {
      background: none;
      padding: 0;
    }
  }

  blockquote {
    border-left: 3px solid #ddd;
    padding-left: 12px;
    margin: 8px 0;
    color: #666;
  }

  hr {
    border: none;
    border-top: 1px solid #eee;
    margin: 16px 0;
  }
}
</style>
