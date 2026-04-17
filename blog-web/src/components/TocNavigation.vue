<template>
  <!-- PC View: Fixed panel on right side -->
  <div class="toc-navigation" :class="{ 'toc-navigation--mobile': isMobile }">
    <!-- PC Panel -->
    <div v-if="!isMobile" class="toc-panel">
      <div class="toc-panel__header">
        <span class="toc-panel__title">目录</span>
        <span class="toc-panel__count">{{ headings.length }}</span>
      </div>
      <nav class="toc-panel__nav">
        <ul class="toc-list">
          <li
            v-for="heading in headings"
            :key="heading.id"
            class="toc-item"
            :class="[
              `toc-item--level-${heading.level}`,
              { 'toc-item--active': heading.id === activeId }
            ]"
          >
            <a
              class="toc-link"
              :class="{ 'toc-link--active': heading.id === activeId }"
              @click="handleSelect(heading.id)"
            >
              <span class="toc-link__indicator" v-if="heading.id === activeId"></span>
              <span class="toc-link__text">{{ heading.text }}</span>
            </a>
          </li>
        </ul>
        <div v-if="headings.length === 0" class="toc-empty">
          <span class="toc-empty__text">暂无目录</span>
        </div>
      </nav>
    </div>

    <!-- Mobile View: Floating button + Drawer -->
    <template v-else>
      <!-- Floating button -->
      <button
        class="toc-fab"
        :class="{ 'toc-fab--hidden': drawerVisible }"
        @click="openDrawer"
        aria-label="打开目录"
      >
        <svg class="toc-fab__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M4 6h16M4 12h16M4 18h12" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span class="toc-fab__badge" v-if="headings.length > 0">{{ headings.length }}</span>
      </button>

      <!-- Drawer backdrop -->
      <Transition name="backdrop-fade">
        <div
          v-if="drawerVisible"
          class="toc-drawer__backdrop"
          @click="closeDrawer"
        ></div>
      </Transition>

      <!-- Drawer -->
      <Transition name="drawer-slide">
        <div v-if="drawerVisible" class="toc-drawer">
          <div class="toc-drawer__header">
            <span class="toc-drawer__title">目录</span>
            <button class="toc-drawer__close" @click="closeDrawer" aria-label="关闭目录">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 6L6 18M6 6l12 12" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
          <nav class="toc-drawer__nav">
            <ul class="toc-list">
              <li
                v-for="heading in headings"
                :key="heading.id"
                class="toc-item"
                :class="[
                  `toc-item--level-${heading.level}`,
                  { 'toc-item--active': heading.id === activeId }
                ]"
              >
                <a
                  class="toc-link"
                  :class="{ 'toc-link--active': heading.id === activeId }"
                  @click="handleSelect(heading.id)"
                >
                  <span class="toc-link__indicator" v-if="heading.id === activeId"></span>
                  <span class="toc-link__text">{{ heading.text }}</span>
                </a>
              </li>
            </ul>
            <div v-if="headings.length === 0" class="toc-empty">
              <span class="toc-empty__text">暂无目录</span>
            </div>
          </nav>
        </div>
      </Transition>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// Props
const props = defineProps({
  headings: {
    type: Array,
    default: () => []
    // Array of { id: string, text: string, level: number, top: number }
  },
  activeId: {
    type: String,
    default: ''
  }
})

// Emits
const emit = defineEmits(['select'])

// Reactive state
const isMobile = ref(false)
const drawerVisible = ref(false)

// Breakpoint for mobile view
const MOBILE_BREAKPOINT = 1024

// Check viewport size
const checkViewport = () => {
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT
}

// Handle heading selection
const handleSelect = (id) => {
  emit('select', id)
  // Close drawer on mobile after selection
  if (isMobile.value) {
    closeDrawer()
  }
}

// Drawer controls
const openDrawer = () => {
  drawerVisible.value = true
  document.body.style.overflow = 'hidden'
}

const closeDrawer = () => {
  drawerVisible.value = false
  document.body.style.overflow = ''
}

// Lifecycle
onMounted(() => {
  checkViewport()
  window.addEventListener('resize', checkViewport)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkViewport)
  document.body.style.overflow = ''
})
</script>

<style lang="scss" scoped>
// ========================================
// PC Panel Styles
// ========================================
.toc-panel {
  position: fixed;
  top: 120px;
  right: 24px;
  width: 200px;
  max-height: calc(100vh - 160px);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  z-index: var(--z-sticky);

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--space-4) var(--space-5);
    border-bottom: 1px solid var(--border-color);
    background: var(--bg-secondary);
  }

  &__title {
    font-size: var(--text-sm);
    font-weight: var(--font-semibold);
    color: var(--text-primary);
    letter-spacing: var(--tracking-wide);
  }

  &__count {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    font-size: var(--text-xs);
    font-weight: var(--font-medium);
    color: var(--color-primary);
    background: var(--color-primary-light);
    border-radius: var(--radius-full);
  }

  &__nav {
    max-height: calc(100vh - 220px);
    overflow-y: auto;
    padding: var(--space-3) 0;

    // Custom scrollbar
    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--border-color);
      border-radius: var(--radius-full);
    }
  }
}

