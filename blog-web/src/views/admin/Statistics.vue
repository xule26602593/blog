<template>
  <div class="statistics-page">
    <van-nav-bar title="访问统计" />

    <!-- 概览卡片 -->
    <div class="overview-cards">
      <div class="stat-card">
        <div class="stat-value">{{ overview.todayPv || 0 }}</div>
        <div class="stat-label">今日 PV</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ overview.todayUv || 0 }}</div>
        <div class="stat-label">今日 UV</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ overview.totalArticles || 0 }}</div>
        <div class="stat-label">文章总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ overview.totalComments || 0 }}</div>
        <div class="stat-label">评论总数</div>
      </div>
    </div>

    <!-- 趋势图表 -->
    <div class="chart-section">
      <div class="section-header">
        <h3>访问趋势</h3>
        <van-dropdown-menu>
          <van-dropdown-item v-model="trendDays" :options="daysOptions" @change="fetchTrend" />
        </van-dropdown-menu>
      </div>
      <div ref="trendChartRef" class="chart"></div>
    </div>

    <!-- 热门文章 -->
    <div class="hot-articles-section">
      <h3>热门文章</h3>
      <div class="article-list">
        <div v-for="(article, index) in hotArticles" :key="article.id" class="article-item">
          <span class="rank">{{ index + 1 }}</span>
          <div class="article-info">
            <div class="article-title">{{ article.title }}</div>
            <div class="article-stats">
              <span>{{ article.viewCount }} 阅读</span>
              <span>{{ article.likeCount }} 点赞</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 待处理提醒 -->
    <div class="pending-section">
      <h3>待处理</h3>
      <div class="pending-items">
        <div class="pending-item" @click="$router.push('/admin/comments')">
          <span class="pending-count">{{ overview.pendingComments || 0 }}</span>
          <span class="pending-label">条评论待审核</span>
        </div>
        <div class="pending-item" @click="$router.push('/admin/messages')">
          <span class="pending-count">{{ overview.pendingMessages || 0 }}</span>
          <span class="pending-label">条留言待审核</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const overview = ref({})
const trendData = ref([])
const hotArticles = ref([])
const trendDays = ref(7)
const trendChartRef = ref(null)
let trendChart = null

const daysOptions = [
  { text: '近 7 天', value: 7 },
  { text: '近 30 天', value: 30 }
]

const fetchOverview = async () => {
  try {
    const res = await request.get('/api/admin/statistics/overview')
    if (res.data) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('获取概览失败:', error)
  }
}

const fetchTrend = async () => {
  try {
    const res = await request.get('/api/admin/statistics/trend', {
      params: { type: 'pv', days: trendDays.value }
    })
    if (res.data) {
      trendData.value = res.data
      await nextTick()
      renderTrendChart()
    }
  } catch (error) {
    console.error('获取趋势失败:', error)
  }
}

const fetchHotArticles = async () => {
  try {
    const res = await request.get('/api/admin/statistics/hot-articles', {
      params: { limit: 10 }
    })
    if (res.data) {
      hotArticles.value = res.data
    }
  } catch (error) {
    console.error('获取热门文章失败:', error)
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value) return

  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: trendData.value.map(item => item.date.slice(5)),
      axisLine: { lineStyle: { color: '#999' } }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      data: trendData.value.map(item => item.pv),
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#1989fa' }
    }]
  }

  trendChart.setOption(option)
}

onMounted(() => {
  fetchOverview()
  fetchTrend()
  fetchHotArticles()
})
</script>

<style lang="scss" scoped>
.statistics-page {
  padding: var(--space-4);
  background: var(--bg-primary);
  min-height: 100vh;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  text-align: center;
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.chart-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-bottom: var(--space-4);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);

  h3 {
    font-size: var(--text-lg);
    font-weight: var(--font-semibold);
  }
}

.chart {
  height: 200px;
}

.hot-articles-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  margin-bottom: var(--space-4);

  h3 {
    font-size: var(--text-lg);
    font-weight: var(--font-semibold);
    margin-bottom: var(--space-3);
  }
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.article-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2);
  border-radius: var(--radius-md);

  &:hover {
    background: var(--bg-hover);
  }
}

.rank {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-full);
  background: var(--color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xs);
  font-weight: var(--font-bold);
}

.article-info {
  flex: 1;
}

.article-title {
  font-size: var(--text-base);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.article-stats {
  font-size: var(--text-xs);
  color: var(--text-muted);

  span {
    margin-right: var(--space-2);
  }
}

.pending-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);

  h3 {
    font-size: var(--text-lg);
    font-weight: var(--font-semibold);
    margin-bottom: var(--space-3);
  }
}

.pending-items {
  display: flex;
  gap: var(--space-4);
}

.pending-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  cursor: pointer;

  &:hover {
    background: var(--bg-hover);
  }
}

.pending-count {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--color-warning);
}

.pending-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}
</style>
