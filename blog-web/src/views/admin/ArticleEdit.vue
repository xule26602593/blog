<template>
  <div class="article-edit">
    <div class="edit-card">
      <div class="edit-header">
        <h2 class="edit-title">{{ isEdit ? '编辑文章' : '发布文章' }}</h2>
      </div>

      <van-form ref="formRef" @submit="handleSubmit" class="edit-form">
        <van-cell-group inset>
          <van-field
            v-model="form.title"
            label="标题"
            placeholder="请输入文章标题"
            :rules="[{ required: true, message: '请输入文章标题' }]"
            maxlength="200"
            show-word-limit
          />

          <van-field
            v-model="form.summary"
            label="摘要"
            type="textarea"
            rows="3"
            autosize
            placeholder="请输入文章摘要"
            maxlength="500"
            show-word-limit
          >
            <template #button>
              <van-button size="small" :loading="aiLoading" @click="handleGenerateSummary">
                AI生成
              </van-button>
            </template>
          </van-field>
        </van-cell-group>

        <div class="form-section">
          <label class="form-label">内容</label>
          <div class="content-toolbar">
            <van-popover
              v-model:show="showAiMenu"
              placement="top-start"
              :actions="aiMenuActions"
              @select="onAiMenuSelect"
            >
              <template #reference>
                <van-button type="primary" size="small">
                  AI 助手
                  <van-icon name="arrow-down" />
                </van-button>
              </template>
            </van-popover>
            <van-button size="small" @click="showTemplateSelector = true">
              模板
            </van-button>
            <van-button size="small" @click="showFormatPanel = true">
              排版
            </van-button>
          </div>
          <MdEditor
            ref="editorRef"
            v-model="form.content"
            :style="{ height: '500px' }"
            placeholder="请输入文章内容，支持Markdown格式"
            :toolbars="editorToolbars"
            :preview="false"
            @onUploadImg="onUploadImg"
          />
        </div>

        <div class="form-section">
          <label class="form-label">封面</label>
          <div class="cover-upload" @click="triggerCoverUpload">
            <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
            <div v-else class="cover-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
              </svg>
              <span>上传封面</span>
            </div>
          </div>
          <input ref="coverInputRef" type="file" accept="image/*" hidden @change="handleCoverChange" />
        </div>

        <van-cell-group inset>
          <van-field
            v-model="categoryName"
            is-link
            readonly
            label="分类"
            placeholder="请选择分类"
            @click="showCategoryPicker = true"
            :rules="[{ required: true, message: '请选择分类' }]"
          />

          <van-field
            v-model="tagNames"
            is-link
            readonly
            label="标签"
            placeholder="请选择标签"
            @click="showTagPicker = true"
          >
            <template #button>
              <van-button size="small" :loading="aiLoading" @click.stop="handleExtractTags">
                AI提取
              </van-button>
            </template>
          </van-field>

          <van-field name="switch" label="置顶">
            <template #input>
              <van-switch v-model="form.isTop" :active-value="1" :inactive-value="0" size="small" />
            </template>
          </van-field>
        </van-cell-group>

        <div class="form-actions">
          <van-button @click="$router.back()">取消</van-button>
          <van-button type="default" @click="handleSaveDraft">保存草稿</van-button>
          <van-button type="primary" native-type="submit">发布文章</van-button>
        </div>
      </van-form>

      <!-- Category Picker -->
      <van-popup v-model:show="showCategoryPicker" position="bottom" round>
        <van-picker
          title="选择分类"
          :columns="categoryColumns"
          @confirm="onCategoryConfirm"
          @cancel="showCategoryPicker = false"
        />
      </van-popup>

      <!-- Tag Picker -->
      <van-popup v-model:show="showTagPicker" position="bottom" round>
        <div class="tag-picker">
          <div class="tag-picker-header">
            <span>选择标签</span>
            <van-button size="small" @click="showTagPicker = false">确定</van-button>
          </div>
          <div class="tag-picker-content">
            <van-checkbox-group v-model="form.tagIds">
              <van-checkbox
                v-for="tag in tags"
                :key="tag.id"
                :name="tag.id"
                shape="square"
              >
                {{ tag.name }}
              </van-checkbox>
            </van-checkbox-group>
          </div>
        </div>
      </van-popup>

      <!-- AI Result Dialog -->
      <AiResultDialog
        v-model:show="showAiDialog"
        :title="aiDialogTitle"
        :content="aiDialogContent"
        :type="aiDialogType"
        @apply="handleApplyAiResult"
      />

      <!-- AI Writing Panel -->
      <AiWritingPanel
        v-model:show="showWritingPanel"
        :article-title="form.title"
        :article-content="form.content"
        :initial-tab="selectedAiTab"
        @apply-content="handleApplyWritingContent"
        @update:initial-tab="selectedAiTab = null"
      />

      <!-- Template Selector -->
      <TemplateSelector
        v-model:show="showTemplateSelector"
        @select="handleTemplateSelect"
      />

      <!-- Format Panel -->
      <FormatPanel
        v-model:show="showFormatPanel"
        :content="form.content"
        @apply="handleFormatApply"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getArticle, saveArticle } from '@/api/article'
