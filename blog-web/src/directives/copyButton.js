import { showToast, showSuccess } from '@/utils/toast'

// 代码块复制按钮自定义指令
export const vCopyButton = {
  mounted(el) {
    // 等待内容渲染完成
    requestAnimationFrame(() => {
      const codeBlocks = el.querySelectorAll('pre')

      codeBlocks.forEach((pre) => {
        // 避免重复添加
        if (pre.querySelector('.copy-btn')) return

        // 设置 pre 为相对定位
        pre.style.position = 'relative'

        // 创建复制按钮
        const copyBtn = document.createElement('button')
        copyBtn.className = 'copy-btn'
        copyBtn.innerHTML = `
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
          </svg>
        `
        copyBtn.title = '复制代码'

        copyBtn.addEventListener('click', async () => {
          const code = pre.querySelector('code')?.textContent || ''
          try {
            await navigator.clipboard.writeText(code)
            copyBtn.innerHTML = `
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
              </svg>
            `
            copyBtn.classList.add('copied')
            copyBtn.title = '已复制'
            showSuccess('✓ 代码已复制到剪贴板')

            setTimeout(() => {
              copyBtn.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
                </svg>
              `
              copyBtn.classList.remove('copied')
              copyBtn.title = '复制代码'
            }, 2000)
          } catch (err) {
            showToast('复制失败')
          }
        })

        pre.appendChild(copyBtn)
      })
    })
  },

  updated(el) {
    // 内容更新时重新添加复制按钮
    const codeBlocks = el.querySelectorAll('pre')
    codeBlocks.forEach((pre) => {
      if (!pre.querySelector('.copy-btn')) {
        pre.style.position = 'relative'
        const copyBtn = document.createElement('button')
        copyBtn.className = 'copy-btn'
        copyBtn.innerHTML = `
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
          </svg>
        `
        copyBtn.title = '复制代码'

        copyBtn.addEventListener('click', async () => {
          const code = pre.querySelector('code')?.textContent || ''
          try {
            await navigator.clipboard.writeText(code)
            copyBtn.innerHTML = `
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16">
                <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
              </svg>
            `
            copyBtn.classList.add('copied')
            copyBtn.title = '已复制'
            showSuccess('✓ 代码已复制到剪贴板')

            setTimeout(() => {
              copyBtn.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" width="16" height="16">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15.666 3.888A2.25 2.25 0 0013.5 2.25h-3c-1.03 0-1.9.693-2.166 1.638m7.332 0c.055.194.084.4.084.612v0a.75.75 0 01-.75.75H9a.75.75 0 01-.75-.75v0c0-.212.03-.418.084-.612m7.332 0c.646.049 1.288.11 1.927.184 1.1.128 1.907 1.077 1.907 2.185V19.5a2.25 2.25 0 01-2.25 2.25H6.75A2.25 2.25 0 014.5 19.5V6.257c0-1.108.806-2.057 1.907-2.185a48.208 48.208 0 011.927-.184" />
                </svg>
              `
              copyBtn.classList.remove('copied')
              copyBtn.title = '复制代码'
            }, 2000)
          } catch (err) {
            showToast('复制失败')
          }
        })

        pre.appendChild(copyBtn)
      }
    })
  }
}
