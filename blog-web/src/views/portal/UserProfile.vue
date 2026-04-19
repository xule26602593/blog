<template>
  <div class="panels">
    <section class="panel">
      <h2 class="panel-title">个人资料</h2>
      <div class="form">
        <div class="form-group">
          <label class="form-label">用户名</label>
          <input :value="userStore.userInfo?.username" disabled class="form-input disabled" />
        </div>
        <div class="form-group">
          <label class="form-label">昵称</label>
          <input v-model="form.nickname" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">邮箱</label>
          <input v-model="form.email" type="email" class="form-input" />
        </div>
        <div class="form-group">
          <label class="form-label">头像</label>
          <div class="avatar-upload">
            <input
              type="file"
              ref="avatarInput"
              accept="image/*"
              @change="handleFileChange"
              hidden
            />
            <div class="avatar-preview" @click="avatarInput?.click()">
              <div v-if="form.avatar" class="avatar-image" :style="{ backgroundImage: `url(${form.avatar})` }"></div>
              <div v-else class="avatar-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                </svg>
              </div>
            </div>
            <div class="upload-info">
              <p class="upload-title">上传头像</p>
              <p class="upload-desc">支持常见图片格式</p>
            </div>
          </div>
        </div>
        <button class="save-btn" @click="handleUpdate">保存修改</button>
      </div>
    </section>

    <section class="panel">
      <h2 class="panel-title">修改密码</h2>
      <div class="form">
        <div class="form-group">
          <label class="form-label">原密码</label>
          <div class="password-input">
            <input
              v-model="passwordForm.oldPassword"
              :type="showOldPassword ? 'text' : 'password'"
              class="form-input"
            />
            <button type="button" class="toggle-password" @click="showOldPassword = !showOldPassword">
              <svg v-if="showOldPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">新密码</label>
          <div class="password-input">
            <input
              v-model="passwordForm.newPassword"
              :type="showNewPassword ? 'text' : 'password'"
              class="form-input"
            />
            <button type="button" class="toggle-password" @click="showNewPassword = !showNewPassword">
              <svg v-if="showNewPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            </button>
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">确认新密码</label>
          <div class="password-input">
            <input
              v-model="passwordForm.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              class="form-input"
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
        </div>
        <button class="save-btn secondary" @click="handlePasswordUpdate">更新密码</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { updateCurrentUser, updatePassword } from '@/api/auth'
import { uploadImage } from '@/api/admin'

const userStore = useUserStore()
const avatarInput = ref()

const form = reactive({
  nickname: '',
  email: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const handleFileChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return

  try {
    const res = await uploadImage(file)
    form.avatar = res.data.url
    showToast({ type: 'success', message: '头像上传成功' })
  } catch (error) {
    console.error('上传失败', error)
  }
}

const handleUpdate = async () => {
  try {
    await updateCurrentUser(form)
    userStore.updateUserInfo(form)
    showToast({ type: 'success', message: '保存成功' })
  } catch (error) {
    console.error('保存失败', error)
  }
}

const handlePasswordUpdate = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    showToast('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    showToast('两次密码输入不一致')
    return
  }
  try {
    await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    showToast({ type: 'success', message: '密码修改成功' })
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('修改密码失败', error)
  }
}

onMounted(() => {
  form.nickname = userStore.userInfo?.nickname || ''
  form.email = userStore.userInfo?.email || ''
  form.avatar = userStore.userInfo?.avatar || ''
})
</script>

<style lang="scss" scoped>
.panels {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-6);
}

.panel {
  padding: var(--space-8);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-2xl);
}

.panel-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-6);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-label {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-secondary);
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
    box-shadow: 0 0 0 3px rgba(180, 83, 9, 0.15);
  }

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;
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

.avatar-upload {
  display: flex;
  align-items: center;
  gap: var(--space-5);
}

.avatar-preview {
  cursor: pointer;
}

.avatar-image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-full);
  background-size: cover;
  background-position: center;
  border: 2px solid var(--border-color);
  transition: all var(--transition-fast);

  &:hover {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 4px rgba(180, 83, 9, 0.15);
  }
}

.avatar-placeholder {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  border: 1px dashed var(--border-color);
  border-radius: var(--radius-full);
  color: var(--text-muted);
  transition: all var(--transition-fast);

  svg {
    width: 24px;
    height: 24px;
  }

  &:hover {
    color: var(--text-primary);
    border-color: var(--text-muted);
  }
}

.upload-info {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.upload-title {
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: var(--text-primary);
}

.upload-desc {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.save-btn {
  height: 44px;
  padding: 0 var(--space-6);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  background: var(--gradient-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  &:active {
    transform: translateY(0);
  }

  &.secondary {
    color: var(--color-primary);
    background: transparent;
    border: 1px solid var(--color-primary);

    &:hover {
      color: white;
      background: var(--gradient-primary);
    }
  }
}

@media (max-width: 768px) {
  .panels {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .panel {
    padding: var(--space-5);
  }

  .avatar-upload {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
