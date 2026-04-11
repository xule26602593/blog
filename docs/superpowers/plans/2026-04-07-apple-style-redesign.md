# Apple-Style Frontend Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the blog frontend from warm amber design to modern Apple-inspired aesthetic with blue-tinted color palette, glass morphism effects, and refined animations.

**Architecture:** Phase 1 focuses on design tokens and core components. Phase 2 transforms Portal pages. Phase 3 handles Admin panel. Each phase produces working, testable UI. Element Plus is replaced with Vant 4.

**Tech Stack:** Vue 3.4, Vite 5, Vant 4, SCSS, CSS Variables

---

## File Structure

### New Files to Create

```
blog-web/src/
├── styles/
│   ├── _variables.scss        # Apple design tokens (colors, typography, spacing)
│   ├── _animations.scss       # Spring-physics keyframes, transitions
│   ├── _glass.scss            # Glass morphism utilities
│   └── _vant-overrides.scss   # Vant component theme customization
├── components/
│   └── common/
│       ├── AppButton.vue      # Apple-style button with spring press effect
│       ├── AppCard.vue        # Glass effect card with hover lift
│       ├── AppInput.vue       # Clean input with focus glow
│       └── AppBadge.vue       # Pill badge component
└── main-vant.js               # Vant initialization (will replace main.js imports)
```

### Files to Modify

```
blog-web/
├── package.json               # Add Vant, remove Element Plus
├── vite.config.js             # Update resolvers for Vant
├── src/
│   ├── main.js                # Replace Element Plus with Vant
│   ├── styles/index.scss      # Import new Apple tokens
│   ├── views/portal/
│   │   ├── Layout.vue         # Redesign header, footer, navigation
│   │   ├── Home.vue           # Hero, article cards, sidebar
│   │   ├── ArticleDetail.vue  # Article page styling
│   │   ├── Login.vue          # Auth forms
│   │   ├── Register.vue       # Auth forms
│   │   ├── Archives.vue       # Archive page
│   │   ├── Search.vue         # Search results
│   │   └── UserCenter.vue     # User profile
│   └── views/admin/
│       ├── Layout.vue         # Admin sidebar, header
│       ├── Dashboard.vue      # Admin dashboard
│       ├── ArticleManage.vue  # Article table
│       ├── ArticleEdit.vue    # Article editor
│       ├── CategoryManage.vue # Category management
│       ├── TagManage.vue      # Tag management
│       └── CommentManage.vue  # Comment moderation
```

---

## Phase 1: Foundation (Design Tokens & Core Components)

### Task 1: Update Dependencies

**Files:**
- Modify: `blog-web/package.json`
- Modify: `blog-web/vite.config.js`

- [ ] **Step 1: Add Vant 4 dependencies to package.json**

Open `blog-web/package.json` and update the dependencies section:

```json
{
  "dependencies": {
    "@vant/auto-import-resolver": "^1.2.0",
    "axios": "^1.6.2",
    "dayjs": "^1.11.10",
    "highlight.js": "^11.9.0",
    "marked": "^11.1.0",
    "md-editor-v3": "^6.4.1",
    "pinia": "^2.1.7",
    "vant": "^4.8.0",
    "vue": "^3.4.0",
    "vue-router": "^4.2.5"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^4.5.2",
    "sass": "^1.69.5",
    "unplugin-auto-import": "^0.17.3",
    "unplugin-vue-components": "^0.26.0",
    "vite": "^5.0.10"
  }
}
```

Remove `element-plus` and `@element-plus/icons-vue` from dependencies.

- [ ] **Step 2: Update Vite config for Vant auto-import**

Replace `blog-web/vite.config.js`:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import path from 'path'

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [VantResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts'
    }),
    Components({
      resolvers: [VantResolver()],
      dts: 'src/components.d.ts'
    })
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false
  }
})
```

- [ ] **Step 3: Install dependencies**

Run:
```bash
cd /home/demo/test1/blog-web && pnpm install
```

Expected: Dependencies installed successfully, Vant 4 added, Element Plus removed.

- [ ] **Step 4: Commit dependency changes**

```bash
git add blog-web/package.json blog-web/pnpm-lock.yaml blog-web/vite.config.js
git commit -m "$(cat <<'EOF'
chore: Replace Element Plus with Vant 4

- Add vant@4.8.0 and @vant/auto-import-resolver@1.2.0
- Remove element-plus and @element-plus/icons-vue
- Update Vite config to use VantResolver

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 2: Create Apple Design Tokens

**Files:**
- Create: `blog-web/src/styles/_variables.scss`

- [ ] **Step 1: Create the variables file with Apple design tokens**

Create `blog-web/src/styles/_variables.scss`:

