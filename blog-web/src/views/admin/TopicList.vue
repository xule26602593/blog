<template>
  <div class="topic-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">话题管理</h2>
      <van-button type="primary" size="small" @click="showCreateDialog = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="btn-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
        新建话题
      </van-button>
    </div>

    <!-- 状态筛选标签 -->
    <div class="status-tabs">
      <van-tag
        v-for="tab in statusTabs"
        :key="tab.value"
        :type="currentStatus === tab.value ? 'primary' : 'default'"
        size="large"
        class="status-tab"
        @click="handleStatusChange(tab.value)"
      >
        {{ tab.label }}
        <span class="tab-count">{{ tab.count }}</span>
      </van-tag>
    </div>

    <!-- 话题卡片列表 -->
    <div class="topic-cards">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            v-for="topic in topicList"
            :key="topic.id"
            class="topic-card"
            @click="handleCardClick(topic)"
          >
            <div class="card-header">
              <div class="header-left">
                <span class="status-dot" :class="getStatusClass(topic.status)"></span>
                <van-tag :type="getStatusTagType(topic.status)" size="medium">
                  {{ getStatusText(topic.status) }}
                </van-tag>
              </div>
              <div class="header-right">
                <van-tag v-if="topic.priority" :type="getPriorityType(topic.priority)" plain size="small">
                  {{ getPriorityText(topic.priority) }}
                </van-tag>
              </div>
            </div>

            <h3 class="topic-title">{{ topic.title }}</h3>

            <div class="topic-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.696.696 1.828.696 2.484 0l4.318-4.318c.696-.696.696-1.828 0-2.484l-9.581-9.581A2.25 2.25 0 0011.318 3H9.568z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6z" />
                </svg>
                {{ getSourceText(topic.source) }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ topic.createTime }}
              </span>
            </div>

            <!-- AI分析状态 -->
            <div v-if="topic.aiAnalyzed" class="ai-status">
              <van-tag type="success" size="small">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="tag-icon">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                AI已分析
              </van-tag>
            </div>
            <div v-else-if="topic.aiAnalyzing" class="ai-status">
              <van-tag type="warning" size="small">
                <van-loading size="12" class="tag-loading" />
                分析中...
              </van-tag>
            </div>

            <!-- 操作按钮 -->
            <div class="card-actions" @click.stop>
              <van-button
                v-if="topic.status === 'PENDING'"
                type="primary"
                size="small"
                plain
                @click="handleStartWriting(topic)"
              >
                开始写作
              </van-button>
              <van-button
                v-if="!topic.aiAnalyzed && !topic.aiAnalyzing"
                type="success"
                size="small"
                plain
                @click="handleAnalyze(topic)"
              >
                AI分析
              </van-button>
              <van-button type="default" size="small" plain @click="handleEdit(topic)">
                编辑
              </van-button>
              <van-button type="danger" size="small" plain @click="handleDelete(topic)">
                删除
              </van-button>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 分页组件 -->
    <div class="pagination">
      <van-pagination
        v-model="queryParams.pageNum"
        :total-items="total"
        :items-per-page="queryParams.pageSize"
        :show-page-size="5"
        force-ellipses
        @change="fetchTopics"
      />
    </div>

    <!-- 新建话题对话框 -->
    <van-dialog
      v-model:show="showCreateDialog"
      title="新建话题"
      show-cancel-button
      :confirm-button-text="submitting ? '创建中...' : '创建'"
      :confirm-button-disabled="submitting"
      @confirm="handleCreate"
      @cancel="resetForm"
    >
      <div class="create-form">
        <van-field
          v-model="formData.title"
          label="话题标题"
          placeholder="请输入话题标题"
          required
          :rules="[{ required: true, message: '请输入话题标题' }]"
        />
        <van-field
          v-model="formData.sourceText"
          is-link
          readonly
          label="来源"
          placeholder="请选择来源"
          required
          @click="showSourcePicker = true"
        />
        <van-field
          v-model="formData.description"
          label="描述"
          type="textarea"
          rows="3"
          placeholder="请输入话题描述（可选）"
        />
        <van-field label="自动AI分析">
          <template #input>
            <van-switch v-model="formData.autoAnalyze" size="small" />
          </template>
        </van-field>
      </div>
    </van-dialog>

    <!-- 来源选择器弹出层 -->
    <van-popup v-model:show="showSourcePicker" position="bottom" round>
      <van-picker
        title="选择来源"
        :columns="sourceOptions"
        @confirm="onSourceConfirm"
        @cancel="showSourcePicker = false"
      />
    </van-popup>

    <!-- 删除确认对话框 -->
    <van-dialog
      v-model:show="showDeleteDialog"
      title="确认删除"
      message="确定要删除该话题吗？此操作不可恢复。"
      show-cancel-button
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getTopics, createTopic, deleteTopic, analyzeTopic } from '@/api/topic'

