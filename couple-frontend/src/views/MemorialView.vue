<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMemorialList, createMemorial, deleteMemorial } from '@/api/memorial'
import { ElMessage, ElMessageBox } from 'element-plus'

const memorials = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ title: '', eventDate: '', description: '' })
const formErrors = ref({ title: '', eventDate: '' })

async function fetchList() {
  loading.value = true
  try {
    memorials.value = await getMemorialList()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { title: '', eventDate: '', description: '' }
  formErrors.value = { title: '', eventDate: '' }
  dialogVisible.value = true
}

function validateForm(): boolean {
  formErrors.value = { title: '', eventDate: '' }
  if (!form.value.title.trim()) {
    formErrors.value.title = '请输入纪念日名称'
    return false
  }
  if (!form.value.eventDate) {
    formErrors.value.eventDate = '请选择日期'
    return false
  }
  return true
}

async function handleCreate() {
  if (!validateForm()) return
  saving.value = true
  try {
    await createMemorial({
      title: form.value.title.trim(),
      eventDate: form.value.eventDate,
      description: form.value.description || undefined,
    })
    ElMessage.success('纪念日创建成功 💕')
    dialogVisible.value = false
    await fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(item: any) {
  try {
    await ElMessageBox.confirm(`确定删除「${item.title}」吗？`, '提示', { type: 'warning' })
    await deleteMemorial(item.id)
    memorials.value = memorials.value.filter(m => m.id !== item.id)
    ElMessage.success('已删除')
  } catch { /* cancel */ }
}

onMounted(fetchList)
</script>

<template>
  <div class="memorial-view fade-in">
    <header class="page-header">
      <div>
        <h1>纪念日</h1>
        <p>记录属于我们的每一个重要日子</p>
      </div>
      <button class="create-btn" @click="openCreate">
        <span>+</span> 新建纪念日
      </button>
    </header>

    <!-- Empty State -->
    <div v-if="memorials.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">📅</div>
      <h2>还没有纪念日</h2>
      <p>记录你们的第一次见面、第一次旅行...</p>
      <button class="empty-btn" @click="openCreate">✨ 创建第一个纪念日</button>
    </div>

    <!-- Memorial Cards -->
    <div v-if="memorials.length > 0" class="memorial-grid">
      <div
        v-for="item in memorials"
        :key="item.id"
        class="memorial-card"
      >
        <button class="card-delete" @click="handleDelete(item)" title="删除">✕</button>

        <div class="card-heart">
          {{ item.daysPassed === 0 ? '💕' : item.daysPassed <= 30 ? '🌱' : item.daysPassed <= 365 ? '🌸' : '💗' }}
        </div>

        <h3 class="card-title">{{ item.title }}</h3>
        <div class="card-date">{{ item.eventDate }}</div>

        <div class="card-count">
          <span class="count-number">{{ item.daysPassed }}</span>
          <span class="count-unit">天</span>
        </div>
        <div class="count-label">已经一起走过</div>

        <div v-if="item.description" class="card-desc">{{ item.description }}</div>
      </div>
    </div>

    <!-- Create Dialog -->
    <Teleport to="body">
      <Transition name="dialog-fade">
        <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
          <div class="dialog-card">
            <h3>新建纪念日</h3>

            <div class="dialog-field">
              <label>名称 <span class="required">*</span></label>
              <input
                v-model="form.title"
                type="text"
                placeholder="例如：第一次见面"
                maxlength="32"
                class="dialog-input"
              />
              <span v-if="formErrors.title" class="field-error">{{ formErrors.title }}</span>
            </div>

            <div class="dialog-field">
              <label>日期 <span class="required">*</span></label>
              <input
                v-model="form.eventDate"
                type="date"
                class="dialog-input"
              />
              <span v-if="formErrors.eventDate" class="field-error">{{ formErrors.eventDate }}</span>
            </div>

            <div class="dialog-field">
              <label>备注</label>
              <textarea
                v-model="form.description"
                placeholder="一些想说的话..."
                maxlength="128"
                rows="3"
                class="dialog-textarea"
              ></textarea>
            </div>

            <div class="dialog-actions">
              <button class="dialog-cancel" @click="dialogVisible = false">取消</button>
              <button class="dialog-confirm" :disabled="saving" @click="handleCreate">
                {{ saving ? '创建中...' : '✨ 创建' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.memorial-view {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 28px 60px;
}

/* Header */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 26px;
  font-weight: 800;
  color: var(--color-text);
  letter-spacing: 1px;
  margin-bottom: 4px;
}

.page-header p {
  font-size: 13px;
  color: var(--color-text-soft);
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
  white-space: nowrap;
}

.create-btn span { font-size: 18px; line-height: 1; }

.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}

/* Empty */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
}

.empty-icon { font-size: 56px; margin-bottom: 16px; }

.empty-state h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 8px;
}

.empty-state p {
  color: var(--color-text-soft);
  font-size: 14px;
  margin-bottom: 24px;
}

.empty-btn {
  padding: 12px 28px;
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

.empty-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}

/* Memorial Grid */
.memorial-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.memorial-card {
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  padding: 24px 20px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  text-align: center;
  position: relative;
  transition: all var(--transition);
  animation: cardIn 0.4s ease-out both;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.memorial-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--color-border-strong);
}

