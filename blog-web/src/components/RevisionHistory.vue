<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="revision-history">
      <div class="header">
        <h3>版本历史</h3>
        <van-icon name="cross" @click="visible = false" />
      </div>

      <van-loading v-if="loading" class="loading-container" />

      <div v-else class="revision-list">
        <div
          v-for="rev in revisions"
          :key="rev.id"
          class="revision-item"
          @click="previewRevision(rev)"
        >
          <div class="rev-info">
            <div class="rev-version">版本 {{ rev.version }}</div>
            <div class="rev-meta">
              <span>{{ rev.editorNickname || '作者' }}</span>
              <span>{{ formatTime(rev.createTime) }}</span>
            </div>
            <div v-if="rev.changeNote" class="rev-note">{{ rev.changeNote }}</div>
          </div>
          <van-button size="small" @click.stop="confirmRestore(rev)">恢复</van-button>
        </div>

        <van-empty v-if="revisions.length === 0" description="暂无历史版本" />
      </div>
    </div>

    <!-- 预览弹窗 -->
    <van-dialog v-model:show="showPreview" title="版本预览" :show-confirm-button="false">
      <div class="preview-content">
        <h4>{{ previewData.title }}</h4>
        <div class="preview-text">{{ previewData.summary }}</div>
        <div class="preview-actions">
          <van-button block @click="showPreview = false">关闭</van-button>
        </div>
      </div>
    </van-dialog>
  </van-popup>
</template>

<script setup>
import { ref, watch } from 'vue'
import { showConfirmDialog, showToast } from 'vant'
import request from '@/utils/request'

const props = defineProps({
  show: { type: Boolean, default: false },
  articleId: { type: Number, default: null }
})

const emit = defineEmits(['update:show', 'restored'])

const visible = ref(false)
const loading = ref(false)
const revisions = ref([])
const showPreview = ref(false)
const previewData = ref({})

watch(() => props.show, (val) => {
  visible.value = val
  if (val && props.articleId) {
    fetchRevisions()
  }
})

watch(visible, (val) => {
  emit('update:show', val)
})

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const fetchRevisions = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/admin/articles/${props.articleId}/revisions`, {
      params: { pageNum: 1, pageSize: 20 }
    })
    if (res.data) {
      revisions.value = res.data.records
    }
  } catch (error) {
    console.error('获取版本历史失败:', error)
  } finally {
    loading.value = false
  }
}

const previewRevision = async (rev) => {
  try {
    const res = await request.get(`/api/admin/articles/${props.articleId}/revisions/${rev.version}`)
    if (res.data) {
      previewData.value = res.data
      showPreview.value = true
    }
  } catch (error) {
    showToast('获取版本详情失败')
  }
}

const confirmRestore = (rev) => {
  showConfirmDialog({
    title: '确认恢复',
    message: `确定要恢复到版本 ${rev.version} 吗？当前内容将被覆盖。`
  })
    .then(() => restoreRevision(rev.version))
    .catch(() => {})
}

const restoreRevision = async (version) => {
  try {
    await request.post(`/api/admin/articles/${props.articleId}/revisions/${version}/restore`)
    showToast({ type: 'success', message: '恢复成功' })
    visible.value = false
    emit('restored')
  } catch (error) {
    showToast('恢复失败')
  }
}
</script>

<style lang="scss" scoped>
.revision-history {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);

  h3 {
    font-size: var(--text-lg);
    font-weight: var(--font-semibold);
  }
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.revision-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
}

.revision-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);

  &:hover {
    background: var(--bg-hover);
  }
}

.rev-info {
  flex: 1;
}

.rev-version {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.rev-meta {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-top: var(--space-1);

  span {
    margin-right: var(--space-2);
  }
}

.rev-note {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-top: var(--space-1);
}

.preview-content {
  padding: var(--space-4);

  h4 {
    font-size: var(--text-lg);
    margin-bottom: var(--space-3);
  }
}

.preview-text {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
}

.preview-actions {
  margin-top: var(--space-4);
}
</style>
