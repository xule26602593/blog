<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="sidebar-inner">
        <!-- Logo -->
        <div class="brand">
          <div class="brand-mark">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
            </svg>
          </div>
          <div class="brand-text">
            <span class="brand-name">随笔</span>
            <span class="brand-sub">管理后台</span>
          </div>
        </div>

        <!-- Navigation -->
        <nav class="nav">
          <div class="nav-section">
            <span class="nav-section-title">概览</span>
            <router-link
              to="/admin"
              class="nav-item"
              :class="{ active: route.path === '/admin' }"
            >
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
              </svg>
              <span class="nav-label">仪表盘</span>
            </router-link>
          </div>
          <div class="nav-section">
            <span class="nav-section-title">内容管理</span>
            <router-link
              to="/admin/articles"
              class="nav-item"
              :class="{ active: isActive('/admin/articles') }"
            >
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
              </svg>
              <span class="nav-label">文章管理</span>
            </router-link>
            <router-link
              to="/admin/categories"
              class="nav-item"
              :class="{ active: isActive('/admin/categories') }"
            >
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
              </svg>
              <span class="nav-label">分类管理</span>
            </router-link>
            <router-link
              to="/admin/tags"
              class="nav-item"
              :class="{ active: isActive('/admin/tags') }"
            >
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 005.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 009.568 3z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6z" />
              </svg>
              <span class="nav-label">标签管理</span>
            </router-link>
            <router-link
              to="/admin/comments"
              class="nav-item"
              :class="{ active: isActive('/admin/comments') }"
            >
              <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M7.5 8.25h9m-9 3H12m-9.75 1.51c0 1.6 1.123 2.994 2.707 3.227 1.129.166 2.27.293 3.423.379.35.026.67.21.865.501L12 21l2.755-4.133a1.14 1.14 0 01.865-.501 48.172 48.172 0 003.423-.379c1.584-.233 2.707-1.626 2.707-3.228V6.741c0-1.602-1.123-2.995-2.707-3.228A48.394 48.394 0 0012 3c-2.392 0-4.744.175-7.043.513C3.373 3.746 2.25 5.14 2.25 6.741v6.018z" />
              </svg>
              <span class="nav-label">评论管理</span>
            </router-link>
          </div>
        </nav>

        <!-- Collapse Button -->
        <button class="collapse-btn" @click="isCollapse = !isCollapse">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
          </svg>
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="main">
      <!-- Header -->
      <header class="header">
        <div class="header-content">
          <div class="header-left">
            <h1 class="header-title">{{ currentPage?.title }}</h1>
            <p class="header-desc">{{ currentPage?.description }}</p>
          </div>

          <div class="header-actions">
            <div v-if="pendingCount > 0" class="pending-badge">
              <span class="pending-dot"></span>
              <span class="pending-label">待审核</span>
              <span class="pending-count">{{ pendingCount }}</span>
            </div>

            <div class="user-dropdown">
              <button class="user-btn" @click="showUserMenu = !showUserMenu">
                <div class="user-avatar">
                  <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo.avatar" alt="avatar" />
                  <span v-else>{{ userStore.userInfo?.nickname?.charAt(0) }}</span>
                </div>
                <span class="user-name">{{ userStore.userInfo?.nickname }}</span>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="chevron" :class="{ rotated: showUserMenu }">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
                </svg>
              </button>
              <Transition name="dropdown">
                <div v-if="showUserMenu" class="dropdown-menu">
                  <button class="dropdown-item" @click="handleCommand('home')">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25" />
                    </svg>
                    返回首页
                  </button>
                  <div class="dropdown-divider"></div>
                  <button class="dropdown-item danger" @click="handleCommand('logout')">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9" />
                    </svg>
                    退出登录
                  </button>
                </div>
              </Transition>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Content -->
      <div class="content">
        <router-view />
      </div>
    </div>

    <!-- Mobile Bottom Navigation -->
    <nav class="mobile-admin-nav">
      <div class="mobile-admin-nav-inner">
        <router-link to="/admin" class="mobile-admin-item" :class="{ active: route.path === '/admin' }">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zM9.75 8.625c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V8.625zM16.5 4.125c0-.621.504-1.125 1.125-1.125h2.25C20.496 3 21 3.504 21 4.125v15.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125V4.125z" />
          </svg>
          <span>仪表盘</span>
        </router-link>
        <router-link to="/admin/articles" class="mobile-admin-item" :class="{ active: route.path.startsWith('/admin/articles') }">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
          </svg>
          <span>文章</span>
        </router-link>
        <router-link to="/admin/categories" class="mobile-admin-item" :class="{ active: route.path.startsWith('/admin/categories') }">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9a2.25 2.25 0 00-2.25-2.25h-5.379a1.5 1.5 0 01-1.06-.44z" />
          </svg>
          <span>分类</span>
        </router-link>
        <router-link to="/admin/tags" class="mobile-admin-item" :class="{ active: route.path.startsWith('/admin/tags') }">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581c.699.699 1.78.872 2.607.33a18.095 18.095 0 005.223-5.223c.542-.827.369-1.908-.33-2.607L11.16 3.66A2.25 2.25 0 009.568 3z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 6h.008v.008H6V6z" />
          </svg>
          <span>标签</span>
        </router-link>
        <router-link to="/admin/comments" class="mobile-admin-item" :class="{ active: route.path.startsWith('/admin/comments') }">
          <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M7.5 8.25h9m-9 3H12m-9.75 1.51c0 1.6 1.123 2.994 2.707 3.227 1.129.166 2.27.293 3.423.379.35.026.67.21.865.501L12 21l2.755-4.133a1.14 1.14 0 01.865-.501 48.172 48.172 0 003.423-.379c1.584-.233 2.707-1.626 2.707-3.228V6.741c0-1.602-1.123-2.995-2.707-3.228A48.394 48.394 0 0012 3c-2.392 0-4.744.175-7.043.513C3.373 3.746 2.25 5.14 2.25 6.741v6.018z" />
          </svg>
          <span>评论</span>
        </router-link>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { usePendingCount } from '@/composables/usePendingCount'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isCollapse = ref(false)