```scss
// ========================================
// Apple-Style Design Tokens
// ========================================
// Design Philosophy:
// 1. Clarity - Clean visual hierarchy, generous whitespace
// 2. Deference - Content-first approach, UI recedes
// 3. Depth - Subtle layering, glass effects, meaningful shadows

// ========================================
// Color System - Light Mode
// ========================================
:root {
  // Backgrounds - Clean whites and subtle grays
  --bg-primary: #FFFFFF;
  --bg-secondary: #F5F5F7;
  --bg-tertiary: #E8E8ED;
  --bg-card: #FFFFFF;
  --bg-elevated: rgba(255, 255, 255, 0.72);
  --bg-blur: rgba(255, 255, 255, 0.8);
  --bg-hover: rgba(0, 0, 0, 0.04);
  --bg-active: rgba(0, 0, 0, 0.08);

  // Text - Apple's gray scale
  --text-primary: #1D1D1F;
  --text-secondary: #6E6E73;
  --text-tertiary: #86868B;
  --text-muted: #AEAEB2;
  --text-inverse: #FFFFFF;

  // Accent - Apple Blue
  --color-primary: #007AFF;
  --color-primary-hover: #0056CC;
  --color-primary-light: rgba(0, 122, 255, 0.1);
  --color-accent: #5856D6;

  // System Colors
  --color-success: #34C759;
  --color-warning: #FF9500;
  --color-error: #FF3B30;
  --color-info: #5AC8FA;

  // Borders & Shadows
  --border-color: rgba(0, 0, 0, 0.08);
  --border-light: rgba(0, 0, 0, 0.04);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 12px 40px rgba(0, 0, 0, 0.12);
  --shadow-xl: 0 24px 80px rgba(0, 0, 0, 0.16);
  --shadow-glow: 0 0 40px rgba(0, 122, 255, 0.15);

  // Glass Effect
  --glass-bg: rgba(255, 255, 255, 0.72);
  --glass-border: rgba(255, 255, 255, 0.5);
  --glass-blur: 20px;

  // Spacing - 4px base grid
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-8: 32px;
  --space-10: 40px;
  --space-12: 48px;
  --space-16: 64px;
  --space-20: 80px;
  --space-24: 96px;

  // Border Radius - Apple's continuous corners
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --radius-xl: 20px;
  --radius-2xl: 28px;
  --radius-full: 9999px;

  // Typography - SF Pro inspired scale
  --font-sans: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text',
    'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-serif: 'New York', Georgia, 'Noto Serif SC', serif;
  --font-mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', monospace;

  // Font Sizes - Apple's scale
  --text-xs: 0.6875rem;    // 11px
  --text-sm: 0.8125rem;    // 13px
  --text-base: 0.9375rem;  // 15px
  --text-lg: 1.0625rem;    // 17px
  --text-xl: 1.3125rem;    // 21px
  --text-2xl: 1.6875rem;   // 27px
  --text-3xl: 2.125rem;    // 34px
  --text-4xl: 2.75rem;     // 44px
  --text-5xl: 3.5rem;      // 56px
  --text-6xl: 4.5rem;      // 72px

  // Line Heights
  --leading-tight: 1.1;
  --leading-snug: 1.25;
  --leading-normal: 1.47;
  --leading-relaxed: 1.65;

  // Font Weights
  --font-regular: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;

  // Letter Spacing
  --tracking-tight: -0.02em;
  --tracking-normal: 0;
  --tracking-wide: 0.02em;

  // Transitions - Apple's timing functions
  --transition-fast: 200ms cubic-bezier(0.25, 0.1, 0.25, 1);
  --transition-base: 300ms cubic-bezier(0.25, 0.1, 0.25, 1);
  --transition-slow: 500ms cubic-bezier(0.25, 0.1, 0.25, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
  --ease-bounce: cubic-bezier(0.2, 0.8, 0.2, 1);

  // Z-Index
  --z-dropdown: 100;
  --z-sticky: 200;
  --z-fixed: 300;
  --z-modal-backdrop: 400;
  --z-modal: 500;
  --z-popover: 600;
  --z-tooltip: 700;
}

// ========================================
// Color System - Dark Mode
// ========================================
html.dark {
  --bg-primary: #000000;
  --bg-secondary: #1C1C1E;
  --bg-tertiary: #2C2C2E;
  --bg-card: #1C1C1E;
  --bg-elevated: rgba(28, 28, 30, 0.72);
  --bg-blur: rgba(28, 28, 30, 0.8);
  --bg-hover: rgba(255, 255, 255, 0.06);
  --bg-active: rgba(255, 255, 255, 0.1);

  --text-primary: #F5F5F7;
  --text-secondary: #A1A1A6;
  --text-tertiary: #6E6E73;
  --text-muted: #48484A;
  --text-inverse: #1D1D1F;

  --color-primary: #0A84FF;
  --color-primary-hover: #409CFF;
  --color-primary-light: rgba(10, 132, 255, 0.15);

  --border-color: rgba(255, 255, 255, 0.1);
  --border-light: rgba(255, 255, 255, 0.05);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 12px 40px rgba(0, 0, 0, 0.5);
  --shadow-xl: 0 24px 80px rgba(0, 0, 0, 0.6);

  --glass-bg: rgba(28, 28, 30, 0.72);
  --glass-border: rgba(255, 255, 255, 0.1);
}
```

- [ ] **Step 2: Commit design tokens**

```bash
git add blog-web/src/styles/_variables.scss
git commit -m "$(cat <<'EOF'
feat: Add Apple-style design tokens

- Blue-tinted color palette for light/dark modes
- SF Pro-inspired typography scale
- Apple's continuous corner radius system
- Spring-physics timing functions

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 3: Create Animation Utilities

**Files:**
- Create: `blog-web/src/styles/_animations.scss`

- [ ] **Step 1: Create animations file with spring-physics keyframes**

Create `blog-web/src/styles/_animations.scss`:

```scss
// ========================================
// Apple-Style Animations
// ========================================

// ========================================
// Keyframes
// ========================================
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.96);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideInLeft {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ========================================
// Animation Classes
// ========================================
.animate-fade-in {
  animation: fadeIn var(--transition-base) var(--ease-bounce);
}

.animate-fade-in-up {
  animation: fadeInUp var(--transition-slow) var(--ease-bounce);
}

.animate-fade-in-down {
  animation: fadeInDown var(--transition-slow) var(--ease-bounce);
}

.animate-scale-in {
  animation: scaleIn var(--transition-base) var(--ease-bounce);
}

.animate-slide-in-right {
  animation: slideInRight var(--transition-base) var(--ease-bounce);
}

.animate-slide-in-left {
  animation: slideInLeft var(--transition-base) var(--ease-bounce);
}

// ========================================
// Micro-interactions
// ========================================

// Button Press Effect
.btn-press {
  transition: transform 100ms var(--ease-spring);

  &:active {
    transform: scale(0.97);
  }
}

// Card Hover Lift
.card-lift {
  transition: all 300ms var(--ease-bounce);

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
}

// Focus Ring
.focus-ring {
  &:focus-visible {
    outline: none;
    box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.3);
  }
}

// Loading Spinner
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

