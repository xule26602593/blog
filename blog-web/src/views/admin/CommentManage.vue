<template>
  <div class="manage-page">
    <div class="toolbar">
      <van-dropdown-menu>
        <van-dropdown-item v-model="queryParams.status" :options="statusOptions" @change="handleStatusChange" />
      </van-dropdown-menu>
    </div>

    <div class="comment-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div
            v-for="comment in commentList"
            :key="comment.id"
            class="comment-card"
          >
            <div class="comment-header">
              <span class="comment-id">#{{ comment.id }}</span>
              <van-tag :type="getStatusType(comment.status)" size="medium">{{ getStatusText(comment.status) }}</van-tag>
            </div>
            <div class="comment-article">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z" />
              </svg>
              {{ comment.articleTitle || '未知文章' }}
            </div>
            <div class="comment-author">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
              </svg>
              {{ comment.nickname }}
            </div>
            <div class="comment-content">{{ comment.content }}</div>
            <div class="comment-footer">
              <div class="comment-time">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {{ comment.createTime }}
              </div>
              <div class="comment-actions">
                <template v-if="comment.status === 0">
                  <van-button type="success" size="small" plain @click="handleAudit(comment, 1)">通过</van-button>
                  <van-button type="warning" size="small" plain @click="handleAudit(comment, 2)">拒绝</van-button>
                </template>
                <van-button type="danger" size="small" plain @click="handleDelete(comment)">删除</van-button>
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
        @change="fetchComments"
      />
    </div>

    <!-- Delete Confirmation Dialog -->
    <van-dialog
      v-model:show="showDeleteDialog"
      title="确认删除"
      message="确定要删除该评论吗？"
      show-cancel-button
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast, showSuccess } from '@/utils/toast'
import { getAdminComments, auditComment, deleteComment } from '@/api/comment'
import { usePendingCount } from '@/composables/usePendingCount'

const { fetchPendingCount, decrementPendingCount } = usePendingCount()

const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const commentList = ref([])
const total = ref(0)
const showDeleteDialog = ref(false)
const commentToDelete = ref(null)

const queryParams = reactive({
  status: -1,
  pageNum: 1,
  pageSize: 10
})

const statusOptions = [
  { text: '全部', value: -1 },
  { text: '待审核', value: 0 },
  { text: '已通过', value: 1 },
  { text: '已拒绝', value: 2 }
]

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'success', 2: 'danger' }
  return types[status] || 'default'
}

const getStatusText = (status) => {
  const texts = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return texts[status] || '未知'
}

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getAdminComments(queryParams)
    commentList.value = res.data?.records || []
    total.value = res.data?.total || 0
    finished.value = commentList.value.length >= total.value
  } catch (error) {
    console.error('获取评论失败', error)
    showToast('获取评论失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  fetchComments()
}

const onRefresh = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchComments()
}

const handleStatusChange = () => {
  queryParams.pageNum = 1
  finished.value = false
  fetchComments()
}

const handleAudit = async (row, status) => {
  try {
    await auditComment(row.id, status)
    showSuccess(status === 1 ? '审核通过' : '已拒绝')
    decrementPendingCount()
    fetchComments()
  } catch (error) {
    console.error('审核失败', error)
    showToast('审核失败')
  }
}

const handleDelete = (row) => {
  commentToDelete.value = row
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  if (!commentToDelete.value) return
  try {
    await deleteComment(commentToDelete.value.id)
    showSuccess('删除成功')
    // 如果删除的是待审核评论，需要更新待审核数量
    if (commentToDelete.value.status === 0) {
      decrementPendingCount()
    }
    fetchComments()
  } catch (error) {
    console.error('删除失败', error)
    showToast('删除失败')
  } finally {
    commentToDelete.value = null
  }
}

onMounted(() => {
  fetchComments()
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
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.comment-list {
  min-height: 300px;
}

.comment-card {
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

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.comment-id {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-family: monospace;
}

.comment-article,
.comment-author {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);

  svg {
    width: 16px;
    height: 16px;
    color: var(--text-muted);
  }
}

.comment-content {
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: 1.6;
  margin-bottom: var(--space-3);
  padding: var(--space-3);
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
}

.comment-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.comment-time {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--text-tertiary);

  svg {
    width: 14px;
    height: 14px;
  }
}

.comment-actions {
  display: flex;
  gap: var(--space-2);
}

.pagination {
  display: flex;
  justify-content: center;
  padding: var(--space-4) 0;
}

@media (max-width: 768px) {
  .toolbar {
    justify-content: center;
  }

  .comment-footer {
    flex-direction: column;
    gap: var(--space-3);
    align-items: flex-start;
  }

  .comment-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
