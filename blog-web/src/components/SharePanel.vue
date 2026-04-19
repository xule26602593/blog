<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    round
    :style="{ padding: '20px' }"
  >
    <div class="share-panel">
      <h3 class="panel-title">分享文章</h3>

      <div class="share-options">
        <button class="share-option" @click="copyLink">
          <div class="option-icon copy">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13.19 8.688a4.5 4.5 0 011.242 7.244l-4.5 4.5a4.5 4.5 0 01-6.364-6.364l1.757-1.757m13.35-.622l1.757-1.757a4.5 4.5 0 00-6.364-6.364l-4.5 4.5a4.5 4.5 0 001.242 7.244" />
            </svg>
          </div>
          <span>复制链接</span>
        </button>

        <button class="share-option" @click="showQRCode">
          <div class="option-icon qrcode">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 013.75 9.375v-4.5zM3.75 14.625c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5a1.125 1.125 0 01-1.125-1.125v-4.5zM13.5 4.875c0-.621.504-1.125 1.125-1.125h4.5c.621 0 1.125.504 1.125 1.125v4.5c0 .621-.504 1.125-1.125 1.125h-4.5A1.125 1.125 0 0113.5 9.375v-4.5z" />
              <path stroke-linecap="round" stroke-linejoin="round" d="M6.75 6.75h.75v.75h-.75v-.75zM6.75 16.5h.75v.75h-.75v-.75zM16.5 6.75h.75v.75h-.75v-.75zM13.5 13.5h.75v.75h-.75v-.75zM13.5 19.5h.75v.75h-.75v-.75zM19.5 13.5h.75v.75h-.75v-.75zM19.5 19.5h.75v.75h-.75v-.75zM16.5 16.5h.75v.75h-.75v-.75z" />
            </svg>
          </div>
          <span>二维码</span>
        </button>

        <button class="share-option" @click="shareToWeibo">
          <div class="option-icon weibo">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10.098 20c-4.612 0-8.348-2.725-8.348-6.084 0-1.755.88-3.686 2.398-5.427 2.023-2.322 4.976-3.697 7.113-3.697.88 0 1.624.244 2.159.707l.066.058.048.043c.294.26.474.427.71.688.39.429.693.895.927 1.45.657-.196 1.407-.297 2.237-.297 2.397 0 4.091.97 4.091 2.334 0 .465-.177.897-.49 1.252l-.035.04-.037.04c-.1.109-.21.208-.33.307.14.092.27.19.39.295.52.444.82 1.023.82 1.665 0 1.78-2.01 3.098-4.908 3.098-.95 0-1.82-.149-2.594-.43C13.172 18.69 11.742 20 10.098 20zm1.172-12.68c-1.552 0-3.922 1.143-5.565 3.03-1.234 1.416-1.935 2.96-1.935 4.252 0 2.206 2.842 4.084 6.328 4.084 1.206 0 2.287-.474 3.124-1.376l.027-.03.028-.028c.25-.247.465-.527.655-.852.51.12 1.054.18 1.628.18 2.242 0 3.908-.88 3.908-2.07 0-.328-.166-.64-.47-.896-.306-.26-.73-.467-1.246-.617l-.785-.228.638-.512c.28-.224.478-.436.61-.655.106-.177.158-.346.158-.512 0-.71-1.148-1.334-2.591-1.334-.7 0-1.338.093-1.886.27l-.534.173-.164-.535c-.18-.587-.448-1.085-.82-1.493-.184-.198-.34-.34-.603-.574l-.066-.058c-.277-.24-.678-.36-1.141-.36zm6.594-.865c-.548-.137-.95-.348-1.186-.626-.237-.28-.323-.63-.256-1.038.14-.855.98-1.593 2.06-2.006 1.08-.412 2.26-.412 3.076.017.39.205.67.49.82.835.15.345.18.75.09 1.188-.18.87-.93 1.573-1.94 1.875-.5.15-1.03.2-1.58.15-.39-.03-.77-.13-1.08-.395h-.004zm1.675-2.61c-.37-.092-.81-.058-1.22.09-.52.19-.9.52-.97.85-.03.14-.01.26.05.36.07.11.19.19.35.24.37.1.82.02 1.26-.21.43-.23.73-.55.8-.87.02-.11.01-.22-.02-.32-.03-.1-.11-.18-.25-.14z"/>
            </svg>
          </div>
          <span>微博</span>
        </button>
      </div>

      <button class="cancel-btn" @click="visible = false">取消</button>
    </div>
  </van-popup>

  <!-- 二维码弹窗 -->
  <van-popup
    v-model:show="showQRPopup"
    round
    :style="{ padding: '24px' }"
  >
    <div class="qrcode-popup">
      <h4 class="qrcode-title">{{ title }}</h4>
      <div ref="qrcodeRef" class="qrcode-container"></div>
      <p class="qrcode-hint">微信扫码阅读文章</p>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, watch } from 'vue'
import { showToast } from 'vant'
import qrcode from 'qrcode-generator'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  url: {
    type: String,
    required: true
  },
  title: {
    type: String,
    default: '分享文章'
  }
})

const emit = defineEmits(['update:show'])

const visible = ref(false)
const showQRPopup = ref(false)
const qrcodeRef = ref(null)

watch(() => props.show, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:show', val)
})

const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(props.url)
    showToast({ type: 'success', message: '链接已复制' })
    visible.value = false
  } catch (error) {
    showToast('复制失败，请手动复制')
  }
}

const showQRCode = () => {
  visible.value = false
  showQRPopup.value = true

  setTimeout(() => {
    generateQRCode()
  }, 100)
}

const generateQRCode = () => {
  if (!qrcodeRef.value) return

  qrcodeRef.value.innerHTML = ''

  const qr = qrcode(0, 'M')
  qr.addData(props.url)
  qr.make()

  const size = 180
  const cellSize = size / qr.getModuleCount()

  const canvas = document.createElement('canvas')
  canvas.width = size
  canvas.height = size
  const ctx = canvas.getContext('2d')

  for (let row = 0; row < qr.getModuleCount(); row++) {
    for (let col = 0; col < qr.getModuleCount(); col++) {
      ctx.fillStyle = qr.isDark(row, col) ? '#1a1a1a' : '#ffffff'
      ctx.fillRect(col * cellSize, row * cellSize, cellSize, cellSize)
    }
  }

  qrcodeRef.value.appendChild(canvas)
}

const shareToWeibo = () => {
  const shareUrl = `https://service.weibo.com/share/share.php?url=${encodeURIComponent(props.url)}&title=${encodeURIComponent(props.title)}`
  window.open(shareUrl, '_blank')
  visible.value = false
}
</script>

<style lang="scss" scoped>
.share-panel {
  text-align: center;
}

.panel-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-6);
}

.share-options {
  display: flex;
  justify-content: space-around;
  margin-bottom: var(--space-6);
}

.share-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: transform var(--transition-fast);

  &:hover {
    transform: scale(1.05);
  }

  span {
    font-size: var(--text-sm);
    color: var(--text-secondary);
  }
}

.option-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-xl);
  transition: all var(--transition-fast);

  svg {
    width: 28px;
    height: 28px;
  }

  &.copy {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }

  &.qrcode {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    color: white;
  }

  &.weibo {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    color: white;
  }
}

.cancel-btn {
  width: 100%;
  height: 48px;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.qrcode-popup {
  text-align: center;
}

.qrcode-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 200px;
}

.qrcode-container {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-3);

  canvas {
    border-radius: var(--radius-md);
  }
}

.qrcode-hint {
  font-size: var(--text-xs);
  color: var(--text-muted);
}
</style>