// Skeleton Loading
.skeleton {
  background: linear-gradient(
    90deg,
    var(--bg-secondary) 25%,
    var(--bg-tertiary) 50%,
    var(--bg-secondary) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

// ========================================
// Page Transitions
// ========================================
.page-enter-active {
  animation: fadeInUp 400ms var(--ease-bounce);
}

.page-leave-active {
  animation: fadeIn 200ms var(--transition-fast) reverse;
}

// ========================================
// Reduced Motion
// ========================================
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
```

- [ ] **Step 2: Commit animations**

```bash
git add blog-web/src/styles/_animations.scss
git commit -m "$(cat <<'EOF'
feat: Add Apple-style animation utilities

- Spring-physics keyframes (fadeInUp, scaleIn, etc.)
- Micro-interaction classes (btn-press, card-lift)
- Focus ring and loading states
- Reduced motion support

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 4: Create Glass Morphism Utilities

**Files:**
- Create: `blog-web/src/styles/_glass.scss`

- [ ] **Step 1: Create glass morphism utilities**

Create `blog-web/src/styles/_glass.scss`:

```scss
// ========================================
// Glass Morphism Utilities
// ========================================

// Base Glass Effect
.glass {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
}

// Glass Card - Elevated appearance
.glass-card {
  background: var(--bg-elevated);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
}

// Glass Header - For sticky headers
.glass-header {
  background: var(--bg-blur);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
}

// Glass Panel - For modals and overlays
.glass-panel {
  background: var(--glass-bg);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
}

// Glass Button
.glass-btn {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);

  &:hover {
    background: rgba(255, 255, 255, 0.25);
    border-color: rgba(255, 255, 255, 0.3);
  }

  html.dark & {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.1);

    &:hover {
      background: rgba(255, 255, 255, 0.12);
      border-color: rgba(255, 255, 255, 0.15);
    }
  }
}

// Glass Input
.glass-input {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);

  &:focus {
    background: rgba(255, 255, 255, 0.12);
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.15);
  }

  html.dark & {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.08);

    &:focus {
      background: rgba(255, 255, 255, 0.08);
      box-shadow: 0 0 0 4px rgba(10, 132, 255, 0.2);
    }
  }
}
```

- [ ] **Step 2: Commit glass utilities**

```bash
git add blog-web/src/styles/_glass.scss
git commit -m "$(cat <<'EOF'
feat: Add glass morphism utilities

- Base glass effect with blur and saturation
- Glass variants: card, header, panel, button, input
- Light/dark mode support

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 5: Create Vant Overrides

**Files:**
- Create: `blog-web/src/styles/_vant-overrides.scss`

- [ ] **Step 1: Create Vant theme overrides**

Create `blog-web/src/styles/_vant-overrides.scss`:

```scss
// ========================================
// Vant Component Overrides - Apple Style
// ========================================

// ========================================
// Button
// ========================================
.van-button {
  font-family: var(--font-sans);
  font-weight: var(--font-medium);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);

  &:active {
    transform: scale(0.97);
  }
}

.van-button--primary {
  background: var(--color-primary);
  border-color: var(--color-primary);

  &:hover {
    background: var(--color-primary-hover);
    border-color: var(--color-primary-hover);
  }
}

.van-button--default {
  background: var(--bg-card);
  border-color: var(--border-color);
  color: var(--text-primary);

  &:hover {
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}

// ========================================
// Input
// ========================================
.van-field {
  font-family: var(--font-sans);

  .van-field__control {
    font-size: var(--text-base);
    color: var(--text-primary);

    &::placeholder {
      color: var(--text-muted);
    }
  }
}

.van-cell {
  font-family: var(--font-sans);
  background: var(--bg-card);
  color: var(--text-primary);

  &::after {
    border-color: var(--border-color);
  }
}

.van-cell-group--inset {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

// ========================================
// Card
// ========================================
.van-card {
  font-family: var(--font-sans);
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);

  &:hover {
    box-shadow: var(--shadow-md);
  }
}

// ========================================
// Dialog
// ========================================
.van-dialog {
  font-family: var(--font-sans);
  border-radius: var(--radius-2xl);
  overflow: hidden;

  .van-dialog__header {
    font-weight: var(--font-semibold);
    color: var(--text-primary);
  }

  .van-dialog__message {
    color: var(--text-secondary);
  }
}

// ========================================
// Popup
// ========================================
.van-popup {
  font-family: var(--font-sans);

  &--round {
    border-radius: var(--radius-2xl) var(--radius-2xl) 0 0;
  }
}

// ========================================
// Tag
// ========================================
.van-tag {
  font-family: var(--font-sans);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);

  &--primary {
    background: var(--color-primary-light);
    color: var(--color-primary);
  }
}

// ========================================
// NavBar
// ========================================
.van-nav-bar {
  font-family: var(--font-sans);
  background: var(--bg-blur);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);

  .van-nav-bar__title {
    font-weight: var(--font-semibold);
    color: var(--text-primary);
  }

  .van-nav-bar__text,
  .van-nav-bar__arrow {
    color: var(--color-primary);
  }
}

// ========================================
// Tab
// ========================================
.van-tabs {
  font-family: var(--font-sans);

  .van-tabs__nav {
    background: transparent;
  }

  .van-tab {
    font-weight: var(--font-medium);
    color: var(--text-secondary);
    transition: color var(--transition-fast);

    &--active {
      color: var(--color-primary);
    }
  }

  .van-tabs__line {
    background: var(--color-primary);
    border-radius: var(--radius-full);
    height: 3px;
  }
}

// ========================================
// List
// ========================================
.van-list {
  font-family: var(--font-sans);
}

.van-cell {
  &:active {
    background: var(--bg-hover);
  }
}

// ========================================
// Pull Refresh
// ========================================
.van-pull-refresh {
  .van-pull-refresh__head {
    color: var(--text-secondary);
  }
}

// ========================================
// Loading
// ========================================
.van-loading {
  .van-loading__spinner {
    color: var(--color-primary);
  }

  .van-loading__text {
    color: var(--text-secondary);
    font-family: var(--font-sans);
  }
}

