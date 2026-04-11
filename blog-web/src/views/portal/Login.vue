<template>
  <div class="auth-page">
    <div class="auth-container">
      <!-- 左侧装饰 -->
      <div class="auth-decoration">
        <div class="decoration-content">
          <div class="brand-large">
            <span class="brand-mark">随</span>
            <span class="brand-name">随笔</span>
          </div>
          <p class="brand-tagline">记录想法，分享思考</p>
          <div class="decoration-features">
            <div class="feature">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
              </svg>
              <span>简洁的写作体验</span>
            </div>
            <div class="feature">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456zM16.894 20.567L16.5 21.75l-.394-1.183a2.25 2.25 0 00-1.423-1.423L13.5 18.75l1.183-.394a2.25 2.25 0 001.423-1.423l.394-1.183.394 1.183a2.25 2.25 0 001.423 1.423l1.183.394-1.183.394a2.25 2.25 0 00-1.423 1.423z" />
              </svg>
              <span>优雅的界面设计</span>
            </div>
            <div class="feature">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
              </svg>
              <span>专注于内容创作</span>
            </div>
          </div>
        </div>
        <div class="decoration-pattern"></div>
      </div>

      <!-- 右侧表单 -->
      <div class="auth-form-section">
        <div class="auth-card">
          <div class="auth-header">
            <h1 class="auth-title">欢迎回来</h1>
            <p class="auth-subtitle">登录您的账号继续</p>
          </div>

          <form class="auth-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="input-label">用户名</label>
              <input
                ref="usernameRef"
                v-model="form.username"
                type="text"
                class="form-input"
                placeholder="请输入用户名"
                @blur="validateField('username')"
              />
              <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
            </div>

            <div class="form-group">
              <label class="input-label">密码</label>
              <div class="password-input">
                <input
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="请输入密码"
                  @blur="validateField('password')"
                  @keyup.enter="handleLogin"
                />
                <button type="button" class="toggle-password" @click="showPassword = !showPassword">
                  <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </button>
              </div>
              <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
            </div>

            <div class="auth-options">
              <label class="remember-me">
                <input v-model="form.remember" type="checkbox" />
                <span>记住我</span>
              </label>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>

            <button
              type="submit"
              class="auth-btn"
              :disabled="loading"
            >
              <span v-if="loading" class="loading-spinner"></span>
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <div class="auth-footer">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const showPassword = ref(false)
const usernameRef = ref()

const form = reactive({
  username: '',
  password: '',
  remember: false
})

const errors = reactive({
  username: '',
  password: ''
})

const validateField = (field) => {
  if (field === 'username') {
    if (!form.username) {
      errors.username = '请输入用户名'
    } else if (form.username.length < 2 || form.username.length > 20) {
      errors.username = '用户名长度为 2-20 个字符'
    } else {
      errors.username = ''
    }
  }
  if (field === 'password') {
    if (!form.password) {
      errors.password = '请输入密码'
    } else if (form.password.length < 6 || form.password.length > 20) {
      errors.password = '密码长度为 6-20 个字符'
    } else {
      errors.password = ''
    }
  }
}

const validate = () => {
  validateField('username')
  validateField('password')
  return !errors.username && !errors.password
}

const handleLogin = async () => {
  if (loading.value) return
  if (!validate()) return

  loading.value = true

  try {
    await userStore.login({
      username: form.username,
      password: form.password
    })

    if (form.remember) {
      localStorage.setItem('rememberedUsername', form.username)
    } else {
      localStorage.removeItem('rememberedUsername')
    }

    showToast({ type: 'success', message: '登录成功' })
    router.push(route.query.redirect || '/')
  } catch (error) {
    if (error?.message) {
      showToast(error.message)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const rememberedUsername = localStorage.getItem('rememberedUsername')
  if (rememberedUsername) {
    form.username = rememberedUsername
    form.remember = true
  }
})
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  background: var(--bg-primary);
}

.auth-container {
  display: flex;
  width: 100%;
  min-height: 100vh;
}

// 左侧装饰区
.auth-decoration {
  display: none;
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  overflow: hidden;

  @media (min-width: 1024px) {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.decoration-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  padding: var(--space-10);
}

.brand-large {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.brand-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  font-size: 32px;
  font-weight: var(--font-bold);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-xl);
}

.brand-name {
  font-size: var(--text-3xl);
  font-weight: var(--font-bold);
}

.brand-tagline {
  font-size: var(--text-lg);
  opacity: 0.9;
  margin-bottom: var(--space-10);
}

.decoration-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  max-width: 280px;
  margin: 0 auto;
}

.feature {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);

  svg {
    width: 24px;
    height: 24px;
  }

  span {
    font-size: var(--text-sm);
  }
}

.decoration-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle at 1px 1px, rgba(255, 255, 255, 0.15) 1px, transparent 0);
  background-size: 40px 40px;
}

// 右侧表单区
.auth-form-section {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: var(--space-6);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: var(--space-10);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.6s var(--ease-out);
}

.auth-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.auth-title {
  font-family: var(--font-serif);
  font-size: var(--text-2xl);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-2);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.auth-subtitle {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.input-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
}

.form-input {
  height: 48px;
  padding: 0 var(--space-4);
  font-size: var(--text-base);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    background: var(--bg-card);
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(180, 83, 9, 0.15);
  }
}

html.dark .form-input:focus {
  box-shadow: 0 0 0 4px rgba(10, 132, 255, 0.2);
}

.password-input {
  position: relative;

  .form-input {
    width: 100%;
    padding-right: 48px;
  }

  .toggle-password {
    position: absolute;
    right: 12px;
    top: 50%;
    transform: translateY(-50%);
    padding: var(--space-1);
    background: none;
    border: none;
    color: var(--text-muted);
    cursor: pointer;
    transition: color var(--transition-fast);

    &:hover {
      color: var(--text-primary);
    }

    svg {
      width: 20px;
      height: 20px;
    }
  }
}

.error-text {
  font-size: var(--text-xs);
  color: var(--color-error, #ef4444);
}

.auth-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: var(--text-sm);
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  cursor: pointer;

  input {
    width: 16px;
    height: 16px;
    accent-color: var(--color-primary);
  }
}

.forgot-link {
  color: var(--color-primary);
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.auth-btn {
  height: 48px;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);

  &:hover:not(:disabled) {
    background: var(--color-accent);
    box-shadow: var(--shadow-lg);
  }

  &:active:not(:disabled) {
    transform: scale(0.98);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  margin-top: var(--space-8);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-color);
  text-align: center;
  font-size: var(--text-sm);
  color: var(--text-secondary);

  a {
    color: var(--color-primary);
    font-weight: var(--font-medium);
    text-decoration: none;
    margin-left: var(--space-1);

    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 480px) {
  .auth-card {
    padding: var(--space-6);
  }
}
</style>
