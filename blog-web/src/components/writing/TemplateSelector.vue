<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="template-selector">
      <div class="selector-header">
        <h3>选择写作模板</h3>
        <van-icon name="cross" @click="close" />
      </div>

      <div class="selector-content">
        <van-loading v-if="loading" size="24px" vertical>加载中...</van-loading>

        <template v-else>
          <div v-for="template in templates" :key="template.id" class="template-card" @click="handleSelect(template)">
            <div class="card-header">
              <span class="card-name">{{ template.name }}</span>
              <van-tag v-if="template.isBuiltin" size="small" type="primary">内置</van-tag>
            </div>
            <div class="card-desc">{{ template.description }}</div>
            <div class="card-meta">
              <span>使用 {{ template.usageCount || 0 }} 次</span>
            </div>
          </div>

          <van-empty v-if="templates.length === 0" description="暂无模板" />
        </template>
      </div>

      <div class="selector-footer">
        <van-button block @click="handleSelect(null)">使用空白模板</van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { getTemplates } from '@/api/template'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['update:show', 'select'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const loading = ref(false)
const templates = ref([])

const loadTemplates = async () => {
  loading.value = true
  try {
    const res = await getTemplates()
    templates.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleSelect = (template) => {
  emit('select', template)
  close()
}

const close = () => {
  visible.value = false
}

onMounted(loadTemplates)

watch(() => props.show, (val) => {
  if (val) {
    loadTemplates()
  }
})
</script>

<style scoped>
.template-selector {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.selector-header h3 {
  margin: 0;
  font-size: 16px;
}

.selector-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.template-card {
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: var(--van-primary-color);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-name {
  font-weight: 500;
}

.card-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.card-meta {
  font-size: 12px;
  color: #999;
}

.selector-footer {
  padding: 16px;
  border-top: 1px solid #eee;
}
</style>
