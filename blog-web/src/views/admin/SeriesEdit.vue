<template>
  <div class="series-edit">
    <div class="edit-card">
      <div class="edit-header">
        <h2 class="edit-title">{{ isEdit ? '编辑系列' : '新建系列' }}</h2>
      </div>

      <van-form ref="formRef" @submit="handleSubmit" class="edit-form">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="名称"
            placeholder="请输入系列名称"
            :rules="[{ required: true, message: '请输入系列名称' }]"
            maxlength="100"
            show-word-limit
          />

          <van-field
            v-model="form.description"
            label="介绍"
            type="textarea"
            rows="3"
            autosize
            placeholder="请输入系列介绍"
            maxlength="500"
            show-word-limit
          />
        </van-cell-group>

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
          <van-field name="radio" label="模式">
            <template #input>
              <van-radio-group v-model="form.mode" direction="horizontal">
                <van-radio :name="0">有序（章节式）</van-radio>
                <van-radio :name="1">无序（主题式）</van-radio>
              </van-radio-group>
            </template>
          </van-field>

          <van-field name="switch" label="状态">
            <template #input>
              <van-switch v-model="form.status" :active-value="1" :inactive-value="0" size="small" />
            </template>
          </van-field>
        </van-cell-group>

        <div class="form-section">
          <label class="form-label">文章管理</label>
          <div class="article-list">
            <div v-for="(article, index) in seriesArticles" :key="article.id" class="article-item">
              <div v-if="form.mode === 0" class="chapter-order">
                <van-button size="small" :disabled="index === 0" @click="moveArticle(index, -1)">↑</van-button>
                <span class="order-num">第{{ index + 1 }}章</span>
                <van-button size="small" :disabled="index === seriesArticles.length - 1" @click="moveArticle(index, 1)">↓</van-button>
              </div>
              <div class="article-info">
                <span class="article-title">{{ article.title }}</span>
              </div>
              <van-button size="small" type="danger" @click="removeArticle(index)">移除</van-button>
            </div>
            <van-empty v-if="seriesArticles.length === 0" description="暂无文章" image-size="60" />
          </div>
          <van-button block type="default" @click="showArticlePicker = true" class="add-article-btn">
            添加文章
          </van-button>
        </div>

        <div class="form-actions">
          <van-button @click="$router.back()">取消</van-button>
          <van-button type="primary" native-type="submit">保存</van-button>
        </div>
      </van-form>
    </div>

    <!-- Article Picker -->
    <van-popup v-model:show="showArticlePicker" position="bottom" round style="height: 60%">
      <div class="article-picker">
        <div class="picker-header">
          <span>选择文章</span>
          <van-button size="small" @click="showArticlePicker = false">关闭</van-button>
        </div>
        <van-search v-model="articleSearchKeyword" placeholder="搜索文章" @search="searchArticles" />
        <div class="picker-content">
          <div
            v-for="article in availableArticles"
            :key="article.id"
            class="picker-item"
            @click="addArticle(article)"
          >
            <span>{{ article.title }}</span>
            <van-icon name="plus" />
          </div>
          <van-empty v-if="availableArticles.length === 0" description="暂无可添加的文章" />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, shallowRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showSuccess } from '@/utils/toast'
import { getAdminSeriesDetail, saveSeries, addArticlesToSeries, removeArticleFromSeries, updateArticlesOrder } from '@/api/series'
import { getAdminArticles } from '@/api/article'
import { uploadImage } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const formRef = ref()
const coverInputRef = ref()
const showArticlePicker = ref(false)
const articleSearchKeyword = ref('')
const availableArticles = ref([])
const seriesArticles = ref([])

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  id: null,
  name: '',
  description: '',
  coverImage: '',
  mode: 0,
  sort: 0,
  status: 1
})

const triggerCoverUpload = () => {
  coverInputRef.value?.click()
}

const handleCoverChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadImage(file)
    form.coverImage = res.data.url
    showSuccess('封面上传成功')
  } catch (error) {
    console.error('上传封面失败', error)
    showToast('上传封面失败')
  }
  event.target.value = ''
}

const fetchSeries = async () => {
  if (!route.params.id) return
  try {
    const res = await getAdminSeriesDetail(route.params.id)
    const data = res.data
    form.id = data.id
    form.name = data.name
    form.description = data.description
    form.coverImage = data.coverImage
    form.mode = data.mode
    form.status = data.status
    seriesArticles.value = data.articles || []
  } catch (error) {
    console.error('获取系列失败', error)
  }
}

const searchArticles = async () => {
  try {
    const res = await getAdminArticles({
      pageNum: 1,
      pageSize: 20,
      title: articleSearchKeyword.value || undefined,
      status: 1
    })
    const existingIds = new Set(seriesArticles.value.map(a => a.id))
    availableArticles.value = (res.data?.records || []).filter(a => !existingIds.has(a.id))
  } catch (error) {
    console.error('搜索文章失败', error)
  }
}

const addArticle = async (article) => {
  if (isEdit.value && form.id) {
    try {
      await addArticlesToSeries(form.id, [article.id])
      seriesArticles.value.push({
        id: article.id,
        title: article.title,
        chapterOrder: seriesArticles.value.length + 1
      })
      showSuccess('添加成功')
    } catch (error) {
      console.error('添加文章失败', error)
      showToast('添加失败')
    }
  } else {
    seriesArticles.value.push({
      id: article.id,
      title: article.title,
      chapterOrder: seriesArticles.value.length + 1
    })
  }
  showArticlePicker.value = false
}

const removeArticle = async (index) => {
  if (isEdit.value && form.id) {
    try {
      await removeArticleFromSeries(form.id, seriesArticles.value[index].id)
      seriesArticles.value.splice(index, 1)
      showSuccess('移除成功')
    } catch (error) {
      console.error('移除文章失败', error)
      showToast('移除失败')
    }
  } else {
    seriesArticles.value.splice(index, 1)
  }
}

const moveArticle = async (index, direction) => {
  const newIndex = index + direction
  const temp = seriesArticles.value[index]
  seriesArticles.value[index] = seriesArticles.value[newIndex]
  seriesArticles.value[newIndex] = temp

  if (isEdit.value && form.id) {
    try {
      await updateArticlesOrder(form.id, seriesArticles.value.map(a => a.id))
    } catch (error) {
      console.error('更新顺序失败', error)
    }
  }
}

const handleSubmit = async () => {
  try {
    const data = {
      ...form,
      articleIds: isEdit.value ? undefined : seriesArticles.value.map(a => a.id)
    }
    await saveSeries(data)
    showSuccess(isEdit.value ? '更新成功' : '创建成功')
    router.push('/admin/series')
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  }
}

onMounted(() => {
  searchArticles()
  if (isEdit.value) {
    fetchSeries()
  }
})
</script>

<style lang="scss" scoped>
.series-edit {
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

.article-list {
  margin-bottom: var(--space-4);
}

.article-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-2);
}

.chapter-order {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.order-num {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  min-width: 50px;
  text-align: center;
}

.article-info {
  flex: 1;
  padding: 0 var(--space-3);
}

.article-title {
  font-size: var(--text-sm);
}

.add-article-btn {
  margin-top: var(--space-2);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
  padding: var(--space-4);
  border-top: 1px solid var(--border-light);
}

.article-picker {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.picker-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
}

.picker-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  cursor: pointer;

  &:hover {
    background: var(--bg-hover);
  }
}

:deep(.van-cell-group--inset) {
  margin: 0;
  margin-bottom: var(--space-4);
}

:deep(.van-field__label) {
  width: auto;
}
</style>