const showUserMenu = ref(false)
const { pendingCount, fetchPendingCount } = usePendingCount()

const menuItems = [
  { path: '/admin', label: '仪表盘' },
  { path: '/admin/articles', label: '文章管理' },
  { path: '/admin/categories', label: '分类管理' },
  { path: '/admin/tags', label: '标签管理' },
  { path: '/admin/comments', label: '评论管理' }
]

const pageMeta = [
  { path: '/admin', title: '仪表盘', description: '站点数据概览' },
  { path: '/admin/articles', title: '文章管理', description: '管理文章内容' },
  { path: '/admin/categories', title: '分类管理', description: '管理分类目录' },
  { path: '/admin/tags', title: '标签管理', description: '管理标签体系' },
  { path: '/admin/comments', title: '评论管理', description: '审核评论内容' }
]

const isActive = (path) => {
  if (path === '/admin') {
    return route.path === '/admin'
  }
  return route.path.startsWith(path)
}

const currentPage = computed(() => {
  return pageMeta.find(item => route.path.startsWith(item.path)) || pageMeta[0]
})

const handleCommand = (command) => {
  showUserMenu.value = false
  if (command === 'home') {
    router.push('/')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

// Close dropdown when clicking outside
const handleClickOutside = (event) => {
  const dropdown = document.querySelector('.user-dropdown')
  if (dropdown && !dropdown.contains(event.target)) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  fetchPendingCount()
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--bg-primary);
}

// ========================================
// Sidebar
// ========================================
.sidebar {
  width: 280px;
  flex-shrink: 0;
  padding: var(--space-5);
  transition: width var(--transition-base);

  &.collapsed {
    width: 88px;
  }
}

.sidebar-inner {
  position: sticky;
  top: var(--space-5);
  display: flex;
  flex-direction: column;
  height: calc(100vh - var(--space-10));
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
}

// Brand
.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  margin-bottom: var(--space-6);
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-lg);
  flex-shrink: 0;

  svg {
    width: 24px;
    height: 24px;
    color: white;
  }
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
  overflow: hidden;
}

.brand-name {
  font-family: var(--font-serif);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  white-space: nowrap;
}

.brand-sub {
  font-size: var(--text-xs);
  color: var(--text-muted);
  white-space: nowrap;
}

.collapsed .brand-text {
  display: none;
}

// Navigation
.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  overflow-y: auto;
}

.nav-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.nav-section-title {
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: var(--space-2) var(--space-3);
  white-space: nowrap;
  overflow: hidden;
}

