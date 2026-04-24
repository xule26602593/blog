// blog-web/src/stores/reading.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useReadingStore = defineStore('reading', () => {
  const fontSize = ref(localStorage.getItem('reading-fontSize') || 'medium')
  const theme = ref(localStorage.getItem('reading-theme') || 'default')

  const fontSizes = {
    small: '14px',
    medium: '16px',
    large: '18px',
    xlarge: '20px'
  }

  const themes = {
    default: {
      bg: 'var(--bg-primary)',
      text: 'var(--text-primary)'
    },
    sepia: {
      bg: '#f4ecd8',
      text: '#5c4b37'
    },
    dark: {
      bg: '#1a1a1a',
      text: '#e0e0e0'
    }
  }

  const setFontSize = (size) => {
    fontSize.value = size
    localStorage.setItem('reading-fontSize', size)
  }

  const setTheme = (newTheme) => {
    theme.value = newTheme
    localStorage.setItem('reading-theme', newTheme)
  }

  const getFontSizeValue = () => fontSizes[fontSize.value] || fontSizes.medium
  const getThemeValue = () => themes[theme.value] || themes.default

  return {
    fontSize,
    theme,
    fontSizes,
    themes,
    setFontSize,
    setTheme,
    getFontSizeValue,
    getThemeValue
  }
})
