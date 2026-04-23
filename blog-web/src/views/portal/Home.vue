<template>
  <div class="home">
    <!-- 初始加载状态 -->
    <div v-if="initialLoading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 内容 -->
    <template v-else>
    <!-- Hero - 编辑风格的大标题 -->
    <section class="hero">
      <div class="hero-content">
        <p class="hero-label">Personal Blog</p>
        <h1 class="hero-title">随笔</h1>
        <p class="hero-desc">记录想法，分享思考</p>
        <div class="hero-stats">
          <div class="stat">
            <span class="stat-value">{{ articleList.length }}</span>
            <span class="stat-label">篇文章</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <span class="stat-value">{{ categories.length }}</span>
            <span class="stat-label">个分类</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat">
            <span class="stat-value">{{ tags.length }}</span>
            <span class="stat-label">个标签</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Content -->
    <div class="content">
      <!-- Main -->
      <div class="main-content">
        <!-- Featured - 更突出的置顶文章 -->
        <section v-if="featuredArticle" class="featured">
          <router-link :to="'/article/' + featuredArticle.id" class="featured-card">
            <div class="featured-badge">置顶推荐</div>
            <div class="featured-content">
              <h2 class="featured-title">{{ featuredArticle.title }}</h2>
              <p class="featured-summary">{{ featuredArticle.summary }}</p>
              <div class="featured-meta">
                <span class="meta-date">{{ formatDate(featuredArticle.publishTime) }}</span>
                <span class="meta-dot">·</span>
                <span class="meta-views">{{ featuredArticle.viewCount }} 次阅读</span>
              </div>
            </div>
            <div class="featured-arrow">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M17 8l4 4m0 0l-4 4m4-4H3" />
              </svg>
            </div>
          </router-link>
        </section>

        <!-- Articles -->
        <section class="articles">
          <div class="section-header">
            <h3 class="section-title">最新文章</h3>
            <div class="section-line"></div>
          </div>

          <article
            v-for="(article, index) in articleList"
            :key="article.id"
            class="article-item"
            :style="{ '--delay': index * 0.05 + 's' }"
            @click="router.push('/article/' + article.id)"
          >
            <div v-if="article.coverImage" class="article-cover">
              <img :src="article.coverImage" :alt="article.title" loading="lazy" />
              <div class="cover-overlay"></div>
            </div>
            <div class="article-body">
              <div class="article-header">
                <span v-if="article.categoryName" class="article-category">{{ article.categoryName }}</span>
                <span class="article-date">{{ formatDate(article.publishTime) }}</span>
              </div>
              <h4 class="article-title">{{ article.title }}</h4>
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
                <div v-if="article.tags?.length" class="article-tags">
                  <span
                    v-for="tag in article.tags.slice(0, 2)"
                    :key="tag.id"
                    class="tag"
                  >
                    {{ tag.name }}
                  </span>
                  <span v-if="article.tags.length > 2" class="tag more">+{{ article.tags.length - 2 }}</span>
                </div>
              </div>
            </div>
          </article>

          <div v-if="hasMore" class="load-more">
            <button class="load-btn" @click="loadMore" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? '加载中...' : '加载更多文章' }}
            </button>
          </div>
        </section>
      </div>

      <!-- Sidebar -->
      <aside class="sidebar">
        <!-- Hot Series -->
        <section v-if="hotSeries.length > 0" class="sidebar-section">
          <div class="section-title-row">
            <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
            <h4 class="sidebar-title">热门系列</h4>
          </div>
          <div class="series-list">
            <router-link
              v-for="series in hotSeries"
              :key="series.id"
              :to="'/series/' + series.id"
              class="series-item"
            >
              <span class="series-name">{{ series.name }}</span>
              <span class="series-count">{{ series.articleCount }}篇</span>
            </router-link>
          </div>
        </section>

        <!-- Hot -->
        <section class="sidebar-section hot-section">
          <div class="section-title-row">
            <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15.362 5.214A8.252 8.252 0 0112 21 8.25 8.25 0 016.038 7.048 8.287 8.287 0 009 9.6a8.983 8.983 0 013.361-6.867 8.21 8.21 0 003 2.48z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 18a3.75 3.75 0 00.495-7.467 5.99 5.99 0 00-1.925 3.546 5.974 5.974 0 01-2.133-1A3.75 3.75 0 0012 18z" />
            </svg>
            <h4 class="sidebar-title">热门文章</h4>
          </div>
          <div class="hot-list">
            <router-link
              v-for="(article, index) in hotArticles"
              :key="article.id"
              :to="'/article/' + article.id"
              class="hot-item"
            >
              <span class="hot-rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
              <span class="hot-title">{{ article.title }}</span>
            </router-link>
          </div>
        </section>

        <!-- Categories -->
        <section class="sidebar-section">
          <div class="section-title-row">
            <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
            <h4 class="sidebar-title">分类</h4>
          </div>
          <div class="category-list">
            <button
              v-for="category in categories"
              :key="category.id"
              class="category-item"
              @click="router.push('/category/' + category.id)"
            >
              <span class="category-name">{{ category.name }}</span>
              <span class="category-count">{{ category.articleCount }}</span>
            </button>
          </div>
        </section>

        <!-- Tags -->
        <section class="sidebar-section">
          <div class="section-title-row">
            <svg class="section-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 005.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 009.568 3z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6z" />
            </svg>
            <h4 class="sidebar-title">标签</h4>
          </div>
          <div class="tag-cloud">
            <button
              v-for="tag in tags"
              :key="tag.id"
              class="tag-btn"
              @click="router.push('/tag/' + tag.id)"
            >
              {{ tag.name }}
            </button>
          </div>
        </section>
      </aside>
    </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getArticles, getHotArticles, getTopArticles } from '@/api/article'
