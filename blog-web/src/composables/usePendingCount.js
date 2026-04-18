import { ref } from 'vue'
import { getPendingCount } from '@/api/comment'

const pendingCount = ref(0)

export function usePendingCount() {
  const fetchPendingCount = async () => {
    try {
      const res = await getPendingCount()
      pendingCount.value = res.data || 0
    } catch (error) {
      console.error('获取待审核数量失败', error)
    }
  }

  const decrementPendingCount = () => {
    if (pendingCount.value > 0) {
      pendingCount.value--
    }
  }

  return {
    pendingCount,
    fetchPendingCount,
    decrementPendingCount
  }
}
