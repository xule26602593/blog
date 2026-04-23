<template>
  <div class="series-list-page">
    <div class="page-header">
      <h1 class="page-title">系列文章</h1>
      <p class="page-desc">系统化的知识整理，按系列学习更高效</p>
    </div>

    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <template v-else>
      <div class="series-grid">
        <router-link
          v-for="series in seriesList"
          :key="series.id"
          :to="'/series/' + series.id"
          class="series-card"
        >
          <div class="card-cover">
            <img v-if="series.coverImage" :src="series.coverImage" :alt="series.name" />
            <div v-else class="cover-placeholder">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
              </svg>
            </div>
            <div class="mode-badge" :class="{ ordered: series.mode === 0 }">
              {{ series.mode === 0 ? '有序' : '无序' }}
            </div>
          </div>
          <div class="card-content">
            <h3 class="card-title">{{ series.name }}</h3>
            <p class="card-desc">{{ series.description || '暂无介绍' }}</p>
            <div class="card-meta">
              <span>{{ series.articleCount }} 篇文章</span>
              <span>{{ series.viewCount }} 次浏览</span>
            </div>
          </div>
        </router-link>
      </div>

      <van-empty v-if="seriesList.length === 0" description="暂无系列" />

      <div v-if="hasMore" class="load-more">
        <van-button block @click="loadMore" :loading="loadingMore">加载更多</van-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSeries } from '@/api/series'

const loading = ref(true)
const loadingMore = ref(false)
const seriesList = ref([])
const pageNum = ref(1)
const hasMore = ref(true)

const fetchSeries = async () => {
  try {
    const res = await getSeries({ pageNum: pageNum.value, pageSize: 12 })
    if (pageNum.value === 1) {
      seriesList.value = res.data?.records || []
    } else {
      seriesList.value.push(...(res.data?.records || []))
    }
    hasMore.value = seriesList.value.length < (res.data?.total || 0)
  } catch (error) {
    console.error('获取系列失败', error)
  }
}

const loadMore = async () => {
  loadingMore.value = true
  pageNum.value++
  await fetchSeries()
  loadingMore.value = false
}

onMounted(async () => {
  await fetchSeries()
  loading.value = false
})
</script>

<style lang="scss" scoped>
.series-list-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: var(--space-6);
}

.page-header {
  text-align: center;
  margin-bottom: var(--space-10);
}

.page-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--space-3);
}

.page-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-10);
}

.series-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-6);
}

.series-card {
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
  text-decoration: none;
  transition: all var(--transition-base);

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-4px);
  }
}

.card-cover {
  position: relative;
  height: 160px;
  background: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 48px;
    height: 48px;
    color: var(--text-muted);
  }
}

.mode-badge {
  position: absolute;
  top: var(--space-3);
  right: var(--space-3);
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-sm);

  &.ordered {
    background: #10b981;
  }
}

.card-content {
  flex: 1;
  padding: var(--space-4);
}

.card-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-2);
  color: var(--text-primary);
}

.card-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  gap: var(--space-4);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.load-more {
  margin-top: var(--space-8);
}

@media (max-width: 768px) {
  .series-list-page {
    padding: var(--space-4);
  }

  .series-grid {
    grid-template-columns: 1fr;
  }
}
</style>
