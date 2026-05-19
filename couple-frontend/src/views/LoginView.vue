<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login, register } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const store = useUserStore()
const isLogin = ref(true)
const form = ref({ username: '', password: '' })
const confirmPwd = ref('')
const loading = ref(false)

async function handleSubmit() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (!isLogin.value) {
    if (form.value.username.length < 3) {
      ElMessage.warning('用户名至少3位')
      return
    }
    if (form.value.password.length < 6) {
      ElMessage.warning('密码至少6位')
      return
    }
    if (form.value.password !== confirmPwd.value) {
      ElMessage.warning('两次密码不一致')
      return
    }
  }
  loading.value = true
  try {
    const fn = isLogin.value ? login : register
    const res = await fn(form.value)
    store.setAuth(res.token, res.user)
    ElMessage.success(isLogin.value ? '欢迎回来，亲爱的 💕' : '注册成功，开始属于你们的旅程吧～')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- Decorative background words -->
    <div class="bg-words">
      <span>sweet</span>
      <span>love</span>
      <span>forever</span>
      <span>together</span>
      <span>happiness</span>
      <span>warmth</span>
      <span>cuddle</span>
      <span>cherish</span>
    </div>

    <div class="login-container">
      <!-- Left: Illustration -->
      <div class="login-illustration">
        <div class="illustration-content">
          <div class="ill-emoji">💕</div>
          <h1 class="ill-title">情侣空间</h1>
          <p class="ill-desc">
            在世界的某个角落<br />
            两颗心的距离<br />
            从这里开始靠近
          </p>
          <div class="ill-decorations">
            <span class="deco-dot" v-for="i in 5" :key="i" :style="{ animationDelay: i * 0.3 + 's' }"></span>
          </div>
        </div>
      </div>

      <!-- Right: Form -->
      <div class="login-form-side">
        <div class="form-card">
          <div class="form-header">
            <h2>{{ isLogin ? '欢迎回来' : '加入我们' }}</h2>
            <p>{{ isLogin ? '继续记录你们的甜蜜时刻' : '开启一段温暖的旅程' }}</p>
          </div>

          <div class="form-tabs">
            <button :class="{ active: isLogin }" @click="isLogin = true">登 录</button>
            <button :class="{ active: !isLogin }" @click="isLogin = false">注 册</button>
            <div class="tab-slider" :class="{ right: !isLogin }"></div>
          </div>

          <form class="form-body" @submit.prevent="handleSubmit">
            <div class="input-group">
              <label>用户名</label>
              <input
                v-model="form.username"
                type="text"
                placeholder="输入用户名"
                maxlength="32"
                autocomplete="username"
              />
            </div>

            <div class="input-group">
              <label>密码</label>
              <input
                v-model="form.password"
                type="password"
                placeholder="输入密码"
                maxlength="32"
                autocomplete="current-password"
              />
            </div>

            <Transition name="slide">
              <div v-if="!isLogin" class="input-group">
                <label>确认密码</label>
                <input
                  v-model="confirmPwd"
                  type="password"
                  placeholder="再次输入密码"
                  maxlength="32"
                />
              </div>
            </Transition>

            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="btn-loading"></span>
              <span v-else>{{ isLogin ? '💕 登 录' : '🌸 注 册' }}</span>
            </button>
          </form>

          <div class="form-footer">
            <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
            <a href="javascript:void(0)" @click="isLogin = !isLogin">
              {{ isLogin ? '立即注册' : '去登录' }}
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 40px;
  position: relative;
  z-index: 1;
}