import { getHotSeries } from '@/api/series'
import { useAppStore } from '@/stores/app'
import dayjs from 'dayjs'

const router = useRouter()
const appStore = useAppStore()

const loading = ref(false)
const initialLoading = ref(true)
const articleList = ref([])
const hotArticles = ref([])
const hotSeries = ref([])
const topArticles = ref([])
const pageNum = ref(1)
const hasMore = ref(true)
const categories = ref([])
const tags = ref([])

const featuredArticle = computed(() => topArticles.value[0] || null)

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')

const fetchArticles = async () => {
  loading.value = true
  try {
    const res = await getArticles({ pageNum: pageNum.value, pageSize: 10 })
    if (pageNum.value === 1) {
      articleList.value = res.data?.records || []
    } else {
      articleList.value.push(...(res.data?.records || []))
    }
    hasMore.value = articleList.value.length < (res.data?.total || 0)
  } catch (error) {
    console.error('获取文章失败', error)
  } finally {
    loading.value = false
  }
}

const fetchHotArticles = async () => {
  try {
    const res = await getHotArticles(5)
    hotArticles.value = res.data || []
  } catch (error) {
    console.error('获取热门文章失败', error)
  }
}

const fetchTopArticles = async () => {
  try {
    const res = await getTopArticles()
    topArticles.value = res.data || []
  } catch (error) {
    console.error('获取置顶文章失败', error)
  }
}

const fetchHotSeries = async () => {
  try {
    const res = await getHotSeries(5)
    hotSeries.value = res.data || []
  } catch (error) {
    console.error('获取热门系列失败', error)
  }
}

const loadMore = () => {
  pageNum.value++
  fetchArticles()
}

onMounted(async () => {
  try {
    await appStore.initApp()
    categories.value = appStore.categories
    tags.value = appStore.tags
    await Promise.all([
      fetchArticles(),
      fetchHotArticles(),
      fetchTopArticles(),
      fetchHotSeries()
    ])
  } catch (error) {
    console.error('初始化失败', error)
  } finally {
    initialLoading.value = false
  }
})
</script>

<style lang="scss" scoped>
.home {
  animation: fadeIn var(--transition-slow) ease;
}

// ========================================
// Loading Container
// ========================================
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: var(--space-4);
  animation: fadeIn var(--transition-base) ease;
}

