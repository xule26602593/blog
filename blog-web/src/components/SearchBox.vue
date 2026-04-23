<template>
  <div class="search-box">
    <input
      v-model="keyword"
      type="text"
      :placeholder="placeholder"
      class="search-input"
      @focus="onFocus"
      @blur="onBlur"
      @input="onInput"
      @keyup.enter="onSearch"
    />

    <!-- 搜索建议下拉 -->
    <div class="suggestions" v-show="showSuggestions && suggestions.length > 0">
      <div
        class="suggestion-item"
        v-for="item in suggestions"
        :key="item"
        @click="selectSuggestion(item)"
      >
        {{ item }}
      </div>
    </div>

    <!-- 搜索历史和热门 -->
    <div class="search-panel" v-show="showPanel">
      <div class="panel-section" v-if="history.length > 0">
        <div class="section-header">
          <span>搜索历史</span>
          <span class="clear-btn" @click="clearHistory">清空</span>
        </div>
        <div class="tags">
          <span
            class="tag"
            v-for="item in history"
            :key="item"
            @click="selectSuggestion(item)"
          >
            {{ item }}
          </span>
        </div>
      </div>

      <div class="panel-section" v-if="hotKeywords.length > 0">
        <div class="section-header">
          <span>热门搜索</span>
        </div>
        <div class="tags">
          <span
            class="tag"
            v-for="item in hotKeywords"
            :key="item"
            @click="selectSuggestion(item)"
          >
            {{ item }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import searchApi from '@/api/search'

const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索文章'
  }
})

const router = useRouter()

const keyword = ref('')
const showSuggestions = ref(false)
const showPanel = ref(false)
const suggestions = ref([])
const history = ref([])
const hotKeywords = ref([])

let debounceTimer = null

onMounted(async () => {
  try {
    const [historyRes, hotRes] = await Promise.all([
      searchApi.getHistory(),
      searchApi.getHotKeywords()
    ])
    history.value = historyRes.data || []
    hotKeywords.value = hotRes.data || []
  } catch (e) {
    // ignore
  }
})

const onFocus = () => {
  if (keyword.value) {
    showSuggestions.value = true
  } else {
    showPanel.value = true
  }
}

const onBlur = () => {
  setTimeout(() => {
    showSuggestions.value = false
    showPanel.value = false
  }, 200)
}

const onInput = () => {
  if (debounceTimer) clearTimeout(debounceTimer)

  if (!keyword.value) {
    suggestions.value = []
    showSuggestions.value = false
    showPanel.value = true
    return
  }

  debounceTimer = setTimeout(async () => {
    try {
      const res = await searchApi.getSuggestions(keyword.value)
      suggestions.value = res.data || []
      showSuggestions.value = true
      showPanel.value = false
    } catch (e) {
      suggestions.value = []
    }
  }, 300)
}

const onSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
}

const selectSuggestion = (item) => {
  keyword.value = item
  onSearch()
}

const clearHistory = () => {
  history.value = []
}
</script>

<style lang="scss" scoped>
.search-box {
  position: relative;
}

.search-input {
  width: 100%;
  height: 40px;
  padding: 0 16px;
  font-size: 14px;
  color: var(--text-primary, #333);
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-color, #e5e5e5);
  border-radius: 8px;
  outline: none;
  transition: all 0.2s;

  &::placeholder {
    color: var(--text-muted, #999);
  }

  &:focus {
    border-color: var(--text-muted, #999);
  }
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-color, #e5e5e5);
  border-radius: 0 0 8px 8px;
  z-index: 100;
  max-height: 300px;
  overflow-y: auto;
}

.suggestion-item {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color, #f5f5f5);
  cursor: pointer;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--bg-secondary, #f5f5f5);
  }
}

.search-panel {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: var(--bg-card, #fff);
  border: 1px solid var(--border-color, #e5e5e5);
  border-radius: 0 0 8px 8px;
  z-index: 100;
  padding: 16px;
}

.panel-section {
  margin-bottom: 16px;

  &:last-child {
    margin-bottom: 0;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary, #333);
}

.clear-btn {
  font-size: 12px;
  font-weight: normal;
  color: var(--text-muted, #999);
  cursor: pointer;

  &:hover {
    color: var(--text-secondary, #666);
  }
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 12px;
  font-size: 13px;
  color: var(--text-secondary, #666);
  background: var(--bg-secondary, #f5f5f5);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: var(--border-color, #e5e5e5);
  }
}
</style>