/* Background floating words */
.bg-words {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg-words span {
  position: absolute;
  font-family: 'Georgia', 'Times New Roman', serif;
  font-size: clamp(24px, 3vw, 48px);
  color: rgba(200, 155, 130, 0.06);
  font-style: italic;
  font-weight: 700;
  animation: drift 20s linear infinite;
  white-space: nowrap;
}

.bg-words span:nth-child(1) { top: 8%; left: 5%; animation-delay: 0s; }
.bg-words span:nth-child(2) { top: 18%; left: 70%; animation-delay: -3s; }
.bg-words span:nth-child(3) { top: 35%; left: 15%; animation-delay: -6s; }
.bg-words span:nth-child(4) { top: 50%; left: 75%; animation-delay: -9s; }
.bg-words span:nth-child(5) { top: 65%; left: 8%; animation-delay: -12s; }
.bg-words span:nth-child(6) { top: 78%; left: 60%; animation-delay: -15s; }
.bg-words span:nth-child(7) { top: 88%; left: 30%; animation-delay: -18s; }
.bg-words span:nth-child(8) { top: 92%; left: 80%; animation-delay: -2s; }

@keyframes drift {
  0% { transform: translateY(0) translateX(0) rotate(0deg); opacity: 0.6; }
  25% { transform: translateY(-20px) translateX(15px) rotate(2deg); opacity: 1; }
  50% { transform: translateY(0) translateX(0) rotate(0deg); opacity: 0.6; }
  75% { transform: translateY(20px) translateX(-15px) rotate(-2deg); opacity: 0.4; }
  100% { transform: translateY(0) translateX(0) rotate(0deg); opacity: 0.6; }
}

/* Layout */
.login-container {
  display: flex;
  max-width: 880px;
  width: 100%;
  min-height: 520px;
  background: var(--color-surface);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg), 0 0 0 1px rgba(255,255,255,0.5) inset;
  border: 1px solid var(--color-border);
  overflow: hidden;
  position: relative;
  z-index: 1;
  animation: cardIn 0.6s ease-out;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

/* Left Illustration */
.login-illustration {
  flex: 1;
  background: linear-gradient(160deg,
    rgba(232, 141, 125, 0.12) 0%,
    rgba(242, 185, 135, 0.08) 40%,
    rgba(245, 208, 140, 0.06) 100%
  );
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  position: relative;
  overflow: hidden;
}

.illustration-content {
  text-align: center;
  position: relative;
  z-index: 1;
}

.ill-emoji {
  font-size: 56px;
  margin-bottom: 16px;
  animation: gentleBounce 3s ease-in-out infinite;
}

@keyframes gentleBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.ill-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: 3px;
  margin-bottom: 16px;
}

.ill-desc {
  font-size: 14px;
  color: var(--color-text-soft);
  line-height: 2;
  margin-bottom: 28px;
}

.ill-decorations {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.deco-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  opacity: 0.5;
  animation: dotPulse 2s ease-in-out infinite;
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 0.8; transform: scale(1.2); }
}

/* Right Form */
.login-form-side {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 40px;
}

.form-card {
  width: 100%;
  max-width: 340px;
}

.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.form-header p {
  font-size: 13px;
  color: var(--color-text-soft);
}

/* Tabs */
.form-tabs {
  display: flex;
  position: relative;
  background: rgba(232, 141, 125, 0.06);
  border-radius: var(--radius-sm);
  padding: 4px;
  margin-bottom: 24px;
}

.form-tabs button {
  flex: 1;
  padding: 10px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-soft);
  cursor: pointer;
  position: relative;
  z-index: 1;
  transition: color var(--transition);
  font-family: inherit;
}

.form-tabs button.active {
  color: var(--color-text);
  font-weight: 600;
}

.tab-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(180, 120, 90, 0.12);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-slider.right {
  transform: translateX(100%);
}

/* Form Body */
.form-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-group label {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-soft);
  padding-left: 2px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-group input {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.7);
  font-size: 14px;
  color: var(--color-text);
  font-family: inherit;
  transition: all var(--transition);
  outline: none;
}

.input-group input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.12);
  background: rgba(255, 252, 248, 0.9);
}

.input-group input::placeholder {
  color: var(--color-text-muted);
}

.submit-btn {
  margin-top: 8px;
  padding: 14px;
  border: none;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  letter-spacing: 2px;
  transition: all var(--transition);
  font-family: inherit;
  position: relative;
  overflow: hidden;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.35);
}

.submit-btn:active {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-loading {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: var(--color-text-soft);
}

.form-footer a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color var(--transition);
}

.form-footer a:hover {
  color: var(--color-primary-dark);
}

/* Transition */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Responsive */
@media (max-width: 768px) {
  .login-page {
    padding: 20px;
  }
  .login-container {
    flex-direction: column;
    min-height: auto;
  }
  .login-illustration {
    padding: 28px 24px;
  }
  .ill-emoji {
    font-size: 40px;
  }
  .ill-title {
    font-size: 22px;
  }
  .ill-desc {
    font-size: 12px;
    margin-bottom: 12px;
  }
  .login-form-side {
    padding: 20px 24px 32px;
  }
}
</style>