// ========================================
// Hero - 编辑风格 + 动态装饰
// ========================================
.hero {
  position: relative;
  padding: var(--space-20) 0 var(--space-16);
  text-align: center;
  overflow: hidden;

  // 渐变背景
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 400px;
    background: radial-gradient(ellipse at center top, rgba(180, 83, 9, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }

  // 动态装饰圆形
  &::after {
    content: '';
    position: absolute;
    top: 20%;
    right: 10%;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, rgba(180, 83, 9, 0.1) 0%, transparent 70%);
    border-radius: 50%;
    animation: float-soft 8s ease-in-out infinite;
    pointer-events: none;
  }
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 700px;
  margin: 0 auto;
}

.hero-label {
  display: inline-block;
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--color-primary);
  text-transform: uppercase;
  letter-spacing: 0.15em;
  margin-bottom: var(--space-4);
}

.hero-title {
  font-family: var(--font-sans);
  font-size: var(--text-6xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  margin-bottom: var(--space-4);
  line-height: var(--leading-tight);
  color: var(--text-primary);
}

.hero-desc {
  font-size: var(--text-xl);
  color: var(--text-secondary);
  margin-bottom: var(--space-10);
  font-weight: var(--font-light);
}

.hero-stats {
  display: inline-flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-4) var(--space-8);
  background: var(--bg-elevated);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-md);
}

.stat {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  text-align: center;
}

.stat-value {
  font-family: var(--font-serif);
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--color-primary);
  animation: countUp 0.8s var(--ease-out);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-weight: var(--font-medium);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--border-color);
}

// ========================================
// Content
// ========================================
.content {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: var(--space-12);
}

// ========================================
// Main Content
// ========================================
.main-content {
  min-width: 0;
}

// Featured
.featured {
  margin-bottom: var(--space-12);
}

.featured-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-8);
  background: var(--bg-card);
  border-radius: var(--radius-2xl);
  text-decoration: none;
  overflow: hidden;
  transition: all var(--transition-base);

  // 渐变边框效果
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    padding: 2px;
    background: var(--gradient-primary);
    border-radius: inherit;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: xor;
    -webkit-mask-composite: xor;
    opacity: 0.3;
    transition: opacity var(--transition-base);
  }

  // 左侧强调条
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: var(--gradient-primary);
    transform: scaleY(0);
    transform-origin: bottom;
    transition: transform var(--transition-base);
  }

  &:hover {
    box-shadow: var(--shadow-card-hover), var(--shadow-glow);
    transform: translateY(-4px);

    &::before {
      opacity: 1;
    }

    &::after {
      transform: scaleY(1);
    }

    .featured-title {
      color: var(--color-primary);
    }

    .featured-arrow {
      transform: translateX(4px);
      opacity: 1;
    }
  }
}

.featured-badge {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: white;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  box-shadow: 0 2px 8px rgba(180, 83, 9, 0.3);
}

.featured-content {
  flex: 1;
  min-width: 0;
}

.featured-title {
  font-family: var(--font-serif);
  font-size: var(--text-2xl);
  margin-bottom: var(--space-3);
  transition: color var(--transition-fast);
  line-height: var(--leading-snug);
}

.featured-summary {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-4);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.featured-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.meta-dot {
  color: var(--border-color);
}

.featured-arrow {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border-radius: var(--radius-full);
  opacity: 0.6;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
    color: var(--color-primary);
  }
}

// Articles
.articles {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.section-title {
  font-family: var(--font-serif);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  white-space: nowrap;
}

.section-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, var(--border-color), transparent);
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
    border-color: var(--border-color);

    .article-title {
      color: var(--color-primary);
    }

    .article-cover img {
      transform: scale(1.05);
    }
  }
}

