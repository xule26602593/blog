<template>
  <div class="tag-page">
    <header class="page-header">
      <h1 class="page-title">{{ tagName }}</h1>
      <p class="page-desc">共 {{ total }} 篇文章</p>
    </header>

    <section class="articles">
      <article
        v-for="(article, index) in articleList"
        :key="article.id"
        class="article-item"
        :style="{ '--delay': index * 0.05 + 's' }"
        @click="router.push('/article/' + article.id)"
      >
        <div v-if="article.coverImage" class="article-cover">
          <img :src="article.coverImage" :alt="article.title" loading="lazy" />
        </div>
        <div class="article-body">
          <div class="article-header">
            <span v-if="article.categoryName" class="article-category">{{ article.categoryName }}</span>
            <span class="article-date">{{ formatDate(article.publishTime) }}</span>
          </div>
          <h3 class="article-title">{{ article.title }}</h3>
          <p class="article-summary">{{ article.summary }}</p>
          <div class="article-footer">
            <div class="article-stats">
              <span class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                {{ article.viewCount }}
              </span>
              <span class="stat-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                </svg>
                {{ article.likeCount }}
              </span>
            </div>
          </div>
        </div>
      </article>

      <div v-if="hasMore" class="load-more">
        <button class="load-btn" @click="loadMore" :disabled="loading">
          <span v-if="loading" class="loading-spinner"></span>
          {{ loading ? '加载中...' : '加载更多' }}
        </button>
      </div>

      <div v-if="articleList.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 005.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 009.568 3z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6z" />
          </svg>
        </div>
        <p class="empty-text">该标签下暂无文章</p>
        <router-link to="/" class="back-link">返回首页</router-link>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getArticlesByTag } from '@/api/article'
import { useAppStore } from '@/stores/app'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()

const loading = ref(false)
const articleList = ref([])
const pageNum = ref(1)
const total = ref(0)
const hasMore = ref(false)

const tagId = computed(() => route.params.id)

const tagName = computed(() => {
  const tag = appStore.tags.find(t => t.id === Number(tagId.value))
  return tag?.name || '标签文章'
})

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticlesByTag(tagId.value, pageNum.value, 10)
    if (pageNum.value === 1) {
      articleList.value = res.data?.records || []
    } else {
      articleList.value.push(...(res.data?.records || []))
    }
    total.value = res.data?.total || 0
    hasMore.value = articleList.value.length < total.value
  } catch (error) {
    console.error('获取标签文章失败', error)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  pageNum.value++
  fetchArticles()
}

onMounted(async () => {
  await appStore.initApp()
  fetchArticles()
})
</script>

<style lang="scss" scoped>
.tag-page {
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

.articles {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.article-item {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-6);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-base);
  animation: slideUp var(--transition-slow) var(--ease-out) backwards;
  animation-delay: var(--delay);

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-4px);

    .article-title {
      color: var(--color-primary);
    }

    .article-cover img {
      transform: scale(1.05);
    }
  }
}

.article-cover {
  width: 180px;
  height: 120px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-secondary);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--transition-slow);
  }
}

.article-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.article-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.article-category {
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
}

.article-date {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.article-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: var(--leading-snug);
}

.article-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-4);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.article-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-item {
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

.load-more {
  display: flex;
  justify-content: center;
  padding-top: var(--space-8);
}

.load-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
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
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
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
  margin-bottom: var(--space-4);
}

.back-link {
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: var(--text-3xl);
  }

  .article-item {
    flex-direction: column;
    gap: var(--space-4);
  }

  .article-cover {
    width: 100%;
    height: 160px;
  }
}
</style>
