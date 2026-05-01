import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserAchievements } from '@/api/achievement'

export const useAchievementStore = defineStore('achievement', () => {
  const achievements = ref([])
  const loading = ref(false)

  const groupedAchievements = computed(() => {
    const groups = {
      content: { label: '内容成就', items: [] },
      social: { label: '社交成就', items: [] },
      activity: { label: '活动成就', items: [] },
      special: { label: '特殊成就', items: [] }
    }
    
    achievements.value.forEach(a => {
      if (groups[a.category]) {
        groups[a.category].items.push(a)
      }
    })
    
    return groups
  })

  const stats = computed(() => {
    const total = achievements.value.length
    const unlocked = achievements.value.filter(a => a.unlocked).length
    const totalPoints = achievements.value
      .filter(a => a.unlocked)
      .reduce((sum, a) => sum + a.points, 0)
    
    return { total, unlocked, totalPoints }
  })

  async function fetchAchievements() {
    if (loading.value) return
    loading.value = true
    try {
      const { data } = await getUserAchievements()
      achievements.value = data
    } catch (error) {
      console.error('Failed to fetch achievements:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    achievements,
    loading,
    groupedAchievements,
    stats,
    fetchAchievements
  }
})
