import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCheckinStatus, checkin as checkinApi } from '@/api/checkin'

export const useCheckinStore = defineStore('checkin', () => {
  const status = ref({
    isCheckedToday: false,
    consecutiveDays: 0,
    totalDays: 0,
    totalPoints: 0,
    maxConsecutiveDays: 0,
    todayPoints: 0
  })
  
  const loading = ref(false)
  const lastCheckinResult = ref(null)
  const canCheckin = computed(() => !status.value.isCheckedToday)

  async function fetchStatus() {
    try {
      const { data } = await getCheckinStatus()
      status.value = data
    } catch (error) {
      console.error('Failed to fetch checkin status:', error)
    }
  }

  async function doCheckin() {
    if (loading.value) return null
    
    loading.value = true
    try {
      const { data } = await checkinApi()
      lastCheckinResult.value = data
      await fetchStatus()
      return data
    } catch (error) {
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    status,
    loading,
    lastCheckinResult,
    canCheckin,
    fetchStatus,
    doCheckin
  }
})
