<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMe, updateMe } from '@/api/user'
import { uploadFile } from '@/api/file'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const store = useUserStore()
const form = ref({
  nickname: '',
  avatar: '',
  birthday: '',
  city: '',
})
const saving = ref(false)
const uploading = ref(false)
const fileInput = ref<HTMLInputElement>()
const constellation = ref('')

async function fetchUser() {
  try {
    const user = await getMe()
    form.value.nickname = user.nickname || ''
    form.value.avatar = user.avatar || ''
    form.value.birthday = user.birthday || ''
    form.value.city = user.city || ''
    constellation.value = user.constellation || ''
    store.updateUser(user)
  } catch { /* handled */ }
}

async function handleAvatarChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  // Validate
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('头像图片不超过 5MB')
    return
  }

  uploading.value = true
  try {
    const res = await uploadFile(file)
    form.value.avatar = res.url
    ElMessage.success('头像上传成功')
  } finally {
    uploading.value = false
  }
  input.value = ''
}

function updateBirthday() {
  if (form.value.birthday) {
    // Constellation will be calculated on save
    ElMessage.success('保存后将自动计算星座')
  }
}

async function handleSave() {
  saving.value = true
  try {
    const user = await updateMe({
      nickname: form.value.nickname,
      avatar: form.value.avatar,
      birthday: form.value.birthday || undefined,
      city: form.value.city,
    })
    constellation.value = user.constellation || ''
    store.updateUser(user)
    ElMessage.success('保存成功 💕')
  } finally {
    saving.value = false
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
    store.logout()
    router.push('/login')
  } catch { /* cancelled */ }
}

onMounted(fetchUser)
</script>

<template>
  <div class="settings-view fade-in">
    <header class="settings-header">
      <h1>个人设置</h1>
      <p>管理你的个人信息和账号</p>
    </header>

    <div class="settings-card">
      <!-- Avatar Upload -->
      <div class="avatar-section">
        <div class="avatar-preview" @click="fileInput?.click()" title="点击上传头像">
          <img v-if="form.avatar" :src="form.avatar" alt="avatar" class="avatar-img" />
          <span v-else class="avatar-fallback">{{ form.nickname?.charAt(0) || '💕' }}</span>
          <div class="avatar-overlay">
            <span v-if="uploading" class="spinner"></span>
            <span v-else>📷</span>
          </div>
        </div>
        <div class="avatar-info">
          <div class="avatar-name">{{ form.nickname || '未设置昵称' }}</div>
          <div class="avatar-hint">点击头像上传，支持 jpg/png，≤ 5MB</div>
        </div>
        <input
          type="file"
          accept="image/*"
          @change="handleAvatarChange"
          ref="fileInput"
          style="display:none"
        />
      </div>

      <!-- Form -->
      <div class="form-section">
        <div class="field">
          <label>昵称</label>
          <input v-model="form.nickname" type="text" placeholder="你的昵称" maxlength="16" />
          <span class="field-hint">对方可以看到你的昵称</span>
        </div>

        <div class="field-row">
          <div class="field flex-1">
            <label>出生日期</label>
            <input
              v-model="form.birthday"
              type="date"
              class="date-input"
              @change="updateBirthday"
            />
            <span class="field-hint">用于计算星座</span>
          </div>

          <div class="field flex-1">
            <label>星座</label>
            <div class="constellation-display">
              <span v-if="constellation" class="constellation-value">
                {{ constellation === '水瓶座' ? '♒' :
                   constellation === '双鱼座' ? '♓' :
                   constellation === '白羊座' ? '♈' :
                   constellation === '金牛座' ? '♉' :
                   constellation === '双子座' ? '♊' :
                   constellation === '巨蟹座' ? '♋' :
                   constellation === '狮子座' ? '♌' :
                   constellation === '处女座' ? '♍' :
                   constellation === '天秤座' ? '♎' :
                   constellation === '天蝎座' ? '♏' :
                   constellation === '射手座' ? '♐' : '♑'
                }}
                {{ constellation }}
              </span>
              <span v-else class="constellation-empty">设置生日后自动计算</span>
            </div>
          </div>
        </div>

        <div class="field">
          <label>所在城市</label>
          <input v-model="form.city" type="text" placeholder="输入你的城市" maxlength="32" />
          <span class="field-hint">对方可以看到你的城市信息</span>
        </div>
      </div>

      <!-- Save -->
      <button class="save-btn" :disabled="saving || uploading" @click="handleSave">
        <span v-if="saving" class="spinner"></span>
        <span v-else>💾 保存设置</span>
      </button>

      <!-- Actions -->
      <div class="actions-section">
        <button class="action-link" @click="router.push('/bind')">💑 情侣绑定</button>
        <button class="action-link danger" @click="handleLogout">退出登录</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.settings-view {
  max-width: 560px;
  margin: 0 auto;
  padding: 32px 28px 60px;
}

