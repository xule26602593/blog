<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="format-panel">
      <div class="panel-header">
        <h3>智能排版</h3>
        <van-icon name="cross" @click="close" />
      </div>

      <div class="quick-actions">
        <van-button size="small" @click="handleQuickFormat">一键排版</van-button>
        <van-button size="small" @click="handleCheckLinks">检查链接</van-button>
      </div>

      <div class="rules-section">
        <h4>排版规则</h4>
        <van-loading v-if="loading" size="24px" />

        <div v-else class="rules-list">
          <van-cell-group>
            <van-cell v-for="rule in rules" :key="rule.id">
              <template #title>
                <van-checkbox v-model="selectedRules[rule.ruleKey]">
                  {{ rule.ruleName }}
                </van-checkbox>
              </template>
              <template #label>
                <div class="rule-desc">{{ rule.description }}</div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </div>

      <div class="panel-footer">
        <van-button type="primary" block @click="handleApply">应用排版</van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { showToast } from 'vant'
import { getFormatRules, applyFormat, checkLinks } from '@/api/format'

const props = defineProps({
  show: Boolean,
  content: String
})

const emit = defineEmits(['update:show', 'apply'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const loading = ref(false)
const rules = ref([])
const selectedRules = ref({})

const loadRules = async () => {
  loading.value = true
  try {
    const res = await getFormatRules()
    rules.value = res.data || []
    rules.value.forEach(rule => {
      selectedRules.value[rule.ruleKey] = rule.isDefault === 1
    })
  } finally {
    loading.value = false
  }
}

const getSelectedRuleKeys = () => {
  return Object.entries(selectedRules.value)
    .filter(([, selected]) => selected)
    .map(([key]) => key)
}

const handleQuickFormat = async () => {
  const selectedKeys = getSelectedRuleKeys()
  if (selectedKeys.length === 0) {
    showToast('请选择至少一个规则')
    return
  }

  showToast('正在排版...')
  const res = await applyFormat(props.content, selectedKeys)
  emit('apply', res.data)
  showToast('排版完成')
  close()
}

const handleCheckLinks = async () => {
  showToast('正在检查链接...')
  const res = await checkLinks(props.content)
  const result = res.data
  if (result.invalid > 0) {
    showToast(`发现 ${result.invalid} 个无效链接`)
  } else {
    showToast({ type: 'success', message: '所有链接有效' })
  }
}

const handleApply = async () => {
  const selectedKeys = getSelectedRuleKeys()
  const res = await applyFormat(props.content, selectedKeys)
  emit('apply', res.data)
  close()
}

const close = () => {
  visible.value = false
}

onMounted(loadRules)

watch(() => props.show, (val) => {
  if (val) {
    loadRules()
  }
})
</script>

<style scoped>
.format-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
}

.quick-actions {
  display: flex;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.rules-section {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.rules-section h4 {
  margin: 0 0 12px;
}

.rule-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.panel-footer {
  padding: 16px;
  border-top: 1px solid #eee;
}
</style>
