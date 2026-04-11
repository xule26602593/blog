# 博客系统设计系统

## 设计风格

**风格定位**: 简约文艺 / E-Ink Paper / 阅读优先

**核心原则**:
- 大量留白，呼吸感强
- 衬线字体为主，优雅文艺
- 高对比度，阅读舒适
- 深色模式完美适配
- 响应式设计，移动优先

---

## 配色方案

### 亮色模式

| 用途 | 颜色 | CSS变量 |
|------|------|---------|
| 主背景 | `#FDFBF7` (纸白) | `--bg-primary` |
| 卡片背景 | `#FFFFFF` | `--bg-card` |
| 次级背景 | `#F8F6F1` (米白) | `--bg-secondary` |
| 主文字 | `#1A1A1A` (墨黑) | `--text-primary` |
| 次级文字 | `#4A4A4A` (铅笔灰) | `--text-secondary` |
| 辅助文字 | `#6A6A6A` | `--text-muted` |
| 主色调 | `#8B7355` (棕褐) | `--color-primary` |
| 强调色 | `#B8A07E` (金棕) | `--color-accent` |
| 边框色 | `#E8E4DD` | `--border-color` |
| 阴影色 | `rgba(0,0,0,0.06)` | `--shadow-color` |

### 深色模式

| 用途 | 颜色 | CSS变量 |
|------|------|---------|
| 主背景 | `#0F0F0F` | `--bg-primary` |
| 卡片背景 | `#1A1A1A` | `--bg-card` |
| 次级背景 | `#141414` | `--bg-secondary` |
| 主文字 | `#E8E4DD` (纸白) | `--text-primary` |
| 次级文字 | `#A0A0A0` | `--text-secondary` |
| 辅助文字 | `#6A6A6A` | `--text-muted` |
| 主色调 | `#C4A87C` (浅金) | `--color-primary` |
| 强调色 | `#D4B896` | `--color-accent` |
| 边框色 | `#2A2A2A` | `--border-color` |
| 阴影色 | `rgba(0,0,0,0.4)` | `--shadow-color` |

---

## 字体系统

### 主字体

```css
/* 中文衬线 - 标题 */
font-family: 'Noto Serif SC', 'Source Han Serif SC', serif;

/* 西文衬线 - 标题 */
font-family: 'Cormorant Garamond', 'Playfair Display', serif;

/* 正文 - 无衬线 */
font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
```

### 字体大小

| 用途 | 大小 | 行高 |
|------|------|------|
| 标题 H1 | `2.5rem` (40px) | 1.3 |
| 标题 H2 | `1.875rem` (30px) | 1.35 |
| 标题 H3 | `1.5rem` (24px) | 1.4 |
| 标题 H4 | `1.25rem` (20px) | 1.45 |
| 正文 | `1rem` (16px) | 1.8 |
| 小字 | `0.875rem` (14px) | 1.6 |
| 辅助 | `0.75rem` (12px) | 1.5 |

### Google Fonts 导入

```css
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;500;600;700&family=Noto+Sans+SC:wght@300;400;500;600&family=Noto+Serif+SC:wght@400;500;600;700&display=swap');
```

---

## 间距系统

```css
--space-xs: 4px;
--space-sm: 8px;
--space-md: 16px;
--space-lg: 24px;
--space-xl: 32px;
--space-2xl: 48px;
--space-3xl: 64px;
```

---

## 卡片设计

### 基础卡片

```css
.card {
  background: var(--bg-card);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  box-shadow: 0 2px 12px var(--shadow-color);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.card:hover {
  box-shadow: 0 8px 24px var(--shadow-color);
  transform: translateY(-4px);
}
```

### 卡片内边距

- 紧凑: `padding: 16px`
- 标准: `padding: 24px`
- 宽松: `padding: 32px`

---

## 过渡动画

```css
/* 标准过渡 */
--transition-fast: 150ms ease;
--transition-base: 250ms ease;
--transition-slow: 350ms ease;

/* 缓动函数 */
--ease-out: cubic-bezier(0.16, 1, 0.3, 1);
--ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);
```

---

## 响应式断点

```css
/* 移动端 */
@media (max-width: 480px) { }

/* 平板竖屏 */
@media (max-width: 768px) { }

/* 平板横屏 */
@media (max-width: 1024px) { }

/* 桌面端 */
@media (min-width: 1025px) { }

/* 大屏 */
@media (min-width: 1440px) { }
```

---

## 深色模式切换

```css
/* 亮色模式 (默认) */
:root {
  --bg-primary: #FDFBF7;
  /* ... */
}

/* 深色模式 */
html.dark {
  --bg-primary: #0F0F0F;
  /* ... */
}
```

---

## 组件规范

### 导航栏

- 高度: 64px
- 背景: 半透明毛玻璃效果
- 定位: 浮动式，距离顶部有间距

### 侧边栏

- 宽度: 300px (桌面端)
- 移动端: 全宽，堆叠在内容下方
- 卡片间距: 20px

### 文章卡片

- 图片比例: 16:9 或 3:2
- 标题字重: 600
- 摘要行数: 2行截断
- 标签: 圆角徽章

### 按钮

- 主按钮: 背景 var(--color-primary)
- 边框按钮: 透明背景 + 边框
- 圆角: 8px (小) / 12px (中) / 24px (药丸)

---

## 避免的反模式

1. ❌ 不要使用 emoji 作为图标
2. ❌ 不要在悬停时使用 scale 变换导致布局偏移
3. ❌ 深色模式下文字对比度不足
4. ❌ 卡片背景在亮色模式过于透明
5. ❌ 使用 scale 导致内容溢出
6. ❌ 过渡动画时间过长 (>500ms)

---

## Pre-Delivery Checklist

- [ ] 所有可点击元素有 `cursor: pointer`
- [ ] 悬停状态有明显视觉反馈
- [ ] 过渡时间在 150-300ms 范围内
- [ ] 深色模式文字对比度 >= 4.5:1
- [ ] 响应式测试: 375px, 768px, 1024px, 1440px
- [ ] 图片有 alt 属性
- [ ] 支持 `prefers-reduced-motion`
