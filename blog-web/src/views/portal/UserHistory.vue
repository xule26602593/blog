<template>
  <div class="history-page">
    <!-- 操作栏 -->
    <div class="action-bar">
      <span class="total-count">共 {{ total }} 条记录</span>
      <button v-if="total > 0" class="clear-btn" @click="handleClear">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.74 9l-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 01-2.244 2.077H8.084a2.25 2.25 0 01-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 00-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 013.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 00-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 00-7.5 0" />
        </svg>
        清空历史
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <div v-else-if="historyList.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <p class="empty-text">暂无阅读历史</p>
      <p class="empty-hint">浏览文章时会自动记录</p>
    </div>

    <!-- 历史列表 -->
    <div v-else class="history-list">
      <div
        v-for="item in historyList"
        :key="item.id"
        class="history-card"
        @click="goToArticle(item.articleId)"
      >
        <div v-if="item.coverImage" class="card-cover">
          <img :src="item.coverImage" :alt="item.title" loading="lazy" />
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ item.title }}</h3>
          <div class="card-meta">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <span>{{ formatDate(item.lastReadTime) }}</span>
          </div>
        </div>
        <button class="remove-btn" @click.stop="handleRemove(item.articleId)">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="pagination">
      <button
        class="page-btn"
        :disabled="pageNum === 1"
        @click="changePage(pageNum - 1)"
      >
        上一页
      </button>
      <span class="page-info">{{ pageNum }} / {{ totalPages }}</span>
      <button
        class="page-btn"
        :disabled="pageNum >= totalPages"
        @click="changePage(pageNum + 1)"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getHistory, deleteHistory, clearHistory } from '@/api/history'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const historyList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchHistory = async () => {
  loading.value = true
  try {
    const res = await getHistory({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    historyList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取阅读历史失败', error)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  pageNum.value = page
  fetchHistory()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const handleRemove = async (articleId) => {
  try {
    await showConfirmDialog({
      title: '删除记录',
      message: '确定要删除这条阅读记录吗？'
    })
    await deleteHistory(articleId)
    showToast({ type: 'success', message: '已删除' })
    fetchHistory()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
    }
  }
}

const handleClear = async () => {
  try {
    await showConfirmDialog({
      title: '清空历史',
      message: '确定要清空所有阅读历史吗？此操作不可恢复。'
    })
    await clearHistory()
    showToast({ type: 'success', message: '已清空' })
    historyList.value = []
    total.value = 0
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空失败', error)
    }
  }
}

const formatDate = (date) => dayjs(date).format('MM-DD HH:mm')

onMounted(() => {
  fetchHistory()
})
</script>

<style lang="scss" scoped>
.history-page {
  max-width: 900px;
  margin: 0 auto;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.total-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.clear-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-danger, #ef4444);
  background: transparent;
  border: 1px solid currentColor;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--color-danger, #ef4444);
    color: white;
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 40vh;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-16) var(--space-6);
  color: var(--text-muted);
}

.empty-icon {
  margin-bottom: var(--space-6);

  svg {
    width: 64px;
    height: 64px;
    opacity: 0.5;
  }
}

.empty-text {
  font-size: var(--text-lg);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.empty-hint {
  font-size: var(--text-sm);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.history-card {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-3);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-sm);
  }
}

.card-cover {
  flex-shrink: 0;
  width: 80px;
  height: 60px;
  border-radius: var(--radius-md);
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.card-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: var(--space-2);
}

.card-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--text-muted);

  svg {
    width: 14px;
    height: 14px;
  }
}

.remove-btn {
  position: absolute;
  top: 50%;
  right: var(--space-3);
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-full);
  color: var(--text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition-fast);

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--color-danger, #ef4444);
  }
}

.history-card:hover .remove-btn {
  opacity: 1;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-6);
}

.page-btn {
  height: 36px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.page-info {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

@media (max-width: 640px) {
  .card-cover {
    width: 60px;
    height: 45px;
  }

  .remove-btn {
    opacity: 1;
  }
}
</style>
