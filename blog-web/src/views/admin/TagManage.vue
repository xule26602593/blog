<template>
  <div class="manage-page">
    <div class="toolbar">
      <van-button type="primary" size="small" @click="handleAdd">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="btn-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
        新增标签
      </van-button>
    </div>

    <div class="tag-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div class="tag-grid">
            <div
              v-for="tag in tagList"
              :key="tag.id"
              class="tag-card"
            >
              <div class="tag-header">
                <span class="tag-id">#{{ tag.id }}</span>
              </div>
              <div class="tag-preview" :style="{ background: tag.color }">
                {{ tag.name }}
              </div>
              <div class="tag-color">
                <span class="color-dot" :style="{ background: tag.color }"></span>
                <span>{{ tag.color }}</span>
              </div>
              <div class="tag-actions">
                <van-button type="primary" size="small" plain @click="handleEdit(tag)">编辑</van-button>
                <van-button type="danger" size="small" plain @click="handleDelete(tag)">删除</van-button>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <div class="pagination">
      <van-pagination
        v-model="queryParams.pageNum"
        :total-items="total"
        :items-per-page="queryParams.pageSize"
        :show-page-size="5"
        force-ellipses
        @change="fetchTags"
      />
    </div>

    <!-- Edit Dialog -->
    <van-popup v-model:show="dialogVisible" position="bottom" round style="height: auto; max-height: 80vh;">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3 class="dialog-title">{{ isEdit ? '编辑标签' : '新增标签' }}</h3>
          <button class="dialog-close" @click="dialogVisible = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <van-form @submit="handleSubmit" class="dialog-form">
          <van-field
            v-model="form.name"
            label="名称"
            placeholder="请输入标签名称"
            :rules="[{ required: true, message: '请输入标签名称' }]"
          />
          <van-field name="color" label="颜色">
            <template #input>
              <div class="color-picker-row">
                <input
                  type="color"
                  v-model="form.color"
                  class="color-input"
                />
                <van-field
                  v-model="form.color"
                  placeholder="#171717"
                  class="color-text"
                />
              </div>
            </template>
          </van-field>
          <div class="dialog-actions">
            <van-button block @click="dialogVisible = false">取消</van-button>
            <van-button type="primary" block native-type="submit">确定</van-button>
          </div>
        </van-form>
      </div>
    </van-popup>

    <!-- Delete Confirmation Dialog -->
    <van-dialog
      v-model:show="showDeleteDialog"
      title="确认删除"
      message="确定要删除该标签吗？"
      show-cancel-button
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import { getAdminTags, saveTag, deleteTag } from '@/api/tag'

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const tagList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const showDeleteDialog = ref(false)
const tagToDelete = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  name: '',
  color: '#171717'
})

const fetchTags = async () => {
  loading.value = true
  try {
    const res = await getAdminTags(queryParams)
    tagList.value = res.data?.records || []
    total.value = res.data?.total || 0
    finished.value = tagList.value.length >= total.value
  } catch (error) {
    console.error('获取标签失败', error)
    showToast('获取标签失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  fetchTags()
}

const onRefresh = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchTags()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.color = '#171717'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.color = row.color
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await saveTag(form)
    showToast({ type: 'success', message: isEdit.value ? '更新成功' : '新增成功' })
    dialogVisible.value = false
    fetchTags()
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  }
}

const handleDelete = (row) => {
  tagToDelete.value = row
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!tagToDelete.value) return
  try {
    await deleteTag(tagToDelete.value.id)
    showToast({ type: 'success', message: '删除成功' })
    fetchTags()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  } finally {
    tagToDelete.value = null
  }
}

onMounted(() => {
  fetchTags()
})
</script>

<style lang="scss" scoped>
.manage-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.toolbar {
  display: flex;
  justify-content: flex-end;
}

.btn-icon {
  width: 16px;
  height: 16px;
  margin-right: var(--space-1);
}

.tag-list {
  min-height: 300px;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-4);
}

.tag-card {
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);
  }
}

.tag-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.tag-id {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-family: monospace;
}

.tag-preview {
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  text-align: center;
  margin-bottom: var(--space-3);
}

.tag-color {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-3);
}

.color-dot {
  width: 16px;
  height: 16px;
  border-radius: var(--radius-sm);
}

.tag-actions {
  display: flex;
  gap: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.pagination {
  display: flex;
  justify-content: center;
  padding: var(--space-4) 0;
}

.dialog-content {
  padding: var(--space-4);
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.dialog-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
}

.dialog-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-muted);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);

  svg {
    width: 20px;
    height: 20px;
  }

  &:hover {
    background: var(--bg-hover);
    color: var(--text-primary);
  }
}

.dialog-form {
  :deep(.van-field__label) {
    width: auto;
  }
}

.color-picker-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.color-input {
  width: 40px;
  height: 40px;
  padding: 0;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  background: none;

  &::-webkit-color-swatch-wrapper {
    padding: 0;
  }

  &::-webkit-color-swatch {
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
  }
}

.color-text {
  flex: 1;

  :deep(.van-field__control) {
    font-family: monospace;
  }
}

.dialog-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
  margin-top: var(--space-4);
}

@media (max-width: 768px) {
  .toolbar {
    justify-content: center;
  }

  .tag-grid {
    grid-template-columns: 1fr;
  }
}
</style>
