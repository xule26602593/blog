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
