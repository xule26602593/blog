<!-- ReplyItem.vue -->
<template>
  <div class="reply-item">
    <img :src="getUserAvatar({ avatar: reply.avatar, nickname: reply.nickname, userId: reply.userId })" class="avatar" />

    <div class="content">
      <div class="header">
        <span class="nickname">{{ reply.nickname }}</span>
        <template v-if="reply.replyToNickname">
          <span class="reply-to">回复</span>
          <span class="reply-to-name">@{{ reply.replyToNickname }}</span>
        </template>
        <span class="time">{{ formatTime(reply.createTime) }}</span>
      </div>

      <div class="text" v-html="renderContent(reply.content)"></div>

      <div class="actions">
        <button class="action-btn" @click="handleLike">
          <span :class="['like-icon', { liked: reply.isLiked }]">👍</span>
          <span class="count">{{ reply.likeCount || 0 }}</span>
        </button>
        <button class="action-btn" @click="$emit('reply', reply)">
          回复
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatTimeAgo } from '@/utils/time'
import { getUserAvatar } from '@/utils/avatar'

const props = defineProps({
  reply: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['reply', 'like'])

const formatTime = (time) => formatTimeAgo(time)

const renderContent = (content) => {
  return content.replace(/@([一-龥a-zA-Z0-9_]+)/g, '<span class="mention">@$1</span>')
}

const handleLike = () => {
  emit('like', props.reply)
}
</script>

<style scoped>
.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.avatar {
  width: 28px;
  height: 28px;
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
  gap: 6px;
  margin-bottom: 2px;
  font-size: 13px;
}

.nickname {
  font-weight: 500;
  color: #323233;
}

.reply-to {
  color: #969799;
}

.reply-to-name {
  color: #1989fa;
}

.time {
  font-size: 12px;
  color: #969799;
}

.text {
  font-size: 13px;
  line-height: 1.5;
  color: #323233;
}

.text :deep(.mention) {
  color: #1989fa;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: #646566;
  cursor: pointer;
  border-radius: 4px;
}

.action-btn:hover {
  background: #f7f8fa;
}

.count {
  color: #969799;
  font-size: 12px;
}
</style>
