# 阅读体验优化实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为博客文章详情页添加目录导航、代码块复制、图片放大、阅读时长四项阅读体验优化功能。

**Architecture:** 纯前端实现，在 ArticleDetail.vue 页面集成四个功能模块。使用 Vue 3 Composition API，通过 composables 封装复用逻辑，使用 v-viewer 插件实现图片放大功能。

**Tech Stack:** Vue 3, Vite, v-viewer, highlight.js, marked

---

## 文件结构

```
blog-web/src/
├── components/
│   └── TocNavigation.vue      # 目录导航组件（新建）
├── composables/
│   └── useToc.js              # TOC 提取和滚动监听逻辑（新建）
├── utils/
│   └── readingTime.js         # 阅读时长计算工具（新建）
├── views/portal/
│   └── ArticleDetail.vue      # 文章详情页（修改）
└── main.js                    # 注册 v-viewer 插件（修改）
```

---

## Task 1: 安装 v-viewer 依赖

**Files:**
- Modify: `blog-web/package.json`

- [ ] **Step 1: 安装 v-viewer 依赖**

Run: `cd d:/project/test1/blog-web && pnpm add v-viewer`

Expected: 成功安装 v-viewer 和 viewerjs 依赖

- [ ] **Step 2: 验证安装**

Run: `cd d:/project/test1/blog-web && cat package.json | grep v-viewer`

Expected: 输出包含 `"v-viewer": "^x.x.x"`

---

## Task 2: 创建阅读时长计算工具

**Files:**
- Create: `blog-web/src/utils/readingTime.js`

- [ ] **Step 1: 创建阅读时长计算工具函数**

Create file `blog-web/src/utils/readingTime.js`:

```javascript
/**
 * 计算文章阅读时长和字数
 * @param {string} text - 文章内容（纯文本或HTML）
 * @returns {{ words: number, minutes: number }}
 */
export function calculateReadingTime(text) {
  if (!text) return { words: 0, minutes: 1 }

  // 移除 HTML 标签，获取纯文本
  const plainText = text.replace(/<[^>]+>/g, '')

  // 统计中文字符数
  const chineseChars = plainText.match(/[\u4e00-\u9fa5]/g) || []
  const chineseCount = chineseChars.length

  // 统计英文单词数
  const englishWords = plainText.match(/[a-zA-Z]+/g) || []
  const englishCount = englishCount = englishWords.length

  // 总字数（中文 + 英文单词）
  const totalWords = chineseCount + englishCount

  // 阅读时长（按 300 字/分钟计算，最少 1 分钟）
  const minutes = Math.max(1, Math.ceil(totalWords / 300))

  return {
    words: totalWords,
    minutes
  }
}
```

- [ ] **Step 2: 验证文件创建**

Run: `cat d:/project/test1/blog-web/src/utils/readingTime.js`

Expected: 文件内容正确显示

- [ ] **Step 3: Commit**

```bash
cd d:/project/test1/blog-web && git add src/utils/readingTime.js && git commit -m "feat: add reading time calculation utility"
```

---

## Task 3: 创建 TOC composable

**Files:**
- Create: `blog-web/src/composables/useToc.js`

- [ ] **Step 1: 创建 composables 目录**

Run: `mkdir -p d:/project/test1/blog-web/src/composables`

Expected: 目录创建成功

- [ ] **Step 2: 创建 useToc composable**

Create file `blog-web/src/composables/useToc.js`:

