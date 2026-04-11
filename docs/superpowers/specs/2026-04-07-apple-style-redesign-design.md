# Apple-Style Frontend Redesign Design Specification

**Date:** 2026-04-07
**Status:** Draft
**Scope:** Full frontend redesign (Portal + Admin)

## Overview

Transform the blog frontend from the current warm amber design to a modern Apple-inspired aesthetic with clean typography, glass morphism effects, refined animations, and a blue-tinted color palette.

## Design Principles

### Core Apple Design Values

1. **Clarity** - Clean visual hierarchy, generous whitespace, purposeful elements
2. **Deference** - Content-first approach, UI recedes to highlight content
3. **Depth** - Subtle layering, glass effects, meaningful shadows

### Key Visual Characteristics

- **Typography:** SF Pro-inspired font stack, precise weight hierarchy
- **Color:** Blue-dominant palette with subtle gradients, refined dark mode
- **Motion:** Spring-physics animations, smooth 60fps transitions
- **Materials:** Frosted glass, translucent overlays, soft shadows

---

## Technology Stack

### Changes

| Component | Current | New |
|-----------|---------|-----|
| UI Library | Element Plus | Vant 4 |
| Styling | SCSS + CSS Variables | SCSS + CSS Variables (Apple tokens) |
| Animations | CSS transitions | CSS + Vue transitions |

### New Dependencies

```json
{
  "vant": "^4.8.0",
  "@vant/auto-import-resolver": "^1.2.0"
}
```

### Removed Dependencies

```json
{
  "element-plus": "^2.4.4",
  "@element-plus/icons-vue": "^2.3.1"
}
```

---

## Design Tokens

### Color System

#### Light Mode

```css
:root {
  /* Backgrounds - Clean whites and subtle grays */
  --bg-primary: #FFFFFF;
  --bg-secondary: #F5F5F7;
  --bg-tertiary: #E8E8ED;
  --bg-card: #FFFFFF;
  --bg-elevated: rgba(255, 255, 255, 0.72);
  --bg-blur: rgba(255, 255, 255, 0.8);

  /* Text - Apple's gray scale */
  --text-primary: #1D1D1F;
  --text-secondary: #6E6E73;
  --text-tertiary: #86868B;
  --text-muted: #AEAEB2;
  --text-inverse: #FFFFFF;

  /* Accent - Apple Blue */
  --color-primary: #007AFF;
  --color-primary-hover: #0056CC;
  --color-primary-light: rgba(0, 122, 255, 0.1);
  --color-accent: #5856D6; /* Apple Purple for variety */

  /* System Colors */
  --color-success: #34C759;
  --color-warning: #FF9500;
  --color-error: #FF3B30;
  --color-info: #5AC8FA;

  /* Borders & Shadows */
  --border-color: rgba(0, 0, 0, 0.08);
  --border-light: rgba(0, 0, 0, 0.04);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.08);
  --shadow-lg: 0 12px 40px rgba(0, 0, 0, 0.12);
  --shadow-xl: 0 24px 80px rgba(0, 0, 0, 0.16);
  --shadow-glow: 0 0 40px rgba(0, 122, 255, 0.15);

  /* Glass Effect */
  --glass-bg: rgba(255, 255, 255, 0.72);
  --glass-border: rgba(255, 255, 255, 0.5);
  --glass-blur: 20px;
}
```

#### Dark Mode

```css
html.dark {
  /* Backgrounds - Apple's dark theme */
  --bg-primary: #000000;
  --bg-secondary: #1C1C1E;
  --bg-tertiary: #2C2C2E;
  --bg-card: #1C1C1E;
  --bg-elevated: rgba(28, 28, 30, 0.72);
  --bg-blur: rgba(28, 28, 30, 0.8);

  /* Text */
  --text-primary: #F5F5F7;
  --text-secondary: #A1A1A6;
  --text-tertiary: #6E6E73;
  --text-muted: #48484A;
  --text-inverse: #1D1D1F;

  /* Accent - Brighter for dark mode */
  --color-primary: #0A84FF;
  --color-primary-hover: #409CFF;
  --color-primary-light: rgba(10, 132, 255, 0.15);

  /* Borders & Shadows */
  --border-color: rgba(255, 255, 255, 0.1);
  --border-light: rgba(255, 255, 255, 0.05);
  --shadow-sm: 0 1px 3px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 12px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 12px 40px rgba(0, 0, 0, 0.5);
  --shadow-xl: 0 24px 80px rgba(0, 0, 0, 0.6);

  /* Glass Effect */
  --glass-bg: rgba(28, 28, 30, 0.72);
  --glass-border: rgba(255, 255, 255, 0.1);
}
```

### Typography