.collapsed .nav-section-title {
  visibility: hidden;
  height: 0;
  padding: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  .nav-icon {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    opacity: 0.7;
    transition: opacity var(--transition-fast);
  }

  .nav-label {
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
  }

  .nav-badge {
    padding: 2px var(--space-2);
    font-size: var(--text-xs);
    font-weight: var(--font-semibold);
    color: white;
    background: var(--color-primary);
    border-radius: var(--radius-full);
  }

  &:hover {
    color: var(--text-primary);
    background: var(--bg-hover);

    .nav-icon {
      opacity: 1;
    }
  }

  &.active {
    color: var(--color-primary);
    background: var(--color-primary-light);

    .nav-icon {
      opacity: 1;
    }
  }
}

.collapsed .nav-item {
  justify-content: center;
  padding: var(--space-3);

  .nav-label,
  .nav-badge {
    display: none;
  }
}

// Collapse Button
.collapse-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: var(--space-3);
  margin-top: var(--space-4);
  background: none;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);
    border-color: var(--color-primary);
  }
}

// ========================================
// Main Content
// ========================================
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

// Header
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  padding: var(--space-5) var(--space-6);
  background: var(--bg-blur);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-6);
}

.header-left {
  min-width: 0;
}

.header-title {
  font-family: var(--font-serif);
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-1);
  color: var(--text-primary);
}

.header-desc {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.pending-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary-light);
  border: 1px solid rgba(0, 122, 255, 0.2);
  border-radius: var(--radius-full);
}

.pending-dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.pending-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.pending-count {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--color-primary);
}

.user-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) var(--space-3) var(--space-1) var(--space-1);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.08);
  }

  .chevron {
    width: 14px;
    height: 14px;
    color: var(--text-muted);
    transition: transform var(--transition-fast);
  }
}

.user-name {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

// User Dropdown
.user-dropdown {
  position: relative;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  color: white;
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.chevron.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + var(--space-2));
  right: 0;
  min-width: 160px;
  padding: var(--space-1);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  z-index: var(--z-dropdown);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: none;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: left;

  svg {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--text-primary);
  }

  &.danger:hover {
    color: var(--color-error);
  }
}

.dropdown-divider {
  height: 1px;
  background: var(--border-light);
  margin: var(--space-1) 0;
}

// Dropdown transition
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

// Content
.content {
  flex: 1;
  padding: var(--space-6);
}

// ========================================
// Responsive
// ========================================
@media (max-width: 1024px) {
  .sidebar {
    width: 68px;
    padding: var(--space-3);
  }

  .sidebar-inner {
    padding: var(--space-2);
  }

  .brand {
    justify-content: center;
    padding: var(--space-2);
    margin-bottom: var(--space-4);
  }

  .brand-mark {
    width: 36px;
    height: 36px;

    svg {
      width: 20px;
      height: 20px;
    }
  }

  .brand-text,
  .nav-section-title,
  .nav-label,
  .nav-badge {
    display: none;
  }

  .nav-section {
    gap: var(--space-1);
  }

  .nav-item {
    justify-content: center;
    padding: var(--space-2);

    .nav-icon {
      width: 22px;
      height: 22px;
    }
  }

  .collapse-btn {
    display: none;
  }
}

@media (max-width: 768px) {
  .admin-layout {
    flex-direction: column;
    padding-bottom: calc(70px + env(safe-area-inset-bottom));
  }

  .sidebar {
    display: none;
  }

  .header {
    padding: var(--space-3) var(--space-4);
  }

  .header-content {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: var(--space-2);
  }

  .header-left {
    flex: 1;
    min-width: 0;
  }

  .header-title {
    font-size: var(--text-xl);
  }

  .header-desc {
    display: none;
  }

  .pending-badge {
    display: none;
  }

  .user-btn {
    padding: var(--space-1);
  }

  .user-name {
    display: none;
  }

  .content {
    padding: var(--space-4);
  }
}

// ========================================
// Mobile Bottom Navigation
// ========================================
.mobile-admin-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--bg-overlay);
  backdrop-filter: blur(16px);
  border-top: 1px solid var(--border-color);
  padding-bottom: env(safe-area-inset-bottom);
  z-index: var(--z-fixed);
}

.mobile-admin-nav-inner {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 64px;
  padding: 0 var(--space-2);
}

.mobile-admin-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  flex: 1;
  height: 100%;
  padding: var(--space-2) 0;
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--text-muted);
  background: none;
  border: none;
  text-decoration: none;
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
    flex-shrink: 0;
    transition: all var(--transition-fast);
  }

  &:hover {
    color: var(--text-secondary);
  }

  &.active {
    color: var(--color-primary);

    svg {
      transform: scale(1.1);
    }
  }
}

@media (max-width: 768px) {
  .mobile-admin-nav {
    display: block;
  }
}
</style>
