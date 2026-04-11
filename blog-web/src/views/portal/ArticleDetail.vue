<template>
  <div class="article-detail">
    <article class="article">
      <!-- 文章头部 -->
      <header class="article-header">
        <div class="header-top">
          <div class="article-meta">
            <span v-if="article.categoryName" class="meta-category">{{ article.categoryName }}</span>
            <span class="meta-date">{{ formatDate(article.publishTime) }}</span>
            <span class="meta-dot">·</span>
            <span class="meta-reading">{{ article.viewCount }} 次阅读</span>
          </div>
        </div>
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="header-bottom">
          <div class="author-info">
            <div class="author-avatar">{{ article.authorName?.charAt(0) || 'A' }}</div>
            <div class="author-details">
              <span class="author-name">{{ article.authorName || '匿名' }}</span>
              <span class="author-role">作者</span>
            </div>
          </div>
          <div v-if="article.tags?.length" class="article-tags">
            <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
          </div>
        </div>
      </header>

      <!-- 封面图 -->
      <div v-if="article.coverImage" class="article-cover">
        <img :src="article.coverImage" :alt="article.title" loading="lazy" />
      </div>

      <!-- 文章内容 -->
      <div class="article-content markdown-body" v-html="renderedContent"></div>

      <!-- 文章底部操作 -->
      <footer class="article-footer">
        <div class="footer-actions">
          <button class="action-btn" :class="{ active: article.isLiked }" @click="handleLike">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
            </svg>
            <span>{{ article.isLiked ? '已赞' : '点赞' }}</span>
            <span v-if="article.likeCount" class="count">{{ article.likeCount }}</span>
          </button>
          <button class="action-btn" :class="{ active: article.isFavorited }" @click="handleFavorite">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" />
            </svg>
            <span>{{ article.isFavorited ? '已收藏' : '收藏' }}</span>
          </button>
        </div>
      </footer>
    </article>

    <!-- 评论区 -->
    <section class="comments">
      <div class="comments-header">
        <h3 class="comments-title">
          评论
          <span class="comments-count">{{ article.commentCount || 0 }}</span>
        </h3>
      </div>

      <div class="comment-form">
        <div class="comment-avatar">{{ userStore.userInfo?.nickname?.charAt(0) || '?' }}</div>
        <div class="comment-input-wrapper">
          <textarea
            v-model="commentContent"
            :rows="3"
            placeholder="分享你的想法..."
            class="comment-textarea"
          ></textarea>
          <div class="comment-actions">
            <button
              class="submit-btn"
              @click="handleComment"
              :disabled="commentLoading || !commentContent.trim()"
            >
              <span v-if="commentLoading" class="loading-spinner"></span>
              {{ commentLoading ? '发送中...' : '发表评论' }}
            </button>
          </div>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">{{ comment.nickname?.charAt(0) || '?' }}</div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-author">{{ comment.nickname }}</span>
              <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
          </div>
        </div>
        <div v-if="comments.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
            </svg>
          </div>
          <p class="empty-text">暂无评论，来抢沙发吧~</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { marked } from 'marked'
import hljs from 'highlight.js'
import dayjs from 'dayjs'
import { getArticle, likeArticle, favoriteArticle } from '@/api/article'
import { getComments, addComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const article = ref({})
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)

const renderedContent = computed(() => {
  if (!article.value.content) return ''
  marked.setOptions({
    highlight: (code, lang) => {
      if (lang && hljs.getLanguage(lang)) {
        return hljs.highlight(code, { language: lang }).value
      }
      return hljs.highlightAuto(code).value
    }
  })
  return marked(article.value.content)
})

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const fetchArticle = async () => {
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data || {}
    document.title = `${article.value.title} - 随笔`
  } catch (error) {
    console.error('获取文章失败', error)
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id, { pageNum: 1, pageSize: 50 })
    comments.value = res.data?.records || []
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    return
  }
  try {
    const res = await likeArticle(article.value.id)
    article.value.isLiked = res.data
    article.value.likeCount += res.data ? 1 : -1
  } catch (error) {
    console.error('点赞失败', error)
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    return
  }
  try {
    const res = await favoriteArticle(article.value.id)
    article.value.isFavorited = res.data
    showToast(res.data ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败', error)
  }
}

const handleComment = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    return
  }
  if (!commentContent.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    await addComment({
      articleId: article.value.id,
      content: commentContent.value
    })
    showToast({ type: 'success', message: '评论成功' })
    commentContent.value = ''
    fetchComments()
  } catch (error) {
    console.error('评论失败', error)
  } finally {
    commentLoading.value = false
  }
}

onMounted(() => {
  fetchArticle()
  fetchComments()
})
</script>

<style lang="scss" scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  animation: slideUp var(--transition-slow) var(--ease-out);
}

// ========================================
// Article
// ========================================
.article {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.article-header {
  padding: var(--space-10) var(--space-10) var(--space-8);
}

.header-top {
  margin-bottom: var(--space-5);
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  font-size: var(--text-sm);
}

.meta-category {
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
}

.meta-date {
  color: var(--text-muted);
}

.meta-dot {
  color: var(--text-muted);
}

.meta-reading {
  color: var(--text-muted);
}

.article-title {
  font-family: var(--font-serif);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: -0.02em;
  margin-bottom: var(--space-6);
  line-height: var(--leading-tight);
  color: var(--text-primary);
}

.header-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-color);
}

