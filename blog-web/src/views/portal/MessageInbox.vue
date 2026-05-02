<template>
  <div class="message-inbox">
    <van-nav-bar title="私信" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" class="loading-container" />

    <template v-else>
      <div v-if="conversations.length > 0" class="conversation-list">
        <div
          class="conversation-item"
          v-for="conv in conversations"
          :key="conv.id"
          @click="openChat(conv)"
        >
          <div class="conv-avatar">
            <img :src="getAvatar(conv.peerAvatar)" :alt="conv.peerNickname" />
          </div>
          <div class="conv-content">
            <div class="conv-header">
              <span class="conv-name">{{ conv.peerNickname }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="conv-preview">{{ conv.lastMessage }}</div>
          </div>
          <van-badge v-if="conv.unreadCount > 0" :content="conv.unreadCount" />
        </div>
      </div>

      <van-empty v-else description="暂无私信" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getAvatar } from '@/utils/avatar'
import { getConversations } from '@/api/privateMessage'

const router = useRouter()

const loading = ref(false)
const conversations = ref([])

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}

const fetchConversations = async () => {
  loading.value = true
  try {
    const res = await getConversations({ pageNum: 1, pageSize: 50 })
    if (res.data) {
      conversations.value = res.data.records
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
  } finally {
    loading.value = false
  }
}

const openChat = (conv) => {
  router.push({ name: 'MessageChat', params: { id: conv.id }, query: { peerId: conv.peerId, peerName: conv.peerNickname } })
}

onMounted(() => {
  fetchConversations()
})
</script>

<style lang="scss" scoped>
.message-inbox {
  min-height: 100vh;
  background: var(--bg-primary);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 50px;
}

.conversation-list {
  padding: var(--space-2);
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  margin-bottom: var(--space-2);
  cursor: pointer;

  &:hover {
    background: var(--bg-hover);
  }
}

.conv-avatar {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.conv-content {
  flex: 1;
  min-width: 0;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-1);
}

.conv-name {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.conv-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.conv-preview {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
