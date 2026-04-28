# 内容智能化系统 - 阶段四：AI 问答助手

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现文章 AI 问答助手，允许用户就文章内容提问。

**Architecture:** 流式响应 + SSE + 聊天界面

**Prerequisites:**
- 完成阶段一：智能摘要 + 标签
- AI 服务基类已实现
- 流式状态机已实现

---

## Task 4.1: 创建文章问答服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/ai/ArticleChatService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/ArticleChatServiceImpl.java`
- Create: `blog-server/src/main/java/com/blog/controller/portal/AiPortalController.java`

- [ ] **Step 1: 创建 ArticleChatService 接口**

```java
package com.blog.service.ai;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface ArticleChatService {
    
    /**
     * 文章问答
     */
    SseEmitter chat(Long articleId, String question);
}
```

- [ ] **Step 2: 创建 ArticleChatServiceImpl**

```java
package com.blog.service.impl.ai;

import com.blog.domain.entity.Article;
import com.blog.service.ArticleService;
import com.blog.service.ai.AiService;
import com.blog.service.ai.ArticleChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ArticleChatServiceImpl implements ArticleChatService {
    
    private final AiService aiService;
    private final ArticleService articleService;
    
    @Override
    public SseEmitter chat(Long articleId, String question) {
        Article article = articleService.getById(articleId);
        if (article == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new RuntimeException("文章不存在"));
            return emitter;
        }
        
        return aiService.generateStream("chat_article", Map.of(
            "title", article.getTitle() != null ? article.getTitle() : "",
            "content", truncate(article.getContent(), 10000),
            "question", question != null ? question : ""
        ));
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
```

- [ ] **Step 3: 创建 AiPortalController**

```java
package com.blog.controller.portal;

import com.blog.service.ai.ArticleChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/portal/ai")
@RequiredArgsConstructor
public class AiPortalController {
    
    private final ArticleChatService articleChatService;
    
    /**
     * 文章问答（流式）
     * 需要登录认证
     */
    @GetMapping("/chat")
    public SseEmitter chat(
        @RequestParam Long articleId,
        @RequestParam String question
    ) {
        return articleChatService.chat(articleId, question);
    }
}
```

- [ ] **Step 4: 提交变更**

```bash
git add src/main/java/com/blog/service/
git add src/main/java/com/blog/controller/portal/AiPortalController.java
git commit -m "feat: add article chat service and portal controller"
```

---

## Task 4.2: 创建 AI 问答助手组件

**Files:**
- Create: `blog-web/src/components/AiChatAssistant.vue`

- [ ] **Step 1: 创建 AiChatAssistant 组件**

