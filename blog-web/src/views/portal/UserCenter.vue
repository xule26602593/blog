<template>
  <div class="user-center">
    <header class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-desc">管理您的账号和收藏</p>
    </header>

    <!-- 标签导航 -->
    <nav class="tabs">
      <router-link
        v-for="tab in tabs"
        :key="tab.path"
        :to="tab.path"
        class="tab-item"
        :class="{ active: isActive(tab.path) }"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" :d="tab.iconPath" />
        </svg>
        <span>{{ tab.label }}</span>
      </router-link>
    </nav>

    <!-- 子路由内容 -->
    <router-view v-slot="{ Component }">
      <transition name="fade" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  {
    path: '/user/profile',
    label: '个人资料',
    iconPath: 'M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z'
  },
  {
    path: '/user/favorites',
    label: '我的收藏',
    iconPath: 'M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z'
  },
  {
    path: '/user/history',
    label: '阅读历史',
    iconPath: 'M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z'
  }
]

const isActive = (path) => {
  return route.path === path || route.path.startsWith(path + '/')
}
</script>

<style lang="scss" scoped>
.user-center {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: var(--space-6);
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

.tabs {
  display: flex;
  gap: var(--space-3);
  margin-bottom: var(--space-6);
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &.active {
    color: white;
    background: var(--gradient-primary);
    border-color: transparent;
    box-shadow: var(--shadow-sm);
  }
}

.tab-icon {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .page-title {
    font-size: var(--text-3xl);
  }

  .tabs {
    gap: var(--space-2);
  }

  .tab-item {
    padding: var(--space-2) var(--space-3);
    font-size: var(--text-xs);
    gap: var(--space-1);
  }

  .tab-icon {
    width: 14px;
    height: 14px;
  }
}
</style>
