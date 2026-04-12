# 阅读体验优化设计文档

## 概述

为博客系统添加四项阅读体验优化功能，全部采用纯前端实现，无需后端改动。

| 功能 | 说明 |
|------|------|
| 目录导航 TOC | 文章右侧悬浮目录，支持点击跳转和滚动高亮 |
| 代码块一键复制 | 每个代码块右上角添加复制按钮 |
| 图片点击放大 | 点击图片全屏查看，支持缩放和多图切换 |
| 阅读时长估算 | 文章标题下显示阅读时长和字数统计 |

## 技术方案

**实现方式**：纯前端实现（Vue 3），不涉及后端改动

**原因**：
- 四个功能都是展示层增强，不涉及核心业务逻辑
- 纯前端实现开发效率高，可快速上线
- 不增加服务器负担

## 文件结构

```
blog-web/src/
├── components/
│   ├── TocNavigation.vue      # 目录导航组件（新建）
│   ├── ImageViewer.vue        # 图片放大查看器（新建）
│   └── CodeBlock.vue          # 代码块复制按钮组件（新建）
├── views/portal/
│   └── ArticleDetail.vue      # 文章详情页（修改）
├── composables/
│   └── useToc.js              # TOC 提取逻辑（新建）
└── utils/
    └── readingTime.js         # 阅读时长计算工具（新建）
```

## 数据流

```
Markdown 内容 → 渲染为 HTML →
  ├── 提取标题 → 生成 TOC 目录
  ├── 检测代码块 → 添加复制按钮
  ├── 检测图片 → 绑定点击放大事件
  └── 计算字数 → 显示阅读时长
```

---

## 功能一：目录导航 TOC

### 功能描述

文章详情页右侧悬浮目录导航，帮助读者快速跳转章节。

### 功能细节

1. **目录生成**
   - 解析文章 HTML 中的 h1-h6 标签
   - 生成树形目录结构
   - 记录每个标题的 id 和位置

2. **显示位置**
   - PC 端：右侧悬浮，宽度约 200px
   - 移动端：底部按钮，点击展开目录抽屉

3. **交互功能**
   - 点击标题：平滑滚动到对应位置
   - 滚动监听：自动高亮当前阅读的章节
   - 层级缩进：根据 h1-h6 显示不同层级

### 视觉设计

```
┌─────────────────────────────┐
│ 目录                         │
├─────────────────────────────┤
│ ▸ 引言                       │
│   ▸ 背景介绍                 │
│ ▸ 核心概念                   │  ← 当前高亮（加粗/高亮色）
│   ▸ 定义                     │
│ ▸ 实现方案                   │
│ ▸ 总结                       │
└─────────────────────────────┘
```

### 组件接口

```vue
<TocNavigation
  :headings="headings"        // 标题列表
  :active-id="activeId"       // 当前高亮的标题 id
  @select="scrollToHeading"   // 点击标题事件
/>
```

### 标题数据结构

```typescript
interface Heading {
  id: string;          // 标题 id（用于锚点）
  text: string;        // 标题文本
  level: number;       // 标题级别 1-6
  top: number;         // 距离页面顶部的位置
}
```

---

## 功能二：代码块一键复制

### 功能描述

每个代码块右上角添加复制按钮，方便读者复制代码。

### 功能细节

1. **按钮添加**
   - Markdown 渲染完成后，遍历所有 `<pre><code>` 元素
   - 在 `<pre>` 元素内插入复制按钮
   - 按钮定位在代码块右上角

2. **复制功能**
   - 使用 `navigator.clipboard.writeText()` API
   - 复制成功显示 "已复制" 提示
   - 1.5 秒后提示恢复为复制图标

3. **样式处理**
   - 复制按钮使用绝对定位
   - 不影响代码块的原始布局
   - 支持暗色/亮色主题

### 视觉设计

```
┌─────────────────────────────────────────┐
│                              [📋 复制]  │
│ function hello() {                      │
│   console.log('Hello World');           │
│ }                                       │
└─────────────────────────────────────────┘
```

### 交互状态

| 状态 | 显示 |
|------|------|
| 默认 | 复制图标 |
| Hover | 背景变深 |
| 点击后 | "✓ 已复制"，1.5 秒后恢复 |

### 实现方式

- 使用 `nextTick` 在 Markdown 渲染后处理代码块
- 或使用自定义指令 `v-code-copy`

