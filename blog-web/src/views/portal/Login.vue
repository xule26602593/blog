<template>
  <div class="auth-page">
    <!-- 背景装饰 -->
    <div class="auth-bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
      <div class="grid-pattern"></div>
    </div>

    <div class="auth-container">
      <!-- 左侧品牌区 - 桌面端显示 -->
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <span class="logo-icon">随</span>
          </div>
          <h1 class="brand-title">随笔</h1>
          <p class="brand-tagline">记录想法，分享思考</p>

          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">简洁写作</span>
                <span class="feature-desc">专注于内容创作</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.456 2.456L21.75 6l-1.035.259a3.375 3.375 0 00-2.456 2.456z" />
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">优雅设计</span>
                <span class="feature-desc">沉浸式阅读体验</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">知识社区</span>
                <span class="feature-desc">分享与交流思想</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="auth-form-section">
        <div class="auth-card">
          <!-- 移动端 Logo -->
          <div class="mobile-logo">
            <span class="logo-icon-small">随</span>
          </div>

          <div class="auth-header">
            <h2 class="auth-title">欢迎回来</h2>
            <p class="auth-subtitle">登录您的账号继续创作之旅</p>
          </div>

          <form class="auth-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="input-label" for="username">
                <span>用户名</span>
                <span class="required">*</span>
              </label>
              <div class="input-wrapper" :class="{ 'has-error': errors.username }">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                  </svg>
                </div>
                <input
                  id="username"
                  ref="usernameRef"
                  v-model="form.username"
                  type="text"
                  class="form-input"
                  placeholder="请输入用户名"
                  autocomplete="username"
                  @blur="validateField('username')"
                  @input="clearError('username')"
                />
              </div>
              <transition name="error-fade">
                <span v-if="errors.username" class="error-text">
                  <svg viewBox="0 0 24 24" fill="currentColor" class="error-icon">
                    <path fill-rule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" clip-rule="evenodd" />
                  </svg>
                  {{ errors.username }}
                </span>
              </transition>
            </div>

            <div class="form-group">
              <label class="input-label" for="password">
                <span>密码</span>
                <span class="required">*</span>
              </label>
              <div class="input-wrapper" :class="{ 'has-error': errors.password }">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
                  </svg>
                </div>
                <input
                  id="password"
                  v-model="form.password"
                  :type="showPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="请输入密码"
                  autocomplete="current-password"
                  @blur="validateField('password')"
                  @input="clearError('password')"
                  @keyup.enter="handleLogin"
                />
                <button type="button" class="toggle-password" @click="showPassword = !showPassword" :aria-label="showPassword ? '隐藏密码' : '显示密码'">
                  <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                    <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </button>
              </div>
              <transition name="error-fade">
                <span v-if="errors.password" class="error-text">
                  <svg viewBox="0 0 24 24" fill="currentColor" class="error-icon">
                    <path fill-rule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" clip-rule="evenodd" />
                  </svg>
                  {{ errors.password }}
                </span>
              </transition>
            </div>

            <div class="auth-options">
              <label class="remember-me">
                <input v-model="form.remember" type="checkbox" />
                <span class="checkbox-custom"></span>
                <span class="checkbox-label">记住我</span>
              </label>
              <a href="#" class="forgot-link" @click.prevent="handleForgotPassword">忘记密码？</a>
            </div>

            <button
              type="submit"
              class="auth-btn"
              :class="{ 'is-loading': loading }"
              :disabled="loading"
            >
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else class="btn-text">登录</span>
            </button>
          </form>

          <div class="auth-divider">
            <span>或</span>
          </div>

          <div class="auth-footer">
            <span class="footer-text">还没有账号？</span>
            <router-link to="/register" class="footer-link">立即注册</router-link>
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

const clearError = (field) => {
  if (errors[field]) {
    errors[field] = ''
  }
}

const validate = () => {
  validateField('username')
  validateField('password')
  return !errors.username && !errors.password
}