```javascript
import { ref, onMounted, onUnmounted, nextTick } from 'vue'

/**
 * TOC 目录提取和滚动监听 composable
 * @param {import('vue').Ref<HTMLElement|null>} containerRef - 文章容器 DOM 引用
 * @returns {{ headings: import('vue').Ref<Array>, activeId: import('vue').Ref<string> }}
 */
export function useToc(containerRef) {
  const headings = ref([])
  const activeId = ref('')

  // 从文章容器中提取标题
  const extractHeadings = () => {
    if (!containerRef.value) return []

    const headingElements = containerRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
    const result = []

    headingElements.forEach((el, index) => {
      // 为每个标题生成唯一 id
      const id = el.id || `heading-${index}`
      if (!el.id) {
        el.id = id
      }

      result.push({
        id,
        text: el.textContent || '',
        level: parseInt(el.tagName.charAt(1)),
        top: el.offsetTop
      })
    })

    headings.value = result
    return result
  }

  // 滚动监听，更新当前高亮的标题
  const handleScroll = () => {
    if (!containerRef.value || headings.value.length === 0) return

    const scrollTop = window.scrollY
    const offset = 100 // 偏移量，用于提前高亮

    // 从上往下找到第一个已经滚过的标题
    let currentId = headings.value[0]?.id || ''

    for (const heading of headings.value) {
      if (heading.top - offset <= scrollTop) {
        currentId = heading.id
      }
    }

    activeId.value = currentId
  }

  // 滚动到指定标题
  const scrollToHeading = (id) => {
    const element = document.getElementById(id)
    if (element) {
      const offset = 80 // 顶部导航栏高度
      const top = element.offsetTop - offset
      window.scrollTo({
        top,
        behavior: 'smooth'
      })
      activeId.value = id
    }
  }

  onMounted(() => {
    nextTick(() => {
      extractHeadings()
      window.addEventListener('scroll', handleScroll)
    })
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  return {
    headings,
    activeId,
    extractHeadings,
    scrollToHeading
  }
}
```

- [ ] **Step 3: 验证文件创建**

Run: `cat d:/project/test1/blog-web/src/composables/useToc.js`

Expected: 文件内容正确显示

- [ ] **Step 4: Commit**

```bash
cd d:/project/test1/blog-web && git add src/composables/useToc.js && git commit -m "feat: add TOC extraction and scroll tracking composable"
```

---

## Task 4: 创建目录导航组件

**Files:**
- Create: `blog-web/src/components/TocNavigation.vue`

- [ ] **Step 1: 创建目录导航组件**

Create file `blog-web/src/components/TocNavigation.vue`:

```vue
<template>
  <div class="toc-navigation" :class="{ 'is-mobile': isMobile }">
    <!-- PC 端：右侧悬浮 -->
    <div v-if="!isMobile" class="toc-panel">
      <div class="toc-header">目录</div>
      <div class="toc-list">
        <a
          v-for="heading in headings"
          :key="heading.id"
          class="toc-item"
          :class="{ active: heading.id === activeId }"
          :style="{ paddingLeft: `${(heading.level - 1) * 12 + 12}px` }"
          @click="handleSelect(heading.id)"
        >
          {{ heading.text }}
        </a>
      </div>
    </div>

    <!-- 移动端：底部按钮 + 抽屉 -->
    <template v-else>
      <button class="toc-mobile-btn" @click="showDrawer = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
        </svg>
      </button>
      
      <div v-if="showDrawer" class="toc-drawer-overlay" @click="showDrawer = false">
        <div class="toc-drawer" @click.stop>
          <div class="toc-drawer-header">
            <span>目录</span>
            <button class="close-btn" @click="showDrawer = false">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="toc-list">
            <a
              v-for="heading in headings"
              :key="heading.id"
              class="toc-item"
              :class="{ active: heading.id === activeId }"
              :style="{ paddingLeft: `${(heading.level - 1) * 12 + 16}px` }"
              @click="handleSelectMobile(heading.id)"
            >
              {{ heading.text }}
            </a>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  headings: {
    type: Array,
    default: () => []
  },
  activeId: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['select'])

const isMobile = ref(false)
const showDrawer = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 1024
}

const handleSelect = (id) => {
  emit('select', id)
}

const handleSelectMobile = (id) => {
  emit('select', id)
  showDrawer.value = false
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})
</script>

<style lang="scss" scoped>
.toc-navigation {
  position: fixed;
  z-index: var(--z-sticky);
}

// PC 端面板
.toc-panel {
  width: 200px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.toc-header {
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
}

.toc-list {
  padding: var(--space-2) 0;
}

.toc-item {
  display: block;
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;

  &:hover {
    color: var(--color-primary);
    background: var(--bg-hover);
  }

  &.active {
    color: var(--color-primary);
    font-weight: var(--font-medium);
    background: var(--color-primary-light);
  }
}

// 移动端按钮
.toc-mobile-btn {
  position: fixed;
  right: 20px;
  bottom: 100px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 24px;
    height: 24px;
    color: var(--text-secondary);
  }

  &:hover {
    transform: scale(1.05);
    box-shadow: var(--shadow-lg);
  }

  &:active {
    transform: scale(0.95);
  }
}

// 移动端抽屉
.toc-drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: var(--z-modal-backdrop);
  animation: fadeIn 0.2s ease;
}

.toc-drawer {
  position: fixed;
  right: 0;
  top: 0;
  bottom: 0;
  width: 280px;
  max-width: 80vw;
  background: var(--bg-card);
  box-shadow: var(--shadow-xl);
  animation: slideInRight 0.3s ease;
  display: flex;
  flex-direction: column;
}

.toc-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  border-bottom: 1px solid var(--border-color);
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: background var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
    color: var(--text-secondary);
  }

  &:hover {
    background: var(--bg-hover);
  }
}

.toc-drawer .toc-list {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2) 0;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideInRight {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

// 隐藏移动端
@media (min-width: 1024px) {
  .toc-mobile-btn,
  .toc-drawer-overlay {
    display: none !important;
  }
}

// 隐藏 PC 端
@media (max-width: 1023px) {
  .toc-panel {
    display: none !important;
  }
}
</style>
```

