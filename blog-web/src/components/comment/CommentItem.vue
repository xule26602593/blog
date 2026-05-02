<!-- CommentItem.vue -->
<template>
  <div class="comment-item">
    <img :src="getUserAvatar({ avatar: comment.avatar, nickname: comment.nickname, userId: comment.userId })" class="avatar" />

    <div class="content">
      <div class="header">
        <span class="nickname">{{ comment.nickname }}</span>
        <span class="time">{{ formatTime(comment.createTime) }}</span>
      </div>

      <div class="text" v-html="renderContent(comment.content)"></div>

      <div class="actions">
        <button class="action-btn" @click="handleLike">
          <span :class="['like-icon', { liked: comment.isLiked }]">👍</span>
          <span class="count" @click.stop="showLikes">{{ comment.likeCount || 0 }}</span>
        </button>
        <button class="action-btn" @click="$emit('reply', comment)">
          回复
        </button>
      </div>

      <!-- 回复列表 -->
      <div v-if="comment.replies && comment.replies.length > 0" class="replies">
        <ReplyItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :reply="reply"
          @reply="$emit('reply', $event)"
          @like="$emit('like', $event)"
        />

        <button
          v-if="comment.replyCount > comment.replies.length"
          class="more-replies"
          @click="$emit('load-replies', comment.id)"
        >
          展开更多 {{ comment.replyCount - comment.replies.length }} 条回复
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import ReplyItem from './ReplyItem.vue'
import { formatTimeAgo } from '@/utils/time'
import { getUserAvatar } from '@/utils/avatar'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['reply', 'like', 'show-likes', 'load-replies'])

const formatTime = (time) => formatTimeAgo(time)

const renderContent = (content) => {
  // 将@用户转换为高亮显示
  return content.replace(/@([一-龥a-zA-Z0-9_]+)/g, '<span class="mention">@$1</span>')
}

const handleLike = () => {
  emit('like', props.comment)
}

const showLikes = () => {
  emit('show-likes', props.comment)
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #ebedf0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.content {
  flex: 1;
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-weight: 500;
  color: #323233;
}

.time {
  font-size: 12px;
  color: #969799;
}

.text {
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
  word-break: break-word;
}

.text :deep(.mention) {
  color: #1989fa;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #646566;
  cursor: pointer;
  border-radius: 4px;
}

.action-btn:hover {
  background: #f7f8fa;
}

.like-icon.liked {
  filter: none;
}

.count {
  color: #969799;
}

.replies {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #ebedf0;
}

.more-replies {
  display: block;
  width: 100%;
  padding: 8px;
  margin-top: 8px;
  border: none;
  background: #f7f8fa;
  color: #1989fa;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
}

.more-replies:hover {
  background: #ebedf0;
}
</style>
