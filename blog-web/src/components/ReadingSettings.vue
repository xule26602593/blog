<!-- blog-web/src/components/ReadingSettings.vue -->
<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ padding: '20px' }"
  >
    <div class="reading-settings">
      <h3 class="settings-title">阅读设置</h3>

      <!-- Font Size -->
      <div class="settings-section">
        <span class="section-label">字体大小</span>
        <div class="font-size-options">
          <button
            v-for="(label, key) in fontSizeLabels"
            :key="key"
            class="size-btn"
            :class="{ active: readingStore.fontSize === key }"
            @click="readingStore.setFontSize(key)"
          >
            {{ label }}
          </button>
        </div>
      </div>

      <!-- Theme -->
      <div class="settings-section">
        <span class="section-label">背景颜色</span>
        <div class="theme-options">
          <button
            v-for="(colors, key) in readingStore.themes"
            :key="key"
            class="theme-btn"
            :class="{ active: readingStore.theme === key }"
            :style="{ backgroundColor: colors.bg }"
            @click="readingStore.setTheme(key)"
          >
            <span class="theme-label">{{ themeLabels[key] }}</span>
          </button>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useReadingStore } from '@/stores/reading'

const props = defineProps({
  show: { type: Boolean, default: false }
})

const emit = defineEmits(['update:show'])

const readingStore = useReadingStore()
const visible = ref(false)

const fontSizeLabels = {
  small: '小',
  medium: '中',
  large: '大',
  xlarge: '特大'
}

const themeLabels = {
  default: '默认',
  sepia: '护眼',
  dark: '夜间'
}

watch(() => props.show, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:show', val)
})
</script>

<style lang="scss" scoped>
.reading-settings {
  max-width: 320px;
  margin: 0 auto;
}

.settings-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  text-align: center;
  margin-bottom: var(--space-6);
}

.settings-section {
  margin-bottom: var(--space-6);
}

.section-label {
  display: block;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.font-size-options {
  display: flex;
  gap: var(--space-2);
}

.size-btn {
  flex: 1;
  padding: var(--space-3);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }

  &.active {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: var(--color-primary-light);
  }
}

.theme-options {
  display: flex;
  gap: var(--space-3);
}

.theme-btn {
  flex: 1;
  height: 60px;
  border: 2px solid transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;

  &:hover {
    transform: scale(1.02);
  }

  &.active {
    border-color: var(--color-primary);
  }
}

.theme-label {
  position: absolute;
  bottom: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: var(--text-xs);
  color: var(--text-muted);
}
</style>
