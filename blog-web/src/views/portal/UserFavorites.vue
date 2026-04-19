<template>
  <div class="favorites-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <input
        v-model="keyword"
        type="text"
        placeholder="搜索收藏的文章..."
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <button class="search-btn" @click="handleSearch">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <div v-else-if="favorites.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" />
        </svg>
      </div>
      <p class="empty-text">{{ keyword ? '未找到匹配的文章' : '暂无收藏的文章' }}</p>
      <p class="empty-hint">{{ keyword ? '试试其他关键词' : '浏览文章时点击收藏按钮即可添加' }}</p>
    </div>

    <!-- 收藏列表 -->
    <div v-else class="favorites-list">
      <div
        v-for="item in favorites"
        :key="item.id"
        class="favorite-card"
        @click="goToArticle(item.articleId)"
      >
        <div v-if="item.coverImage" class="card-cover">
          <img :src="item.coverImage" :alt="item.title" loading="lazy" />
        </div>
        <div class="card-content">
          <h3 class="card-title">{{ item.title }}</h3>
          <p class="card-summary">{{ item.summary }}</p>
          <div class="card-meta">
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
              </svg>
              {{ item.viewCount }}
            </span>
            <span class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
              </svg>
              {{ item.likeCount }}
            </span>
            <span class="meta-time">收藏于 {{ formatDate(item.favoriteTime) }}</span>
          </div>
        </div>
        <button class="remove-btn" @click.stop="handleRemove(item)">
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
import { getFavorites, favoriteArticle } from '@/api/favorite'
import dayjs from 'dayjs'

const router = useRouter()

const loading = ref(false)
const favorites = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavorites({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    })
    favorites.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取收藏列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchFavorites()
}

const changePage = (page) => {
  pageNum.value = page
  fetchFavorites()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToArticle = (articleId) => {
  router.push(`/article/${articleId}`)
}

const handleRemove = async (item) => {
  try {
    await showConfirmDialog({
      title: '取消收藏',
      message: '确定要取消收藏这篇文章吗？'
    })
    await favoriteArticle(item.articleId)
    showToast({ type: 'success', message: '已取消收藏' })
    fetchFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消收藏失败', error)
    }
  }
}

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD')

onMounted(() => {
  fetchFavorites()
})
</script>

<style lang="scss" scoped>
.favorites-page {
  max-width: 900px;
  margin: 0 auto;
}

.search-bar {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
}

.search-input {
  flex: 1;
  height: 44px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.15);
  }

  &::placeholder {
    color: var(--text-muted);
  }
}

.search-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
    color: white;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
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

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.favorite-card {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.card-cover {
  flex-shrink: 0;
  width: 120px;
  height: 80px;
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
  gap: var(--space-2);
}

.card-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-summary {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);

  svg {
    width: 14px;
    height: 14px;
  }
}

.meta-time {
  margin-left: auto;
}

.remove-btn {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 32px;
  height: 32px;
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
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--color-danger, #ef4444);
  }
}

.favorite-card:hover .remove-btn {
  opacity: 1;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-4);
  margin-top: var(--space-8);
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
    width: 80px;
    height: 60px;
  }

  .card-summary {
    display: none;
  }

  .remove-btn {
    opacity: 1;
  }

  .meta-time {
    display: none;
  }
}
</style>
