<template>
  <div class="user-profile-public">
    <van-loading v-if="loading" class="loading-container" />

    <template v-else-if="user">
      <!-- User Info Card -->
      <div class="user-card">
        <div class="user-avatar">
          <img :src="user.avatar || defaultAvatar" :alt="user.nickname" />
        </div>
        <h1 class="user-nickname">{{ user.nickname }}</h1>
        <p v-if="user.bio" class="user-bio">{{ user.bio }}</p>
        <a v-if="user.website" :href="user.website" target="_blank" class="user-website">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
          {{ user.website }}
        </a>

        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-value">{{ user.followingCount || 0 }}</span>
            <span class="stat-label">关注</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">{{ user.followerCount || 0 }}</span>
            <span class="stat-label">粉丝</span>
          </div>
        </div>

        <div v-if="userStore.isLoggedIn && user.id !== userStore.userInfo?.id" class="user-actions">
          <van-button
            :type="user.isFollowing ? 'default' : 'primary'"
            @click="handleFollow"
            :loading="followLoading"
          >
            {{ user.isFollowing ? '取消关注' : '关注' }}
          </van-button>
        </div>
      </div>

      <!-- Recent Comments -->
      <div class="comments-section">
        <h2 class="section-title">最近评论</h2>
        <van-loading v-if="commentsLoading" />
        <template v-else-if="comments.length > 0">
          <div class="comment-item" v-for="comment in comments" :key="comment.id">
            <p class="comment-content">{{ comment.content }}</p>
            <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
          </div>
          <div v-if="hasMoreComments" class="load-more">
            <van-button plain size="small" @click="loadMoreComments">加载更多</van-button>
          </div>
        </template>
        <van-empty v-else description="暂无评论" />
      </div>
    </template>

    <van-empty v-else description="用户不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const route = useRoute()
const userStore = useUserStore()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const loading = ref(false)
const user = ref(null)
const comments = ref([])
const commentsLoading = ref(false)
const commentsTotal = ref(0)
const commentsPage = ref(1)
const followLoading = ref(false)

const hasMoreComments = computed(() => comments.value.length < commentsTotal.value)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    month: 'short',
    day: 'numeric'
  })
}

const fetchUser = async () => {
  loading.value = true
  try {
    const res = await request.get(`/api/portal/users/${route.params.id}`)
    user.value = res.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
  } finally {
    loading.value = false
  }
}

const fetchComments = async (isLoadMore = false) => {
  if (isLoadMore) {
    commentsPage.value++
  }
  commentsLoading.value = true
  try {
    const res = await request.get(`/api/portal/users/${route.params.id}/comments`, {
      params: { pageNum: commentsPage.value, pageSize: 5 }
    })
    if (res.data) {
      if (isLoadMore) {
        comments.value.push(...res.data.records)
      } else {
        comments.value = res.data.records
      }
      commentsTotal.value = res.data.total
    }
  } catch (error) {
    console.error('获取评论失败:', error)
  } finally {
    commentsLoading.value = false
  }
}

const loadMoreComments = () => {
  fetchComments(true)
}

const handleFollow = async () => {
  followLoading.value = true
  try {
    await request.post(`/api/follow/${user.value.id}`)
    user.value.isFollowing = !user.value.isFollowing
    if (user.value.isFollowing) {
      user.value.followerCount++
      showToast({ type: 'success', message: '关注成功' })
    } else {
      user.value.followerCount--
      showToast('已取消关注')
    }
  } catch (error) {
    showToast('操作失败')
  } finally {
    followLoading.value = false
  }
}

onMounted(() => {
  fetchUser()
  fetchComments()
})
</script>

<style lang="scss" scoped>
.user-profile-public {
  max-width: 600px;
  margin: 0 auto;
  padding: var(--space-4);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.user-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  text-align: center;
  margin-bottom: var(--space-4);
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  overflow: hidden;
  margin: 0 auto var(--space-4);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.user-nickname {
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.user-bio {
  font-size: var(--text-base);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.user-website {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  margin-bottom: var(--space-4);

  svg {
    width: 14px;
    height: 14px;
  }
}

.user-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-6);
  margin-bottom: var(--space-4);
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: var(--text-xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: var(--border-color);
}

.user-actions {
  margin-top: var(--space-4);
}

.comments-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.section-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.comment-item {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.comment-content {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  margin-bottom: var(--space-2);
}

.comment-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: var(--space-3);
}
</style>
