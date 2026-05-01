<template>
  <div class="checkin-panel">
    <!-- 签到状态卡片 -->
    <div class="checkin-card">
      <div class="checkin-header">
        <div class="header-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="header-text">
          <h3 class="header-title">{{ isCheckedToday ? '今日已签到' : '今日签到' }}</h3>
          <p class="header-subtitle">{{ isCheckedToday ? '明天再来吧' : '点击签到获取积分' }}</p>
        </div>
      </div>

      <!-- 签到按钮 -->
      <button
        class="checkin-btn"
        :class="{ 'checked': isCheckedToday, 'loading': checkinStore.loading }"
        :disabled="isCheckedToday || checkinStore.loading"
        @click="handleCheckin"
      >
        <template v-if="checkinStore.loading">
          <span class="btn-spinner"></span>
          <span>签到中...</span>
        </template>
        <template v-else-if="isCheckedToday">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
          </svg>
          <span>已签到</span>
        </template>
        <template v-else>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z" />
          </svg>
          <span>立即签到</span>
        </template>
      </button>
    </div>

    <!-- 统计数据 -->
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-value">{{ status.consecutiveDays }}</div>
        <div class="stat-label">连续签到</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ status.totalDays }}</div>
        <div class="stat-label">累计签到</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ status.totalPoints }}</div>
        <div class="stat-label">总积分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ status.maxConsecutiveDays }}</div>
        <div class="stat-label">最长连续</div>
      </div>
    </div>

    <!-- 签到结果弹窗 -->
    <van-dialog
      v-model:show="showResult"
      title=""
      :show-confirm-button="true"
      confirm-button-text="太棒了"
      class="checkin-result-dialog"
    >
      <div class="result-content">
        <div class="result-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
          </svg>
        </div>
        <h4 class="result-title">签到成功</h4>
        <p class="result-points">+{{ checkinResult?.points || 0 }} 积分</p>
        <p class="result-streak" v-if="checkinResult?.consecutiveDays > 1">
          已连续签到 {{ checkinResult?.consecutiveDays }} 天
        </p>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { showToast } from 'vant'
import { useCheckinStore } from '@/stores/checkin'

const checkinStore = useCheckinStore()
const showResult = ref(false)

const status = computed(() => checkinStore.status)
const isCheckedToday = computed(() => checkinStore.status.isCheckedToday)
const checkinResult = computed(() => checkinStore.lastCheckinResult)

const handleCheckin = async () => {
  try {
    const result = await checkinStore.doCheckin()
    if (result) {
      showResult.value = true
    }
  } catch (error) {
    showToast({
      type: 'fail',
      message: error.response?.data?.message || '签到失败，请稍后重试'
    })
  }
}

onMounted(() => {
  checkinStore.fetchStatus()
})
</script>

<style lang="scss" scoped>
.checkin-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.checkin-card {
  padding: var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-6);
}

.checkin-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  color: white;

  svg {
    width: 24px;
    height: 24px;
  }
}

.header-text {
  text-align: left;
}

.header-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.header-subtitle {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.checkin-btn {
  width: 100%;
  max-width: 280px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  font-family: var(--font-sans);
  color: white;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-md);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &.checked {
    background: var(--bg-secondary);
    color: var(--text-secondary);
    box-shadow: none;
    cursor: default;
  }

  &.loading {
    opacity: 0.8;
    cursor: wait;
  }

  &:disabled {
    cursor: not-allowed;
  }
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
}

.stat-item {
  padding: var(--space-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  text-align: center;
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    box-shadow: var(--shadow-sm);
  }
}

.stat-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
  margin-bottom: var(--space-1);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-secondary);
}

/* 签到结果弹窗样式 */
.result-content {
  padding: var(--space-8) var(--space-6);
  text-align: center;
}

.result-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto var(--space-4);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border-radius: var(--radius-full);
  color: white;

  svg {
    width: 32px;
    height: 32px;
  }
}

.result-title {
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.result-points {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
  margin-bottom: var(--space-2);
}

.result-streak {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

/* 响应式 */
@media (max-width: 640px) {
  .checkin-card {
    padding: var(--space-6);
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-value {
    font-size: var(--text-xl);
  }
}
</style>
