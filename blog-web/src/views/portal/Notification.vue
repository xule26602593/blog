<template>
  <div class="notification-page">
    <!-- 顶部操作栏 -->
    <div class="header-bar">
      <div class="header-left">
        <span class="page-title">通知中心</span>
        <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
      </div>
      <button v-if="unreadCount > 0" class="mark-all-btn" @click="handleMarkAllRead">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        全部已读
      </button>
    </div>

    <!-- 类型筛选 -->
    <div class="filter-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="filter-tab"
        :class="{ active: currentType === tab.value }"
        @click="changeType(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <div v-else-if="notifications.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
        </svg>
      </div>
      <p class="empty-text">暂无通知</p>
      <p class="empty-hint">关注作者或参与评论后，会收到相关通知</p>
    </div>

    <!-- 通知列表 -->
    <div v-else class="notification-list">
      <div
        v-for="item in notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.isRead }"
        @click="handleClick(item)"
      >
        <div class="item-icon" :class="item.type">
          <!-- 关注通知 -->
          <svg v-if="item.type === 'FOLLOW'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
          </svg>
          <!-- 评论通知 -->
          <svg v-else-if="item.type === 'COMMENT'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 20.25c4.97 0 9-3.694 9-8.25s-4.03-8.25-9-8.25S3 7.444 3 12c0 2.104.859 4.023 2.273 5.48.432.447.74 1.04.586 1.641a4.483 4.483 0 01-.923 1.785A5.969 5.969 0 006 21c1.282 0 2.47-.402 3.445-1.087.81.22 1.668.337 2.555.337z" />
          </svg>
          <!-- 回复通知 -->
          <svg v-else-if="item.type === 'REPLY'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 15a3 3 0 106 0m3-6h-1.415a5.002 5.002 0 00-9.17 0H6a3 3 0 100 6h12a3 3 0 100-6z" />
          </svg>
          <!-- 公告通知 -->
          <svg v-else-if="item.type === 'ANNOUNCEMENT'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.34 15.84c-.688-.06-1.386-.09-2.09-.09H7.5a4.5 4.5 0 110-9h.75c.704 0 1.402-.03 2.09-.09m0 9.18c.253.962.584 1.892.985 2.783.247.55.06 1.21-.463 1.511l-.657.38c-.551.318-1.26.117-1.527-.461a20.845 20.845 0 01-1.44-4.282m3.102.069a18.03 18.03 0 01-.59-4.59c0-1.586.205-3.124.59-4.59m0 9.18a23.848 23.848 0 018.835 2.535M10.34 6.66a23.847 23.848 0 008.835-2.535m0 0A23.74 23.74 0 0018.795 3m.38 1.125a23.91 23.91 0 011.014 5.395m-1.014 8.855c-.118.38-.245.754-.38 1.125m.38-1.125a23.91 23.91 0 001.014-5.395m0-3.46c.495.413.811 1.035.811 1.73 0 .695-.316 1.317-.811 1.73m0-3.46a24.347 24.347 0 010 3.46" />
          </svg>
          <!-- 默认 -->
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
          </svg>
        </div>
        <div class="item-content">
          <h4 class="item-title">{{ item.title }}</h4>
          <p class="item-text">{{ item.content }}</p>
          <span class="item-time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div v-if="!item.isRead" class="unread-dot"></div>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

const loading = ref(false)
const notifications = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const unreadCount = ref(0)
const currentType = ref(null)

const tabs = ref([
  { label: '全部', value: null, count: 0 },
  { label: '关注', value: 'FOLLOW', count: 0 },
  { label: '评论', value: 'COMMENT', count: 0 },
  { label: '回复', value: 'REPLY', count: 0 },
  { label: '公告', value: 'ANNOUNCEMENT', count: 0 }
])

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const res = await getNotifications(currentType.value)
    notifications.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('获取通知列表失败', error)
  } finally {
    loading.value = false
  }
}

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

// 切换类型
const changeType = (type) => {
  currentType.value = type
  pageNum.value = 1
  fetchNotifications()
}

