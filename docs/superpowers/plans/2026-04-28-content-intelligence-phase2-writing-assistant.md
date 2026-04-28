# 内容智能化系统 - 阶段二：辅助写作

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现写作助手功能，包括大纲生成、续写、润色、标题生成。

**Architecture:** 流式响应 + SSE + 前端实时显示

**Prerequisites:**
- 完成阶段一：智能摘要 + 标签
- AI 服务基类已实现
- 流式状态机已实现

---

## Task 2.1: 创建写作助手服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/ai/WritingAssistantService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/WritingAssistantServiceImpl.java`

- [ ] **Step 1: 创建 WritingAssistantService 接口**

```java
package com.blog.service.ai;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface WritingAssistantService {
    
    /**
     * 生成大纲
     */
    SseEmitter generateOutline(String title, String description, String style);
    
    /**
     * 续写
     */
    SseEmitter continueWriting(String context, String direction);
    
    /**
     * 润色
     */
    SseEmitter polish(String content, String style);
    
    /**
     * 生成标题
     */
    SseEmitter generateTitles(String content, int count);
}
```

- [ ] **Step 2: 创建 WritingAssistantServiceImpl**

```java
package com.blog.service.impl.ai;

import com.blog.service.ai.AiService;
import com.blog.service.ai.WritingAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WritingAssistantServiceImpl implements WritingAssistantService {
    
    private final AiService aiService;
    
    @Override
    public SseEmitter generateOutline(String title, String description, String style) {
        String templateKey = "tech".equals(style) ? "outline_tech" : "outline_tutorial";
        return aiService.generateStream(templateKey, Map.of(
            "title", title != null ? title : "",
            "description", description != null ? description : "无"
        ));
    }
    
    @Override
    public SseEmitter continueWriting(String context, String direction) {
        return aiService.generateStream("continue_logic", Map.of(
            "context", context != null ? context : "",
            "direction", direction != null ? direction : "继续写"
        ));
    }
    
    @Override
    public SseEmitter polish(String content, String style) {
        String templateKey = "formal".equals(style) ? "polish_formal" : "polish_casual";
        return aiService.generateStream(templateKey, Map.of(
            "content", content != null ? content : ""
        ));
    }
    
    @Override
    public SseEmitter generateTitles(String content, int count) {
        return aiService.generateStream("titles_generate", Map.of(
            "content", truncate(content, 3000),
            "count", String.valueOf(Math.max(1, Math.min(count, 10)))
        ));
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
```

- [ ] **Step 3: 更新 AiAdminController 添加写作辅助端点**

在 `AiAdminController.java` 中添加：

```java
private final WritingAssistantService writingAssistantService;

/**
 * 生成大纲（流式）
 */
@GetMapping("/writing/outline")
public SseEmitter generateOutline(
    @RequestParam String title,
    @RequestParam(required = false) String description,
    @RequestParam(defaultValue = "tech") String style
) {
    return writingAssistantService.generateOutline(title, description, style);
}

/**
 * 写作辅助（流式）
 */
@PostMapping("/writing/stream")
public SseEmitter writingStream(@RequestBody Map<String, Object> request) {
    String type = (String) request.get("type");
    switch (type) {
        case "continue":
            return writingAssistantService.continueWriting(
                (String) request.get("context"),
                (String) request.get("direction")
            );
        case "polish":
            return writingAssistantService.polish(
                (String) request.get("content"),
                (String) request.get("style")
            );
        case "titles":
            return writingAssistantService.generateTitles(
                (String) request.get("content"),
                request.get("count") != null ? (Integer) request.get("count") : 5
            );
        default:
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new BusinessException("未知的写作辅助类型"));
            return emitter;
    }
}
```

- [ ] **Step 4: 提交变更**

```bash
git add src/main/java/com/blog/service/
git add src/main/java/com/blog/controller/admin/AiAdminController.java
git commit -m "feat: add writing assistant service"
```

---

## Task 2.2: 创建写作助手面板组件

**Files:**
- Create: `blog-web/src/components/AiWritingPanel.vue`

- [ ] **Step 1: 创建 AiWritingPanel 组件**

