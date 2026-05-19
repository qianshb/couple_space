<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createInvite, bind, getCoupleStatus } from '@/api/couple'
import { ElMessage } from 'element-plus'

const router = useRouter()
const myCode = ref('')
const partnerCode = ref('')
const bound = ref(false)
const partnerName = ref('')
const loading = ref(false)
const generating = ref(false)

async function fetchStatus() {
  try {
    const s = await getCoupleStatus()
    if (s.bound) {
      bound.value = true
      partnerName.value = s.partnerNickname || 'TA'
    } else if (s.myInviteCode) {
      myCode.value = s.myInviteCode
    }
  } catch { /* ignore */ }
}

async function handleCreateInvite() {
  generating.value = true
  try {
    const res = await createInvite()
    myCode.value = res.inviteCode
    ElMessage.success('邀请码已生成，快分享给TA吧～')
  } finally {
    generating.value = false
  }
}

async function handleBind() {
  if (!partnerCode.value.trim()) {
    ElMessage.warning('请输入对方的邀请码')
    return
  }
  loading.value = true
  try {
    await bind(partnerCode.value.trim().toUpperCase())
    ElMessage.success('绑定成功！💕')
    router.push('/')
  } finally {
    loading.value = false
  }
}

function copyCode() {
  navigator.clipboard?.writeText(myCode.value)
  ElMessage.success('已复制到剪贴板，快发给TA吧～')
}

onMounted(fetchStatus)
</script>

<template>
  <div class="bind-view fade-in">
    <header class="bind-header">
      <button class="back-link" @click="router.back()">
        <span>←</span> 返回
      </button>
      <h1>情侣绑定</h1>
    </header>

    <!-- Already Bound -->
    <div v-if="bound" class="bound-card">
      <div class="bound-celebration">🎉</div>
      <h2>已成功绑定</h2>
      <p class="bound-desc">
        你和 <strong>{{ partnerName }}</strong> 已经是情侣啦
      </p>
      <button class="bound-btn" @click="router.push('/')">返回首页 💕</button>
    </div>

    <!-- Not Bound -->
    <div v-else class="bind-steps">
      <!-- Step 1 -->
      <div class="step-card">
        <div class="step-indicator">
          <span class="step-num">1</span>
        </div>
        <h3>生成我的邀请码</h3>
        <p class="step-desc">生成后将邀请码发送给对方</p>

        <div v-if="myCode" class="code-display">
          <div class="code-value">{{ myCode }}</div>
          <button class="copy-btn" @click="copyCode">📋 复制邀请码</button>
        </div>
        <button v-else class="generate-btn" :disabled="generating" @click="handleCreateInvite">
          <span v-if="generating" class="spinner"></span>
          <span v-else>🔑 生成邀请码</span>
        </button>
      </div>

      <!-- Connector -->
      <div class="step-connector">
        <div class="connector-line"></div>
        <span class="connector-icon">💗</span>
        <div class="connector-line"></div>
      </div>

      <!-- Step 2 -->
      <div class="step-card">
        <div class="step-indicator">
          <span class="step-num">2</span>
        </div>
        <h3>输入对方的邀请码</h3>
        <p class="step-desc">输入TA分享给你的8位邀请码</p>

        <div class="code-input-wrap">
          <input
            v-model="partnerCode"
            type="text"
            placeholder="输入8位邀请码"
            maxlength="8"
            class="code-input"
            autocomplete="off"
          />
        </div>
        <button
          class="bind-btn"
          :disabled="loading"
          @click="handleBind"
        >
          <span v-if="loading" class="spinner"></span>
          <span v-else>💕 确认绑定</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bind-view {
  max-width: 520px;
  margin: 0 auto;
  padding: 32px 28px 60px;
}

.bind-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.back-link {
  background: none;
  border: none;
  color: var(--color-text-soft);
  font-size: 14px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
  font-family: inherit;
}

.back-link:hover {
  color: var(--color-primary);
  background: rgba(232, 141, 125, 0.06);
}

.bind-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: 1px;
}

/* Bound */
.bound-card {
  text-align: center;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 56px 32px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-md);
}

.bound-celebration {
  font-size: 64px;
  margin-bottom: 16px;
  animation: gentleBounce 2s ease-in-out infinite;
}

@keyframes gentleBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

.bound-card h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 12px;
}

.bound-desc {
  font-size: 15px;
  color: var(--color-text-soft);
  margin-bottom: 28px;
}

.bound-desc strong {
  color: var(--color-primary-dark);
  font-weight: 700;
}

.bound-btn {
  padding: 12px 32px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
}

.bound-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}

/* Steps */
.bind-steps {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.step-card {
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  padding: 32px 28px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  text-align: center;
}

.step-indicator {
  display: flex;
  justify-content: center;
  margin-bottom: 14px;
}

.step-num {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(232, 141, 125, 0.25);
}

.step-card h3 {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 6px;
}

.step-desc {
  font-size: 13px;
  color: var(--color-text-soft);
  margin-bottom: 20px;
}

/* Code Display */
.code-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.code-value {
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 8px;
  color: var(--color-primary-dark);
  background: linear-gradient(135deg,
    rgba(232, 141, 125, 0.06),
    rgba(242, 185, 135, 0.06)
  );
  padding: 14px 32px;
  border-radius: var(--radius-lg);
  border: 2px dashed var(--color-border-strong);
  font-family: 'Courier New', monospace;
}

.copy-btn {
  padding: 8px 20px;
  border: 1px solid var(--color-border-strong);
  border-radius: 20px;
  background: var(--color-surface);
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
}

.copy-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: rgba(232, 141, 125, 0.04);
}

.generate-btn,
.bind-btn {
  padding: 10px 28px;
  border: none;
  border-radius: var(--radius-md);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.generate-btn {
  background: rgba(232, 141, 125, 0.08);
  color: var(--color-primary-dark);
}

.generate-btn:hover:not(:disabled) {
  background: rgba(232, 141, 125, 0.14);
}

.bind-btn {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  width: 100%;
  justify-content: center;
}

.bind-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}

.bind-btn:disabled,
.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Connector */
.step-connector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 8px 0;
}

.connector-line {
  width: 40px;
  height: 1px;
  background: var(--color-border-strong);
}

.connector-icon {
  font-size: 20px;
  padding: 0 8px;
  animation: gentleBounce 2s ease-in-out infinite;
}

/* Code Input */
.code-input-wrap {
  margin-bottom: 16px;
}

.code-input {
  width: 100%;
  max-width: 260px;
  padding: 14px 18px;
  border: 2px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: rgba(255, 248, 242, 0.6);
  font-size: 22px;
  font-weight: 700;
  text-align: center;
  letter-spacing: 4px;
  color: var(--color-text);
  font-family: 'Courier New', monospace;
  text-transform: uppercase;
  outline: none;
  transition: all var(--transition);
}

.code-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 4px rgba(232, 141, 125, 0.1);
  background: rgba(255, 252, 248, 0.9);
}

.code-input::placeholder {
  font-size: 14px;
  font-weight: 400;
  letter-spacing: 1px;
  color: var(--color-text-muted);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .bind-view {
    padding: 20px 16px 40px;
  }
  .step-card {
    padding: 24px 20px;
  }
  .code-value {
    font-size: 28px;
    letter-spacing: 6px;
    padding: 12px 24px;
  }
}
</style>