.settings-header { margin-bottom: 28px; }
.settings-header h1 {
  font-size: 26px; font-weight: 800; color: var(--color-text);
  letter-spacing: 1px; margin-bottom: 4px;
}
.settings-header p { font-size: 13px; color: var(--color-text-soft); }

.settings-card {
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-xl);
  padding: 28px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-md);
}

/* Avatar */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--color-border);
}

.avatar-preview {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), var(--color-primary));
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(232, 141, 125, 0.2);
  flex-shrink: 0;
  cursor: pointer;
  position: relative;
  transition: all var(--transition);
}

.avatar-preview:hover { transform: scale(1.05); }
.avatar-preview:hover .avatar-overlay { opacity: 1; }

.avatar-img { width: 100%; height: 100%; object-fit: cover; }

.avatar-fallback { color: #fff; font-size: 28px; font-weight: 700; }

.avatar-overlay {
  position: absolute; inset: 0;
  background: rgba(40, 28, 24, 0.4);
  display: flex; align-items: center; justify-content: center;
  opacity: 0; transition: opacity var(--transition);
  color: #fff; font-size: 22px;
}

.avatar-name { font-size: 16px; font-weight: 600; color: var(--color-text); }
.avatar-hint { font-size: 12px; color: var(--color-text-muted); margin-top: 2px; }

/* Form */
.form-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 24px;
}

.field { display: flex; flex-direction: column; gap: 6px; }
.field.flex-1 { flex: 1; }

.field-row { display: flex; gap: 14px; }

.field label {
  font-size: 13px; font-weight: 600; color: var(--color-text-soft); letter-spacing: 0.3px;
}

.field input, .date-input {
  padding: 11px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.5);
  font-size: 14px; color: var(--color-text); font-family: inherit;
  outline: none; transition: all var(--transition);
}

.field input:focus, .date-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.1);
  background: rgba(255, 252, 248, 0.8);
}

.field input::placeholder { color: var(--color-text-muted); }

.field-hint { font-size: 11px; color: var(--color-text-muted); padding-left: 2px; }

/* Constellation */
.constellation-display {
  padding: 11px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.3);
  font-size: 14px;
  min-height: 44px;
  display: flex; align-items: center;
}

.constellation-value { color: var(--color-text); font-weight: 600; }
.constellation-empty { color: var(--color-text-muted); font-size: 13px; }

/* Save */
.save-btn {
  display: flex; align-items: center; justify-content: center; gap: 8px;
  width: 100%; padding: 13px; border: none; border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff; font-size: 15px; font-weight: 700; cursor: pointer;
  font-family: inherit; letter-spacing: 1px; transition: all var(--transition);
  margin-bottom: 20px;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}
.save-btn:disabled { opacity: 0.6; cursor: not-allowed; }

/* Actions */
.actions-section {
  display: flex; gap: 10px; padding-top: 20px;
  border-top: 1px solid var(--color-border);
}

.action-link {
  flex: 1; padding: 10px;
  border: 1px solid var(--color-border); border-radius: var(--radius-sm);
  background: transparent; color: var(--color-text-soft);
  font-size: 13px; font-weight: 500; cursor: pointer;
  font-family: inherit; transition: all var(--transition); text-align: center;
}
.action-link:hover {
  border-color: var(--color-primary); color: var(--color-primary);
  background: rgba(232, 141, 125, 0.04);
}
.action-link.danger { color: var(--color-text-muted); }
.action-link.danger:hover {
  border-color: var(--color-danger); color: var(--color-danger);
  background: rgba(212, 117, 106, 0.04);
}

.spinner {
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff; border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .settings-view { padding: 20px 16px 40px; }
  .settings-card { padding: 20px; }
  .settings-header h1 { font-size: 22px; }
  .field-row { flex-direction: column; gap: 18px; }
}
</style>