- [ ] **Step 2: 验证文件创建**

Run: `cat d:/project/test1/blog-web/src/components/TocNavigation.vue`

Expected: 文件内容正确显示

- [ ] **Step 3: Commit**

```bash
cd d:/project/test1/blog-web && git add src/components/TocNavigation.vue && git commit -m "feat: add TOC navigation component with mobile drawer support"
```

---

## Task 5: 注册 v-viewer 插件

**Files:**
- Modify: `blog-web/src/main.js`

- [ ] **Step 1: 修改 main.js 注册 v-viewer**

修改 `blog-web/src/main.js`，添加 v-viewer 注册：

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Vant 4
import Vant from 'vant'
import 'vant/lib/index.css'

// Highlight.js for code blocks
import 'highlight.js/styles/github-dark.css'

// v-viewer for image preview
import VueViewer from 'v-viewer'
import 'viewerjs/dist/viewer.css'

// App
import App from './App.vue'
import router from './router'

// Styles (Apple design system)
import './styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Vant)
app.use(VueViewer)

app.mount('#app')
```

- [ ] **Step 2: 验证修改**

Run: `cat d:/project/test1/blog-web/src/main.js`

Expected: 文件包含 VueViewer 导入和注册

- [ ] **Step 3: Commit**

```bash
cd d:/project/test1/blog-web && git add src/main.js && git commit -m "feat: register v-viewer plugin for image preview"
```

---

## Task 6: 集成功能到 ArticleDetail.vue

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 修改 ArticleDetail.vue - 导入和脚本部分**

替换 `blog-web/src/views/portal/ArticleDetail.vue` 的 `<script setup>` 部分：

```vue
<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { showToast } from 'vant'
import { marked } from 'marked'
import hljs from 'highlight.js'
import dayjs from 'dayjs'
import { getArticle, likeArticle, favoriteArticle } from '@/api/article'
import { getComments, addComment } from '@/api/comment'
import { useUserStore } from '@/stores/user'
import { calculateReadingTime } from '@/utils/readingTime'
import { useToc } from '@/composables/useToc'
import TocNavigation from '@/components/TocNavigation.vue'

const route = useRoute()
const userStore = useUserStore()

const article = ref({})
const comments = ref([])
const commentContent = ref('')
const commentLoading = ref(false)
const articleContentRef = ref(null)

// TOC 目录
const { headings, activeId, extractHeadings, scrollToHeading } = useToc(articleContentRef)

// 阅读时长
const readingInfo = computed(() => {
  return calculateReadingTime(article.value.content || '')
})

const renderedContent = computed(() => {
  if (!article.value.content) return ''
  marked.setOptions({
    highlight: (code, lang) => {
      if (lang && hljs.getLanguage(lang)) {
        return hljs.highlight(code, { language: lang }).value
      }
      return hljs.highlightAuto(code).value
    }
  })
  return marked(article.value.content)
})

const formatDate = (date) => dayjs(date).format('YYYY-MM-DD HH:mm')

const fetchArticle = async () => {
  try {
    const res = await getArticle(route.params.id)
    article.value = res.data || {}
    document.title = `${article.value.title} - 随笔`
    
    // 文章加载后提取目录
    nextTick(() => {
      extractHeadings()
      addCopyButtons()
    })
  } catch (error) {
    console.error('获取文章失败', error)
  }
}

