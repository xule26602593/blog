<template>
  <div class="followers-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-loading type="spinner" size="24px" color="var(--color-primary)" vertical>加载中...</van-loading>
    </div>

    <!-- 空状态 -->
    <div v-else-if="followersList.length === 0" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
        </svg>
      </div>
      <p class="empty-text">暂无粉丝</p>
      <p class="empty-hint">持续创作优质内容，吸引更多粉丝</p>
    </div>

    <!-- 粉丝列表 -->
    <div v-else class="user-list">
      <div
        v-for="user in followersList"
        :key="user.id"
        class="user-card"
        @click="goToUser(user.id)"
      >
        <img
          :src="user.avatar || defaultAvatar"
          :alt="user.nickname"
          class="user-avatar"
        />
        <div class="user-info">
          <h3 class="user-nickname">{{ user.nickname || user.username }}</h3>
          <p class="user-bio">{{ user.bio || '这个人很懒，什么都没写' }}</p>
          <p class="follow-time">关注了你 {{ formatDate(user.followTime) }}</p>
        </div>
        <div class="user-action" @click.stop>
          <FollowButton :user-id="user.id" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getFollowers } from '@/api/follow'
import { useUserStore } from '@/stores/user'
import FollowButton from '@/components/FollowButton.vue'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const followersList = ref([])

const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="%239ca3af"%3E%3Cpath d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/%3E%3C/svg%3E'

const fetchFollowers = async () => {
  const userId = userStore.userInfo?.userId || userStore.userInfo?.id
  if (!userId) {
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const res = await getFollowers(userId)
    followersList.value = res.data || []
  } catch (error) {
    console.error('获取粉丝列表失败', error)
  } finally {
    loading.value = false
  }
}

const goToUser = (userId) => {
  router.push(`/user/${userId}`)
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  fetchFollowers()
})
</script>

<style lang="scss" scoped>
.followers-page {
  max-width: 900px;
  margin: 0 auto;
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

.user-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.user-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.user-avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: var(--radius-full);
  object-fit: cover;
  background: var(--bg-secondary);
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-nickname {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-bio {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.follow-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.user-action {
  flex-shrink: 0;
}

@media (max-width: 640px) {
  .user-avatar {
    width: 48px;
    height: 48px;
  }

  .user-bio {
    display: none;
  }
}
</style>