const router = useRouter()

// 列表状态
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const topicList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null
})

// 当前状态筛选
const currentStatus = ref(null)

// 状态统计（模拟数据，实际应从后端获取）
const statusCounts = ref({
  all: 0,
  pending: 0,
  writing: 0,
  published: 0,
  abandoned: 0
})

// 对话框状态
const showCreateDialog = ref(false)
const showSourcePicker = ref(false)
const showDeleteDialog = ref(false)
const submitting = ref(false)
const topicToDelete = ref(null)

// 表单数据
const formData = reactive({
  title: '',
  source: 'MANUAL',
  sourceText: '手动创建',
  description: '',
  autoAnalyze: true
})

// 状态标签配置
const statusTabs = computed(() => [
  { label: '全部', value: null, count: statusCounts.value.all },
  { label: '待写', value: 'PENDING', count: statusCounts.value.pending },
  { label: '写作中', value: 'WRITING', count: statusCounts.value.writing },
  { label: '已发布', value: 'PUBLISHED', count: statusCounts.value.published },
  { label: '放弃', value: 'ABANDONED', count: statusCounts.value.abandoned }
])

// 来源选项
const sourceOptions = [
  { text: '手动创建', value: 'MANUAL' },
  { text: '读者提问', value: 'READER_QUESTION' },
  { text: '热门话题', value: 'TRENDING' },
  { text: '竞品分析', value: 'COMPETITOR' },
  { text: 'SEO建议', value: 'SEO_SUGGESTION' },
  { text: '其他', value: 'OTHER' }
]

// 获取状态样式类
const getStatusClass = (status) => {
  const classes = {
    'PENDING': 'status-pending',
    'WRITING': 'status-writing',
    'PUBLISHED': 'status-published',
    'ABANDONED': 'status-abandoned'
  }
  return classes[status] || 'status-pending'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const types = {
    'PENDING': 'default',
    'WRITING': 'primary',
    'PUBLISHED': 'success',
    'ABANDONED': 'danger'
  }
  return types[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    'PENDING': '待写',
    'WRITING': '写作中',
    'PUBLISHED': '已发布',
    'ABANDONED': '已放弃'
  }
  return texts[status] || '未知'
}

// 获取优先级标签类型
const getPriorityType = (priority) => {
  const types = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'default'
  }
  return types[priority] || 'default'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const texts = {
    'HIGH': '高优先级',
    'MEDIUM': '中优先级',
    'LOW': '低优先级'
  }
  return texts[priority] || ''
}

// 获取来源文本
const getSourceText = (source) => {
  const option = sourceOptions.find(opt => opt.value === source)
  return option ? option.text : source
}

