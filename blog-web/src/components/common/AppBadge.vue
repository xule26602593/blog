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