.card-delete {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all var(--transition);
}

.memorial-card:hover .card-delete { opacity: 0.5; }
.card-delete:hover {
  opacity: 1 !important;
  background: rgba(212, 117, 106, 0.1);
  color: var(--color-danger);
}

.card-heart {
  font-size: 28px;
  margin-bottom: 10px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 4px;
}

.card-date {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-bottom: 16px;
}

.card-count {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 4px;
  margin-bottom: 4px;
}

.count-number {
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
}

.count-unit {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary);
}

.count-label {
  font-size: 11px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}

.card-desc {
  font-size: 12px;
  color: var(--color-text-soft);
  line-height: 1.6;
  border-top: 1px solid var(--color-border);
  padding-top: 10px;
  margin-top: 4px;
}

/* Dialog */
.dialog-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(74, 53, 48, 0.2);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.dialog-card {
  width: 420px;
  background: var(--color-surface);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: var(--radius-xl);
  padding: 28px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
}

.dialog-card h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 20px;
  text-align: center;
}

.dialog-field {
  margin-bottom: 16px;
}

.dialog-field label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-soft);
  margin-bottom: 6px;
}

.required { color: var(--color-danger); }

.dialog-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.5);
  font-size: 14px;
  color: var(--color-text);
  font-family: inherit;
  outline: none;
  transition: all var(--transition);
}

.dialog-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.1);
}

.dialog-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.5);
  font-size: 14px;
  color: var(--color-text);
  font-family: inherit;
  resize: none;
  outline: none;
  transition: all var(--transition);
}

.dialog-textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.1);
}

.field-error {
  font-size: 11px;
  color: var(--color-danger);
  margin-top: 4px;
  display: block;
}

.dialog-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}

.dialog-cancel,
.dialog-confirm {
  flex: 1;
  padding: 11px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
}

.dialog-cancel {
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-soft);
}

.dialog-cancel:hover {
  background: rgba(232, 141, 125, 0.04);
}

.dialog-confirm {
  border: none;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
}

.dialog-confirm:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(232, 141, 125, 0.3);
}

.dialog-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Dialog Transition */
.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.25s ease;
}

.dialog-fade-enter-active .dialog-card,
.dialog-fade-leave-active .dialog-card {
  transition: transform 0.25s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to { opacity: 0; }

.dialog-fade-enter-from .dialog-card { transform: scale(0.95) translateY(10px); }
.dialog-fade-leave-to .dialog-card { transform: scale(0.95) translateY(10px); }

@media (max-width: 768px) {
  .memorial-view { padding: 20px 16px 40px; }
  .page-header { flex-direction: column; gap: 16px; }
  .page-header h1 { font-size: 22px; }
  .create-btn { align-self: stretch; justify-content: center; }
  .memorial-grid { grid-template-columns: repeat(2, 1fr); }
  .dialog-card { width: 100%; padding: 20px; }
}
</style>
