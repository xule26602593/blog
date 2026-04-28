import { ref, computed } from 'vue'

/**
 * 流式响应状态机
 * 状态: idle | loading | streaming | paused | complete | error | cancelled | retrying
 */
export function useStreamState() {
  const state = ref('idle')
  const content = ref('')
  const errorMessage = ref('')

  // 状态判断
  const isIdle = computed(() => state.value === 'idle')
  const isLoading = computed(() => state.value === 'loading' || state.value === 'retrying')
  const isStreaming = computed(() => state.value === 'streaming')
  const isPaused = computed(() => state.value === 'paused')
  const isComplete = computed(() => state.value === 'complete')
  const isError = computed(() => state.value === 'error')
  const isCancelled = computed(() => state.value === 'cancelled')
  const canRetry = computed(() => ['error', 'cancelled', 'complete'].includes(state.value))
  const canCancel = computed(() => ['loading', 'streaming', 'paused', 'retrying'].includes(state.value))
  const canPause = computed(() => state.value === 'streaming')
  const canContinue = computed(() => state.value === 'paused')

  // 状态转换方法
  const start = () => {
    if (isIdle.value || canRetry.value) {
      state.value = 'loading'
      content.value = ''
      errorMessage.value = ''
    }
  }

  const firstByte = () => {
    if (isLoading.value) {
      state.value = 'streaming'
    }
  }

  const append = (chunk) => {
    if (isStreaming.value) {
      content.value += chunk
    }
  }

  const complete = () => {
    if (isStreaming.value) {
      state.value = 'complete'
    }
  }

  const error = (msg) => {
    state.value = 'error'
    errorMessage.value = msg
  }

  const cancel = () => {
    if (canCancel.value) {
      state.value = 'cancelled'
    }
  }

  const pause = () => {
    if (canPause.value) {
      state.value = 'paused'
    }
  }

  const continue_ = () => {
    if (canContinue.value) {
      state.value = 'streaming'
    }
  }

  const retry = () => {
    if (canRetry.value) {
      state.value = 'retrying'
      content.value = ''
      errorMessage.value = ''
    }
  }

  const reset = () => {
    state.value = 'idle'
    content.value = ''
    errorMessage.value = ''
  }

  return {
    state,
    content,
    errorMessage,
    // 状态判断
    isIdle,
    isLoading,
    isStreaming,
    isPaused,
    isComplete,
    isError,
    isCancelled,
    canRetry,
    canCancel,
    canPause,
    canContinue,
    // 状态转换
    start,
    firstByte,
    append,
    complete,
    error,
    cancel,
    pause,
    continue: continue_,
    retry,
    reset
  }
}
