<template>
  <div class="article-manage">
    <div class="search-bar">
      <van-field
        v-model="queryParams.title"
        placeholder="搜索文章标题"
        clearable
        class="search-input"
      />
      <van-dropdown-menu class="search-dropdown">
        <van-dropdown-item v-model="queryParams.categoryId" :options="categoryOptions" title="分类" />
        <van-dropdown-item v-model="queryParams.status" :options="statusOptions" title="状态" />
      </van-dropdown-menu>
      <div class="search-actions">
        <van-button type="primary" size="small" @click="handleSearch">搜索</van-button>
        <van-button size="small" @click="handleReset">重置</van-button>
      </div>
    </div>

    <div class="toolbar">
      <van-button type="primary" size="small" @click="$router.push('/admin/articles/edit')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="btn-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
        发布文章
      </van-button>
    </div>

    <div class="article-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            v-for="article in articleList"
            :key="article.id"
            class="article-card"
          >
            <div class="article-header">
              <span class="article-id">#{{ article.id }}</span>
              <van-tag :type="getStatusType(article.status)" size="medium">{{ getStatusText(article.status) }}</van-tag>
            </div>
            <h3 class="article-title">{{ article.title }}</h3>
            <div class="article-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
                </svg>
                {{ article.categoryName || '未分类' }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {{ article.viewCount || 0 }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z" />
                </svg>
                {{ article.likeCount || 0 }}
              </span>
            </div>
            <div v-if="article.tags?.length" class="article-tags">
              <van-tag
                v-for="tag in article.tags.slice(0, 3)"
                :key="tag.id"
                plain
                size="small"
                class="tag"
              >
                {{ tag.name }}
              </van-tag>
              <span v-if="article.tags.length > 3" class="tag-more">+{{ article.tags.length - 3 }}</span>
            </div>
            <div class="article-footer">
              <div class="article-actions">
                <van-button type="primary" size="small" plain @click="handleEdit(article)">编辑</van-button>
                <van-button v-if="article.status === 0" type="success" size="small" plain @click="handlePublish(article)">发布</van-button>
                <van-button v-if="article.status === 1" type="warning" size="small" plain @click="handleWithdraw(article)">撤回</van-button>
                <van-button type="danger" size="small" plain @click="handleDelete(article)">删除</van-button>
              </div>
              <div class="article-toggle">
                <span class="toggle-label">置顶</span>
                <van-switch :model-value="article.isTop === 1" size="small" @change="handleTopChange(article)" />
              </div>
            </div>
            <div class="article-time">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              {{ article.createTime }}
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <div class="pagination">
      <van-pagination
        v-model="queryParams.pageNum"
        :total-items="total"
        :items-per-page="queryParams.pageSize"
        :show-page-size="5"
        force-ellipses
        @change="fetchArticles"
      />
    </div>

    <!-- Delete Confirmation Dialog -->
    <van-dialog
      v-model:show="showDeleteDialog"
      title="确认删除"
      message="确定要删除该文章吗？此操作不可恢复。"
      show-cancel-button
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccess } from '@/utils/toast'
import { getAdminArticles, deleteArticle, updateArticleStatus, toggleArticleTop } from '@/api/article'
import { getAdminAllCategories } from '@/api/category'

const router = useRouter()

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const articleList = ref([])
const categories = ref([])
const total = ref(0)
const showDeleteDialog = ref(false)
const articleToDelete = ref(null)

const queryParams = reactive({
  title: '',
  categoryId: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})

const categoryOptions = computed(() => {
  const options = [{ text: '全部分类', value: null }]
  categories.value.forEach(item => {
    options.push({ text: item.name, value: item.id })
  })
  return options
})

const statusOptions = [
  { text: '全部状态', value: null },
  { text: '草稿', value: 0 },
  { text: '已发布', value: 1 },
  { text: '回收站', value: 2 }
]

const getStatusType = (status) => {
  const types = { 0: 'default', 1: 'success', 2: 'danger' }
  return types[Number(status)] || 'default'
}

const getStatusText = (status) => {
  const texts = { 0: '草稿', 1: '已发布', 2: '回收站' }
  return texts[Number(status)] || '未知'
}

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getAdminArticles(queryParams)
    articleList.value = res.data?.records || []
    total.value = res.data?.total || 0
    finished.value = articleList.value.length >= total.value
  } catch (error) {
    console.error('获取文章列表失败', error)
    showToast('获取文章列表失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getAdminAllCategories()
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

const onLoad = () => {
  fetchArticles()
}

const onRefresh = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchArticles()
}

const handleSearch = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchArticles()
}

const handleReset = () => {
  queryParams.title = ''
  queryParams.categoryId = null
  queryParams.status = null
  queryParams.pageNum = 1
  finished.value = false
  fetchArticles()
}

const handleEdit = (row) => {
  router.push(`/admin/articles/edit/${row.id}`)
}

const handlePublish = async (row) => {
  try {
    await updateArticleStatus(row.id, 1)
    showSuccess('发布成功')
    fetchArticles()
  } catch (error) {
    console.error('发布失败', error)
    showToast('发布失败')
  }
}

const handleWithdraw = async (row) => {
  try {
    await updateArticleStatus(row.id, 0)
    showSuccess('撤回成功')
    fetchArticles()
  } catch (error) {
    console.error('撤回失败', error)
    showToast('撤回失败')
  }
}

const handleTopChange = async (row) => {
  try {
    await toggleArticleTop(row.id)
    showSuccess('操作成功')
    fetchArticles()
  } catch (error) {
    console.error('操作失败', error)
    showToast('操作失败')
  }
}

const handleDelete = (row) => {
  articleToDelete.value = row
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!articleToDelete.value) return
  try {
    await deleteArticle(articleToDelete.value.id)
    showSuccess('删除成功')
    fetchArticles()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  } finally {
    articleToDelete.value = null
  }
}

onMounted(() => {
  fetchCategories()
})
</script>

<style lang="scss" scoped>
.article-manage {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: var(--space-2) var(--space-3);
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);

  &:focus {
    outline: none;
    border-color: var(--color-primary);
  }
}

.search-dropdown {
  flex-shrink: 0;
}

.search-actions {
  display: flex;
  gap: var(--space-2);
}

.toolbar {
  display: flex;
  justify-content: flex-end;

  .btn-icon {
    width: 16px;
    height: 16px;
    margin-right: var(--space-1);
  }
}

.article-list {
  min-height: 300px;
}

.article-card {
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-3);
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);
  }
}

.article-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.article-id {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-family: monospace;
}

.article-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  line-height: 1.4;
}

.article-meta {
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

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-1);
  margin-bottom: var(--space-3);
}

.tag {
  background: var(--bg-secondary);
  color: var(--text-secondary);
}

.tag-more {
  font-size: var(--text-xs);
  color: var(--text-muted);
  padding: 2px var(--space-2);
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.article-actions {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.article-toggle {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.toggle-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.article-time {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-top: var(--space-3);
  font-size: var(--text-xs);
  color: var(--text-tertiary);

  svg {
    width: 14px;
    height: 14px;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  padding: var(--space-4) 0;
}

@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }

  .search-dropdown {
    width: 100%;
  }

  .search-actions {
    justify-content: flex-end;
  }

  .toolbar {
    justify-content: center;
  }

  .article-footer {
    flex-direction: column;
    gap: var(--space-3);
    align-items: flex-start;
  }

  .article-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .article-toggle {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