const fetchComments = async () => {
  try {
    const res = await getComments(route.params.id, { pageNum: 1, pageSize: 50 })
    comments.value = res.data?.records || []
  } catch (error) {
    console.error('获取评论失败', error)
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    return
  }
  try {
    const res = await likeArticle(article.value.id)
    article.value.isLiked = res.data
    article.value.likeCount += res.data ? 1 : -1
  } catch (error) {
    console.error('点赞失败', error)
  }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    return
  }
  try {
    const res = await favoriteArticle(article.value.id)
    article.value.isFavorited = res.data
    showToast(res.data ? '已添加到收藏' : '已取消收藏')
  } catch (error) {
    console.error('收藏失败', error)
  }
}

const handleComment = async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    return
  }
  if (!commentContent.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  commentLoading.value = true
  try {
    await addComment({
      articleId: article.value.id,
      content: commentContent.value
    })
    showToast({ type: 'success', message: '评论成功' })
    commentContent.value = ''
    fetchComments()
  } catch (error) {
    console.error('评论失败', error)
  } finally {
    commentLoading.value = false
  }
}

// 添加代码块复制按钮
const addCopyButtons = () => {
  if (!articleContentRef.value) return
  
  const codeBlocks = articleContentRef.value.querySelectorAll('pre')
  
  codeBlocks.forEach((pre) => {
    // 避免重复添加
    if (pre.querySelector('.copy-btn')) return
    
    // 设置 pre 为相对定位
    pre.style.position = 'relative'
    
    // 创建复制按钮
    const copyBtn = document.createElement('button')
    copyBtn.className = 'copy-btn'
    copyBtn.innerHTML = `
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
        <path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
      </svg>
    `
    copyBtn.title = '复制代码'
    
    copyBtn.addEventListener('click', async () => {
      const code = pre.querySelector('code')?.textContent || ''
      try {
        await navigator.clipboard.writeText(code)
        copyBtn.innerHTML = `
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
          </svg>
        `
        copyBtn.title = '已复制'
        showToast({ type: 'success', message: '代码已复制' })
        
        setTimeout(() => {
          copyBtn.innerHTML = `
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
            </svg>
          `
          copyBtn.title = '复制代码'
        }, 2000)
      } catch (err) {
        showToast('复制失败')
      }
    })
    
    pre.appendChild(copyBtn)
  })
}

onMounted(() => {
  fetchArticle()
  fetchComments()
})
</script>
```

- [ ] **Step 2: 修改 ArticleDetail.vue - 模板部分**

替换 `<template>` 部分：

```vue
<template>
  <div class="article-detail">
    <!-- 目录导航（PC端右侧悬浮） -->
    <TocNavigation
      v-if="headings.length > 0"
      class="toc-sidebar"
      :headings="headings"
      :active-id="activeId"
      @select="scrollToHeading"
    />

    <article class="article">
      <!-- 文章头部 -->
      <header class="article-header">
        <div class="header-top">
          <div class="article-meta">
            <span v-if="article.categoryName" class="meta-category">{{ article.categoryName }}</span>
            <span class="meta-date">{{ formatDate(article.publishTime) }}</span>
            <span class="meta-dot">·</span>
            <span class="meta-reading">约 {{ readingInfo.minutes }} 分钟</span>
            <span class="meta-dot">·</span>
            <span class="meta-words">{{ readingInfo.words }} 字</span>
          </div>
        </div>
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="header-bottom">
          <div class="author-info">
            <div class="author-avatar">{{ article.authorName?.charAt(0) || 'A' }}</div>
            <div class="author-details">
              <span class="author-name">{{ article.authorName || '匿名' }}</span>
              <span class="author-role">作者</span>
            </div>
          </div>
          <div v-if="article.tags?.length" class="article-tags">
            <span v-for="tag in article.tags" :key="tag.id" class="tag">{{ tag.name }}</span>
          </div>
        </div>
      </header>

      <!-- 封面图 -->
      <div v-if="article.coverImage" class="article-cover">
        <img :src="article.coverImage" :alt="article.title" loading="lazy" />
      </div>

      <!-- 文章内容（使用 v-viewer 指令实现图片点击放大） -->
      <div 
        ref="articleContentRef" 
        class="article-content markdown-body" 
        v-html="renderedContent"
        v-viewer
      ></div>

      <!-- 文章底部操作 -->
      <footer class="article-footer">
        <div class="footer-actions">
          <button class="action-btn" :class="{ active: article.isLiked }" @click="handleLike">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
            </svg>
            <span>{{ article.isLiked ? '已赞' : '点赞' }}</span>
            <span v-if="article.likeCount" class="count">{{ article.likeCount }}</span>
          </button>
          <button class="action-btn" :class="{ active: article.isFavorited }" @click="handleFavorite">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z" />
            </svg>
            <span>{{ article.isFavorited ? '已收藏' : '收藏' }}</span>
          </button>
        </div>
      </footer>
    </article>

    <!-- 评论区 -->
    <section class="comments">
      <div class="comments-header">
        <h3 class="comments-title">
          评论
          <span class="comments-count">{{ article.commentCount || 0 }}</span>
        </h3>
      </div>

      <div class="comment-form">
        <div class="comment-avatar">{{ userStore.userInfo?.nickname?.charAt(0) || '?' }}</div>
        <div class="comment-input-wrapper">
          <textarea
            v-model="commentContent"
            :rows="3"
            placeholder="分享你的想法..."
            class="comment-textarea"
          ></textarea>
          <div class="comment-actions">
            <button
              class="submit-btn"
              @click="handleComment"
              :disabled="commentLoading || !commentContent.trim()"
            >
              <span v-if="commentLoading" class="loading-spinner"></span>
              {{ commentLoading ? '发送中...' : '发表评论' }}
            </button>
          </div>
        </div>
      </div>

      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <div class="comment-avatar">{{ comment.nickname?.charAt(0) || '?' }}</div>
          <div class="comment-body">
            <div class="comment-header">
              <span class="comment-author">{{ comment.nickname }}</span>
              <span class="comment-time">{{ formatDate(comment.createTime) }}</span>
            </div>
            <div class="comment-text">{{ comment.content }}</div>
          </div>
        </div>
        <div v-if="comments.length === 0" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.625 12a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H8.25m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0H12m4.125 0a.375.375 0 11-.75 0 .375.375 0 01.75 0zm0 0h-.375M21 12c0 4.556-4.03 8.25-9 8.25a9.764 9.764 0 01-2.555-.337A5.972 5.972 0 015.41 20.97a5.969 5.969 0 01-.474-.065 4.48 4.48 0 00.978-2.025c.09-.457-.133-.901-.467-1.226C3.93 16.178 3 14.189 3 12c0-4.556 4.03-8.25 9-8.25s9 3.694 9 8.25z" />
            </svg>
          </div>
          <p class="empty-text">暂无评论，来抢沙发吧~</p>
        </div>
      </div>
    </section>
  </div>
