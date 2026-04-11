import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'

export const useAppStore = defineStore('app', () => {
  const categories = ref([])
  const tags = ref([])
  const initialized = ref(false)

  // 初始化应用数据
  async function initApp() {
    if (initialized.value) return

    try {
      const [categoriesRes, tagsRes] = await Promise.all([
        getCategories(),
        getTags()
      ])
      categories.value = categoriesRes.data || []
      tags.value = tagsRes.data || []
      initialized.value = true
    } catch (error) {
      console.error('初始化应用数据失败', error)
    }
  }

  // 刷新分类
  async function refreshCategories() {
    const res = await getCategories()
    categories.value = res.data || []
  }

  // 刷新标签
  async function refreshTags() {
    const res = await getTags()
    tags.value = res.data || []
  }

  return {
    categories,
    tags,
    initialized,
    initApp,
    refreshCategories,
    refreshTags
  }
})
