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