```css
:root {
  /* Font Stack - SF Pro inspired */
  --font-sans: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text',
    'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  --font-serif: 'New York', Georgia, 'Noto Serif SC', serif;
  --font-mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', monospace;

  /* Font Sizes - Apple's scale */
  --text-xs: 0.6875rem;    /* 11px */
  --text-sm: 0.8125rem;    /* 13px */
  --text-base: 0.9375rem;  /* 15px - Apple body text is slightly smaller */
  --text-lg: 1.0625rem;    /* 17px */
  --text-xl: 1.3125rem;    /* 21px */
  --text-2xl: 1.6875rem;   /* 27px */
  --text-3xl: 2.125rem;    /* 34px */
  --text-4xl: 2.75rem;     /* 44px */
  --text-5xl: 3.5rem;      /* 56px */
  --text-6xl: 4.5rem;      /* 72px */

  /* Line Heights */
  --leading-tight: 1.1;
  --leading-snug: 1.25;
  --leading-normal: 1.47;  /* Apple's body line height */
  --leading-relaxed: 1.65;

  /* Font Weights */
  --font-regular: 400;
  --font-medium: 500;
  --font-semibold: 600;
  --font-bold: 700;

  /* Letter Spacing */
  --tracking-tight: -0.02em;
  --tracking-normal: 0;
  --tracking-wide: 0.02em;
}
```

### Spacing & Sizing

```css
:root {
  /* Spacing - 4px base grid */
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

  /* Border Radius - Apple's continuous corners */
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --radius-xl: 20px;
  --radius-2xl: 28px;
  --radius-full: 9999px;

  /* Transitions - Apple's timing functions */
  --transition-fast: 200ms cubic-bezier(0.25, 0.1, 0.25, 1);
  --transition-base: 300ms cubic-bezier(0.25, 0.1, 0.25, 1);
  --transition-slow: 500ms cubic-bezier(0.25, 0.1, 0.25, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
  --ease-bounce: cubic-bezier(0.2, 0.8, 0.2, 1);
}
```

---

## Component Redesigns

### Phase 1: Portal Pages

#### 1.1 Header/Navigation

**Current:** Sticky header with amber branding, glassmorphism blur

**New Apple Style:**
- Ultra-subtle frosted glass effect with `backdrop-filter: blur(20px) saturate(180%)`
- Larger, bolder brand mark with SF Pro Display weight
- Pill-shaped navigation items with hover glow
- Search bar with animated expansion and inner shadow
- Refined user dropdown with spring animation
- Mobile: Sliding drawer with haptic-like feedback animations

#### 1.2 Hero Section

**Current:** Large serif title "随笔" with gradient text

**New Apple Style:**
- Dramatic typography: 72px bold title with tight letter-spacing
- Subtle gradient text: white to light gray (light mode), gray to white (dark mode)
- Animated background: subtle noise texture or gradient mesh
- Floating stats with glass morphism cards
- Call-to-action with pill button and arrow icon

#### 1.3 Article Cards

**Current:** Card with cover image, amber accent border on hover

**New Apple Style:**
- Clean white cards with soft shadows, no visible borders
- Cover image with subtle rounded corners and hover zoom
- Category pill with blue background
- Title in SF Pro Display semibold
- Hover: lift effect with enhanced shadow, no border color change
- Glass effect on featured article

#### 1.4 Sidebar Components

**Current:** Warm background sections with amber highlights

**New Apple Style:**
- Translucent cards with `backdrop-filter`
- Section icons in blue accent
- Hot articles with numbered rank in blue pills
- Tag cloud with hover glow effect
- Animated category counts

#### 1.5 Article Detail Page

**Current:** Serif title, warm color scheme

**New Apple Style:**
- Large, confident title in SF Pro Display
- Clean metadata row with SF Pro Text
- Glass effect floating action buttons (like, bookmark, share)
- Code blocks with rounded corners and syntax highlighting
- Comment section with modern input styling
- Smooth scroll-to-top button

#### 1.6 Footer

**Current:** Warm secondary background

**New Apple Style:**
- Subtle gradient or glass effect
- Simplified link groups with hover animations
- Smaller, refined copyright text

### Phase 2: Admin Panel

#### 2.1 Admin Layout

**Current:** Collapsible sidebar with amber accents

**New Apple Style:**
- Cleaner sidebar with frosted glass effect
- Navigation icons in blue accent
- Subtle hover states with scale transform
- Header with refined user dropdown
- Mobile: Bottom tab bar with haptic animations

#### 2.2 Dashboard

**Current:** Card-based stats with warm colors

**New Apple Style:**
- Large stat cards with subtle gradients
- Animated number counters
- Clean chart styling with blue accent
- Recent activity with timeline design

#### 2.3 Article Editor

**Current:** Element Plus form components

