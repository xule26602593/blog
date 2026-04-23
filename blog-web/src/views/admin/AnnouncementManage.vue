<template>
  <div class="manage-page">
    <div class="toolbar">
      <van-button type="primary" size="small" @click="handleAdd">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="btn-icon">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
        </svg>
        新增公告
      </van-button>
    </div>

    <div class="announcement-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            v-for="announcement in announcementList"
            :key="announcement.id"
            class="announcement-card"
          >
            <div class="announcement-header">
              <span class="announcement-id">#{{ announcement.id }}</span>
              <van-tag :type="announcement.status === 1 ? 'success' : 'warning'" size="medium">
                {{ announcement.status === 1 ? '已发布' : '草稿' }}
              </van-tag>
            </div>
            <div class="announcement-body">
              <h3 class="announcement-title">{{ announcement.title }}</h3>
              <p class="announcement-content">{{ truncateContent(announcement.content) }}</p>
            </div>
            <div class="announcement-meta">
              <div class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                创建: {{ formatDate(announcement.createTime) }}
              </div>
              <div v-if="announcement.publishTime" class="meta-item">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                发布: {{ formatDate(announcement.publishTime) }}
              </div>
            </div>
            <div class="announcement-actions">
              <van-button
                v-if="announcement.status === 0"
                type="success"
                size="small"
                plain
                @click="handlePublish(announcement)"
              >
                发布
              </van-button>
              <van-button type="primary" size="small" plain @click="handleEdit(announcement)">编辑</van-button>
              <van-button type="danger" size="small" plain @click="handleDelete(announcement)">删除</van-button>
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
        @change="fetchAnnouncements"
      />
    </div>

    <!-- Edit Dialog -->
    <van-popup v-model:show="dialogVisible" position="bottom" round style="height: auto; max-height: 80vh;">
      <div class="dialog-content">
        <div class="dialog-header">
          <h3 class="dialog-title">{{ isEdit ? '编辑公告' : '新增公告' }}</h3>
          <button class="dialog-close" @click="dialogVisible = false">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        <van-form @submit="handleSubmit" class="dialog-form">
          <van-field
            v-model="form.title"
            label="标题"
            placeholder="请输入公告标题"
            :rules="[{ required: true, message: '请输入公告标题' }]"
          />
          <van-field
            v-model="form.content"
            label="内容"
            type="textarea"
            rows="5"
            autosize
            placeholder="请输入公告内容"
            :rules="[{ required: true, message: '请输入公告内容' }]"
          />
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
      message="确定要删除该公告吗？"
      show-cancel-button
      @confirm="confirmDelete"
    />

    <!-- Publish Confirmation Dialog -->
    <van-dialog
      v-model:show="showPublishDialog"
      title="确认发布"
      message="发布后将通知所有用户，确定要发布吗？"
      show-cancel-button
      @confirm="confirmPublish"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import {
  getAnnouncements,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
  publishAnnouncement
} from '@/api/announcement'

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const announcementList = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const showDeleteDialog = ref(false)
const showPublishDialog = ref(false)
const announcementToDelete = ref(null)
const announcementToPublish = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  id: null,
  title: '',
  content: ''
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const truncateContent = (content) => {
  if (!content) return '暂无内容'
  const maxLength = 100
  if (content.length > maxLength) {
    return content.substring(0, maxLength) + '...'
  }
  return content
}

const fetchAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncements()
    announcementList.value = res.data?.records || res.data || []
    total.value = res.data?.total || announcementList.value.length
    finished.value = true
  } catch (error) {
    console.error('获取公告失败', error)
    showToast('获取公告失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  fetchAnnouncements()
}

const onRefresh = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchAnnouncements()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.title = ''
  form.content = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.title = row.title
  form.content = row.content
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await updateAnnouncement(form.id, form)
    } else {
      await createAnnouncement(form)
    }
    showToast({ type: 'success', message: isEdit.value ? '更新成功' : '新增成功' })
    dialogVisible.value = false
    fetchAnnouncements()
  } catch (error) {
    console.error('保存失败', error)
    showToast('保存失败')
  }
}

const handleDelete = (row) => {
  announcementToDelete.value = row
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!announcementToDelete.value) return
  try {
    await deleteAnnouncement(announcementToDelete.value.id)
    showToast({ type: 'success', message: '删除成功' })
    fetchAnnouncements()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  } finally {
    announcementToDelete.value = null
  }
}

const handlePublish = (row) => {
  announcementToPublish.value = row
  showPublishDialog.value = true
}

const confirmPublish = async () => {
  if (!announcementToPublish.value) return
  try {
    await publishAnnouncement(announcementToPublish.value.id)
    showToast({ type: 'success', message: '发布成功，已通知所有用户' })
    fetchAnnouncements()
  } catch (error) {
    console.error('发布失败', error)
    showToast('发布失败')
  } finally {
    announcementToPublish.value = null
  }
}

onMounted(() => {
  fetchAnnouncements()
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

.announcement-list {
  min-height: 300px;
}

.announcement-card {
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

.announcement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.announcement-id {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-family: monospace;
}

.announcement-body {
  margin-bottom: var(--space-3);
}

.announcement-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.announcement-content {
  font-size: var(--text-sm);
  color: var(--text-muted);
  line-height: 1.6;
  word-break: break-all;
}

.announcement-meta {
  display: flex;
  flex-wrap: wrap;
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
    flex-shrink: 0;
  }
}

.announcement-actions {
  display: flex;
  gap: var(--space-2);
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);

  :deep(.van-button__text) {
    display: flex;
    align-items: center;
    justify-content: center;
  }
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

  .announcement-actions {
    flex-wrap: wrap;
  }
}
</style>
