<template>
  <div class="archives">
    <header class="page-header">
      <h1 class="page-title">归档</h1>
      <p class="page-desc">按时间浏览所有文章</p>
    </header>

    <section class="stats">
      <div class="stat-card">
        <span class="stat-value">{{ articles.length }}</span>
        <span class="stat-label">篇文章</span>
      </div>
      <div class="stat-card">
        <span class="stat-value">{{ yearCount }}</span>
        <span class="stat-label">个年份</span>
      </div>
    </section>

    <section class="timeline">
      <div v-for="(group, year) in groupedArticles" :key="year" class="year-group">
        <div class="year-header">
          <h2 class="year-title">{{ year }}</h2>
          <span class="year-count">{{ group.length }} 篇</span>
        </div>

        <div class="article-list">
          <article
            v-for="article in group"
            :key="article.id"
            class="article-item"
            @click="router.push('/article/' + article.id)"
          >
            <time class="article-date">{{ formatDate(article.publishTime) }}</time>
            <h3 class="article-title">{{ article.title }}</h3>
          </article>
        </div>
      </div>

      <div v-if="articles.length === 0" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
          </svg>
        </div>
        <p class="empty-text">暂无文章</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArchiveList } from '@/api/article'
import dayjs from 'dayjs'

const router = useRouter()
const articles = ref([])

const formatDate = date => dayjs(date).format('MM-DD')

const groupedArticles = computed(() => {
  const groups = {}
  articles.value.forEach(article => {
    const year = dayjs(article.publishTime).format('YYYY')
    if (!groups[year]) {
      groups[year] = []
    }
    groups[year].push(article)
  })
  return Object.keys(groups).sort((a, b) => b - a).reduce((obj, key) => {
    obj[key] = groups[key]
    return obj
  }, {})
})

const yearCount = computed(() => Object.keys(groupedArticles.value).length)

const fetchArticles = async () => {
  try {
    const res = await getArchiveList()
    articles.value = res.data || []
  } catch (error) {
    console.error('获取归档失败', error)
  }
}

onMounted(() => {
  fetchArticles()
})
</script>

<style lang="scss" scoped>
.archives {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-10);
}

.page-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: -0.02em;
  margin-bottom: var(--space-2);
}

.page-desc {
  font-size: var(--text-lg);
  color: var(--text-secondary);
}

.stats {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-10);
}

.stat-card {
  display: flex;
  flex-direction: column;
  padding: var(--space-5) var(--space-6);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
}

.stat-value {
  font-size: var(--text-3xl);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-1);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.year-group {
  position: relative;
  padding-left: var(--space-8);
  border-left: 2px solid var(--border-color);
}

.year-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  margin-left: calc(-1 * var(--space-8) - 1px);
  padding-left: var(--space-8);
}

.year-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
}

.year-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
  background: var(--bg-secondary);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.article-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);

    .article-title {
      color: var(--text-secondary);
    }
  }
}

.article-date {
  flex-shrink: 0;
  width: 50px;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-muted);
}

.article-title {
  flex: 1;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  transition: color var(--transition-fast);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-12) var(--space-6);
  color: var(--text-muted);
}

.empty-icon {
  margin-bottom: var(--space-4);

  svg {
    width: 64px;
    height: 64px;
    opacity: 0.5;
  }
}

.empty-text {
  font-size: var(--text-base);
}

@media (max-width: 768px) {
  .page-title {
    font-size: var(--text-3xl);
  }

  .stats {
    flex-direction: column;
  }

  .year-group {
    padding-left: var(--space-6);
  }

  .year-header {
    margin-left: calc(-1 * var(--space-6) - 1px);
    padding-left: var(--space-6);
  }

  .article-item {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }

  .article-date {
    width: auto;
  }
}
</style>
