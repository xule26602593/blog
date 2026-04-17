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
          <p class="brand-tagline">开启你的创作之旅</p>

          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M16.862 4.487l1.687-1.688a1.875 1.875 0 112.652 2.652L10.582 16.07a4.5 4.5 0 01-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 011.13-1.897l8.932-8.931zm0 0L19.5 7.125M18 14v4.75A2.25 2.25 0 0115.75 21H5.25A2.25 2.25 0 013 18.75V8.25A2.25 2.25 0 015.25 6H10" />
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">自由创作</span>
                <span class="feature-desc">记录每一个灵感瞬间</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">社区互动</span>
                <span class="feature-desc">与志同道合者交流</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">快速高效</span>
                <span class="feature-desc">简洁的编辑体验</span>
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
            <h2 class="auth-title">创建账号</h2>
            <p class="auth-subtitle">加入我们，开始写作之旅</p>
          </div>

          <form class="auth-form" @submit.prevent="handleRegister">
            <div class="form-row">
              <div class="form-group form-group-half">
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
                    v-model="form.username"
                    type="text"
                    class="form-input"
                    placeholder="字母数字下划线"
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

              <div class="form-group form-group-half">
                <label class="input-label" for="nickname">
                  <span>昵称</span>
                  <span class="required">*</span>
                </label>
                <div class="input-wrapper" :class="{ 'has-error': errors.nickname }">
                  <div class="input-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z" />
                    </svg>
                  </div>
                  <input
                    id="nickname"
                    v-model="form.nickname"
                    type="text"
                    class="form-input"
                    placeholder="显示名称"
                    autocomplete="nickname"
                    @blur="validateField('nickname')"
                    @input="clearError('nickname')"
                  />
                </div>
                <transition name="error-fade">
                  <span v-if="errors.nickname" class="error-text">
                    <svg viewBox="0 0 24 24" fill="currentColor" class="error-icon">
                      <path fill-rule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" clip-rule="evenodd" />
                    </svg>
                    {{ errors.nickname }}
                  </span>
                </transition>
              </div>
            </div>

            <div class="form-group">
              <label class="input-label" for="email">
                <span>邮箱</span>
                <span class="optional">（选填）</span>
              </label>
              <div class="input-wrapper" :class="{ 'has-error': errors.email }">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
                  </svg>
                </div>
                <input
                  id="email"
                  v-model="form.email"
                  type="email"
                  class="form-input"
                  placeholder="example@email.com"
                  autocomplete="email"
                  @blur="validateField('email')"
                  @input="clearError('email')"
                />
              </div>
              <transition name="error-fade">
                <span v-if="errors.email" class="error-text">
                  <svg viewBox="0 0 24 24" fill="currentColor" class="error-icon">
                    <path fill-rule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" clip-rule="evenodd" />
                  </svg>
                  {{ errors.email }}
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
                  placeholder="6-20位密码"
                  autocomplete="new-password"
                  @blur="validateField('password')"
                  @input="clearError('password')"
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
              <!-- 密码强度指示器 -->
              <div v-if="form.password" class="password-strength">
                <div class="strength-bars">
                  <span class="strength-bar" :class="{ active: passwordStrength >= 1 }"></span>
                  <span class="strength-bar" :class="{ active: passwordStrength >= 2 }"></span>
                  <span class="strength-bar" :class="{ active: passwordStrength >= 3 }"></span>
                  <span class="strength-bar" :class="{ active: passwordStrength >= 4 }"></span>
                </div>
                <span class="strength-text" :class="strengthClass">{{ strengthText }}</span>
              </div>
            </div>

            <div class="form-group">
              <label class="input-label" for="confirmPassword">
                <span>确认密码</span>
                <span class="required">*</span>
              </label>
              <div class="input-wrapper" :class="{ 'has-error': errors.confirmPassword, 'has-success': !errors.confirmPassword && form.confirmPassword && form.password === form.confirmPassword }">
                <div class="input-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z" />
                  </svg>
                </div>
                <input
                  id="confirmPassword"
                  v-model="form.confirmPassword"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  class="form-input"
                  placeholder="再次输入密码"
                  autocomplete="new-password"
                  @blur="validateField('confirmPassword')"
                  @input="clearError('confirmPassword')"
                  @keyup.enter="handleRegister"
                />
                <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword" :aria-label="showConfirmPassword ? '隐藏密码' : '显示密码'">
                  <svg v-if="showConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
                  </svg>
                  <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                    <path d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                </button>
                <div v-if="!errors.confirmPassword && form.confirmPassword && form.password === form.confirmPassword" class="input-success-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M4.5 12.75l6 6 9-13.5" />
                  </svg>
                </div>
              </div>
              <transition name="error-fade">
                <span v-if="errors.confirmPassword" class="error-text">
                  <svg viewBox="0 0 24 24" fill="currentColor" class="error-icon">
                    <path fill-rule="evenodd" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" clip-rule="evenodd" />
                  </svg>
                  {{ errors.confirmPassword }}
                </span>
              </transition>
            </div>

            <div class="auth-agreement">
              <label class="agreement-label">
                <input v-model="form.agreement" type="checkbox" />
                <span class="checkbox-custom"></span>
                <span class="agreement-text">
                  我已阅读并同意
                  <span class="link" @click.stop="showAgreement">《用户协议》</span>
                </span>
              </label>
            </div>

            <button
              type="submit"
              class="auth-btn"
              :class="{ 'is-loading': loading }"
              :disabled="loading || !form.agreement"
            >
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else class="btn-text">注册</span>
            </button>
          </form>

          <div class="auth-divider">
            <span>或</span>
          </div>

          <div class="auth-footer">
            <span class="footer-text">已有账号？</span>
            <router-link to="/login" class="footer-link">立即登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { register } from '@/api/auth'