.author-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.author-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
}

.author-info:hover .author-avatar {
  box-shadow: 0 0 0 3px var(--color-primary);
}

.author-details {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.author-name {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.author-role {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.tag {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  background: rgba(180, 83, 9, 0.08);
  border-radius: var(--radius-full);
  color: var(--color-primary);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--gradient-primary);
    color: white;
    box-shadow: 0 2px 6px rgba(180, 83, 9, 0.3);
  }
}

.article-cover {
  margin: 0 var(--space-10);
  border-radius: var(--radius-xl);
  overflow: hidden;
  position: relative;

  // 渐变遮罩
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(180, 83, 9, 0.1) 0%, transparent 50%);
    pointer-events: none;
    transition: opacity var(--transition-fast);
  }

  &:hover::after {
    opacity: 0.5;
  }

  img {
    width: 100%;
    display: block;
  }
}

.article-content {
  padding: var(--space-10);
  padding-top: var(--space-8);
}

.article-footer {
  padding: var(--space-6) var(--space-10) var(--space-10);
  border-top: 1px solid var(--border-color);
  position: sticky;
  bottom: 0;
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.footer-actions {
  display: flex;
  gap: var(--space-3);
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-5);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;

  svg {
    width: 18px;
    height: 18px;
    transition: transform var(--transition-fast);
  }

  // 点击涟漪效果
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: var(--gradient-primary);
    opacity: 0;
    transition: opacity var(--transition-fast);
  }

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);

    svg {
      transform: scale(1.1);
    }
  }

  &:active {
    transform: translateY(0);
  }

  &.active {
    color: white;
    background: var(--gradient-primary);
    border-color: transparent;
    box-shadow: 0 2px 10px rgba(180, 83, 9, 0.4);

    svg {
      fill: currentColor;
    }
  }

  .count {
    padding-left: var(--space-1);
    font-size: var(--text-xs);
    opacity: 0.8;
  }
}

// ========================================
// Comments - 玻璃态效果
// ========================================
.comments {
  margin-top: var(--space-10);
  padding: var(--space-8);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-2xl);
}

.comments-header {
  margin-bottom: var(--space-6);
}

.comments-title {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  font-family: var(--font-serif);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
}

.comments-count {
  font-size: var(--text-sm);
  font-weight: var(--font-normal);
  color: white;
  background: var(--gradient-primary);
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
}

.comment-form {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-6);
  border-bottom: 1px solid var(--border-color);
}

.comment-avatar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  box-shadow: 0 0 0 2px transparent;
  transition: all var(--transition-fast);
}

.comment-form:focus-within .comment-avatar {
  box-shadow: 0 0 0 2px var(--color-primary), 0 0 12px rgba(180, 83, 9, 0.3);
}

.comment-input-wrapper {
  flex: 1;
  min-width: 0;
}

.comment-textarea {
  width: 100%;
  min-height: 80px;
  padding: var(--space-3) var(--space-4);
  font-family: var(--font-sans);
  font-size: var(--text-sm);
  line-height: var(--leading-relaxed);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  resize: vertical;
  outline: none;
  transition: all var(--transition-fast);
  margin-bottom: var(--space-3);

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.15), var(--shadow-glow);
  }
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover:not(:disabled) {
    background: var(--color-accent);
    box-shadow: var(--shadow-md);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.comment-item {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.comment-item .comment-avatar {
  width: 40px;
  height: 40px;
  font-size: var(--text-xs);
  box-shadow: 0 0 0 2px transparent;
  transition: all var(--transition-fast);
}

.comment-item:hover .comment-avatar {
  box-shadow: 0 0 0 2px var(--color-primary), 0 0 8px rgba(180, 83, 9, 0.3);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.comment-author {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.comment-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.comment-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-10) var(--space-6);
  color: var(--text-muted);
}

.empty-icon {
  margin-bottom: var(--space-4);

  svg {
    width: 48px;
    height: 48px;
    opacity: 0.5;
  }
}

.empty-text {
  font-size: var(--text-sm);
}

// ========================================
// Responsive
// ========================================
@media (max-width: 768px) {
  .article-header {
    padding: var(--space-6);
  }

  .article-title {
    font-size: var(--text-3xl);
  }

  .header-bottom {
    flex-direction: column;
    align-items: flex-start;
  }

  .article-cover {
    margin: 0 var(--space-6);
    border-radius: var(--radius-lg);
  }

  .article-content {
    padding: var(--space-6);
  }

  .article-footer {
    padding: var(--space-5) var(--space-6) var(--space-6);
  }

  .footer-actions {
    flex-wrap: wrap;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
  }

  .comments {
    padding: var(--space-6);
  }

  .comment-form {
    flex-direction: column;
    align-items: flex-start;
  }

  .comment-avatar {
    width: 36px;
    height: 36px;
  }
}

@media (max-width: 480px) {
  .article-header {
    padding: var(--space-5);
  }

  .article-title {
    font-size: var(--text-2xl);
  }

  .article-cover {
    margin: 0 var(--space-5);
    border-radius: var(--radius-md);
  }

  .article-content {
    padding: var(--space-5);
  }

  .article-footer {
    padding: var(--space-5);
    gap: var(--space-2);
  }
}
</style>