**New Apple Style:**
- Vant form components styled to match
- Clean textarea with focus glow
- Tag/category selectors with pill design
- Preview panel with split view
- Auto-save indicator

#### 2.4 Data Tables

**Current:** Element Plus tables with warm styling

**New Apple Style:**
- Clean tables with minimal borders
- Hover rows with subtle background
- Action buttons in pills
- Pagination with Apple-style buttons

---

## Animation Guidelines

### Micro-interactions

```css
/* Button Press */
.btn:active {
  transform: scale(0.97);
  transition: transform 100ms var(--ease-spring);
}

/* Card Hover */
.card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  transition: all 300ms var(--ease-bounce);
}

/* Focus Ring */
:focus-visible {
  outline: none;
  box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.3);
}

/* Page Transitions */
.page-enter-active {
  animation: fadeInUp 400ms var(--ease-bounce);
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
```

### Glass Morphism

```css
.glass {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(180%);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
}

.glass-card {
  background: var(--bg-elevated);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-md);
}
```

---

## Implementation Phases

### Phase 1: Foundation (Priority: Portal)

1. **Update Dependencies**
   - Install Vant 4 and configure auto-import
   - Remove Element Plus
   - Update Vite config for Vant

2. **Design System**
   - Replace CSS variables in `index.scss`
   - Create new typography system
   - Implement glass morphism utilities
   - Add animation keyframes

3. **Core Components**
   - Create `AppButton.vue` - Apple-style buttons
   - Create `AppCard.vue` - Glass effect cards
   - Create `AppInput.vue` - Clean input fields
   - Create `AppBadge.vue` - Pill badges

4. **Portal Layout**
   - Redesign `portal/Layout.vue` header
   - Implement new navigation
   - Update footer design
   - Add mobile bottom navigation

5. **Portal Pages**
   - Redesign `Home.vue` hero and cards
   - Update `ArticleDetail.vue`
   - Redesign `Archives.vue`
   - Update `Search.vue`
   - Style `Login.vue` / `Register.vue`

### Phase 2: Admin Panel

6. **Admin Layout**
   - Redesign `admin/Layout.vue` sidebar
   - Update header and navigation

7. **Admin Pages**
   - Redesign `Dashboard.vue`
   - Update `ArticleManage.vue`
   - Style `ArticleEdit.vue`
   - Update `CategoryManage.vue`, `TagManage.vue`, `CommentManage.vue`

8. **Vant Migration**
   - Replace Element Plus form components
   - Update table implementations
   - Style modals and dialogs

### Phase 3: Polish

9. **Animations**
   - Add page transitions
   - Implement scroll animations
   - Add loading states

10. **Dark Mode**
    - Verify all dark mode styles
    - Test contrast ratios
    - Smooth theme transitions

---

## File Structure

```
blog-web/src/
├── styles/
│   ├── index.scss          # Main styles (Apple tokens)
│   ├── _variables.scss     # Design tokens
│   ├── _animations.scss    # Keyframes and transitions
│   ├── _glass.scss         # Glass morphism utilities
│   └── _vant-overrides.scss # Vant theme customization
├── components/
│   ├── common/
│   │   ├── AppButton.vue
│   │   ├── AppCard.vue
│   │   ├── AppInput.vue
│   │   └── AppBadge.vue
│   └── portal/
│       ├── ArticleCard.vue
│       └── SidebarSection.vue
└── views/
    ├── portal/
    │   ├── Layout.vue
    │   ├── Home.vue
    │   ├── ArticleDetail.vue
    │   └── ...
    └── admin/
        ├── Layout.vue
        ├── Dashboard.vue
        └── ...
```

---

## Success Criteria

1. **Visual Quality**
   - Clean, minimal aesthetic matching Apple's design language
   - Consistent spacing and typography across all pages
   - Proper dark mode with smooth transitions

2. **Performance**
   - First Contentful Paint < 1.5s
   - No layout shifts during load
   - Smooth 60fps animations

3. **Accessibility**
   - WCAG 2.1 AA compliance
   - Proper focus indicators
   - Screen reader compatible

4. **User Experience**
   - Intuitive navigation
   - Clear visual hierarchy
   - Delightful micro-interactions

---

## Risks & Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Vant mobile-first may need desktop adjustments | Medium | Create desktop-specific overrides |
| Element Plus to Vant migration complexity | High | Phase migration, keep some EP temporarily if needed |
| Performance impact of blur effects | Low | Use CSS containment, test on low-end devices |
| Dark mode consistency | Medium | Create comprehensive token system, visual testing |

---

## References

- [Apple Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- [SF Pro Typography](https://developer.apple.com/fonts/)
- [Vant 4 Documentation](https://vant-ui.github.io/vant/#/en-US)
- [CSS backdrop-filter](https://developer.mozilla.org/en-US/docs/Web/CSS/backdrop-filter)