---

## 功能三：图片点击放大

### 功能描述

点击文章中的图片，全屏放大查看。

### 功能细节

1. **图片绑定**
   - 文章渲染后，给所有 `<img>` 标签添加点击事件
   - 鼠标悬停显示放大图标提示

2. **放大查看**
   - 全屏显示图片
   - 支持鼠标滚轮缩放
   - 支持拖拽移动

3. **多图支持**
   - 支持左右切换浏览多图
   - 显示当前图片序号（如 1/5）

4. **关闭方式**
   - 点击空白区域关闭
   - 按 ESC 键关闭
   - 点击关闭按钮

### 技术选型

使用 `v-viewer` 插件（基于 Viewer.js）

**优点**：
- 轻量级，功能完善
- 支持 Vue 3
- 支持缩放、旋转、拖拽
- 支持多图浏览

### 安装

```bash
pnpm add v-viewer @types/viewerjs
```

### 使用方式

```vue
import { directive as viewerDirective } from 'v-viewer'

// 在文章容器上使用指令
<div class="article-content" v-viewer>
  <!-- Markdown 渲染的 HTML -->
</div>
```

---

## 功能四：阅读时长估算

### 功能描述

在文章标题下方显示阅读时长和字数统计。

### 功能细节

1. **计算公式**
   ```
   阅读时长 = Math.ceil(总字数 / 300) 分钟
   ```

2. **字数统计**
   - 中文：统计字符数
   - 英文：统计单词数
   - 混合内容：分别计算后相加

3. **显示格式**
   - 阅读时长："约 5 分钟阅读"
   - 字数统计："共 1500 字"

### 显示位置

```
┌─────────────────────────────────────────────┐
│  深入理解 Vue 3 响应式原理                    │
│                                             │
│  作者：张三 · 2024-01-15 · 约 8 分钟 · 2400 字  │
├─────────────────────────────────────────────┤
```

### 工具函数

```javascript
// utils/readingTime.js
export function calculateReadingTime(text) {
  // 移除 HTML 标签
  const plainText = text.replace(/<[^>]+>/g, '')

  // 统计中文字符
  const chineseChars = plainText.match(/[\u4e00-\u9fa5]/g) || []
  const chineseCount = chineseChars.length

  // 统计英文单词
  const englishWords = plainText.match(/[a-zA-Z]+/g) || []
  const englishCount = englishWords.length

  // 总字数（中文 + 英文单词）
  const totalWords = chineseCount + englishCount

  // 阅读时长（按 300 字/分钟计算）
  const minutes = Math.ceil(totalWords / 300)

  return {
    words: totalWords,
    minutes: minutes < 1 ? 1 : minutes
  }
}
```

---

## 修改文件清单

### 新建文件

| 文件 | 说明 |
|------|------|
| `src/components/TocNavigation.vue` | 目录导航组件 |
| `src/components/ImageViewer.vue` | 图片放大组件（如需自定义） |
| `src/composables/useToc.js` | TOC 提取和滚动监听逻辑 |
| `src/utils/readingTime.js` | 阅读时长计算工具 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `src/views/portal/ArticleDetail.vue` | 集成四个功能组件 |
| `src/main.js` | 注册 v-viewer 插件（如使用） |

---

## 兼容性考虑

### 响应式设计

- **PC 端**（≥1024px）：目录右侧悬浮显示
- **平板/移动端**（<1024px）：目录收起为按钮，点击展开抽屉

### 主题适配

- 所有组件支持暗色/亮色主题
- 使用 CSS 变量，继承现有主题系统

### 浏览器兼容

- `navigator.clipboard` 需要 HTTPS 或 localhost
- 不支持的浏览器降级为 `document.execCommand('copy')`

---

## 测试要点

1. **目录导航**
   - 各级标题正确显示
   - 点击跳转平滑滚动
   - 滚动时高亮正确切换
   - 移动端抽屉正常工作

2. **代码复制**
   - 各语言代码块都能复制
   - 复制内容完整正确
   - 复制状态提示正常

3. **图片放大**
   - 单图点击放大
   - 多图左右切换
   - 缩放和拖拽正常
   - ESC 和点击空白关闭

4. **阅读时长**
   - 中文统计正确
   - 英文统计正确
   - 混合内容统计正确