```vue
<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="ai-writing-panel">
      <!-- 标题栏 -->
      <div class="panel-header">
        <span class="title">AI 写作助手</span>
        <van-icon name="cross" @click="visible = false" />
      </div>
      
      <!-- 功能标签页 -->
      <van-tabs v-model:active="activeTab">
        <van-tab title="大纲" name="outline">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              label="标题"
              placeholder="请输入文章标题"
            />
            <van-field
              v-model="description"
              label="说明"
              type="textarea"
              rows="2"
              placeholder="补充说明（可选）"
            />
            <van-radio-group v-model="outlineStyle" direction="horizontal">
              <van-radio name="tech">技术文章</van-radio>
              <van-radio name="tutorial">教程文章</van-radio>
            </van-radio-group>
          </div>
        </van-tab>
        
        <van-tab title="续写" name="continue">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入上下文内容"
            />
            <van-radio-group v-model="direction" direction="horizontal">
              <van-radio name="继续写">继续写</van-radio>
              <van-radio name="向下写">向下写</van-radio>
            </van-radio-group>
          </div>
        </van-tab>
        
        <van-tab title="润色" name="polish">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入需要润色的内容"
            />
            <van-radio-group v-model="polishStyle" direction="horizontal">
              <van-radio name="formal">正式风格</van-radio>
              <van-radio name="casual">轻松风格</van-radio>
            </van-radio-group>
          </div>
        </van-tab>
        
        <van-tab title="标题" name="titles">
          <div class="tab-content">
            <van-field
              v-model="inputText"
              type="textarea"
              rows="4"
              placeholder="请输入文章内容，AI将生成标题建议"
            />
          </div>
        </van-tab>
      </van-tabs>
      
      <!-- 操作按钮 -->
      <div class="action-bar">
        <van-button v-if="!streamState.isStreaming.value" type="primary" block @click="handleGenerate">
          生成
        </van-button>
        <van-button v-else type="danger" block @click="handleCancel">
          取消
        </van-button>
      </div>
      
      <!-- 输出区域 -->
      <div class="output-area">
        <div class="output-header">
          <span>生成结果</span>
          <div class="output-actions">
            <van-button v-if="streamState.isComplete.value" size="small" @click="handleCopy">
              复制
            </van-button>
            <van-button v-if="streamState.isComplete.value" type="primary" size="small" @click="handleApply">
              应用到编辑器
            </van-button>
          </div>
        </div>
        <div class="output-content">
          <template v-if="streamState.isLoading.value">
            <van-loading size="24px">生成中...</van-loading>
          </template>
          <template v-else-if="streamState.content.value">
            <pre>{{ streamState.content.value }}</pre>
            <span v-if="streamState.isStreaming.value" class="cursor">█</span>
          </template>
          <template v-else>
            <span class="placeholder">生成结果将在这里显示</span>
          </template>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { showToast } from 'vant'
import { streamRequest } from '@/api/ai'
import { useStreamState } from '@/composables/useStreamState'

const props = defineProps({
  show: Boolean,
  articleTitle: String,
  articleContent: String
})

const emit = defineEmits(['apply-content', 'update:show'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const streamState = useStreamState()
const activeTab = ref('outline')
const inputText = ref('')
const description = ref('')
const outlineStyle = ref('tech')
const direction = ref('继续写')
const polishStyle = ref('formal')

let abortController = null

// 从文章标题自动填充
watch(() => props.show, (show) => {
  if (show && props.articleTitle && activeTab.value === 'outline') {
    inputText.value = props.articleTitle
  }
})

const handleGenerate = () => {
  streamState.start()
  
  const data = {
    type: activeTab.value
  }
  
  if (activeTab.value === 'outline') {
    data.title = inputText.value
    data.description = description.value
    data.style = outlineStyle.value
    // 使用 GET 请求
    generateOutline(data)
  } else {
    if (activeTab.value === 'continue') {
      data.context = inputText.value
      data.direction = direction.value
    } else if (activeTab.value === 'polish') {
      data.content = inputText.value
      data.style = polishStyle.value
    } else if (activeTab.value === 'titles') {
      data.content = inputText.value
      data.count = 5
    }
    
    abortController = streamRequest(
      '/api/admin/ai/writing/stream',
      data,
      (chunk) => {
        if (streamState.isLoading.value) streamState.firstByte()
        streamState.append(chunk)
      },
      () => streamState.complete(),
      (error) => streamState.error(error.message)
    )
  }
}

const generateOutline = async (data) => {
  // 大纲使用 GET 请求
  const url = `/api/admin/ai/writing/outline?title=${encodeURIComponent(data.title)}&description=${encodeURIComponent(data.description || '')}&style=${data.style}`
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
              if (content) streamState.append(content)
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
      }
    })
}

const handleCancel = () => {
  if (abortController) {
    abortController.abort()
  }
  streamState.cancel()
}

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(streamState.content.value)
    showToast({ type: 'success', message: '已复制' })
  } catch {
    showToast('复制失败')
  }
}

const handleApply = () => {
  emit('apply-content', streamState.content.value)
  visible.value = false
}
</script>

<style scoped>
.ai-writing-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 16px;
  font-weight: 500;
}

.tab-content {
  padding: 16px;
}

.action-bar {
  padding: 16px;
  border-top: 1px solid #eee;
}

.output-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #f7f8fa;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.output-actions {
  display: flex;
  gap: 8px;
}

.output-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: white;
  border-radius: 4px;
}

.output-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  margin: 0;
}

.cursor {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.placeholder {
  color: #999;
}
</style>
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/components/AiWritingPanel.vue
git commit -m "feat: add AI writing panel component"
```

---

## Task 2.3: 集成写作助手到文章编辑页

**Files:**
- Modify: `blog-web/src/views/admin/ArticleEdit.vue`

- [ ] **Step 1: 在 ArticleEdit.vue 中添加写作助手**

在 `<script setup>` 中添加：

```javascript
// 添加导入
import AiWritingPanel from '@/components/AiWritingPanel.vue'

// 添加状态
const showWritingPanel = ref(false)

// 添加方法
const handleApplyWritingContent = (content) => {
  // 将生成的内容追加到文章内容
  form.content += '\n\n' + content
}
```

在模板中添加写作助手按钮和面板：

```vue
<!-- 在工具栏添加写作助手按钮 -->
<van-button type="primary" size="small" @click="showWritingPanel = true">
  AI 写作助手
</van-button>

<!-- 写作助手面板 -->
<AiWritingPanel
  v-model:show="showWritingPanel"
  :article-title="form.title"
  :article-content="form.content"
  @apply-content="handleApplyWritingContent"
/>
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/views/admin/ArticleEdit.vue
git commit -m "feat: integrate AI writing assistant in article edit"
```

---

## 完成检查

- [ ] 大纲生成功能正常（流式）
- [ ] 续写功能正常（流式）
- [ ] 润色功能正常（流式）
- [ ] 标题生成功能正常（流式）
- [ ] 写作助手面板交互正常
- [ ] 内容应用到编辑器正常

## 下一步

完成本阶段后，继续执行 `2026-04-28-content-intelligence-phase3-recommendation.md`
