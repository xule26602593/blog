<!-- CommentInput.vue -->
<template>
  <div class="comment-input">
    <!-- 回复提示 -->
    <div v-if="replyTo" class="reply-hint">
      <span>回复 <strong>@{{ replyTo.nickname }}</strong></span>
      <button class="cancel-btn" @click="$emit('cancel-reply')">
        <van-icon name="cross" />
      </button>
    </div>

    <!-- 输入区域 -->
    <div class="input-wrapper">
      <textarea
        ref="textareaRef"
        v-model="content"
        :placeholder="placeholder"
        rows="3"
        @input="handleInput"
        @keydown="handleKeydown"
      />

      <!-- @用户选择器 -->
      <div v-if="showMentionPicker" class="mention-picker">
        <div v-if="loading" class="loading">搜索中...</div>
        <template v-else>
          <div
            v-for="user in matchedUsers"
            :key="user.userId"
            class="user-item"
            @click="selectMention(user)"
          >
            <img :src="getUserAvatar({ avatar: user.avatar, nickname: user.nickname, userId: user.userId })" class="avatar" />
            <span class="nickname">{{ user.nickname }}</span>
          </div>
          <div v-if="matchedUsers.length === 0" class="no-result">
            未找到用户
          </div>
        </template>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="tools">
        <button class="tool-btn" @click="toggleEmojiPicker">
          <span>😊</span>
          <span class="tool-text">表情</span>
        </button>
      </div>
      <button
        class="submit-btn"
        :disabled="!content.trim() || submitting"
        @click="submit"
      >
        {{ submitting ? '发表中...' : '发表评论' }}
      </button>
    </div>

    <!-- Emoji选择器 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      @select="insertEmoji"
    />
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { Toast } from 'vant'
import EmojiPicker from './EmojiPicker.vue'
import { searchUsers } from '@/api/comment'
import { getUserAvatar } from '@/utils/avatar'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  },
  replyTo: {
    type: Object,
    default: null
  },
  placeholder: {
    type: String,
    default: '写下你的评论...'
  }
})

const emit = defineEmits(['submit', 'cancel-reply'])

const content = ref('')
const submitting = ref(false)
const showEmojiPicker = ref(false)
const showMentionPicker = ref(false)
const matchedUsers = ref([])
const loading = ref(false)
const mentionStartIndex = ref(-1)
const textareaRef = ref(null)

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji) => {
  content.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
}

const handleInput = async (e) => {
  const value = e.target.value
  const cursorPos = e.target.selectionStart

  // 检测@符号
  const lastAtIndex = value.lastIndexOf('@', cursorPos - 1)
  if (lastAtIndex !== -1) {
    // 检查@后面是否有空格（表示已完成）
    const textAfterAt = value.slice(lastAtIndex + 1, cursorPos)
    if (!textAfterAt.includes(' ') && textAfterAt.length <= 20) {
      mentionStartIndex.value = lastAtIndex
      showMentionPicker.value = true
      await searchMentionUsers(textAfterAt)
      return
    }
  }

  showMentionPicker.value = false
}

const searchMentionUsers = async (keyword) => {
  if (!keyword) {
    matchedUsers.value = []
    return
  }

  loading.value = true
  try {
    const { data } = await searchUsers(keyword, 5)
    matchedUsers.value = data || []
  } catch (error) {
    console.error('Search users failed:', error)
    matchedUsers.value = []
  } finally {
    loading.value = false
  }
}

const selectMention = (user) => {
  // 替换@及其后面的文本为 @昵称
  const before = content.value.slice(0, mentionStartIndex.value)
  const after = content.value.slice(textareaRef.value.selectionStart)
  content.value = `${before}@${user.nickname} ${after}`

  showMentionPicker.value = false
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

const handleKeydown = (e) => {
  // Ctrl/Cmd + Enter 提交
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    submit()
  }
}

const submit = async () => {
  if (!content.value.trim() || submitting.value) return

  submitting.value = true
  try {
    const data = {
      articleId: props.articleId,
      content: content.value.trim()
    }

    if (props.replyTo) {
      data.parentId = props.replyTo.parentId || props.replyTo.id
      data.replyId = props.replyTo.id
      data.replyToUserId = props.replyTo.userId
    }

    emit('submit', data)
    content.value = ''
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.comment-input {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px;
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #e8f4ff;
  border-radius: 4px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #1989fa;
}

.cancel-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
}

.input-wrapper {
  position: relative;
}

textarea {
  width: 100%;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  padding: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
  box-sizing: border-box;
}

textarea:focus {
  border-color: #1989fa;
}

.mention-picker {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}

.mention-picker .loading,
.mention-picker .no-result {
  padding: 12px;
  text-align: center;
  color: #969799;
}

.mention-picker .user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.mention-picker .user-item:hover {
  background: #f7f8fa;
}

.mention-picker .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.mention-picker .nickname {
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.tools {
  display: flex;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
}

.tool-btn:hover {
  background: #ebedf0;
}

.tool-text {
  color: #646566;
}

.submit-btn {
  padding: 8px 16px;
  background: #1989fa;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.submit-btn:disabled {
  background: #c8c9cc;
  cursor: not-allowed;
}
</style>
