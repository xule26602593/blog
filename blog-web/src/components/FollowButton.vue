<template>
  <button
    class="follow-btn"
    :class="{ following: isFollowing, loading }"
    :disabled="loading || isSelf"
    @click="handleClick"
  >
    <span v-if="loading" class="loading-spinner"></span>
    <template v-else>
      <svg v-if="!isFollowing" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
      </svg>
      <span>{{ buttonText }}</span>
    </template>
  </button>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { followUser, unfollowUser, checkFollow } from '@/api/follow'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

const userStore = useUserStore()
const isFollowing = ref(false)
const loading = ref(false)

const isSelf = computed(() => {
  return userStore.userInfo?.id === props.userId
})

const buttonText = computed(() => {
  if (isFollowing.value) return '已关注'
  return '关注'
})

const checkFollowStatus = async () => {
  if (!userStore.isLoggedIn || isSelf.value) return

  try {
    const res = await checkFollow(props.userId)
    isFollowing.value = res.data
  } catch (e) {
    console.error('检查关注状态失败', e)
  }
}

const handleClick = async () => {
  if (!userStore.isLoggedIn) {
    // 未登录时跳转登录页
    window.location.href = '/#/login'
    return
  }

  loading.value = true
  try {
    if (isFollowing.value) {
      await unfollowUser(props.userId)
      isFollowing.value = false
    } else {
      await followUser(props.userId)
      isFollowing.value = true
    }
  } catch (e) {
    console.error('关注操作失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkFollowStatus()
})
</script>

<style lang="scss" scoped>
.follow-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover:not(:disabled):not(.following) {
    background: var(--color-accent);
  }

  &.following {
    background: var(--bg-secondary);
    color: var(--text-secondary);
    border: 1px solid var(--border-color);

    &:hover {
      color: var(--color-error);
      border-color: var(--color-error);
    }
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
