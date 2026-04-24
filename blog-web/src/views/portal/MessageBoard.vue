<!-- blog-web/src/views/portal/MessageBoard.vue -->
<template>
  <div class="message-board">
    <!-- Header -->
    <div class="board-header">
      <h1 class="board-title">留言板</h1>
      <p class="board-desc">欢迎留下你的想法和建议</p>
    </div>

    <!-- Message Form -->
    <div class="message-form-wrapper">
      <van-cell-group inset>
        <van-field
          v-if="!userStore.isLoggedIn"
          v-model="form.nickname"
          label="昵称"
          placeholder="请输入昵称"
          :rules="[{ required: true, message: '请输入昵称' }]"
        />
        <van-field
          v-if="!userStore.isLoggedIn"
          v-model="form.email"
          label="邮箱"
          type="email"
          placeholder="请输入邮箱（选填）"
        />
        <van-field
          v-model="form.content"
          rows="4"
          autosize
          type="textarea"
          label="留言"
          placeholder="写下你的留言..."
          :rules="[{ required: true, message: '请输入留言内容' }]"
        />
      </van-cell-group>
      <div class="form-actions">
        <van-button type="primary" block @click="submitForm" :loading="submitting">
          提交留言
        </van-button>
      </div>
    </div>

    <!-- Message List -->
    <div class="message-list">
      <div class="list-header">
        <span class="list-title">留言列表</span>
        <span class="list-count">{{ total }} 条留言</span>
      </div>

      <van-loading v-if="loading" class="loading-container" />

      <template v-else-if="messageList.length > 0">
        <div class="message-item" v-for="msg in messageList" :key="msg.id">
          <div class="message-avatar">
            <img :src="msg.avatar || defaultAvatar" :alt="msg.nickname" />
          </div>
          <div class="message-content">
            <div class="message-meta">
              <span class="message-author">{{ msg.nickname }}</span>
              <span class="message-time">{{ formatDate(msg.createTime) }}</span>
            </div>
            <div class="message-text">{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="hasMore" class="load-more">
          <van-button plain @click="loadMore" :loading="loadingMore">
            加载更多
          </van-button>
        </div>
      </template>

      <van-empty v-else description="暂无留言，来说点什么吧~" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { getMessages, submitMessage } from '@/api/message'

const userStore = useUserStore()

const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

const loading = ref(false)
const submitting = ref(false)
const loadingMore = ref(false)
const messageList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 10

const form = reactive({
  nickname: '',
  email: '',
  content: ''
})

const hasMore = computed(() => messageList.value.length < total.value)

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const fetchMessages = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
  }

  try {
    const res = await getMessages({ pageNum: pageNum.value, pageSize })
    if (res.data) {
      if (isLoadMore) {
        messageList.value.push(...res.data.records)
      } else {
        messageList.value = res.data.records
      }
      total.value = res.data.total
    }
  } catch (error) {
    console.error('获取留言失败:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const submitForm = async () => {
  if (!form.content.trim()) {
    showToast('请输入留言内容')
    return
  }

  if (!userStore.isLoggedIn && !form.nickname.trim()) {
    showToast('请输入昵称')
    return
  }

  submitting.value = true
  try {
    await submitMessage({
      content: form.content,
      nickname: userStore.isLoggedIn ? undefined : form.nickname,
      email: userStore.isLoggedIn ? undefined : form.email
    })
    showToast({ type: 'success', message: '留言提交成功，等待审核' })
    form.content = ''
    form.nickname = ''
    form.email = ''
  } catch (error) {
    showToast('提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const loadMore = () => {
  pageNum.value++
  fetchMessages(true)
}

onMounted(() => {
  fetchMessages()
})
</script>

<style lang="scss" scoped>
.message-board {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--space-4);
}

.board-header {
  text-align: center;
  padding: var(--space-8) 0;
}

.board-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.board-desc {
  font-size: var(--text-base);
  color: var(--text-secondary);
}

.message-form-wrapper {
  margin-bottom: var(--space-8);
}

.form-actions {
  margin-top: var(--space-4);
  padding: 0 var(--space-4);
}

.message-list {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: var(--space-4);
}

.list-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.list-count {
  font-size: var(--text-sm);
  color: var(--text-muted);
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: var(--space-8);
}

.message-item {
  display: flex;
  gap: var(--space-3);
  padding: var(--space-4) 0;
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.message-avatar {
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

.message-content {
  flex: 1;
  min-width: 0;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.message-author {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.message-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.message-text {
  font-size: var(--text-base);
  color: var(--text-secondary);
  line-height: var(--leading-relaxed);
  word-break: break-word;
}

.load-more {
  display: flex;
  justify-content: center;
  padding: var(--space-4);
}
</style>