const router = useRouter()

const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  agreement: false
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

// 密码强度计算
const passwordStrength = computed(() => {
  const pwd = form.password
  let strength = 0
  if (pwd.length >= 6) strength++
  if (pwd.length >= 10) strength++
  if (/[A-Z]/.test(pwd) && /[a-z]/.test(pwd)) strength++
  if (/\d/.test(pwd) && /[!@#$%^&*(),.?":{}|<>]/.test(pwd)) strength++
  return strength
})

const strengthText = computed(() => {
  const texts = ['', '弱', '一般', '较强', '强']
  return texts[passwordStrength.value]
})

const strengthClass = computed(() => {
  const classes = ['', 'weak', 'fair', 'good', 'strong']
  return classes[passwordStrength.value]
})

const validateField = (field) => {
  if (field === 'username') {
    if (!form.username) {
      errors.username = '请输入用户名'
    } else if (form.username.length < 2 || form.username.length > 20) {
      errors.username = '用户名长度为 2-20 个字符'
    } else if (!/^[a-zA-Z0-9_]+$/.test(form.username)) {
      errors.username = '用户名只能包含字母、数字和下划线'
    } else {
      errors.username = ''
    }
  }
  if (field === 'nickname') {
    if (!form.nickname) {
      errors.nickname = '请输入昵称'
    } else if (form.nickname.length < 2 || form.nickname.length > 20) {
      errors.nickname = '昵称长度为 2-20 个字符'
    } else {
      errors.nickname = ''
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
  if (field === 'confirmPassword') {
    if (!form.confirmPassword) {
      errors.confirmPassword = '请确认密码'
    } else if (form.confirmPassword !== form.password) {
      errors.confirmPassword = '两次输入的密码不一致'
    } else {
      errors.confirmPassword = ''
    }
  }
  if (field === 'email' && form.email) {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      errors.email = '邮箱格式不正确'
    } else {
      errors.email = ''
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
  validateField('nickname')
  validateField('password')
  validateField('confirmPassword')
  if (form.email) validateField('email')
  return !errors.username && !errors.password && !errors.confirmPassword && !errors.nickname && !errors.email
}

const showAgreement = () => {
  showToast('用户协议内容')
}

const handleRegister = async () => {
  if (loading.value) return

  if (!form.agreement) {
    showToast('请先阅读并同意用户协议')
    return
  }

  if (!validate()) return

  loading.value = true

  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      email: form.email || undefined
    })

    showToast({ type: 'success', message: '注册成功，请登录' })
    router.push('/login')
  } catch (error) {
    if (error?.message) {
      showToast(error.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
// ========================================
// 注册页面 - Apple-Style Design
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
    right: -100px;
    background: linear-gradient(135deg, rgba(88, 86, 214, 0.15), rgba(52, 199, 89, 0.1));
    animation-delay: 0s;
  }

  .circle-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    left: -50px;
    background: linear-gradient(135deg, rgba(0, 122, 255, 0.12), rgba(88, 86, 214, 0.08));
    animation-delay: -7s;
  }

  .circle-3 {
    width: 300px;
    height: 300px;
    top: 50%;
    right: 30%;
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
    max-width: 560px;
    background: var(--bg-secondary);
  }
}

.auth-card {
  width: 100%;
  max-width: 440px;
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
  gap: var(--space-4);
}

.form-row {
  display: flex;
  gap: var(--space-3);

  @media (max-width: 480px) {
    flex-direction: column;
    gap: var(--space-4);
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);

  &.form-group-half {
    flex: 1;
    min-width: 0; // 防止 flex 子项溢出
  }
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

  .optional {
    color: var(--text-tertiary);
    font-weight: var(--font-regular);
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

  &.has-success {
    border-color: var(--color-success);

    &:focus-within {
      box-shadow: 0 0 0 4px rgba(52, 199, 89, 0.15);
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

.input-wrapper.has-success .input-icon svg {
  color: var(--color-success);
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

.input-success-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 48px;
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
    color: var(--color-success);
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

// ========== 密码强度 ==========
.password-strength {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding-left: var(--space-1);
}

.strength-bars {
  display: flex;
  gap: 4px;
}

.strength-bar {
  width: 24px;
  height: 4px;
  background: var(--border-color);
  border-radius: 2px;
  transition: background var(--transition-fast);

  &.active {
    background: var(--color-error);
  }
}

.strength-bar:nth-child(2).active { background: var(--color-warning); }
.strength-bar:nth-child(3).active { background: var(--color-info); }
.strength-bar:nth-child(4).active { background: var(--color-success); }

.strength-text {
  font-size: var(--text-xs);
  color: var(--text-tertiary);

  &.weak { color: var(--color-error); }
  &.fair { color: var(--color-warning); }
  &.good { color: var(--color-info); }
  &.strong { color: var(--color-success); }
}

// ========== 协议 ==========
.auth-agreement {
  margin-top: var(--space-2);
}

.agreement-label {
  display: flex;
  align-items: flex-start;
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
    margin-top: 2px;

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

  .agreement-text {
    font-size: var(--text-sm);
    color: var(--text-secondary);
    line-height: 1.4;
  }

  .link {
    color: var(--color-primary);
    font-weight: var(--font-medium);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
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
    opacity: 0.6;
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

  .toggle-password,
  .input-success-icon {
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

  .agreement-label .checkbox-custom {
    border-color: var(--text-tertiary);
  }
}
</style>