const handleForgotPassword = () => {
  showToast('请联系管理员重置密码')
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
// ========================================
// 登录页面 - Apple-Style Design
// ========================================

.auth-page {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: var(--bg-primary);
}

// ========== 背景装饰 ==========
.auth-bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;

  .circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    animation: float 20s ease-in-out infinite;
  }

  .circle-1 {
    width: 500px;
    height: 500px;
    top: -150px;
    left: -100px;
    background: linear-gradient(135deg, rgba(0, 122, 255, 0.15), rgba(88, 86, 214, 0.1));
    animation-delay: 0s;
  }

  .circle-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    right: -50px;
    background: linear-gradient(135deg, rgba(88, 86, 214, 0.12), rgba(0, 122, 255, 0.08));
    animation-delay: -7s;
  }

  .circle-3 {
    width: 300px;
    height: 300px;
    top: 40%;
    left: 30%;
    background: linear-gradient(135deg, rgba(52, 199, 89, 0.08), rgba(0, 122, 255, 0.06));
    animation-delay: -14s;
  }

  .grid-pattern {
    position: absolute;
    inset: 0;
    background-image:
      linear-gradient(rgba(0, 0, 0, 0.02) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 0, 0, 0.02) 1px, transparent 1px);
    background-size: 60px 60px;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-20px) rotate(2deg); }
  50% { transform: translateY(0) rotate(0deg); }
  75% { transform: translateY(20px) rotate(-2deg); }
}

html.dark .auth-bg-decoration {
  .circle {
    opacity: 0.6;
  }
  .grid-pattern {
    background-image:
      linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  }
}

// ========== 主容器 ==========
.auth-container {
  display: flex;
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

// ========== 左侧品牌区 ==========
.auth-brand {
  display: none;
  flex: 1;
  position: relative;
  padding: var(--space-16);
  overflow: hidden;

  @media (min-width: 1024px) {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.brand-content {
  position: relative;
  z-index: 2;
  text-align: center;
  max-width: 420px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  margin-bottom: var(--space-6);
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border-radius: var(--radius-2xl);
  box-shadow:
    0 20px 40px rgba(0, 122, 255, 0.3),
    0 0 0 1px rgba(255, 255, 255, 0.2) inset;
  animation: logoFloat 6s ease-in-out infinite;

  .logo-icon {
    font-size: 36px;
    font-weight: var(--font-bold);
    color: white;
    font-family: var(--font-serif);
  }
}

@keyframes logoFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.brand-title {
  font-family: var(--font-serif);
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
  margin: 0 0 var(--space-3);
  letter-spacing: var(--tracking-tight);
}

.brand-tagline {
  font-size: var(--text-lg);
  color: var(--text-secondary);
  margin: 0 0 var(--space-12);
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-secondary);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
    transform: translateX(4px);
  }
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--color-primary-light), transparent);
  border-radius: var(--radius-lg);
  flex-shrink: 0;

  svg {
    width: 22px;
    height: 22px;
    color: var(--color-primary);
  }
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-title {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
}

.feature-desc {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
}

// ========== 右侧表单区 ==========
.auth-form-section {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  flex: 1;
  padding: var(--space-6);
  padding-top: var(--space-8);
  min-height: 100vh;
  overflow-y: auto;

  @media (min-width: 1024px) {
    align-items: center;
    padding-top: var(--space-6);
    max-width: 520px;
    background: var(--bg-secondary);
  }
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: var(--space-8);
  background: var(--bg-card);
  border-radius: var(--radius-2xl);
  box-shadow: var(--shadow-lg);
  margin: auto 0;

  @media (min-width: 1024px) {
    padding: var(--space-10);
    box-shadow: var(--shadow-lg);
    border: 1px solid var(--border-color);
    margin: 0;
  }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

// ========== 移动端 Logo ==========
.mobile-logo {
  display: flex;
  justify-content: center;
  margin-bottom: var(--space-5);

  @media (min-width: 1024px) {
    display: none;
  }

  .logo-icon-small {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    font-size: 22px;
    font-weight: var(--font-bold);
    color: white;
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
    border-radius: var(--radius-xl);
    font-family: var(--font-serif);
    box-shadow: 0 6px 20px rgba(0, 122, 255, 0.25);
  }
}

// ========== 头部 ==========
.auth-header {
  text-align: center;
  margin-bottom: var(--space-6);

  @media (min-width: 768px) {
    margin-bottom: var(--space-8);
  }
}

.auth-title {
  font-family: var(--font-serif);
  font-size: var(--text-xl);
  font-weight: var(--font-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--space-1);
  letter-spacing: var(--tracking-tight);

  @media (min-width: 768px) {
    font-size: var(--text-2xl);
    margin-bottom: var(--space-2);
  }
}

.auth-subtitle {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin: 0;
}

// ========== 表单 ==========
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
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);

  .required {
    color: var(--color-error);
  }
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: var(--bg-secondary);
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
  overflow: hidden;

  &:hover:not(:focus-within) {
    border-color: var(--text-muted);
  }

  &:focus-within {
    border-color: var(--color-primary);
    background: var(--bg-card);
    box-shadow: 0 0 0 4px var(--color-primary-light);
  }

  &.has-error {
    border-color: var(--color-error);

    &:focus-within {
      box-shadow: 0 0 0 4px rgba(255, 59, 48, 0.15);
    }
  }
}

