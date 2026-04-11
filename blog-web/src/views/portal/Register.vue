<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <h1 class="auth-title">创建账号</h1>
        <p class="auth-subtitle">开始您的写作之旅</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <input
            v-model="form.username"
            type="text"
            class="form-input"
            placeholder="用户名"
            @blur="validateField('username')"
          />
          <span v-if="errors.username" class="error-text">{{ errors.username }}</span>
        </div>

        <div class="form-group">
          <input
            v-model="form.nickname"
            type="text"
            class="form-input"
            placeholder="昵称"
            @blur="validateField('nickname')"
          />
          <span v-if="errors.nickname" class="error-text">{{ errors.nickname }}</span>
        </div>

        <div class="form-group">
          <input
            v-model="form.email"
            type="email"
            class="form-input"
            placeholder="邮箱（选填）"
          />
          <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
        </div>

        <div class="form-group">
          <div class="password-input">
            <input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="密码"
              @blur="validateField('password')"
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

        <div class="form-group">
          <div class="password-input">
            <input
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
              placeholder="确认密码"
              @blur="validateField('confirmPassword')"
              @keyup.enter="handleRegister"
            />
            <button type="button" class="toggle-password" @click="showConfirmPassword = !showConfirmPassword">
              <svg v-if="showConfirmPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
          <span v-if="errors.confirmPassword" class="error-text">{{ errors.confirmPassword }}</span>
        </div>

        <div class="auth-agreement">
          <label class="agreement-label">
            <input v-model="form.agreement" type="checkbox" />
            <span>
              我已阅读并同意
              <span class="link" @click.stop="showAgreement">用户协议</span>
            </span>
          </label>
        </div>

        <button
          type="submit"
          class="auth-btn"
          :disabled="loading || !form.agreement"
        >
          <span v-if="loading" class="loading-spinner"></span>
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="auth-footer">
        <span>已有账号？</span>
        <router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
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
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
  background: var(--bg-primary);
  position: relative;
  overflow: hidden;

  // 装饰背景
  &::before {
    content: '';
    position: absolute;
    top: -20%;
    right: -10%;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(180, 83, 9, 0.08) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -15%;
    left: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(180, 83, 9, 0.05) 0%, transparent 70%);
    border-radius: 50%;
    pointer-events: none;
  }
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
  position: relative;
  overflow: hidden;
  animation: slideUp 0.6s var(--ease-out);

  // 渐变边框效果
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    padding: 1px;
    background: linear-gradient(135deg, rgba(180, 83, 9, 0.2), transparent, rgba(180, 83, 9, 0.1));
    border-radius: inherit;
    mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
    mask-composite: xor;
    -webkit-mask-composite: xor;
    pointer-events: none;
  }
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
  gap: var(--space-4);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.form-input {
  height: 44px;
  padding: 0 var(--space-4);
  font-size: var(--text-sm);
  font-family: var(--font-sans);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  outline: none;
  transition: all var(--transition-fast);
  width: 100%;

  &::placeholder {
    color: var(--text-muted);
  }

  &:focus {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.15), var(--shadow-glow);
  }
}

.password-input {
  position: relative;

  .form-input {
    padding-right: 44px;
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
      width: 18px;
      height: 18px;
    }
  }
}

.error-text {
  font-size: var(--text-xs);
  color: var(--color-error, #ef4444);
}

.auth-agreement {
  margin-bottom: var(--space-2);

  .agreement-label {
    display: flex;
    align-items: center;
    gap: var(--space-2);
    font-size: var(--text-sm);
    color: var(--text-secondary);
    cursor: pointer;

    input {
      width: 16px;
      height: 16px;
      accent-color: var(--color-primary);
    }
  }

  .link {
    color: var(--text-primary);
    font-weight: var(--font-medium);
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }
}

.auth-btn {
  width: 100%;
  height: 44px;
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
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
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
    transition: left 0.5s ease;
  }

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg), var(--shadow-glow);

    &::before {
      left: 100%;
    }
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  text-align: center;
  margin-top: var(--space-6);
  padding-top: var(--space-6);
  border-top: 1px solid var(--border-light);
  font-size: var(--text-sm);
  color: var(--text-secondary);

  a {
    color: var(--color-primary);
    font-weight: var(--font-medium);
    margin-left: var(--space-1);
    transition: color var(--transition-fast);

    &:hover {
      color: var(--color-accent);
    }
  }
}

@media (max-width: 480px) {
  .auth-card {
    padding: var(--space-6);
  }
}
</style>
