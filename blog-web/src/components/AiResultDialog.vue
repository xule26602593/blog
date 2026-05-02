<template>
  <van-dialog
    v-model:show="visible"
    :title="title"
    :show-confirm-button="false"
    close-on-click-overlay
  >
    <div class="ai-result-dialog">
      <!-- 预览/编辑区域 -->
      <div class="content-area">
        <template v-if="!isEditing">
          <div class="preview-content" v-html="renderedContent"></div>
        </template>
        <template v-else>
          <van-field
            v-model="editedContent"
            type="textarea"
            rows="6"
            autosize
            placeholder="请编辑内容"
          />
        </template>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <div class="left-buttons">
          <van-button v-if="!isEditing" size="small" @click="isEditing = true">
            编辑
          </van-button>
          <van-button v-else size="small" @click="isEditing = false">
            取消编辑
          </van-button>
          <van-button size="small" @click="handleCopy">
            复制
          </van-button>
        </div>
        <van-button type="primary" size="small" @click="handleApply">
          应用
        </van-button>
      </div>
    </div>
  </van-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { showToast, showSuccess } from '@/utils/toast'

const props = defineProps({
  show: Boolean,
  title: {
    type: String,
    default: 'AI生成结果'
  },
  content: String,
  type: {
    type: String,
    default: 'other'
  }
})

const emit = defineEmits(['apply', 'update:show'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const isEditing = ref(false)
const editedContent = ref('')

watch(() => props.content, (newVal) => {
  editedContent.value = newVal || ''
  isEditing.value = false
}, { immediate: true })

// 简单的 Markdown 渲染（实际项目可使用 md-editor-v3）
const renderedContent = computed(() => {
  return props.content?.replace(/\n/g, '<br>') || ''
})

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(props.content)
    showSuccess('已复制')
  } catch {
    showToast('复制失败')
  }
}

const handleApply = () => {
  const finalContent = isEditing.value ? editedContent.value : props.content
  emit('apply', finalContent)
  visible.value = false
}
</script>

<style scoped>
.ai-result-dialog {
  padding: 16px;
}

.content-area {
  min-height: 150px;
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.preview-content {
  padding: 8px;
  background: #f7f8fa;
  border-radius: 4px;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-buttons {
  display: flex;
  gap: 8px;
}
</style>
