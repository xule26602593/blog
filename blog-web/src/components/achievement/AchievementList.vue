<template>
  <div class="achievement-list">
    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="overview-item">
        <span class="overview-value">{{ stats.unlocked }}/{{ stats.total }}</span>
        <span class="overview-label">已解锁</span>
      </div>
      <div class="overview-divider"></div>
      <div class="overview-item">
        <span class="overview-value">{{ stats.totalPoints }}</span>
        <span class="overview-label">成就积分</span>
      </div>
    </div>

    <!-- 分类标签页 -->
    <van-tabs
      v-model:active="activeTab"
      animated
      shrink
      class="category-tabs"
    >
      <van-tab
        v-for="(group, key) in groupedAchievements"
        :key="key"
        :title="group.label"
        :name="key"
      >
        <div class="achievements-grid">
          <div
            v-for="achievement in group.items"
            :key="achievement.id"
            class="achievement-card"
            :class="[getRarityClass(achievement.rarity), { unlocked: achievement.unlocked }]"
          >
            <!-- 成就图标 -->
            <div class="achievement-icon">
              <svg v-if="achievement.unlocked" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
              </svg>
            </div>

            <!-- 成就信息 -->
            <div class="achievement-info">
              <div class="achievement-header">
                <h4 class="achievement-name">{{ achievement.name }}</h4>
                <span class="achievement-points">+{{ achievement.points }}</span>
              </div>
              <p class="achievement-desc">{{ achievement.description }}</p>

              <!-- 进度条 -->
              <div class="progress-wrapper" v-if="achievement.targetCount > 1">
                <div class="progress-bar">
                  <div
                    class="progress-fill"
                    :style="{ width: getProgressWidth(achievement) }"
                  ></div>
                </div>
                <span class="progress-text">
                  {{ achievement.currentCount || 0 }}/{{ achievement.targetCount }}
                </span>
              </div>
            </div>

            <!-- 解锁标记 -->
            <div class="unlock-badge" v-if="achievement.unlocked">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!group.items.length" class="empty-state">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
            </svg>
            <p>暂无该分类成就</p>
          </div>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAchievementStore } from '@/stores/achievement'

const achievementStore = useAchievementStore()
const activeTab = ref('content')

const groupedAchievements = computed(() => achievementStore.groupedAchievements)
const stats = computed(() => achievementStore.stats)

const getRarityClass = (rarity) => {
  const rarityMap = {
    common: '',
    rare: 'rarity-rare',
    epic: 'rarity-epic',
    legendary: 'rarity-legendary'
  }
  return rarityMap[rarity] || ''
}

const getProgressWidth = (achievement) => {
  if (!achievement.targetCount) return '0%'
  const progress = Math.min((achievement.currentCount || 0) / achievement.targetCount, 1)
  return `${progress * 100}%`
}

onMounted(() => {
  achievementStore.fetchAchievements()
})
</script>

<style lang="scss" scoped>
.achievement-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.stats-overview {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-8);
  padding: var(--space-6);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.overview-value {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
  color: var(--color-primary);
}

.overview-label {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.overview-divider {
  width: 1px;
  height: 40px;
  background: var(--border-color);
}

/* 分类标签页 */
.category-tabs {
  :deep(.van-tabs__wrap) {
    height: 48px;
    background: var(--bg-card);
    border-radius: var(--radius-xl);
    border: 1px solid var(--border-color);
    padding: var(--space-1);
  }

  :deep(.van-tab) {
    font-size: var(--text-sm);
    font-weight: var(--font-medium);
    color: var(--text-secondary);
    padding: 0 var(--space-4);
    border-radius: var(--radius-lg);
    transition: all var(--transition-fast);
  }

  :deep(.van-tab--active) {
    color: white;
    background: var(--gradient-primary);
  }

  :deep(.van-tabs__line) {
    display: none;
  }

  :deep(.van-tabs__content) {
    padding-top: var(--space-4);
  }
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.achievement-card {
  position: relative;
  padding: var(--space-5);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  transition: all var(--transition-base);
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: var(--border-color);
    transition: background var(--transition-fast);
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  /* 稀有度样式 */
  &.rarity-rare {
    &::before {
      background: linear-gradient(90deg, #22C55E, #16A34A);
    }

    .achievement-icon {
      background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(22, 163, 74, 0.1));
      color: #22C55E;
    }
  }

  &.rarity-epic {
    &::before {
      background: linear-gradient(90deg, #A855F7, #7C3AED);
    }

    .achievement-icon {
      background: linear-gradient(135deg, rgba(168, 85, 247, 0.15), rgba(124, 58, 237, 0.1));
      color: #A855F7;
    }
  }

  &.rarity-legendary {
    &::before {
      background: linear-gradient(90deg, #F97316, #EA580C);
    }

    .achievement-icon {
      background: linear-gradient(135deg, rgba(249, 115, 22, 0.15), rgba(234, 88, 12, 0.1));
      color: #F97316;
    }
  }

  /* 已解锁样式 */
  &.unlocked {
    border-color: var(--color-success);

    &::before {
      background: linear-gradient(90deg, var(--color-success), #22C55E);
    }

    .achievement-icon {
      background: linear-gradient(135deg, rgba(52, 199, 89, 0.15), rgba(34, 197, 94, 0.1));
      color: var(--color-success);
    }
  }
}

.achievement-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  color: var(--text-muted);
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
  }
}

.achievement-info {
  flex: 1;
  min-width: 0;
}

.achievement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.achievement-name {
  font-size: var(--text-sm);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.achievement-points {
  font-size: var(--text-xs);
  font-weight: var(--font-semibold);
  color: var(--color-primary);
  white-space: nowrap;
}

.achievement-desc {
  font-size: var(--text-xs);
  color: var(--text-secondary);
  line-height: var(--leading-normal);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.progress-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: var(--bg-secondary);
  border-radius: var(--radius-full);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
  transition: width var(--transition-base);
}

.progress-text {
  font-size: var(--text-xs);
  color: var(--text-muted);
  white-space: nowrap;
}

.unlock-badge {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-success);
  border-radius: var(--radius-full);
  color: white;

  svg {
    width: 14px;
    height: 14px;
  }
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-12);
  color: var(--text-muted);

  svg {
    width: 48px;
    height: 48px;
    opacity: 0.5;
  }

  p {
    font-size: var(--text-sm);
  }
}

/* 响应式 */
@media (max-width: 640px) {
  .achievements-grid {
    grid-template-columns: 1fr;
  }

  .stats-overview {
    padding: var(--space-5);
    gap: var(--space-6);
  }

  .overview-value {
    font-size: var(--text-xl);
  }
}
</style>