// ========================================
// Toast
// ========================================
.van-toast {
  font-family: var(--font-sans);
  border-radius: var(--radius-xl);
  background: var(--bg-elevated);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

// ========================================
// Dark Mode Adjustments
// ========================================
html.dark {
  .van-cell {
    background: var(--bg-card);
    color: var(--text-primary);

    &::after {
      border-color: var(--border-color);
    }
  }

  .van-card {
    background: var(--bg-card);
    border-color: var(--border-color);
  }

  .van-dialog {
    background: var(--bg-card);
  }

  .van-popup {
    background: var(--bg-card);
  }
}
```

- [ ] **Step 2: Commit Vant overrides**

```bash
git add blog-web/src/styles/_vant-overrides.scss
git commit -m "$(cat <<'EOF'
feat: Add Vant component theme overrides

- Apple-style button, input, card styling
- Dialog, popup, nav-bar customization
- Tag, tab, list components
- Dark mode support

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 6: Update Main Styles Entry

**Files:**
- Modify: `blog-web/src/styles/index.scss`

- [ ] **Step 1: Replace index.scss with Apple design system**

This is a complete rewrite of `blog-web/src/styles/index.scss`. The file imports all partials and contains base resets.

```scss
// ========================================
// Apple-Style Design System
// ========================================
// Design Philosophy:
// 1. Clarity - Clean visual hierarchy, generous whitespace
// 2. Deference - Content-first approach, UI recedes
// 3. Depth - Subtle layering, glass effects, meaningful shadows

// Import partials
@import 'variables';
@import 'animations';
@import 'glass';
@import 'vant-overrides';

// ========================================
// Base Reset
// ========================================
*,
*::before,
*::after {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  height: 100%;
  scroll-behavior: smooth;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-rendering: optimizeLegibility;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
}

body {
  height: 100%;
  font-family: var(--font-sans);
  font-size: var(--text-base);
  line-height: var(--leading-normal);
  color: var(--text-primary);
  background: var(--bg-primary);
  transition: background-color var(--transition-slow), color var(--transition-slow);
  padding-left: env(safe-area-inset-left);
  padding-right: env(safe-area-inset-right);
  overscroll-behavior: none;
}

#app {
  height: 100%;
}

a {
  text-decoration: none;
  color: inherit;
  transition: color var(--transition-fast);
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

button {
  font-family: inherit;
  cursor: pointer;
  touch-action: manipulation;
  -webkit-tap-highlight-color: transparent;
}

input, textarea, select {
  font-family: inherit;
  font-size: 16px;
  -webkit-appearance: none;
  appearance: none;
  border-radius: 0;
}

// ========================================
// Typography
// ========================================
h1, h2, h3, h4, h5, h6 {
  font-family: var(--font-sans);
  font-weight: var(--font-semibold);
  line-height: var(--leading-tight);
  color: var(--text-primary);
  letter-spacing: var(--tracking-tight);
}

h1 { font-size: var(--text-5xl); }
h2 { font-size: var(--text-4xl); }
h3 { font-size: var(--text-3xl); }
h4 { font-size: var(--text-2xl); }
h5 { font-size: var(--text-xl); }
h6 { font-size: var(--text-lg); }

// ========================================
// Markdown Styles
// ========================================
.markdown-body {
  line-height: var(--leading-relaxed);
  font-size: var(--text-base);
  color: var(--text-primary);

  h1, h2, h3, h4, h5, h6 {
    margin-top: var(--space-12);
    margin-bottom: var(--space-4);
    font-weight: var(--font-semibold);
    line-height: var(--leading-tight);
    color: var(--text-primary);
    letter-spacing: var(--tracking-tight);
  }

  h1 {
    font-size: var(--text-4xl);
    padding-bottom: var(--space-3);
    border-bottom: 2px solid var(--color-primary);
  }

  h2 {
    font-size: var(--text-3xl);
    padding-bottom: var(--space-2);
    border-bottom: 1px solid var(--border-color);
  }

  h3 { font-size: var(--text-2xl); }
  h4 { font-size: var(--text-xl); }

  p {
    margin-bottom: var(--space-5);
    line-height: var(--leading-relaxed);
  }

  a {
    color: var(--color-primary);
    border-bottom: 1px solid transparent;
    transition: border-color var(--transition-fast);

    &:hover {
      border-bottom-color: var(--color-primary);
    }
  }

  strong {
    color: var(--text-primary);
    font-weight: var(--font-semibold);
  }

  em {
    color: var(--text-secondary);
    font-style: italic;
  }

  code {
    padding: 0.2em 0.45em;
    margin: 0;
    font-size: 0.9em;
    font-family: var(--font-mono);
    background: var(--bg-secondary);
    border-radius: var(--radius-sm);
    color: var(--color-primary);
  }

  pre {
    padding: var(--space-5);
    overflow-x: auto;
    font-size: var(--text-sm);
    line-height: var(--leading-relaxed);
    background: var(--bg-secondary);
    border-radius: var(--radius-lg);
    margin-bottom: var(--space-5);
    border: 1px solid var(--border-color);

    code {
      background: none;
      padding: 0;
      color: var(--text-primary);
    }
  }

  blockquote {
    padding: var(--space-4) var(--space-6);
    margin: var(--space-5) 0;
    color: var(--text-secondary);
    border-left: 3px solid var(--color-primary);
    background: var(--bg-secondary);
    border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
    font-style: italic;
  }

  ul, ol {
    padding-left: var(--space-6);
    margin-bottom: var(--space-5);
  }

  li {
    margin-bottom: var(--space-2);
  }

  img {
    max-width: 100%;
    height: auto;
    border-radius: var(--radius-lg);
    margin: var(--space-6) 0;
  }

  table {
    border-spacing: 0;
    border-collapse: collapse;
    margin-bottom: var(--space-5);
    width: 100%;

    th, td {
      padding: var(--space-3) var(--space-4);
      border: 1px solid var(--border-color);
    }

    th {
      font-weight: var(--font-medium);
      background: var(--bg-secondary);
      color: var(--text-primary);
    }

    tr:nth-child(2n) {
      background: var(--bg-secondary);
    }
  }

  hr {
    border: none;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--border-color), transparent);
    margin: var(--space-10) 0;
  }
}

// ========================================
// Code Highlighting
// ========================================
pre code.hljs {
  display: block;
  overflow-x: auto;
  padding: 1em;
  background: transparent;
}

// ========================================
// Utility Classes
// ========================================
.card {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);

  &:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  font-family: var(--font-sans);
  border-radius: var(--radius-lg);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all var(--transition-fast);

  &:active {
    transform: scale(0.97);
  }
}

.btn-primary {
  background: var(--color-primary);
  color: white;

  &:hover {
    background: var(--color-primary-hover);
    box-shadow: var(--shadow-md);
  }
}

.btn-secondary {
  background: var(--bg-card);
  color: var(--text-primary);
  border-color: var(--border-color);

  &:hover {
    background: var(--bg-secondary);
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}

.tag {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  background: var(--color-primary-light);
  color: var(--color-primary);
  transition: all var(--transition-fast);

  &:hover {
    background: rgba(0, 122, 255, 0.15);
  }
}

// Text utilities
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.text-link {
  color: var(--color-primary);
  border-bottom: 1px solid transparent;
  transition: border-color var(--transition-fast);

  &:hover {
    border-bottom-color: var(--color-primary);
  }
}

.text-accent {
  color: var(--color-primary);
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-color), transparent);
  margin: var(--space-8) 0;
}

// ========================================
// Scrollbar
// ========================================
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: transparent;
}

::-webkit-scrollbar-thumb {
  background: var(--text-muted);
  border-radius: var(--radius-full);

  &:hover {
    background: var(--text-tertiary);
  }
}

* {
  scrollbar-width: thin;
  scrollbar-color: var(--text-muted) transparent;
}

// ========================================
// Selection
// ========================================
::selection {
  background: var(--color-primary);
  color: white;
}

// ========================================
// Focus Styles
// ========================================
:focus-visible {
  outline: 2px solid var(--color-primary);
  outline-offset: 2px;
}

// ========================================
// Mobile Utilities
// ========================================
@media (hover: none) and (pointer: coarse) {
  .card, .btn, .tag, button, a {
    -webkit-tap-highlight-color: transparent;

    &:active {
      transform: scale(0.98);
      opacity: 0.9;
    }
  }

  .card:hover {
    transform: none;
    box-shadow: var(--shadow-sm);
  }
}

.safe-area-top {
  padding-top: env(safe-area-inset-top);
}

.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}

.hide-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }
}

@media (max-width: 640px) {
  .form-inline {
    flex-direction: column;
    gap: var(--space-3);

    > * {
      width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .hide-mobile {
    display: none !important;
  }
}

@media (min-width: 769px) {
  .show-mobile-only {
    display: none !important;
  }
}

.no-select {
  -webkit-user-select: none;
  user-select: none;
}

.img-placeholder {
  background: var(--bg-secondary);
  animation: shimmer 2s infinite;
}
```

- [ ] **Step 2: Commit main styles**

```bash
git add blog-web/src/styles/index.scss
git commit -m "$(cat <<'EOF'
feat: Replace main styles with Apple design system

- Import all partials (variables, animations, glass, vant-overrides)
- Base resets and typography
- Markdown styling
- Utility classes
- Mobile optimizations

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 7: Update Main Entry Point

**Files:**
- Modify: `blog-web/src/main.js`

- [ ] **Step 1: Replace Element Plus with Vant**

Replace `blog-web/src/main.js`:

```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'

// Vant 4
import Vant from 'vant'
import 'vant/lib/index.css'

// Highlight.js for code blocks
import 'highlight.js/styles/github-dark.css'

// App
import App from './App.vue'
import router from './router'

// Styles (Apple design system)
import './styles/index.scss'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Vant)

