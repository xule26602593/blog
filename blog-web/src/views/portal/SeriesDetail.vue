<template>
  <div class="series-detail-page">
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <template v-else-if="series">
      <!-- Series Header -->
      <div class="series-header">
        <div class="header-cover">
          <img v-if="series.coverImage" :src="series.coverImage" :alt="series.name" />
          <div v-else class="cover-placeholder">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
          </div>
        </div>
        <div class="header-content">
          <div class="header-meta">
            <van-tag :type="series.mode === 0 ? 'success' : 'primary'">
              {{ series.mode === 0 ? '有序系列' : '无序系列' }}
            </van-tag>
            <span class="meta-item">{{ series.articleCount }} 篇文章</span>
            <span class="meta-item">{{ series.viewCount }} 次浏览</span>
          </div>
          <h1 class="header-title">{{ series.name }}</h1>
          <p class="header-desc">{{ series.description || '暂无介绍' }}</p>
        </div>
      </div>

      <!-- Articles List -->
      <div class="articles-section">
        <h2 class="section-title">{{ series.mode === 0 ? '章节目录' : '文章列表' }}</h2>
        <div class="articles-list">
          <router-link
            v-for="(article, index) in series.articles"
            :key="article.id"
            :to="'/article/' + article.id"
            class="article-item"
          >
            <div v-if="series.mode === 0" class="chapter-num">
              {{ index + 1 }}
            </div>
            <div class="article-content">
              <h3 class="article-title">{{ article.title }}</h3>
              <p class="article-summary">{{ article.summary }}</p>
              <div class="article-meta">
                <span>{{ article.viewCount }} 次阅读</span>
              </div>
            </div>
            <div class="article-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7" />
              </svg>
            </div>
          </router-link>
        </div>
        <van-empty v-if="!series.articles?.length" description="该系列暂无文章" />
      </div>
    </template>

    <van-empty v-else description="系列不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSeriesDetail } from '@/api/series'

const route = useRoute()
const loading = ref(true)
const series = ref(null)

const fetchSeries = async () => {
  try {
    const res = await getSeriesDetail(route.params.id)
    series.value = res.data
  } catch (error) {
    console.error('获取系列详情失败', error)
  }
}

onMounted(async () => {
  await fetchSeries()
  loading.value = false
})
</script>

<style lang="scss" scoped>
.series-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-6);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-10);
}

.series-header {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-6);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-8);
}

.header-cover {
  width: 200px;
  height: 140px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
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

.header-content {
  flex: 1;
}

.header-meta {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.meta-item {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.header-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  margin-bottom: var(--space-3);
}

.header-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
}

.articles-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.section-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.articles-list {
  display: flex;
  flex-direction: column;
}

.article-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);

    .article-title {
      color: var(--color-primary);
    }

    .article-arrow {
      transform: translateX(4px);
      opacity: 1;
    }
  }
}

.chapter-num {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.article-content {
  flex: 1;
  min-width: 0;
}

.article-title {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  margin-bottom: var(--space-1);
  transition: color var(--transition-fast);
}

.article-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.article-arrow {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  opacity: 0.4;
  transition: all var(--transition-fast);

  svg {
    width: 100%;
    height: 100%;
    color: var(--text-muted);
  }
}

@media (max-width: 768px) {
  .series-detail-page {
    padding: var(--space-4);
  }

  .series-header {
    flex-direction: column;
  }

  .header-cover {
    width: 100%;
    height: 180px;
  }
}
</style>