import { getAdminAllCategories } from '@/api/category'
import { getAdminAllTags } from '@/api/tag'
import { uploadImage } from '@/api/admin'
import { generateSummary, extractTags } from '@/api/ai'
import AiResultDialog from '@/components/AiResultDialog.vue'
import AiWritingPanel from '@/components/AiWritingPanel.vue'
import TemplateSelector from '@/components/writing/TemplateSelector.vue'
import FormatPanel from '@/components/writing/FormatPanel.vue'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const editorRef = shallowRef()
const coverInputRef = ref()
const categories = ref([])
const tags = ref([])
const showCategoryPicker = ref(false)
const showTagPicker = ref(false)

// AI 相关状态
const showAiDialog = ref(false)
const aiDialogTitle = ref('')
const aiDialogContent = ref('')
const aiDialogType = ref('other')
const aiLoading = ref(false)
const showWritingPanel = ref(false)
const showTemplateSelector = ref(false)
const showFormatPanel = ref(false)
const showAiMenu = ref(false)
const selectedAiTab = ref(null)
const aiMenuActions = [
  { text: 'AI 写作助手', value: 'assistant' },
  { text: '生成大纲', value: 'outline' },
  { text: '续写内容', value: 'continue' },
  { text: '扩写内容', value: 'expand' },
  { text: '改写内容', value: 'rewrite' },
  { text: '润色文本', value: 'polish' },
  { text: '生成标题', value: 'titles' },
  { text: '检查纠错', value: 'proofread' }
]

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  categoryId: null,
  tagIds: [],
  isTop: 0,
  status: 0
})

const editorToolbars = [
  'bold', 'underline', 'italic', '-', 'strikeThrough', 'title', 'sub', 'sup',
  'quote', 'unorderedList', 'orderedList', 'task', '-', 'codeRow', 'code',
  'link', 'image', 'table', 'mermaid', '-', 'revoke', 'next', '=',
  'pageFullscreen', 'fullscreen', 'preview', 'previewOnly', 'catalog', 'htmlPreview'
]

const categoryColumns = computed(() => {
  return categories.value.map(item => ({ text: item.name, value: item.id }))
})

const categoryName = computed(() => {
  const cat = categories.value.find(c => c.id === form.categoryId)
  return cat?.name || ''
})

const tagNames = computed(() => {
  return form.tagIds
    .map(id => tags.value.find(t => t.id === id)?.name)
    .filter(Boolean)
    .join(', ')
})

const fetchCategories = async () => {
  try {
    const res = await getAdminAllCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败', error)
  }
}

const fetchTags = async () => {
  try {
    const res = await getAdminAllTags()
    tags.value = res.data || []
  } catch (error) {
    console.error('获取标签失败', error)
  }
}

const fetchArticle = async () => {
  if (!route.params.id) return
  try {
    const res = await getArticle(route.params.id)
    const data = res.data
    form.id = data.id
    form.title = data.title
    form.summary = data.summary
    form.content = data.content
    form.coverImage = data.coverImage
    form.categoryId = data.categoryId
    form.tagIds = data.tags?.map(t => t.id) || []
    form.isTop = data.isTop
    form.status = data.status
  } catch (error) {
    console.error('获取文章失败', error)
  }
}

const onUploadImg = async (files, callback) => {
  const urls = await Promise.all(
    files.map(async (file) => {
      const res = await uploadImage(file)
      return res.data.url
    })
  )
  callback(urls)
}

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    form.coverImage = res.data.url
    showToast({ type: 'success', message: '封面上传成功' })
  } catch (error) {
    console.error('上传封面失败', error)
    showToast('上传封面失败')
  }
  event.target.value = ''
}

