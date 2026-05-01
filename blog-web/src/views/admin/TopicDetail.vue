<template>
  <div class="topic-detail">
    <!-- 导航栏 -->
    <van-nav-bar
      title="话题详情"
      left-arrow
      @click-left="$router.back()"
    >
      <template #right>
        <van-button type="primary" size="small" plain @click="showEditDialog = true">
          编辑
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading size="24px">加载中...</van-loading>
    </div>

    <template v-else-if="topic">
      <!-- 基本信息卡片 -->
      <div class="info-card">
        <div class="card-header">
          <h3 class="card-title">基本信息</h3>
          <van-tag :type="getStatusTagType(topic.status)" size="medium">
            {{ getStatusText(topic.status) }}
          </van-tag>
        </div>

        <div class="info-content">
          <div class="info-row">
            <label class="info-label">标题</label>
            <div class="info-value title-value">{{ topic.title }}</div>
          </div>

          <div class="info-row">
            <label class="info-label">来源</label>
            <div class="info-value">
              <van-tag type="default" size="small">{{ getSourceText(topic.source) }}</van-tag>
            </div>
          </div>

          <div v-if="topic.description" class="info-row">
            <label class="info-label">描述</label>
            <div class="info-value description-value">{{ topic.description }}</div>
          </div>

          <div v-if="topic.sourceLink" class="info-row">
            <label class="info-label">来源链接</label>
            <div class="info-value">
              <a :href="topic.sourceLink" target="_blank" class="source-link">
                {{ topic.sourceLink }}
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="external-icon">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 6H5.25A2.25 2.25 0 003 8.25v10.5A2.25 2.25 0 005.25 21h10.5A2.25 2.25 0 0018 18.75V10.5m-10.5 6L21 3m0 0h-5.25M21 3v5.25" />
                </svg>
              </a>
            </div>
          </div>

          <div class="info-row">
            <label class="info-label">创建时间</label>
            <div class="info-value">{{ topic.createTime }}</div>
          </div>

          <div v-if="topic.priority" class="info-row">
            <label class="info-label">优先级</label>
            <div class="info-value">
              <van-tag :type="getPriorityType(topic.priority)" size="small">
                {{ getPriorityText(topic.priority) }}
              </van-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- AI分析结果卡片 -->
      <div class="analysis-card">
        <div class="card-header">
          <h3 class="card-title">AI分析结果</h3>
          <div class="analysis-status">
            <!-- 待分析状态 -->
            <template v-if="aiStatus === 'pending'">
              <van-button
                type="primary"
                size="small"
                :loading="analyzing"
                @click="handleAnalyze"
              >
                {{ analyzing ? '分析中...' : '开始分析' }}
              </van-button>
            </template>
            <!-- 分析中状态 -->
            <template v-else-if="aiStatus === 'analyzing'">
              <van-tag type="warning" size="medium">
                <van-loading size="12" class="status-loading" />
                分析中...
              </van-tag>
            </template>
            <!-- 已完成状态 -->
            <template v-else-if="aiStatus === 'completed'">
              <van-tag type="success" size="medium">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="status-icon">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                分析完成
              </van-tag>
            </template>
            <!-- 失败状态 -->
            <template v-else-if="aiStatus === 'failed'">
              <van-tag type="danger" size="medium">分析失败</van-tag>
              <van-button type="primary" size="small" @click="handleAnalyze">重新分析</van-button>
            </template>
          </div>
        </div>

        <div class="analysis-content">
          <!-- 待分析提示 -->
          <div v-if="aiStatus === 'pending'" class="empty-analysis">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456zM16.894 20.567L16.5 21.75l-.394-1.183a2.25 2.25 0 00-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 001.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 001.423 1.423l1.183.394-1.183.394a2.25 2.25 0 00-1.423 1.423z" />
            </svg>
            <p>点击"开始分析"让AI为您分析这个话题</p>
          </div>

          <!-- 分析中提示 -->
          <div v-else-if="aiStatus === 'analyzing'" class="analyzing-hint">
            <van-loading size="32" />
            <p>AI正在分析话题内容，请稍候...</p>
            <p class="hint-text">分析内容可能包括写作角度、目标受众、难度评估等</p>
          </div>

          <!-- 分析结果 -->
          <template v-else-if="aiStatus === 'completed' && analysisResult">
            <div class="result-section">
              <h4 class="section-title">写作角度</h4>
              <div class="section-content">
                <p>{{ analysisResult.writingAngle || '暂无' }}</p>
              </div>
            </div>

            <div class="result-section">
              <h4 class="section-title">目标受众</h4>
              <div class="section-content">
                <p>{{ analysisResult.targetAudience || '暂无' }}</p>
              </div>
            </div>

            <div class="result-section">
              <h4 class="section-title">难度预期</h4>
              <div class="section-content">
                <van-tag :type="getDifficultyType(analysisResult.difficulty)" size="medium">
                  {{ getDifficultyText(analysisResult.difficulty) }}
                </van-tag>
              </div>
            </div>

            <div v-if="analysisResult.keywords?.length" class="result-section">
              <h4 class="section-title">关键词</h4>
              <div class="section-content keywords">
                <van-tag
                  v-for="keyword in analysisResult.keywords"
                  :key="keyword"
                  type="primary"
                  plain
                  size="small"
                  class="keyword-tag"
                >
                  {{ keyword }}
                </van-tag>
              </div>
            </div>

            <div v-if="analysisResult.valueAssessment" class="result-section">
              <h4 class="section-title">价值评估</h4>
              <div class="section-content">
                <p>{{ analysisResult.valueAssessment }}</p>
              </div>
            </div>

            <div v-if="analysisResult.writingSuggestions" class="result-section">
              <h4 class="section-title">写作建议</h4>
              <div class="section-content suggestions">
                <ul>
                  <li v-for="(suggestion, index) in analysisResult.writingSuggestions" :key="index">
                    {{ suggestion }}
                  </li>
                </ul>
              </div>
            </div>
          </template>

          <!-- 分析失败提示 -->
          <div v-else-if="aiStatus === 'failed'" class="failed-hint">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="failed-icon">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z" />
            </svg>
            <p>AI分析失败，请点击"重新分析"</p>
            <p v-if="analysisError" class="error-text">{{ analysisError }}</p>
          </div>
        </div>
      </div>

      <!-- 关联文章信息 -->
      <div v-if="topic.articleId" class="article-card">
        <div class="card-header">
          <h3 class="card-title">关联文章</h3>
        </div>
        <div class="article-info">
          <span class="article-id">文章ID: {{ topic.articleId }}</span>
          <van-button type="primary" size="small" plain @click="viewArticle">
            查看文章
          </van-button>
        </div>
      </div>

      <!-- 底部操作栏 -->
      <div class="action-bar">
        <!-- 根据状态显示不同按钮 -->
        <template v-if="topic.status === 'PENDING'">
          <van-button type="primary" block @click="startWriting">
            开始写作
          </van-button>
        </template>
        <template v-else-if="topic.status === 'WRITING'">
          <van-button type="primary" block @click="continueWriting">
            继续写作
          </van-button>
        </template>
        <template v-else-if="topic.status === 'PUBLISHED'">
          <van-button type="default" block @click="viewArticle">
            查看文章
          </van-button>
        </template>

        <!-- 状态更新按钮组 -->
        <div class="status-actions">
          <van-button
            v-if="topic.status === 'PENDING'"
            type="warning"
            size="small"
            @click="handleStatusUpdate('WRITING')"
          >
            标记写作中
          </van-button>
          <van-button
            v-if="topic.status === 'WRITING'"
            type="success"
            size="small"
            @click="handleStatusUpdate('PUBLISHED')"
          >
            标记已发布
          </van-button>
          <van-button
            v-if="['PENDING', 'WRITING'].includes(topic.status)"
            type="default"
            size="small"
            @click="handleStatusUpdate('ABANDONED')"
          >
            放弃
          </van-button>
        </div>
      </div>
    </template>

    <!-- 编辑对话框 -->
    <van-dialog
      v-model:show="showEditDialog"
      title="编辑话题"
      show-cancel-button
      :confirm-button-text="saving ? '保存中...' : '保存'"
      :confirm-button-disabled="saving"
      @confirm="handleSave"
    >
      <div class="edit-form">
        <van-field
          v-model="editForm.title"
          label="标题"
          placeholder="请输入话题标题"
          required
        />
        <van-field
          v-model="editForm.description"
          label="描述"
          type="textarea"
          rows="3"
          placeholder="请输入话题描述"
        />
        <van-field
          v-model="editForm.sourceLink"
          label="来源链接"
          placeholder="请输入来源链接（可选）"
        />
        <van-field name="priority" label="优先级">
          <template #input>
            <van-radio-group v-model="editForm.priority" direction="horizontal">
              <van-radio name="HIGH">高</van-radio>
              <van-radio name="MEDIUM">中</van-radio>
              <van-radio name="LOW">低</van-radio>
            </van-radio-group>
          </template>
        </van-field>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getTopic, updateTopic, analyzeTopic, updateTopicStatus } from '@/api/topic'