app.mount('#app')
```

- [ ] **Step 2: Verify the app starts without errors**

Run:
```bash
cd /home/demo/test1/blog-web && pnpm dev
```

Expected: Dev server starts on port 3000, no import errors for Element Plus.

- [ ] **Step 3: Commit main entry changes**

```bash
git add blog-web/src/main.js
git commit -m "$(cat <<'EOF'
feat: Replace Element Plus with Vant 4

- Remove Element Plus imports
- Add Vant 4 with default CSS
- Import Apple design system styles

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 8: Create AppButton Component

**Files:**
- Create: `blog-web/src/components/common/AppButton.vue`

- [ ] **Step 1: Create the button component**

Create `blog-web/src/components/common/AppButton.vue`:

```vue
<template>
  <button
    :class="['app-button', `app-button--${variant}`, `app-button--${size}`, { 'app-button--loading': loading }]"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="app-button__spinner" />
    <slot v-else />
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'secondary', 'ghost', 'text'].includes(v)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (v) => ['small', 'medium', 'large'].includes(v)
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])
</script>

<style lang="scss" scoped>
.app-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-family: var(--font-sans);
  font-weight: var(--font-medium);
  border-radius: var(--radius-lg);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;

  &:active:not(:disabled) {
    transform: scale(0.97);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &:focus-visible {
    outline: 2px solid var(--color-primary);
    outline-offset: 2px;
  }
}

// Variants
.app-button--primary {
  background: var(--color-primary);
  color: white;

  &:hover:not(:disabled) {
    background: var(--color-primary-hover);
    box-shadow: var(--shadow-md);
  }
}

.app-button--secondary {
  background: var(--bg-card);
  color: var(--text-primary);
  border-color: var(--border-color);

  &:hover:not(:disabled) {
    background: var(--bg-secondary);
    border-color: var(--color-primary);
    color: var(--color-primary);
  }
}

.app-button--ghost {
  background: transparent;
  color: var(--color-primary);

  &:hover:not(:disabled) {
    background: var(--color-primary-light);
  }
}

.app-button--text {
  background: transparent;
  color: var(--text-secondary);
  padding-left: 0;
  padding-right: 0;

  &:hover:not(:disabled) {
    color: var(--color-primary);
  }
}

// Sizes
.app-button--small {
  height: 32px;
  padding: 0 var(--space-3);
  font-size: var(--text-sm);
}

.app-button--medium {
  height: 40px;
  padding: 0 var(--space-5);
  font-size: var(--text-base);
}

.app-button--large {
  height: 48px;
  padding: 0 var(--space-6);
  font-size: var(--text-lg);
}

// Loading
.app-button--loading {
  pointer-events: none;
}

.app-button__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
```

- [ ] **Step 2: Commit AppButton component**

```bash
git add blog-web/src/components/common/AppButton.vue
git commit -m "$(cat <<'EOF'
feat: Add AppButton component with Apple styling

- Primary, secondary, ghost, text variants
- Small, medium, large sizes
- Loading state with spinner
- Spring press effect

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 9: Create AppCard Component

**Files:**
- Create: `blog-web/src/components/common/AppCard.vue`

- [ ] **Step 1: Create the card component**

Create `blog-web/src/components/common/AppCard.vue`:

```vue
<template>
  <div :class="['app-card', { 'app-card--interactive': interactive, 'app-card--glass': glass }]">
    <div v-if="$slots.header" class="app-card__header">
      <slot name="header" />
    </div>
    <div class="app-card__body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="app-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  interactive: {
    type: Boolean,
    default: false
  },
  glass: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.app-card {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-base);
  overflow: hidden;
}

.app-card--interactive {
  cursor: pointer;

  &:hover {
    box-shadow: var(--shadow-lg);
    transform: translateY(-4px);
    border-color: var(--color-primary);
  }

  &:active {
    transform: translateY(-2px);
  }
}