const onCategoryConfirm = ({ selectedOptions }) => {
  form.categoryId = selectedOptions[0]?.value
  showCategoryPicker.value = false
}

const handleSaveDraft = async () => {
  form.status = 0
  await submitForm()
}

const handleSubmit = async () => {
  form.status = 1
  await submitForm()
}

const submitForm = async () => {
  try {
    await saveArticle(form)
    showToast({ type: 'success', message: isEdit.value ? '更新成功' : '发布成功' })
    router.push('/admin/articles')
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  }
}

// AI 功能方法
const handleGenerateSummary = async () => {
  if (!form.title && !form.content) {
    showToast('请先填写标题或内容')
    return
  }

  aiLoading.value = true
  try {
    const res = await generateSummary({
      title: form.title,
      content: form.content
    })
    aiDialogTitle.value = 'AI生成的摘要'
    aiDialogContent.value = res.data
    aiDialogType.value = 'summary'
    showAiDialog.value = true
  } catch (error) {
    console.error('生成摘要失败', error)
    showToast('生成摘要失败')
  } finally {
    aiLoading.value = false
  }
}

const handleExtractTags = async () => {
  if (!form.title && !form.content) {
    showToast('请先填写标题或内容')
    return
  }

  aiLoading.value = true
  try {
    const res = await extractTags({
      title: form.title,
      content: form.content
    })
    // 将标签应用到表单
    if (res.data.existingTags?.length > 0) {
      const existingTagIds = res.data.existingTags.map(tag => tag.id)
      form.tagIds = [...new Set([...form.tagIds, ...existingTagIds])]
    }
    if (res.data.newTagNames?.length > 0) {
      showToast({ type: 'success', message: `发现 ${res.data.newTagNames.length} 个新标签建议: ${res.data.newTagNames.join(', ')}` })
    } else {
      showToast({ type: 'success', message: '标签提取成功' })
    }
  } catch (error) {
    console.error('标签提取失败', error)
    showToast('标签提取失败')
  } finally {
    aiLoading.value = false
  }
}

const handleApplyAiResult = (content) => {
  if (aiDialogType.value === 'summary') {
    form.summary = content
  }
}

const handleApplyWritingContent = (content) => {
  form.content += '\n\n' + content
}

const onAiMenuSelect = (action) => {
  if (action.value === 'assistant') {
    showWritingPanel.value = true
  } else {
    showWritingPanel.value = true
    selectedAiTab.value = action.value
  }
}

const handleTemplateSelect = async (template) => {
  if (!template) return
  form.content = template.content
  showToast({ type: 'success', message: `已应用模板: ${template.name}` })
}

const handleFormatApply = (content) => {
  form.content = content
  showToast({ type: 'success', message: '排版完成' })
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  if (isEdit.value) {
    fetchArticle()
  }
})
</script>

<style lang="scss" scoped>
.article-edit {
  max-width: 900px;
}

.edit-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  overflow: hidden;
}

.edit-header {
  padding: var(--space-6);
  border-bottom: 1px solid var(--border-light);
}

.edit-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
}

.edit-form {
  padding: var(--space-4);
}

.form-section {
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}

.form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.content-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: var(--space-2);
}

:deep(.van-popover__wrapper) {
  display: inline-block;
}

.cover-upload {
  width: 200px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  overflow: hidden;

  &:hover {
    border-color: var(--text-muted);
  }
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-muted);

  svg {
    width: 24px;
    height: 24px;
  }

  span {
    font-size: var(--text-sm);
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.tag-picker {
  background: var(--bg-card);
}

.tag-picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);

  span {
    font-size: var(--text-base);
    font-weight: var(--font-semibold);
  }
}

.tag-picker-content {
  padding: var(--space-4);
  max-height: 300px;
  overflow-y: auto;

  :deep(.van-checkbox) {
    margin-bottom: var(--space-3);
  }
}

:deep(.van-cell-group--inset) {
  margin: 0;
  margin-bottom: var(--space-4);
}

:deep(.van-field__label) {
  width: auto;
}

@media (max-width: 768px) {
  .cover-upload {
    width: 100%;
    height: 150px;
  }

  .form-actions {
    flex-wrap: wrap;

    .van-button {
      flex: 1;
    }
  }
}
</style>