const route = useRoute()
const router = useRouter()

// 加载状态
const loading = ref(true)
const saving = ref(false)
const analyzing = ref(false)

// 话题数据
const topic = ref(null)

// 编辑表单
const showEditDialog = ref(false)
const editForm = reactive({
  title: '',
  description: '',
  sourceLink: '',
  priority: 'MEDIUM'
})

// AI分析状态
const aiStatus = ref('pending') // pending, analyzing, completed, failed
const analysisResult = ref(null)
const analysisError = ref('')
let pollingTimer = null

// 来源选项
const sourceOptions = [
  { text: '手动创建', value: 'MANUAL' },
  { text: '读者提问', value: 'READER_QUESTION' },
  { text: '热门话题', value: 'TRENDING' },
  { text: '竞品分析', value: 'COMPETITOR' },
  { text: 'SEO建议', value: 'SEO_SUGGESTION' },
  { text: '其他', value: 'OTHER' }
]

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

// 获取来源文本
const getSourceText = (source) => {
  const option = sourceOptions.find(opt => opt.value === source)
  return option ? option.text : source
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

// 获取难度标签类型
const getDifficultyType = (difficulty) => {
  const types = {
    'BEGINNER': 'success',
    'INTERMEDIATE': 'warning',
    'ADVANCED': 'danger'
  }
  return types[difficulty] || 'default'
}

// 获取难度文本
const getDifficultyText = (difficulty) => {
  const texts = {
    'BEGINNER': '入门级',
    'INTERMEDIATE': '中级',
    'ADVANCED': '高级'
  }
  return texts[difficulty] || difficulty
}

// 加载话题详情
const fetchTopic = async () => {
  loading.value = true
  try {
    const res = await getTopic(route.params.id)
    topic.value = res.data

    // 更新编辑表单
    editForm.title = topic.value.title
    editForm.description = topic.value.description || ''
    editForm.sourceLink = topic.value.sourceLink || ''
    editForm.priority = topic.value.priority || 'MEDIUM'

    // 更新AI分析状态
    updateAiStatus()
  } catch (error) {
    console.error('获取话题详情失败', error)
    showToast('获取话题详情失败')
  } finally {
    loading.value = false
  }
}

// 更新AI分析状态
const updateAiStatus = () => {
  if (!topic.value) return

  if (topic.value.aiAnalyzing) {
    aiStatus.value = 'analyzing'
    // 开始轮询
    startPolling()
  } else if (topic.value.aiAnalyzed && topic.value.aiAnalysisResult) {
    aiStatus.value = 'completed'
    // 解析分析结果
    parseAnalysisResult(topic.value.aiAnalysisResult)
  } else {
    aiStatus.value = 'pending'
  }
}

// 解析AI分析结果
const parseAnalysisResult = (result) => {
  try {
    // 如果是JSON字符串，解析它
    if (typeof result === 'string') {
      analysisResult.value = JSON.parse(result)
    } else {
      analysisResult.value = result
    }
  } catch (error) {
    console.error('解析AI分析结果失败', error)
    analysisResult.value = null
  }
}

// 开始轮询分析结果
const startPolling = () => {
  if (pollingTimer) return

  pollingTimer = setInterval(async () => {
    try {
      const res = await getTopic(route.params.id)
      topic.value = res.data

      if (!topic.value.aiAnalyzing) {
        // 分析完成或失败
        stopPolling()
        updateAiStatus()
      }
    } catch (error) {
      console.error('轮询话题状态失败', error)
    }
  }, 3000)
}

// 停止轮询
const stopPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

// 触发AI分析
const handleAnalyze = async () => {
  analyzing.value = true
  try {
    await analyzeTopic(topic.value.id)
    aiStatus.value = 'analyzing'
    showToast({ type: 'success', message: 'AI分析已开始' })
    // 开始轮询
    startPolling()
  } catch (error) {
    console.error('触发AI分析失败', error)
    showToast('触发AI分析失败')
    aiStatus.value = 'failed'
  } finally {
    analyzing.value = false
  }
}

// 保存编辑
const handleSave = async () => {
  if (!editForm.title.trim()) {
    showToast('请输入话题标题')
    return
  }

  saving.value = true
  try {
    await updateTopic(topic.value.id, {
      title: editForm.title,
      description: editForm.description,
      sourceLink: editForm.sourceLink,
      priority: editForm.priority
    })
    showToast({ type: 'success', message: '保存成功' })
    showEditDialog.value = false
    // 刷新数据
    await fetchTopic()
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  } finally {
    saving.value = false
  }
}

// 更新状态
const handleStatusUpdate = async (status) => {
  try {
    await updateTopicStatus(topic.value.id, status)
    showToast({ type: 'success', message: '状态更新成功' })
    // 刷新数据
    await fetchTopic()
  } catch (error) {
    console.error('更新状态失败', error)
    showToast('更新状态失败')
  }
}

// 开始写作
const startWriting = () => {
  router.push({
    path: '/admin/articles/edit',
    query: { topicId: topic.value.id, title: topic.value.title }
  })
}

// 继续写作
const continueWriting = () => {
  if (topic.value.articleId) {
    router.push({
      path: '/admin/articles/edit',
      query: { id: topic.value.articleId, topicId: topic.value.id }
    })
  } else {
    startWriting()
  }
}

// 查看文章
const viewArticle = () => {
  if (topic.value.articleId) {
    router.push(`/admin/articles/edit?id=${topic.value.articleId}`)
  }
}

// 生命周期
onMounted(() => {
  fetchTopic()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style lang="scss" scoped>
.topic-detail {
  display: flex;
  flex-direction: column;
  min-height: 100%;
  background: var(--bg-secondary);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.info-card,
.analysis-card,
.article-card {
  background: var(--bg-card);
  margin: var(--space-4);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin: 0;
}

.info-content {
  padding: var(--space-4);
}

.info-row {
  display: flex;
  margin-bottom: var(--space-4);

  &:last-child {
    margin-bottom: 0;
  }
}

.info-label {
  width: 80px;
  flex-shrink: 0;
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.info-value {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--text-primary);
  word-break: break-word;
}

.title-value {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

.description-value {
  white-space: pre-wrap;
}

.source-link {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  color: var(--color-primary);
  text-decoration: none;
  word-break: break-all;

  &:hover {
    text-decoration: underline;
  }
}

.external-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.analysis-status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.status-loading {
  margin-right: 4px;
}

.status-icon {
  width: 14px;
  height: 14px;
  margin-right: 2px;
}

.analysis-content {
  padding: var(--space-4);
  min-height: 200px;
}

.empty-analysis,
.analyzing-hint,
.failed-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  text-align: center;
  color: var(--text-muted);
}

.empty-icon,
.failed-icon {
  width: 48px;
  height: 48px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.empty-analysis p,
.failed-hint p {
  margin: 0;
  font-size: var(--text-sm);
}

.analyzing-hint p {
  margin: var(--space-2) 0 0;
  font-size: var(--text-sm);
}

.hint-text {
  font-size: var(--text-xs);
  opacity: 0.7;
}

.error-text {
  font-size: var(--text-xs);
  color: var(--color-danger);
  margin-top: var(--space-2);
}

.result-section {
  margin-bottom: var(--space-4);

  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-muted);
  margin: 0 0 var(--space-2) 0;
}

.section-content {
  font-size: var(--text-sm);
  color: var(--text-primary);

  p {
    margin: 0;
    line-height: 1.6;
  }
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.keyword-tag {
  margin: 0;
}

.suggestions ul {
  margin: 0;
  padding-left: var(--space-4);

  li {
    margin-bottom: var(--space-2);
    line-height: 1.5;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

.article-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
}

.article-id {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.action-bar {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding: var(--space-4);
  margin-top: auto;
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);
}

.status-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
}

.edit-form {
  padding: var(--space-4);
}

:deep(.van-nav-bar) {
  background: var(--bg-card);
}

:deep(.van-field__label) {
  width: 70px;
}

@media (max-width: 768px) {
  .info-row {
    flex-direction: column;
    gap: var(--space-1);
  }

  .info-label {
    width: auto;
  }

  .status-actions {
    flex-wrap: wrap;

    .van-button {
      flex: 1;
    }
  }
}
</style>