.input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 48px;
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
    color: var(--text-tertiary);
    transition: color var(--transition-fast);
  }
}

.input-wrapper:focus-within .input-icon svg {
  color: var(--color-primary);
}

.form-input {
  flex: 1;
  height: 48px;
  padding: 0 var(--space-4);
  padding-left: 0;
  font-size: var(--text-base);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: transparent;
  border: none;
  outline: none;

  &::placeholder {
    color: var(--text-muted);
  }
}

.password-input {
  position: relative;
}

.toggle-password {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 48px;
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color var(--transition-fast);
  flex-shrink: 0;

  &:hover {
    color: var(--text-primary);
  }

  svg {
    width: 20px;
    height: 20px;
  }
}

// ========== 错误提示 ==========
.error-text {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--color-error);
  padding-left: var(--space-1);

  .error-icon {
    width: 14px;
    height: 14px;
    flex-shrink: 0;
  }
}

.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.2s ease;
}

.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

// ========== 选项行 ==========
.auth-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  user-select: none;

  input {
    display: none;
  }

  .checkbox-custom {
    width: 18px;
    height: 18px;
    border: 1.5px solid var(--text-muted);
    border-radius: 5px;
    transition: all var(--transition-fast);
    position: relative;
    flex-shrink: 0;

    &::after {
      content: '';
      position: absolute;
      inset: 3px;
      background: var(--color-primary);
      border-radius: 2px;
      opacity: 0;
      transform: scale(0);
      transition: all var(--transition-fast);
    }
  }

  input:checked + .checkbox-custom {
    border-color: var(--color-primary);

    &::after {
      opacity: 1;
      transform: scale(1);
    }
  }

  .checkbox-label {
    font-size: var(--text-sm);
    color: var(--text-secondary);
  }
}

.forgot-link {
  font-size: var(--text-sm);
  color: var(--color-primary);
  text-decoration: none;
  font-weight: var(--font-medium);
  transition: opacity var(--transition-fast);

  &:hover {
    opacity: 0.8;
  }
}

// ========== 按钮 ==========
.auth-btn {
  width: 100%;
  height: 48px;
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  color: white;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  position: relative;
  overflow: hidden;
  margin-top: var(--space-2);

  @media (min-width: 768px) {
    height: 52px;
  }

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.2) 0%, transparent 50%);
    opacity: 0;
    transition: opacity var(--transition-fast);
  }

  &:hover:not(:disabled)::before {
    opacity: 1;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow:
      0 8px 24px rgba(0, 122, 255, 0.35),
      0 4px 12px rgba(88, 86, 214, 0.2);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
    box-shadow: 0 4px 12px rgba(0, 122, 255, 0.25);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &.is-loading {
    pointer-events: none;
  }
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// ========== 分割线 ==========
.auth-divider {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin: var(--space-6) 0;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border-color);
  }

  span {
    font-size: var(--text-sm);
    color: var(--text-tertiary);
  }
}

// ========== 底部 ==========
.auth-footer {
  text-align: center;
}

.footer-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.footer-link {
  font-size: var(--text-sm);
  color: var(--color-primary);
  font-weight: var(--font-semibold);
  text-decoration: none;
  margin-left: var(--space-1);
  transition: opacity var(--transition-fast);

  &:hover {
    opacity: 0.8;
  }
}

// ========== 响应式 ==========
@media (max-width: 480px) {
  .auth-page {
    // 移动端不固定高度，允许滚动
    min-height: auto;
  }

  .auth-card {
    padding: var(--space-5);
  }

  // 移动端输入框高度调整
  .input-wrapper {
    height: 44px;
  }

  .input-icon {
    width: 40px;
    height: 44px;
  }

  .form-input {
    height: 44px;
    font-size: 16px; // 防止 iOS 自动缩放
  }

  .toggle-password {
    width: 40px;
    height: 44px;
  }
}

// ========== 暗色主题 ==========
html.dark {
  .auth-brand {
    background: transparent;
  }

  .brand-features .feature-item {
    background: var(--bg-tertiary);
    border-color: var(--border-color);
  }

  .auth-form-section {
    @media (min-width: 1024px) {
      background: var(--bg-secondary);
    }
  }

  .auth-card {
    background: var(--bg-card);
    border-color: var(--border-color);
  }

  .input-wrapper {
    background: var(--bg-tertiary);
    border-color: var(--border-color);

    &:hover:not(:focus-within) {
      border-color: var(--text-tertiary);
    }
  }

  .remember-me .checkbox-custom {
    border-color: var(--text-tertiary);
  }
}
</style>
