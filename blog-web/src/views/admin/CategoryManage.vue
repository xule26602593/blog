<template>
  <div class="manage-page">
    <div class="toolbar">
      <van-button type="primary" size="small" @click="handleAdd">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="btn-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
        新增分类
      </van-button>
    </div>

    <div class="category-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            v-for="category in categoryList"
            :key="category.id"
            class="category-card"
          >
            <div class="category-header">
              <span class="category-id">#{{ category.id }}</span>
              <van-tag :type="category.status === 1 ? 'success' : 'danger'" size="medium">
                {{ category.status === 1 ? '启用' : '禁用' }}
              </van-tag>
            </div>
            <div class="category-body">
              <h3 class="category-name">{{ category.name }}</h3>
              <p class="category-desc">{{ category.description || '暂无描述' }}</p>
            </div>
            <div class="category-meta">
              <div class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 7.5L7.5 3m0 0L12 7.5M7.5 3v13.5m13.5 0L16.5 21m0 0L12 16.5m4.5 4.5V7.5" />
                </svg>
                排序: {{ category.sort }}
              </div>
              <div class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
                </svg>
                文章: {{ category.articleCount || 0 }}
              </div>
            </div>
            <div class="category-actions">
              <van-button type="primary" size="small" plain @click="handleEdit(category)">编辑</van-button>
              <van-button type="danger" size="small" plain @click="handleDelete(category)">删除</van-button>
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
        @change="fetchCategories"
      />
    </div>

    <!-- Edit Dialog -->
    <van-popup v-model:show="dialogVisible" position="bottom" round style="height: auto; max-height: 80vh;">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3 class="dialog-title">{{ isEdit ? '编辑分类' : '新增分类' }}</h3>
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
            placeholder="请输入分类名称"
            :rules="[{ required: true, message: '请输入分类名称' }]"
          />
          <van-field
            v-model="form.description"
            label="描述"
            type="textarea"
            rows="3"
            autosize
            placeholder="请输入描述"
          />
          <van-field v-model="form.sort" label="排序" type="digit" placeholder="请输入排序值" />
          <van-field name="radio" label="状态">
            <template #input>
              <van-radio-group v-model="form.status" direction="horizontal">
                <van-radio :name="1">启用</van-radio>
                <van-radio :name="0">禁用</van-radio>
              </van-radio-group>
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
      message="确定要删除该分类吗？"
      show-cancel-button
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast, showSuccess } from '@/utils/toast'
import { getAdminCategories, saveCategory, deleteCategory } from '@/api/category'

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const categoryList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const showDeleteDialog = ref(false)
const categoryToDelete = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  sort: 0,
  status: 1
})

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getAdminCategories(queryParams)
    categoryList.value = res.data?.records || []
    total.value = res.data?.total || 0
    finished.value = categoryList.value.length >= total.value
  } catch (error) {
    console.error('获取分类失败', error)
    showToast('获取分类失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  fetchCategories()
}

const onRefresh = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchCategories()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.description = ''
  form.sort = 0
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  form.description = row.description
  form.sort = row.sort
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await saveCategory(form)
    showSuccess(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    fetchCategories()
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  }
}

const handleDelete = (row) => {
  categoryToDelete.value = row
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!categoryToDelete.value) return
  try {
    await deleteCategory(categoryToDelete.value.id)
    showSuccess('删除成功')
    fetchCategories()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  } finally {
    categoryToDelete.value = null
  }
}

onMounted(() => {
  fetchCategories()
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

.category-list {
  min-height: 300px;
}

.category-card {
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  margin-bottom: var(--space-3);
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-sm);
  }
}

.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.category-id {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-family: monospace;
}

.category-body {
  margin-bottom: var(--space-3);
}

.category-name {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.category-desc {
  font-size: var(--text-sm);
  color: var(--text-muted);
  line-height: 1.5;
}

.category-meta {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--text-secondary);

  svg {
    width: 16px;
    height: 16px;
  }
}

.category-actions {
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

  .category-meta {
    flex-wrap: wrap;
  }
}
</style>