// 翻页
const changePage = (page) => {
  pageNum.value = page
  fetchNotifications()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 点击通知
const handleClick = async (item) => {
  // 标记为已读
  if (!item.isRead) {
    try {
      await markAsRead(item.id)
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }

  // 根据类型跳转
  switch (item.type) {
    case 'FOLLOW':
      // 跳转到用户中心
      if (item.senderId) {
        router.push(`/user/${item.senderId}/profile`)
      }
      break
    case 'COMMENT':
    case 'REPLY':
      // 跳转到文章详情
      if (item.articleId) {
        router.push(`/article/${item.articleId}`)
      }
      break
    case 'ANNOUNCEMENT':
      // 跳转到公告详情
      if (item.relatedId) {
        router.push(`/announcement/${item.relatedId}`)
      }
      break
    default:
      break
  }
}

// 全部标记已读
const handleMarkAllRead = async () => {
  try {
    await markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    unreadCount.value = 0
    showToast({ type: 'success', message: '已全部标记为已读' })
  } catch (error) {
    console.error('标记全部已读失败', error)
    showToast({ type: 'fail', message: '操作失败' })
  }
}

// 格式化时间
const formatTime = (time) => {
  const date = dayjs(time)
  const now = dayjs()
  const diff = now.diff(date, 'day')

  if (diff < 1) {
    return date.fromNow()
  } else if (diff < 7) {
    return `${diff}天前`
  } else {
    return date.format('MM-DD HH:mm')
  }
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style lang="scss" scoped>
.notification-page {
  max-width: 900px;
  margin: 0 auto;
}

.header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.unread-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: white;
  background: var(--color-danger, #ef4444);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mark-all-btn {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-primary);
  background: transparent;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    color: white;
    background: var(--gradient-primary);
    border-color: transparent;
  }
}

.filter-tabs {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  overflow-x: auto;
  padding-bottom: var(--space-2);

  &::-webkit-scrollbar {
    display: none;
  }
}

.filter-tab {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid transparent;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);
  }

  &.active {
    color: white;
    background: var(--gradient-primary);
    border-color: transparent;
  }

  .tab-count {
    min-width: 18px;
    height: 18px;
    padding: 0 var(--space-1);
    font-size: var(--text-xs);
    color: var(--text-muted);
    background: var(--bg-card);
    border-radius: var(--radius-full);
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &.active .tab-count {
    color: var(--color-primary);
    background: white;
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

.notification-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.notification-item {
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
    box-shadow: var(--shadow-sm);
    transform: translateX(4px);
  }

  &.unread {
    background: linear-gradient(135deg, rgba(180, 83, 9, 0.05) 0%, transparent 100%);
    border-left: 3px solid var(--color-primary);
  }
}

.item-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);

  svg {
    width: 22px;
    height: 22px;
    color: var(--text-muted);
  }

  &.FOLLOW {
    background: rgba(59, 130, 246, 0.1);
    svg { color: #3b82f6; }
  }

  &.COMMENT {
    background: rgba(16, 185, 129, 0.1);
    svg { color: #10b981; }
  }

  &.REPLY {
    background: rgba(245, 158, 11, 0.1);
    svg { color: #f59e0b; }
  }

  &.ANNOUNCEMENT {
    background: rgba(139, 92, 246, 0.1);
    svg { color: #8b5cf6; }
  }
}

.item-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.item-title {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.item-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.unread-dot {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  width: 8px;
  height: 8px;
  background: var(--color-danger, #ef4444);
  border-radius: var(--radius-full);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
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
  .header-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }

  .header-left {
    width: 100%;
    justify-content: space-between;
  }

  .mark-all-btn {
    width: 100%;
    justify-content: center;
  }

  .filter-tabs {
    width: calc(100% + var(--space-6));
    margin-left: calc(var(--space-3) * -1);
    padding: 0 var(--space-3) var(--space-2);
  }

  .item-icon {
    width: 36px;
    height: 36px;

    svg {
      width: 18px;
      height: 18px;
    }
  }

  .notification-item {
    padding: var(--space-3);
  }
}
</style>