.article-cover {
  position: relative;
  width: 220px;
  height: 150px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-secondary);

  // 渐变遮罩层
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(180, 83, 9, 0.1) 0%, transparent 50%);
    opacity: 0;
    transition: opacity var(--transition-fast);
    pointer-events: none;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--transition-slow);
  }

  .cover-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to top, rgba(0,0,0,0.3), transparent);
    opacity: 0;
    transition: opacity var(--transition-fast);
  }

  &:hover .cover-overlay {
    opacity: 1;
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
  font-family: var(--font-serif);
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
  gap: var(--space-4);
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

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.tag {
  padding: 2px var(--space-2);
  font-size: var(--text-xs);
  background: var(--color-primary-light);
  border-radius: var(--radius-full);
  color: var(--color-primary);
  font-weight: var(--font-medium);

  &.more {
    background: var(--bg-tertiary);
    color: var(--text-muted);
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

// ========================================
// Sidebar - 玻璃态效果
// ========================================
.sidebar {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.sidebar-section {
  padding: var(--space-6);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  transition: all var(--transition-base);

  &:hover {
    border-color: rgba(180, 83, 9, 0.2);
    box-shadow: var(--shadow-md);
  }
}

.section-title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.section-icon {
  width: 18px;
  height: 18px;
  color: var(--color-primary);
  transition: all var(--transition-fast);
}

.sidebar-section:hover .section-icon {
  transform: scale(1.1);
  filter: drop-shadow(0 0 4px rgba(180, 83, 9, 0.4));
}

.sidebar-title {
  font-family: var(--font-serif);
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
}

// Hot Articles
.hot-section {
  background: var(--bg-card);
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.hot-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    transform: translateX(4px);

    .hot-title {
      color: var(--color-primary);
    }

    .hot-rank:not(.top) {
      background: rgba(180, 83, 9, 0.1);
      color: var(--color-primary);
    }
  }
}

.hot-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-muted);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  font-family: var(--font-sans);

  &.top {
    color: white;
    background: var(--color-primary);
  }
}

.hot-title {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-snug);
  transition: color var(--transition-fast);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

// Hot Series
.series-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.series-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    transform: translateX(4px);

    .series-name {
      color: var(--color-primary);
    }
  }
}

.series-name {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}

.series-count {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

// Categories
.category-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: none;
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  // 左侧高亮条
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%) scaleY(0);
    width: 3px;
    height: 16px;
    background: var(--gradient-primary);
    border-radius: var(--radius-full);
    transition: transform var(--transition-fast);
  }

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);
    padding-left: calc(var(--space-3) + 4px);

    &::before {
      transform: translateY(-50%) scaleY(1);
    }

    .category-count {
      background: var(--color-primary);
      color: white;
    }
  }
}

.category-count {
  font-size: var(--text-xs);
  color: var(--text-muted);
  padding: 2px var(--space-2);
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

// Tags
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.tag-btn {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

// ========================================
// 响应式
// ========================================
@media (max-width: 1024px) {
  .content {
    grid-template-columns: 1fr;
  }

  .sidebar {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: var(--space-4);
  }

  .hero-title {
    font-size: var(--text-6xl);
  }
}

@media (max-width: 768px) {
  .hero {
    padding: var(--space-12) 0 var(--space-10);
  }

  .hero-title {
    font-size: var(--text-5xl);
  }

  .hero-desc {
    font-size: var(--text-lg);
  }

  .hero-stats {
    gap: var(--space-4);
    padding: var(--space-3) var(--space-6);
  }

  .stat-value {
    font-size: var(--text-xl);
  }

  .article-item {
    flex-direction: column;
    gap: var(--space-4);
  }

  .article-cover {
    width: 100%;
    height: 180px;
  }

  .sidebar {
    grid-template-columns: 1fr;
  }

  .featured-card {
    flex-direction: column;
    text-align: center;
  }

  .featured-arrow {
    display: none;
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: var(--text-4xl);
  }

  .hero-stats {
    flex-wrap: wrap;
    gap: var(--space-3);
  }

  .stat-divider {
    display: none;
  }

  .featured-card {
    padding: var(--space-5);
  }

  .featured-title {
    font-size: var(--text-xl);
  }

  .article-item {
    padding: var(--space-4);
  }

  .article-cover {
    height: 150px;
  }
}
</style>
