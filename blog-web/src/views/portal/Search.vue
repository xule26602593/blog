<template>
  <div class="search-page">
    <header class="page-header">
      <h1 class="page-title">{{ keyword ? `搜索"${keyword}"` : '搜索' }}</h1>
      <p class="page-desc">{{ keyword ? `找到 ${articles.length} 篇文章` : '输入关键词搜索文章' }}</p>
    </header>

    <div class="search-box">
      <input
        v-model="searchKeyword"
        type="text"
        placeholder="输入关键词..."
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">搜索</button>
    </div>

    <div class="results">
      <article
        v-for="article in articles"
        :key="article.id"
        class="result-item"
        @click="router.push('/article/' + article.id)"
      >
        <h3 class="result-title">{{ article.title }}</h3>
        <p class="result-summary">{{ article.summary || '暂无摘要' }}</p>
        <div class="result-meta">
          <span>{{ formatDate(article.publishTime) }}</span>
          <span>{{ article.viewCount }} 阅读</span>
        </div>
      </article>

      <div v-if="keyword && articles.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
          </svg>
        </div>
        <p class="empty-text">未找到相关文章</p>
      </div>

      <div v-if="hasMore" class="load-more">
        <button class="load-btn" @click="loadMore" :disabled="loading">
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { searchArticles } from '@/api/article'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const searchKeyword = ref('')
const articles = ref([])
const loading = ref(false)
const pageNum = ref(1)
const hasMore = ref(false)

const formatDate = date => dayjs(date).format('YYYY-MM-DD')

const handleSearch = () => {
  const nextKeyword = searchKeyword.value.trim()
  if (nextKeyword) {
    router.push({ path: '/search', query: { keyword: nextKeyword } })
  }
}

const fetchArticles = async () => {
  if (!keyword.value) {
    articles.value = []
    hasMore.value = false
    return
  }
  loading.value = true
  try {
    const res = await searchArticles(keyword.value, pageNum.value, 10)
    if (pageNum.value === 1) {
      articles.value = res.data?.records || []
    } else {
      articles.value.push(...(res.data?.records || []))
    }
    hasMore.value = articles.value.length < (res.data?.total || 0)
  } catch (error) {
    console.error('搜索失败', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  pageNum.value++
  fetchArticles()
}

watch(
  () => route.query.keyword,
  newKeyword => {
    keyword.value = typeof newKeyword === 'string' ? newKeyword : ''
    searchKeyword.value = keyword.value
    pageNum.value = 1
    fetchArticles()
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.search-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-8);
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

.search-box {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-10);
}

.search-input {
  flex: 1;
  height: 48px;
  padding: 0 var(--space-5);
  font-size: var(--text-base);
  color: var(--text-primary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    border-color: var(--text-muted);
  }
}

.search-btn {
  padding: 0 var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-inverse);
  background: var(--text-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    opacity: 0.85;
  }
}

.results {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.result-item {
  padding: var(--space-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);

    .result-title {
      color: var(--text-secondary);
    }
  }
}

.result-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
}

.result-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.result-meta {
  display: flex;
  gap: var(--space-4);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.load-more {
  display: flex;
  justify-content: center;
  padding-top: var(--space-6);
}

.load-btn {
  padding: var(--space-3) var(--space-8);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    color: var(--text-primary);
    border-color: var(--text-muted);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
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

  .search-box {
    flex-direction: column;
  }

  .search-btn {
    width: 100%;
  }
}
</style>