</template>
```

- [ ] **Step 3: 修改 ArticleDetail.vue - 样式部分**

在 `<style>` 部分末尾添加新样式：

```scss
// ========================================
// TOC Sidebar Position
// ========================================
.toc-sidebar {
  right: 40px;
  top: 120px;
}

// ========================================
// Code Copy Button
// ========================================
.copy-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition-fast);

  svg {
    color: var(--text-secondary);
  }

  &:hover {
    background: var(--color-primary);
    svg {
      color: white;
    }
  }
}

.markdown-body pre:hover .copy-btn {
  opacity: 1;
}

// 响应式调整
@media (max-width: 1400px) {
  .toc-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .toc-sidebar {
    display: block;
  }
}
```

- [ ] **Step 4: 验证完整修改**

Run: `cat d:/project/test1/blog-web/src/views/portal/ArticleDetail.vue`

Expected: 文件包含所有新功能代码

- [ ] **Step 5: Commit**

```bash
cd d:/project/test1/blog-web && git add src/views/portal/ArticleDetail.vue && git commit -m "feat: integrate TOC, code copy, image viewer, and reading time into article detail page"
```

---

## Task 7: 验证功能

- [ ] **Step 1: 启动开发服务器**

Run: `cd d:/project/test1/blog-web && pnpm dev`

Expected: 开发服务器成功启动

- [ ] **Step 2: 手动验证**

打开浏览器访问文章详情页，验证：
1. 目录导航在右侧正确显示
2. 点击目录项可以跳转
3. 滚动时目录高亮正确切换
4. 代码块悬停显示复制按钮
5. 点击复制按钮成功复制
6. 点击图片可以放大查看
7. 文章标题下显示阅读时长和字数
8. 移动端目录显示为底部按钮

---

## Self-Review Checklist

- [x] Spec coverage: 所有四个功能（TOC、代码复制、图片放大、阅读时长）都有对应实现任务
- [x] Placeholder scan: 无 TBD、TODO 等占位符
- [x] Type consistency: headings 数组结构在 useToc.js 和 TocNavigation.vue 中一致
