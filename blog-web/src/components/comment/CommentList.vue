<!-- CommentList.vue -->
<template>
  <div class="comment-section">
    <!-- 标题和排序 -->
    <div class="header">
      <h3 class="title">评论区 ({{ total }}条)</h3>
      <div class="sort-bar">
        <button
          v-for="option in sortOptions"
          :key="option.value"
          :class="['sort-btn', { active: sortBy === option.value }]"
          @click="changeSort(option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
        @like="handleLike"
        @show-likes="showLikeList"
        @load-replies="loadMoreReplies"
      />

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <van-button block @click="loadMore" :loading="loading">
          加载更多评论
        </van-button>
      </div>

      <!-- 空状态 -->
      <van-empty v-if="!loading && comments.length === 0" description="暂无评论，快来抢沙发吧~" />
    </div>

    <!-- 评论输入 -->
    <div class="comment-input-wrapper">
      <CommentInput
        :article-id="articleId"
        :reply-to="replyTo"
        @submit="handleSubmit"
        @cancel-reply="cancelReply"
      />
    </div>

    <!-- 点赞列表弹窗 -->
    <van-popup
      v-model:show="showLikePopup"
      position="bottom"
      round
      style="height: 50%"
    >
      <div class="like-popup">
        <div class="popup-header">
          <span>点赞列表 ({{ likeTotal }}人)</span>
          <van-icon name="cross" @click="showLikePopup = false" />
        </div>
        <div class="like-list">
          <div v-for="user in likeUsers" :key="user.userId" class="like-user">
            <img :src="getUserAvatar({ avatar: user.avatar, nickname: user.nickname, userId: user.userId })" class="avatar" />
            <span class="nickname">{{ user.nickname }}</span>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { showToast, showSuccess, showFail } from '@/utils/toast'
import { getUserAvatar } from '@/utils/avatar'
import CommentItem from './CommentItem.vue'
import CommentInput from './CommentInput.vue'
import {
  getCommentsList,
  createComment,
  toggleCommentLike,
  getCommentLikes,
  getReplies
} from '@/api/comment'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  }
})

const sortOptions = [
  { value: 'hot', label: '最热' },
  { value: 'newest', label: '最新' },
  { value: 'oldest', label: '最早' }
]

const comments = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortBy = ref('hot')
const loading = ref(false)
const replyTo = ref(null)

// 点赞列表
const showLikePopup = ref(false)
const likeUsers = ref([])
const likeTotal = ref(0)

const hasMore = computed(() => comments.value.length < total.value)

const fetchComments = async (reset = false) => {
  if (reset) {
    page.value = 1
    comments.value = []
  }

  loading.value = true
  try {
    const { data } = await getCommentsList(props.articleId, {
      sortBy: sortBy.value,
      page: page.value,
      size: size.value
    })

    if (reset) {
      comments.value = data.records
    } else {
      comments.value.push(...data.records)
    }
    total.value = data.total
  } catch (error) {
    console.error('Fetch comments failed:', error)
    showFail('加载评论失败')
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchComments()
}

const changeSort = (newSort) => {
  if (sortBy.value !== newSort) {
    sortBy.value = newSort
    fetchComments(true)
  }
}

const handleReply = (comment) => {
  replyTo.value = comment
}

const cancelReply = () => {
  replyTo.value = null
}

const handleSubmit = async (data) => {
  try {
    await createComment(data)
    showSuccess('评论成功')
    
    // 重新加载评论列表以确保数据一致性
    await fetchComments(true)
    
    cancelReply()
  } catch (error) {
    console.error('Create comment failed:', error)
    showFail('评论失败')
  }
}

const handleLike = async (comment) => {
  try {
    const { data } = await toggleCommentLike(comment.id)
    comment.isLiked = data.liked
    comment.likeCount = data.likeCount
  } catch (error) {
    console.error('Toggle like failed:', error)
    showFail('操作失败')
  }
}

const showLikeList = async (comment) => {
  showLikePopup.value = true

  try {
    const { data } = await getCommentLikes(comment.id, { page: 1, size: 50 })
    likeUsers.value = data.records
    likeTotal.value = data.total
  } catch (error) {
    console.error('Fetch likes failed:', error)
  }
}

const loadMoreReplies = async (commentId) => {
  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) return

  try {
    const { data } = await getReplies(commentId, {
      sortBy: sortBy.value,
      page: Math.ceil((comment.replies?.length || 0) / 10) + 1,
      size: 10
    })
    comment.replies = comment.replies || []
    comment.replies.push(...data.records)
  } catch (error) {
    console.error('Load replies failed:', error)
    showFail('加载回复失败')
  }
}

onMounted(() => {
  fetchComments()
})

// 监听文章ID变化
watch(() => props.articleId, () => {
  fetchComments(true)
})
</script>

<style scoped>
.comment-section {
  margin-top: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.sort-bar {
  display: flex;
  gap: 8px;
}

.sort-btn {
  padding: 4px 12px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #646566;
  cursor: pointer;
  border-radius: 4px;
}

.sort-btn.active {
  background: #1989fa;
  color: #fff;
}

.comment-list {
  min-height: 100px;
}

.load-more {
  padding: 16px 0;
}

.comment-input-wrapper {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebedf0;
}

.like-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebedf0;
  font-size: 16px;
  font-weight: 500;
}

.like-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.like-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #ebedf0;
}

.like-user .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.like-user .nickname {
  font-size: 14px;
  color: #323233;
}
</style>
