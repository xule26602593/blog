<template>
  <div class="message-chat">
    <van-nav-bar :title="peerName" left-arrow @click-left="$router.back()" />

    <div class="chat-container" ref="chatContainer">
      <van-loading v-if="loading" class="loading-container" />

      <div v-else class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'is-self': msg.senderId === currentUserId }"
        >
          <div class="msg-avatar">
            <img :src="msg.senderAvatar || defaultAvatar" />
          </div>
          <div class="msg-content">
            <div class="msg-bubble">{{ msg.content }}</div>
            <div class="msg-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <van-field
        v-model="inputText"
        placeholder="输入消息..."
        :disabled="sending"
        @keyup.enter="sendMessage"
      />
      <van-button type="primary" :loading="sending" @click="sendMessage">发送</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { getMessages, sendMessage as sendMessageApi, markAsRead } from '@/api/privateMessage'

const route = useRoute()
const userStore = useUserStore()
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const currentUserId = userStore.userInfo?.id
const conversationId = route.params.id
const peerId = route.query.peerId
const peerName = route.query.peerName || '聊天'

const loading = ref(false)
const sending = ref(false)
const messages = ref([])
const inputText = ref('')
const chatContainer = ref(null)

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await getMessages(conversationId, { pageNum: 1, pageSize: 100 })
    if (res.data) {
      messages.value = res.data.records
      await nextTick()
      scrollToBottom()
    }
    await markAsRead(conversationId)
  } catch (error) {
    console.error('获取消息失败:', error)
  } finally {
    loading.value = false
  }
}

const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text) return

  sending.value = true
  try {
    await sendMessageApi({
      receiverId: peerId,
      content: text,
      messageType: 1
    })
    inputText.value = ''
    await fetchMessages()
  } catch (error) {
    showToast('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

onMounted(() => {
  fetchMessages()
})
</script>

<style lang="scss" scoped>
.message-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: var(--bg-secondary);
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-4);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 50px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.message-item {
  display: flex;
  gap: var(--space-2);

  &.is-self {
    flex-direction: row-reverse;

    .msg-bubble {
      background: var(--color-primary);
      color: white;
    }

    .msg-time {
      text-align: right;
    }
  }
}

.msg-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.msg-content {
  max-width: 70%;
}

.msg-bubble {
  padding: var(--space-2) var(--space-3);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  font-size: var(--text-base);
  color: var(--text-primary);
  word-break: break-word;
}

.msg-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.chat-input {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-3);
  background: var(--bg-card);
  border-top: 1px solid var(--border-color);

  .van-field {
    flex: 1;
    background: var(--bg-secondary);
    border-radius: var(--radius-lg);
  }
}
</style>