```vue
<template>
  <!-- 悬浮按钮 -->
  <div v-if="!isExpanded" class="chat-fab" @click="isExpanded = true">
    <van-badge :content="unreadCount > 0 ? unreadCount : ''" :show-zero="false">
      <van-icon name="chat-o" size="24" />
    </van-badge>
  </div>
  
  <!-- 聊天窗口 -->
  <van-popup
    v-else
    v-model:show="isExpanded"
    position="bottom"
    :style="{ height: '60%' }"
    round
  >
    <div class="chat-assistant">
      <!-- 标题栏 -->
      <div class="chat-header">
        <span>文章问答助手</span>
        <div class="header-actions">
          <van-icon name="minus" @click="isExpanded = false" />
          <van-icon name="cross" @click="handleClose" />
        </div>
      </div>
      
      <!-- 消息列表 -->
      <div ref="messageListRef" class="message-list">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['message', msg.role]"
        >
          <div class="message-content">
            <template v-if="msg.role === 'user'">
              {{ msg.content }}
            </template>
            <template v-else>
              <pre>{{ msg.content }}</pre>
              <span v-if="index === messages.length - 1 && streamState.isStreaming.value" class="cursor">█</span>
            </template>
          </div>
          <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
        </div>
        
        <div v-if="messages.length === 0" class="empty-message">
          <p>👋 我是文章问答助手</p>
          <p>你可以问我关于这篇文章的任何问题</p>
        </div>
      </div>
      
      <!-- 输入区 -->
      <div class="input-area">
        <van-field
          v-model="inputMessage"
          placeholder="输入你的问题..."
          :disabled="streamState.isStreaming.value"
          @keyup.enter="handleSend"
        />
        <van-button
          type="primary"
          size="small"
          :loading="streamState.isLoading.value"
          :disabled="!inputMessage.trim()"
          @click="handleSend"
        >
          发送
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { useStreamState } from '@/composables/useStreamState'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  },
  articleTitle: String
})

const streamState = useStreamState()
const isExpanded = ref(false)
const messages = ref([])
const inputMessage = ref('')
const messageListRef = ref(null)
const unreadCount = ref(0)
let abortController = null

const handleSend = () => {
  if (!inputMessage.value.trim()) return
  
  const question = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: question,
    timestamp: new Date()
  })
  
  // 添加助手消息占位
  messages.value.push({
    role: 'assistant',
    content: '',
    timestamp: new Date()
  })
  
  // 滚动到底部
  scrollToBottom()
  
  // 发起请求
  streamState.start()
  const url = `/api/portal/ai/chat?articleId=${props.articleId}&question=${encodeURIComponent(question)}`
  const token = localStorage.getItem('token') || ''
  
  abortController = new AbortController()
  
  fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    },
    signal: abortController.signal
  })
    .then(response => {
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      
      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            streamState.complete()
            return
          }
          const text = decoder.decode(value, { stream: true })
          if (streamState.isLoading.value) streamState.firstByte()
          const lines = text.split('\n')
          lines.forEach(line => {
            if (line.startsWith('data:')) {
              const content = line.slice(5).trim()
              if (content) {
                streamState.append(content)
                // 更新最后一条消息
                const lastMsg = messages.value[messages.value.length - 1]
                if (lastMsg.role === 'assistant') {
                  lastMsg.content += content
                }
                scrollToBottom()
              }
            }
          })
          read()
        })
      }
      read()
    })
    .catch(error => {
      if (error.name !== 'AbortError') {
        streamState.error(error.message)
        const lastMsg = messages.value[messages.value.length - 1]
        if (lastMsg.role === 'assistant') {
          lastMsg.content = '抱歉，发生了错误：' + error.message
        }
      }
    })
  
  // 如果窗口收起，增加未读数
  if (!isExpanded.value) {
    unreadCount.value++
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const handleClose = () => {
  if (abortController && streamState.isStreaming.value) {
    abortController.abort()
    streamState.cancel()
  }
  isExpanded.value = false
}

const formatTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 窗口展开时清除未读数
watch(isExpanded, (expanded) => {
  if (expanded) {
    unreadCount.value = 0
  }
})
</script>

<style scoped>
.chat-fab {
  position: fixed;
  right: 16px;
  bottom: 80px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--van-primary-color);
  color: white;
  border-radius: 50%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  z-index: 100;
}

.chat-assistant {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.header-actions {
  display: flex;
  gap: 16px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.message {
  margin-bottom: 16px;
}

.message.user {
  text-align: right;
}

.message-content {
  display: inline-block;
  max-width: 80%;
  padding: 12px;
  border-radius: 8px;
  background: #f0f0f0;
}

.message.user .message-content {
  background: var(--van-primary-color);
  color: white;
}

.message.assistant .message-content {
  background: #f7f8fa;
  text-align: left;
}

.message-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.empty-message {
  text-align: center;
  color: #999;
  padding: 40px 20px;
}

.input-area {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #eee;
}

.input-area .van-field {
  flex: 1;
}
</style>
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/components/AiChatAssistant.vue
git commit -m "feat: add AI chat assistant component"
```

---

## Task 4.3: 集成问答助手到文章详情页

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 在 ArticleDetail.vue 中添加问答助手**

```vue
<template>
  <!-- ... 现有内容 ... -->
  
  <!-- AI 问答助手 -->
  <AiChatAssistant
    :article-id="articleId"
    :article-title="article.title"
  />
</template>

<script setup>
import AiChatAssistant from '@/components/AiChatAssistant.vue'

// ... 现有代码 ...
</script>
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/views/portal/ArticleDetail.vue
git commit -m "feat: integrate AI chat assistant in article detail"
```

---

## 完成检查

- [ ] 文章问答功能正常
- [ ] 流式响应正常
- [ ] 聊天界面交互正常
- [ ] 悬浮按钮显示正常
- [ ] 未读消息计数正常
- [ ] 用户认证检查正常

## 下一步

完成本阶段后，继续执行 `2026-04-28-content-intelligence-phase5-testing.md`