.app-card--glass {
  background: var(--bg-elevated);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

.app-card__header {
  padding: var(--space-5) var(--space-6);
  border-bottom: 1px solid var(--border-color);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.app-card__body {
  padding: var(--space-6);
}

.app-card__footer {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid var(--border-color);
  background: var(--bg-secondary);
}

@media (max-width: 640px) {
  .app-card__header {
    padding: var(--space-4) var(--space-5);
  }

  .app-card__body {
    padding: var(--space-5);
  }

  .app-card__footer {
    padding: var(--space-3) var(--space-5);
  }
}
</style>
```

- [ ] **Step 2: Commit AppCard component**

```bash
git add blog-web/src/components/common/AppCard.vue
git commit -m "$(cat <<'EOF'
feat: Add AppCard component with glass variant

- Interactive mode with hover lift
- Glass morphism variant
- Header/body/footer slots

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 10: Create AppBadge Component

**Files:**
- Create: `blog-web/src/components/common/AppBadge.vue`

- [ ] **Step 1: Create the badge component**

Create `blog-web/src/components/common/AppBadge.vue`:

```vue
<template>
  <span :class="['app-badge', `app-badge--${variant}`, `app-badge--${size}`]">
    <slot />
  </span>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (v) => ['primary', 'success', 'warning', 'error', 'neutral'].includes(v)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (v) => ['small', 'medium', 'large'].includes(v)
  }
})
</script>

<style lang="scss" scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  font-family: var(--font-sans);
  font-weight: var(--font-medium);
  border-radius: var(--radius-full);
  white-space: nowrap;
}

// Variants
.app-badge--primary {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.app-badge--success {
  background: rgba(52, 199, 89, 0.12);
  color: var(--color-success);
}

.app-badge--warning {
  background: rgba(255, 149, 0, 0.12);
  color: var(--color-warning);
}

.app-badge--error {
  background: rgba(255, 59, 48, 0.12);
  color: var(--color-error);
}

.app-badge--neutral {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

// Sizes
.app-badge--small {
  height: 20px;
  padding: 0 var(--space-2);
  font-size: 11px;
}

.app-badge--medium {
  height: 24px;
  padding: 0 var(--space-3);
  font-size: var(--text-xs);
}

.app-badge--large {
  height: 28px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
}
</style>
```

- [ ] **Step 2: Commit AppBadge component**

```bash
git add blog-web/src/components/common/AppBadge.vue
git commit -m "$(cat <<'EOF'
feat: Add AppBadge component with Apple pill styling

- Primary, success, warning, error, neutral variants
- Small, medium, large sizes
- Pill shape with proper contrast

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Phase 2: Portal Pages

### Task 11: Redesign Portal Layout Header

**Files:**
- Modify: `blog-web/src/views/portal/Layout.vue` (lines 1-120 header section)

- [ ] **Step 1: Update header styling to Apple style**

Locate the `.header` section in `blog-web/src/views/portal/Layout.vue` and update the styles:

Find in `<style lang="scss" scoped>` around line 360:

```scss
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  background: var(--bg-overlay);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-color);
}
```

Replace with:

```scss
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  background: var(--bg-blur);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
}
```

- [ ] **Step 2: Update brand styling**

Find `.brand-mark` around line 396 and replace:

```scss
.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  font-family: var(--font-sans);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-lg);
}
```

With:

```scss
.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  font-family: var(--font-sans);
  font-size: var(--text-lg);
  font-weight: var(--font-bold);
  color: white;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-md);
}
```

- [ ] **Step 3: Update nav-link active state**

Find `.nav-link.active` around line 452 and replace:

```scss
&.active {
  color: var(--color-primary);
  background: var(--color-primary-light);

  .nav-icon {
    opacity: 1;
  }
}
```

With:

```scss
&.active {
  color: var(--color-primary);
  background: var(--color-primary-light);

  .nav-icon {
    opacity: 1;
  }
}
```

- [ ] **Step 4: Commit header changes**

```bash
git add blog-web/src/views/portal/Layout.vue
git commit -m "$(cat <<'EOF'
feat: Redesign portal header with Apple style

- Enhanced glass morphism effect with saturation
- Gradient brand mark
- Updated active nav state with blue accent

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 12: Redesign Home Page Hero

**Files:**
- Modify: `blog-web/src/views/portal/Home.vue` (hero section)

- [ ] **Step 1: Update hero title to Apple typography**

In `blog-web/src/views/portal/Home.vue`, find `.hero-title` around line 295 and replace:

```scss
.hero-title {
  font-family: var(--font-serif);
  font-size: var(--text-7xl);
  font-weight: var(--font-bold);
  letter-spacing: -0.02em;
  margin-bottom: var(--space-4);
  line-height: 1;
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--color-primary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
```

With:

```scss
.hero-title {
  font-family: var(--font-sans);
  font-size: var(--text-6xl);
  font-weight: var(--font-bold);
  letter-spacing: var(--tracking-tight);
  margin-bottom: var(--space-4);
  line-height: var(--leading-tight);
  color: var(--text-primary);
}
```

- [ ] **Step 2: Update hero stats to glass effect**

Find `.hero-stats` around line 319 and replace:

```scss
.hero-stats {
  display: inline-flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-4) var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-sm);
}
```

With:

```scss
.hero-stats {
  display: inline-flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-4) var(--space-8);
  background: var(--bg-elevated);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-md);
}
```

- [ ] **Step 3: Commit hero changes**

```bash
git add blog-web/src/views/portal/Home.vue
git commit -m "$(cat <<'EOF'
feat: Redesign home hero with Apple typography

- SF Pro Display font for hero title
- Glass morphism stats container
- Tight letter-spacing for headline

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 13: Redesign Article Cards

**Files:**
- Modify: `blog-web/src/views/portal/Home.vue` (article cards section)

- [ ] **Step 1: Update article card hover state**

In `blog-web/src/views/portal/Home.vue`, find `.article-item` around line 526 and update:

Replace:
```scss
&:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);

  .article-title {
    color: var(--color-primary);
  }

  .article-cover img {
    transform: scale(1.05);
  }
}
```

With:
```scss
&:hover {
  box-shadow: var(--shadow-lg);
  transform: translateY(-4px);
  border-color: var(--border-color);

  .article-title {
    color: var(--color-primary);
  }

  .article-cover img {
    transform: scale(1.05);
  }
}
```

- [ ] **Step 2: Update article category badge**

Find `.article-category` around line 596 and replace:

```scss
.article-category {
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
}
```

With:

```scss
.article-category {
  padding: var(--space-1) var(--space-2);
  font-size: var(--text-xs);
  font-weight: var(--font-medium);
  color: var(--color-primary);
  background: var(--color-primary-light);
  border-radius: var(--radius-sm);
}
```

- [ ] **Step 3: Update article tags**

Find `.tag` around line 667 and replace:

```scss
.tag {
  padding: 2px var(--space-2);
  font-size: var(--text-xs);
  background: rgba(180, 83, 9, 0.08);
  border-radius: var(--radius-full);
  color: var(--color-primary);
  font-weight: var(--font-medium);

  &.more {
    background: var(--bg-tertiary);
    color: var(--text-muted);
  }
}
```

With:

```scss
.tag {
  padding: 2px var(--space-2);
  font-size: var(--text-xs);
  background: var(--color-primary-light);
  border-radius: var(--radius-full);
  color: var(--color-primary);
  font-weight: var(--font-medium);

  &.more {
    background: var(--bg-tertiary);
    color: var(--text-muted);
  }
}
```

- [ ] **Step 4: Commit article card changes**

```bash
git add blog-web/src/views/portal/Home.vue
git commit -m "$(cat <<'EOF'
feat: Redesign article cards with Apple hover effect

