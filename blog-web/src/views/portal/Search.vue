<template>
  <div class="search-page">
    <header class="page-header">
      <h1 class="page-title">{{ keyword ? `搜索"${keyword}"` : '搜索' }}</h1>
      <p class="page-desc">{{ keyword && total > 0 ? `找到 ${total} 篇文章` : keyword ? '未找到相关文章' : '输入关键词搜索文章' }}</p>
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

    <!-- 排序选择 -->
    <div v-if="keyword && total > 0" class="sort-bar">
      <span class="result-count">共 {{ total }} 篇</span>
      <div class="sort-options">
        <button
          :class="['sort-btn', { active: sortBy === 'relevance' }]"
          @click="changeSort('relevance')"
        >相关度</button>
        <button
          :class="['sort-btn', { active: sortBy === 'time' }]"
          @click="changeSort('time')"
        >时间</button>
      </div>
    </div>

    <div class="results">
      <article
        v-for="article in articles"
        :key="article.id"
        class="result-item"
        @click="router.push('/article/' + article.id)"
      >
        <h3 class="result-title" v-html="article.title"></h3>
        <p class="result-content" v-html="article.contentHighlight"></p>
        <div class="result-meta">
          <span v-if="article.categoryName">{{ article.categoryName }}</span>
          <span>{{ formatDate(article.publishTime) }}</span>
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
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import searchApi from '@/api/search'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const keyword = ref('')
const searchKeyword = ref('')
const articles = ref([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const hasMore = ref(false)
const sortBy = ref('relevance')

const formatDate = date => dayjs(date).format('YYYY-MM-DD')

const handleSearch = () => {
  const nextKeyword = searchKeyword.value.trim()
  if (nextKeyword) {
    router.push({ path: '/search', query: { keyword: nextKeyword } })
  }
}

const changeSort = (newSort) => {
  sortBy.value = newSort
  pageNum.value = 1
  fetchArticles()
}

const fetchArticles = async () => {
  if (!keyword.value) {
    articles.value = []
    hasMore.value = false
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res = await searchApi.search({
      keyword: keyword.value,
      page: pageNum.value,
      size: 10,
      sortBy: sortBy.value
    })
    if (pageNum.value === 1) {
      articles.value = res.data?.records || []
      total.value = res.data?.total || 0
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
    sortBy.value = 'relevance'
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
  margin-bottom: var(--space-6);
}

.search-input {
  flex: 1;
  height: 40px;
  padding: 0 var(--space-4);
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
  height: 40px;
  padding: 0 var(--space-5);
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

.sort-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding: var(--space-3) var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

.result-count {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.sort-options {
  display: flex;
  gap: var(--space-2);
}

.sort-btn {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
  }

  &.active {
    color: var(--text-primary);
    background: var(--bg-secondary);
    border-color: var(--text-muted);
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

  :deep(.highlight) {
    color: var(--color-primary);
    font-style: normal;
    font-weight: var(--font-bold);
    background: var(--color-primary-light);
    padding: 1px 4px;
    border-radius: 3px;
  }
}

.result-content {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-3);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;

  :deep(.highlight) {
    color: var(--color-primary);
    font-style: normal;
    font-weight: var(--font-semibold);
    background: var(--color-primary-light);
    padding: 1px 4px;
    border-radius: 3px;
  }
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
}
</style>
