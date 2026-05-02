<template>
  <div class="dashboard">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 仪表盘内容 -->
    <template v-else>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ dashboard.articleCount || 0 }}</span>
          <span class="stat-label">文章</span>
        </div>
        <span class="stat-footer">今日 {{ dashboard.todayArticleCount || 0 }} 篇</span>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ dashboard.commentCount || 0 }}</span>
          <span class="stat-label">评论</span>
        </div>
        <span class="stat-footer">今日 {{ dashboard.todayCommentCount || 0 }} 条</span>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ dashboard.userCount || 0 }}</span>
          <span class="stat-label">用户</span>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <span class="stat-value">{{ formatNumber(dashboard.viewCount || 0) }}</span>
          <span class="stat-label">浏览</span>
        </div>
      </div>
    </div>

    <div class="main-grid">
      <section class="panel">
        <h3 class="panel-title">快捷操作</h3>
        <div class="actions-grid">
          <button class="action-btn" @click="$router.push('/admin/articles/edit')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
            </svg>
            <span>发布文章</span>
          </button>
          <button class="action-btn" @click="$router.push('/admin/categories')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
            </svg>
            <span>分类</span>
          </button>
          <button class="action-btn" @click="$router.push('/admin/tags')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 005.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 009.568 3z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6z" />
            </svg>
            <span>标签</span>
          </button>
          <button class="action-btn" @click="$router.push('/admin/comments')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M7.5 8.25h9m-9 3H12m-9.75 1.51c0 1.6 1.123 2.994 2.707 3.227 1.129.166 2.27.293 3.423.379.35.026.67.21.865.501L12 21l2.755-4.133a1.14 1.14 0 01.865-.501 48.172 48.172 0 003.423-.379c1.584-.233 2.707-1.626 2.707-3.228V6.741c0-1.602-1.123-2.995-2.707-3.228A48.394 48.394 0 0012 3c-2.392 0-4.744.175-7.043.513C3.373 3.746 2.25 5.14 2.25 6.741v6.018z" />
            </svg>
            <span>评论</span>
          </button>
          <button class="action-btn" @click="$router.push('/')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25" />
            </svg>
            <span>前台</span>
          </button>
        </div>
      </section>

      <section class="panel">
        <h3 class="panel-title">系统信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="info-label">系统名称</span>
            <span class="info-value">博客系统</span>
          </div>
          <div class="info-item">
            <span class="info-label">版本号</span>
            <span class="info-value">v2.0.0</span>
          </div>
          <div class="info-item">
            <span class="info-label">技术栈</span>
            <span class="info-value">Vue 3 + Spring Boot</span>
          </div>
          <div class="info-item">
            <span class="info-label">作者</span>
            <span class="info-value">Blog Team</span>
          </div>
        </div>
      </section>
    </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast } from '@/utils/toast'
import { getDashboard } from '@/api/admin'

const dashboard = ref({})
const loading = ref(true)

const formatNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  return num.toLocaleString()
}

const fetchDashboard = async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    dashboard.value = res.data || {}
  } catch (error) {
    console.error('获取仪表盘数据失败', error)
    showToast('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDashboard()
})
</script>

<style lang="scss" scoped>
.dashboard {
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.stat-card {
  padding: var(--space-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  transition: all var(--transition-base);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);
  }
}

.stat-content {
  display: flex;
  flex-direction: column;
  margin-bottom: var(--space-3);
}

.stat-value {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  letter-spacing: -0.02em;
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.stat-footer {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.main-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: var(--space-4);
}

.panel {
  padding: var(--space-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
}

.panel-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border-light);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: var(--space-3);
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 24px;
    height: 24px;
    color: var(--text-secondary);
  }

  span {
    font-size: var(--text-xs);
    color: var(--text-secondary);
  }

  &:hover {
    background: var(--bg-hover);
    border-color: var(--text-muted);

    svg,
    span {
      color: var(--text-primary);
    }
  }
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.info-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.info-value {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .main-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