- Enhanced hover lift with larger shadow
- Updated category and tag badges to blue accent
- Clean border transition

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 14: Redesign Sidebar Components

**Files:**
- Modify: `blog-web/src/views/portal/Home.vue` (sidebar section)

- [ ] **Step 1: Update hot articles section**

In `blog-web/src/views/portal/Home.vue`, find `.hot-section` around line 763 and replace:

```scss
.hot-section {
  background: var(--bg-card);
}
```

With:

```scss
.hot-section {
  background: var(--bg-card);
}
```

- [ ] **Step 2: Update hot rank badge**

Find `.hot-rank` around line 791 and replace:

```scss
.hot-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-muted);
  background: var(--bg-secondary);
  border-radius: var(--radius-sm);
  font-family: var(--font-serif);

  &.top {
    color: white;
    background: var(--color-primary);
  }
}
```

With:

```scss
.hot-rank {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  flex-shrink: 0;
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--text-muted);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  font-family: var(--font-sans);

  &.top {
    color: white;
    background: var(--color-primary);
  }
}
```

- [ ] **Step 3: Update tag button hover**

Find `.tag-btn` around line 864 and replace:

```scss
&:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}
```

With:

```scss
&:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-light);
}
```

- [ ] **Step 4: Commit sidebar changes**

```bash
git add blog-web/src/views/portal/Home.vue
git commit -m "$(cat <<'EOF'
feat: Redesign sidebar with Apple styling

- Clean card background
- Updated rank badges to blue accent
- Consistent tag hover states

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 15: Redesign Login Page

**Files:**
- Modify: `blog-web/src/views/portal/Login.vue`

- [ ] **Step 1: Read current Login.vue**

Run:
```bash
head -100 /home/demo/test1/blog-web/src/views/portal/Login.vue
```

Note the structure for styling updates.

- [ ] **Step 2: Update Login.vue styles to Apple design**

The key changes are:
1. Replace amber colors with blue accent
2. Add glass morphism to form container
3. Update input focus states
4. Style button with Apple blue

Replace the `<style lang="scss" scoped>` section in `blog-web/src/views/portal/Login.vue` with Apple-styled CSS:

```scss
<style lang="scss" scoped>
.login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background: var(--bg-secondary);
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: var(--space-10);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-10);
}

.login-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: var(--space-5);
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: white;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-xl);
}

.login-title {
  font-family: var(--font-sans);
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.login-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.form-input {
  height: 48px;
  padding: 0 var(--space-4);
  font-size: var(--text-base);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    background: var(--bg-card);
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.15);
  }
}

html.dark .form-input:focus {
  box-shadow: 0 0 0 4px rgba(10, 132, 255, 0.2);
}

.form-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-sm);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  cursor: pointer;

  input {
    width: 18px;
    height: 18px;
    accent-color: var(--color-primary);
  }
}