// 获取话题列表
const fetchTopics = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (currentStatus.value) {
      params.status = currentStatus.value
    }
    const res = await getTopics(params)
    topicList.value = res.data?.records || []
    total.value = res.data?.total || 0
    finished.value = topicList.value.length >= total.value

    // 更新状态统计（模拟）
    updateStatusCounts()
  } catch (error) {
    console.error('获取话题列表失败', error)
    showToast('获取话题列表失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 更新状态统计
const updateStatusCounts = () => {
  // 实际项目中应从后端获取统计数据
  statusCounts.value = {
    all: total.value,
    pending: topicList.value.filter(t => t.status === 'PENDING').length,
    writing: topicList.value.filter(t => t.status === 'WRITING').length,
    published: topicList.value.filter(t => t.status === 'PUBLISHED').length,
    abandoned: topicList.value.filter(t => t.status === 'ABANDONED').length
  }
}

// 加载更多
const onLoad = () => {
  fetchTopics()
}

// 下拉刷新
const onRefresh = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchTopics()
}

// 切换状态筛选
const handleStatusChange = (status) => {
  currentStatus.value = status
  queryParams.pageNum = 1
  finished.value = false
  fetchTopics()
}

// 点击卡片进入详情
const handleCardClick = (topic) => {
  router.push(`/admin/topics/${topic.id}`)
}

// 开始写作
const handleStartWriting = (topic) => {
  router.push({
    path: '/admin/articles/edit',
    query: { topicId: topic.id, title: topic.title }
  })
}

// AI分析
const handleAnalyze = async (topic) => {
  try {
    topic.aiAnalyzing = true
    await analyzeTopic(topic.id)
    showToast({ type: 'success', message: 'AI分析已开始' })
    // 重新获取列表以更新状态
    fetchTopics()
  } catch (error) {
    console.error('触发AI分析失败', error)
    showToast('触发AI分析失败')
    topic.aiAnalyzing = false
  }
}

// 编辑话题
const handleEdit = (topic) => {
  router.push(`/admin/topics/${topic.id}`)
}

// 删除话题
const handleDelete = (topic) => {
  topicToDelete.value = topic
  showDeleteDialog.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!topicToDelete.value) return
  try {
    await deleteTopic(topicToDelete.value.id)
    showToast({ type: 'success', message: '删除成功' })
    fetchTopics()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  } finally {
    topicToDelete.value = null
  }
}

// 来源选择确认
const onSourceConfirm = ({ selectedOptions }) => {
  const selected = selectedOptions[0]
  formData.source = selected.value
  formData.sourceText = selected.text
  showSourcePicker.value = false
}

// 创建话题
const handleCreate = async () => {
  if (!formData.title.trim()) {
    showToast('请输入话题标题')
    return
  }
  if (!formData.source) {
    showToast('请选择来源')
    return
  }

  submitting.value = true
  try {
    await createTopic({
      title: formData.title,
      source: formData.source,
      description: formData.description,
      autoAnalyze: formData.autoAnalyze
    })
    showToast({ type: 'success', message: '创建成功' })
    showCreateDialog.value = false
    resetForm()
    queryParams.pageNum = 1
    fetchTopics()
  } catch (error) {
    console.error('创建话题失败', error)
    showToast('创建话题失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.title = ''
  formData.source = 'MANUAL'
  formData.sourceText = '手动创建'
  formData.description = ''
  formData.autoAnalyze = true
}

onMounted(() => {
  fetchTopics()
})
</script>

<style lang="scss" scoped>
.topic-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .btn-icon {
    width: 16px;
    height: 16px;
    margin-right: var(--space-1);
  }
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0;
}

.status-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
}

.status-tab {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.tab-count {
  font-size: var(--text-xs);
  opacity: 0.8;
}

.topic-cards {
  min-height: 300px;
}

.topic-card {
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-3);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-2);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;

  &.status-pending {
    background: var(--text-muted);
  }

  &.status-writing {
    background: var(--color-primary);
  }

  &.status-published {
    background: var(--color-success);
  }

  &.status-abandoned {
    background: var(--color-danger);
  }
}

.topic-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-2) 0;
  line-height: 1.4;
}

.topic-meta {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--text-muted);

  svg {
    width: 16px;
    height: 16px;
  }
}

.ai-status {
  margin-bottom: var(--space-3);

  .tag-icon {
    width: 12px;
    height: 12px;
    margin-right: 2px;
  }

  .tag-loading {
    margin-right: 4px;
  }
}

.card-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.pagination {
  display: flex;
  justify-content: center;
  padding: var(--space-4) 0;
}

.create-form {
  padding: var(--space-4);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: var(--space-3);
    align-items: stretch;
  }

  .status-tabs {
    justify-content: center;
  }

  .card-actions {
    flex-direction: column;

    .van-button {
      width: 100%;
    }
  }
}
</style>