// ========================================
// Table of Contents List
// ========================================
.toc-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.toc-item {
  position: relative;

  // Indentation based on heading level
  &--level-1 .toc-link { padding-left: var(--space-4); }
  &--level-2 .toc-link { padding-left: var(--space-6); }
  &--level-3 .toc-link { padding-left: var(--space-8); }
  &--level-4 .toc-link { padding-left: var(--space-10); }
  &--level-5 .toc-link { padding-left: var(--space-12); }
  &--level-6 .toc-link { padding-left: var(--space-14); }
}

.toc-link {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  padding-right: var(--space-5);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
  border-left: 2px solid transparent;

  &__indicator {
    flex-shrink: 0;
    width: 4px;
    height: 4px;
    background: var(--color-primary);
    border-radius: 50%;
    animation: pulse 1.5s ease-in-out infinite;
  }

  &__text {
    font-size: var(--text-sm);
    color: var(--text-secondary);
    line-height: var(--leading-normal);
    transition: color var(--transition-fast);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &:hover {
    background: var(--bg-hover);

    .toc-link__text {
      color: var(--text-primary);
    }
  }

  &--active {
    background: var(--color-primary-light);
    border-left-color: var(--color-primary);

    .toc-link__text {
      color: var(--color-primary);
      font-weight: var(--font-medium);
    }
  }
}

.toc-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-4);

  &__text {
    font-size: var(--text-sm);
    color: var(--text-muted);
  }
}

// ========================================
// Mobile FAB Button
// ========================================
.toc-fab {
  position: fixed;
  right: 20px;
  bottom: 80px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  box-shadow: var(--shadow-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  z-index: var(--z-fixed);

  &:hover {
    transform: scale(1.05);
    box-shadow: var(--shadow-xl);
  }

  &:active {
    transform: scale(0.95);
  }

  &--hidden {
    opacity: 0;
    pointer-events: none;
    transform: scale(0.8);
  }

  &__icon {
    width: 22px;
    height: 22px;
    color: var(--text-primary);
  }

  &__badge {
    position: absolute;
    top: -4px;
    right: -4px;
    min-width: 18px;
    height: 18px;
    padding: 0 5px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    font-weight: var(--font-semibold);
    color: var(--text-inverse);
    background: var(--color-primary);
    border-radius: var(--radius-full);
    box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
  }
}

// ========================================
// Mobile Drawer
// ========================================
.toc-drawer {
  position: fixed;
  top: 0;
  right: 0;
  width: 280px;
  max-width: 85vw;
  height: 100vh;
  background: var(--bg-primary);
  box-shadow: var(--shadow-xl);
  z-index: var(--z-modal);
  display: flex;
  flex-direction: column;

  &__backdrop {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.4);
    z-index: var(--z-modal-backdrop);
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--space-4) var(--space-5);
    border-bottom: 1px solid var(--border-color);
    flex-shrink: 0;
  }

  &__title {
    font-size: var(--text-base);
    font-weight: var(--font-semibold);
    color: var(--text-primary);
  }

  &__close {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: var(--bg-hover);
    border: none;
    border-radius: var(--radius-md);
    cursor: pointer;
    transition: all var(--transition-fast);

    svg {
      width: 18px;
      height: 18px;
      color: var(--text-secondary);
    }

    &:hover {
      background: var(--bg-active);

      svg {
        color: var(--text-primary);
      }
    }
  }

  &__nav {
    flex: 1;
    overflow-y: auto;
    padding: var(--space-3) 0;

    // Custom scrollbar
    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-track {
      background: transparent;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--border-color);
      border-radius: var(--radius-full);
    }
  }
}

// ========================================
// Animations
// ========================================
@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

// Backdrop fade transition
.backdrop-fade-enter-active,
.backdrop-fade-leave-active {
  transition: opacity var(--transition-base);
}

.backdrop-fade-enter-from,
.backdrop-fade-leave-to {
  opacity: 0;
}

// Drawer slide transition
.drawer-slide-enter-active,
.drawer-slide-leave-active {
  transition: transform var(--transition-base) var(--ease-spring);
}

.drawer-slide-enter-from,
.drawer-slide-leave-to {
  transform: translateX(100%);
}

// ========================================
// Responsive Styles
// ========================================
@media (max-width: 1023px) {
  .toc-panel {
    display: none;
  }
}

@media (min-width: 1024px) {
  .toc-fab,
  .toc-drawer,
  .toc-drawer__backdrop {
    display: none !important;
  }
}

// Safe area for mobile devices with notches
@supports (padding-bottom: env(safe-area-inset-bottom)) {
  .toc-fab {
    bottom: calc(80px + env(safe-area-inset-bottom));
  }

  .toc-drawer__nav {
    padding-bottom: env(safe-area-inset-bottom);
  }
}
</style>