.forgot-link {
  color: var(--color-primary);
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.submit-btn {
  height: 48px;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--color-primary-hover);
    box-shadow: var(--shadow-md);
  }

  &:active {
    transform: scale(0.98);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.login-footer {
  margin-top: var(--space-8);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-color);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--text-secondary);

  a {
    color: var(--color-primary);
    font-weight: var(--font-medium);
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
```

- [ ] **Step 3: Commit login page changes**

```bash
git add blog-web/src/views/portal/Login.vue
git commit -m "$(cat <<'EOF'
feat: Redesign login page with Apple styling

- Glass morphism card container
- Blue accent colors
- Updated form inputs with focus glow
- Apple-style submit button

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Phase 3: Admin Panel

### Task 16: Redesign Admin Layout

**Files:**
- Modify: `blog-web/src/views/admin/Layout.vue`

- [ ] **Step 1: Update admin sidebar colors**

In `blog-web/src/views/admin/Layout.vue`, find `.brand-mark` around line 245 and replace:

```scss
.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-xl);
  flex-shrink: 0;

  svg {
    width: 24px;
    height: 24px;
    color: white;
  }
}
```

With:

```scss
.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-lg);
  flex-shrink: 0;

  svg {
    width: 24px;
    height: 24px;
    color: white;
  }
}
```

- [ ] **Step 2: Update nav item active state**

Find `.nav-item.active` around line 364 and replace:

```scss
&.active {
  color: var(--color-primary);
  background: var(--color-primary-light);

  .nav-icon {
    opacity: 1;
  }
}
```

With:

```scss
&.active {
  color: var(--color-primary);
  background: var(--color-primary-light);

  .nav-icon {
    opacity: 1;
  }
}
```

- [ ] **Step 3: Update header glass effect**

Find `.header` around line 422 and replace:

```scss
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  padding: var(--space-5) var(--space-6);
  background: var(--bg-overlay);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-color);
}
```

With:

```scss
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-sticky);
  padding: var(--space-5) var(--space-6);
  background: var(--bg-blur);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
}
```

- [ ] **Step 4: Update pending badge colors**

Find `.pending-badge` around line 462 and replace:

```scss
.pending-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary-light);
  border: 1px solid rgba(0, 122, 255, 0.2);
  border-radius: var(--radius-full);
}
```

With:

```scss
.pending-badge {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  background: var(--color-primary-light);
  border: 1px solid rgba(0, 122, 255, 0.2);
  border-radius: var(--radius-full);
}
```

- [ ] **Step 5: Commit admin layout changes**

```bash
git add blog-web/src/views/admin/Layout.vue
git commit -m "$(cat <<'EOF'
feat: Redesign admin layout with Apple styling

- Updated sidebar brand mark
- Blue accent for active nav items
- Enhanced header glass morphism
- Updated pending badge to blue

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 17: Replace Element Plus Components in Admin

**Files:**
- Modify: `blog-web/src/views/admin/Layout.vue` (dropdown)
- Modify: `blog-web/src/views/admin/Dashboard.vue`
- Modify: `blog-web/src/views/admin/ArticleManage.vue`

- [ ] **Step 1: Update admin Layout.vue dropdown**

Replace the `el-dropdown` in `blog-web/src/views/admin/Layout.vue` with native HTML dropdown or Vant component.

Find the dropdown section (around lines 74-100) and replace with:

```vue
<div class="user-menu">
  <button class="user-trigger" @click="showUserMenu = !showUserMenu">
    <span class="user-avatar">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</span>
    <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="chevron" :class="{ rotated: showUserMenu }">
      <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5" />
    </svg>
  </button>
  <Transition name="dropdown">
    <div v-if="showUserMenu" class="user-dropdown">
      <div class="dropdown-header">
        <span class="dropdown-avatar">{{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}</span>
        <div class="dropdown-user-info">
          <span class="dropdown-nickname">{{ userStore.userInfo?.nickname || '用户' }}</span>
          <span class="dropdown-email">{{ userStore.userInfo?.email || '未设置邮箱' }}</span>
        </div>
      </div>
      <div class="dropdown-divider"></div>
      <router-link to="/" class="dropdown-item" @click="showUserMenu = false">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25" />
        </svg>
        返回首页
      </router-link>
      <button class="dropdown-item logout" @click="handleLogout">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15m3 0l3-3m0 0l-3-3m3 3H9" />
        </svg>
        退出登录
      </button>
    </div>
  </Transition>
</div>
```

Add to script:
```javascript
const showUserMenu = ref(false)
```

Add dropdown transition styles:
```scss
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all var(--transition-fast);
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.chevron.rotated {
  transform: rotate(180deg);
}
```

- [ ] **Step 2: Update Dashboard.vue to use Vant components**

Open `blog-web/src/views/admin/Dashboard.vue` and:
1. Replace `el-card` with `van-cell-group` or custom card
2. Replace `el-statistic` with custom stat display
3. Update import statements

- [ ] **Step 3: Update ArticleManage.vue to use Vant components**

Open `blog-web/src/views/admin/ArticleManage.vue` and:
1. Replace `el-table` with `van-list` of cards
2. Replace `el-button` with custom `AppButton` or `van-button`
3. Replace `el-pagination` with `van-pagination`
4. Remove Element Plus imports

- [ ] **Step 4: Commit admin component migration**

```bash
git add blog-web/src/views/admin/
git commit -m "$(cat <<'EOF'
feat: Migrate admin components from Element Plus to Vant

- Replace dropdown with native implementation
- Update Dashboard stats display
- Update ArticleManage with Vant list components

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

### Task 18: Final Cleanup and Testing

**Files:**
- Modify: `blog-web/src/components.d.ts`
- Modify: `blog-web/src/auto-imports.d.ts`
- Various files: Remove remaining Element Plus references

- [ ] **Step 1: Search for remaining Element Plus references**

Run:
```bash
cd /home/demo/test1/blog-web && grep -r "el-\|ElementPlus\|element-plus" src/ --include="*.vue" --include="*.js" --include="*.ts"
```

Expected: No remaining references to Element Plus.

- [ ] **Step 2: Update auto-generated type files**

Run dev server to regenerate:
```bash
cd /home/demo/test1/blog-web && pnpm dev &
sleep 5
kill %1 2>/dev/null
```

- [ ] **Step 3: Run build to verify no errors**

Run:
```bash
cd /home/demo/test1/blog-web && pnpm build
```

Expected: Build completes successfully without errors.

- [ ] **Step 4: Manual testing checklist**

Test the following pages:
- [ ] Portal: Home page loads with Apple styling
- [ ] Portal: Header has glass morphism effect
- [ ] Portal: Article cards have correct hover states
- [ ] Portal: Dark mode toggle works correctly
- [ ] Portal: Login page displays correctly
- [ ] Admin: Sidebar navigation works
- [ ] Admin: Dashboard displays stats
- [ ] Admin: Article management table works

- [ ] **Step 5: Commit final cleanup**

```bash
git add -A
git commit -m "$(cat <<'EOF'
chore: Final cleanup for Apple-style redesign

- Remove all Element Plus references
- Update auto-generated type files
- Verify build succeeds

Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
EOF
)"
```

---

## Summary

### Files Created
- `blog-web/src/styles/_variables.scss` - Apple design tokens
- `blog-web/src/styles/_animations.scss` - Spring-physics animations
- `blog-web/src/styles/_glass.scss` - Glass morphism utilities
- `blog-web/src/styles/_vant-overrides.scss` - Vant theme customization
- `blog-web/src/components/common/AppButton.vue` - Apple-style button
- `blog-web/src/components/common/AppCard.vue` - Glass effect card
- `blog-web/src/components/common/AppBadge.vue` - Pill badge

### Files Modified
- `blog-web/package.json` - Vant 4 dependencies
- `blog-web/vite.config.js` - Vant resolver
- `blog-web/src/main.js` - Vant initialization
- `blog-web/src/styles/index.scss` - Complete rewrite
- `blog-web/src/views/portal/Layout.vue` - Header redesign
- `blog-web/src/views/portal/Home.vue` - Hero and cards
- `blog-web/src/views/portal/Login.vue` - Auth form styling
- `blog-web/src/views/admin/Layout.vue` - Admin sidebar
- `blog-web/src/views/admin/Dashboard.vue` - Dashboard components
- `blog-web/src/views/admin/ArticleManage.vue` - Article table

### Key Changes
1. **Color Palette**: Warm amber → Apple blue (#007AFF)
2. **Typography**: Serif + Sans → SF Pro Display/Text
3. **UI Library**: Element Plus → Vant 4
4. **Effects**: Subtle shadows → Glass morphism with blur/saturation
5. **Animations**: Linear → Spring-physics easing

### Risk Mitigation
- Phase migration allows rollback at each commit
- Keep Element Plus temporarily if Vant migration is complex
- Test on low-end devices for blur performance
