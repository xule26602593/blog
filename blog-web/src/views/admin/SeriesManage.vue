<template>
  <div class="series-manage">
    <div class="page-header">
      <h2 class="page-title">系列管理</h2>
      <van-button type="primary" size="small" @click="router.push('/admin/series/edit')">
        新建系列
      </van-button>
    </div>

    <van-search
      v-model="searchName"
      placeholder="搜索系列名称"
      @search="handleSearch"
    />

    <div class="series-list">
      <div v-for="series in seriesList" :key="series.id" class="series-item">
        <div class="series-info">
          <img v-if="series.coverImage" :src="series.coverImage" class="series-cover" />
          <div v-else class="series-cover-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
          </div>
          <div class="series-content">
            <h3 class="series-name">{{ series.name }}</h3>
            <div class="series-meta">
              <span class="meta-item">
                <van-tag :type="series.mode === 0 ? 'primary' : 'success'" size="small">
                  {{ series.mode === 0 ? '有序' : '无序' }}
                </van-tag>
              </span>
              <span class="meta-item">{{ series.articleCount }} 篇文章</span>
              <span class="meta-item">{{ series.viewCount }} 次浏览</span>
            </div>
          </div>
        </div>
        <div class="series-actions">
          <van-button size="small" @click="router.push('/admin/series/edit/' + series.id)">编辑</van-button>
          <van-button size="small" type="danger" @click="confirmDelete(series.id)">删除</van-button>
        </div>
      </div>

      <van-empty v-if="!loading && seriesList.length === 0" description="暂无系列数据" />
    </div>

    <div v-if="hasMore" class="load-more">
      <van-button block @click="loadMore" :loading="loading">加载更多</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getAdminSeries, deleteSeries } from '@/api/series'

const router = useRouter()

const loading = ref(false)
const searchName = ref('')
const seriesList = ref([])
const pageNum = ref(1)
const hasMore = ref(true)

const fetchSeries = async () => {
  loading.value = true
  try {
    const res = await getAdminSeries({
      pageNum: pageNum.value,
      pageSize: 10,
      name: searchName.value || undefined
    })
    if (pageNum.value === 1) {
      seriesList.value = res.data?.records || []
    } else {
      seriesList.value.push(...(res.data?.records || []))
    }
    hasMore.value = seriesList.value.length < (res.data?.total || 0)
  } catch (error) {
    console.error('获取系列失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchSeries()
}

const loadMore = () => {
  pageNum.value++
  fetchSeries()
}

const confirmDelete = async (id) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定删除该系列吗？'
    })
    await handleDelete(id)
  } catch {
    // 用户取消
  }
}

const handleDelete = async (id) => {
  try {
    await deleteSeries(id)
    showToast({ type: 'success', message: '删除成功' })
    pageNum.value = 1
    fetchSeries()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  }
}

onMounted(() => {
  fetchSeries()
})
</script>

<style lang="scss" scoped>
.series-manage {
  max-width: 900px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
}

.series-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  margin-top: var(--space-4);
}

.series-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.series-info {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.series-cover {
  width: 60px;
  height: 40px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}

.series-cover-placeholder {
  width: 60px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);

  svg {
    width: 20px;
    height: 20px;
    color: var(--text-muted);
  }
}

.series-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.series-name {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
}

.series-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
}

.series-actions {
  display: flex;
  gap: var(--space-2);
}

.load-more {
  margin-top: var(--space-6);
}
</style>
