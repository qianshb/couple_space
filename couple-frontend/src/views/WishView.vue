<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { createWish, getWishList, toggleWish, deleteWish } from '@/api/wish'
import { ElMessage, ElMessageBox } from 'element-plus'

const wishes = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const form = ref({ title: '', description: '' })
const formErrors = ref({ title: '' })

const pendingWishes = computed(() => wishes.value.filter(w => !w.fulfilled))
const fulfilledWishes = computed(() => wishes.value.filter(w => w.fulfilled))

async function fetchList() {
  loading.value = true
  try {
    wishes.value = await getWishList()
  } catch {
    wishes.value = []
  } finally {
    loading.value = false
  }
}

function openCreate() {
  form.value = { title: '', description: '' }
  formErrors.value = { title: '' }
  dialogVisible.value = true
}

function validateForm(): boolean {
  formErrors.value = { title: '' }
  if (!form.value.title.trim()) {
    formErrors.value.title = '请输入心愿名称'
    return false
  }
  return true
}

async function handleCreate() {
  if (!validateForm()) return
  saving.value = true
  try {
    await createWish({
      title: form.value.title.trim(),
      description: form.value.description || undefined,
    })
    ElMessage.success('心愿添加成功 💝')
    dialogVisible.value = false
    await fetchList()
  } catch {
    /* error shown by interceptor */
  } finally {
    saving.value = false
  }
}

async function handleToggle(item: any) {
  try {
    const updated = await toggleWish(item.id)
    const idx = wishes.value.findIndex(w => w.id === item.id)
    if (idx !== -1) wishes.value[idx] = updated
  } catch { /* handled */ }
}

async function handleDelete(item: any) {
  try {
    await ElMessageBox.confirm(`确定删除「${item.title}」吗？`, '提示', { type: 'warning' })
    await deleteWish(item.id)
    wishes.value = wishes.value.filter(w => w.id !== item.id)
    ElMessage.success('已删除')
  } catch { /* cancel */ }
}

onMounted(fetchList)
</script>

<template>
  <div class="wish-view fade-in">
    <header class="page-header">
      <div>
        <h1>心愿单</h1>
        <p>想和TA一起做的事，一起实现它们</p>
      </div>
      <button class="create-btn" @click="openCreate">
        <span>+</span> 添加心愿
      </button>
    </header>

    <!-- Empty State -->
    <div v-if="wishes.length === 0 && !loading" class="empty-state">
      <div class="empty-icon">💝</div>
      <h2>心愿单还是空的</h2>
      <p>写下你们想一起做的事吧</p>
      <button class="empty-btn" @click="openCreate">✨ 添加第一个心愿</button>
    </div>

    <div v-if="wishes.length > 0" class="wish-list">
      <!-- Pending -->
      <section v-if="pendingWishes.length > 0" class="wish-section">
        <h3 class="section-title">
          <span>📝</span> 未实现
          <span class="section-count">{{ pendingWishes.length }}</span>
        </h3>
        <div class="wish-cards">
          <div
            v-for="item in pendingWishes"
            :key="item.id"
            class="wish-card"
            :style="{ animationDelay: pendingWishes.indexOf(item) * 0.04 + 's' }"
          >
            <button class="card-check" @click="handleToggle(item)" title="标记为已实现">○</button>
            <div class="card-body">
              <div class="card-title">{{ item.title }}</div>
              <div v-if="item.description" class="card-desc">{{ item.description }}</div>
            </div>
            <button class="card-delete" @click="handleDelete(item)" title="删除">✕</button>
          </div>
        </div>
      </section>

      <!-- Fulfilled -->
      <section v-if="fulfilledWishes.length > 0" class="wish-section fulfilled-section">
        <h3 class="section-title fulfilled-title">
          <span>✅</span> 已实现
          <span class="section-count done">{{ fulfilledWishes.length }}</span>
        </h3>
        <div class="wish-cards">
          <div
            v-for="item in fulfilledWishes"
            :key="item.id"
            class="wish-card fulfilled"
          >
            <button class="card-check checked" @click="handleToggle(item)" title="标记为未实现">●</button>
            <div class="card-body">
              <div class="card-title done">{{ item.title }}</div>
              <div v-if="item.description" class="card-desc">{{ item.description }}</div>
            </div>
            <button class="card-delete" @click="handleDelete(item)" title="删除">✕</button>
          </div>
        </div>
      </section>
    </div>

    <!-- Create Dialog -->
    <Teleport to="body">
      <Transition name="dialog-fade">
        <div v-if="dialogVisible" class="dialog-overlay" @click.self="dialogVisible = false">
          <div class="dialog-card">
            <h3>添加心愿</h3>

            <div class="dialog-field">
              <label>心愿 <span class="required">*</span></label>
              <input
                v-model="form.title"
                type="text"
                placeholder="例如：一起去看极光"
                maxlength="32"
                class="dialog-input"
              />
              <span v-if="formErrors.title" class="field-error">{{ formErrors.title }}</span>
            </div>

            <div class="dialog-field">
              <label>描述</label>
              <textarea
                v-model="form.description"
                placeholder="详细描述一下这个心愿..."
                maxlength="256"
                rows="3"
                class="dialog-textarea"
              ></textarea>
            </div>

            <div class="dialog-actions">
              <button class="dialog-cancel" @click="dialogVisible = false">取消</button>
              <button class="dialog-confirm" :disabled="saving" @click="handleCreate">
                {{ saving ? '添加中...' : '💝 添加' }}
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<style scoped>
.wish-view {
  max-width: 640px;
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
.empty-state h2 { font-size: 20px; font-weight: 700; color: var(--color-text); margin-bottom: 8px; }
.empty-state p { color: var(--color-text-soft); font-size: 14px; margin-bottom: 24px; }
.empty-btn {
  padding: 12px 28px; border: none; border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff; font-size: 15px; font-weight: 600; cursor: pointer; font-family: inherit; transition: all var(--transition);
}
.empty-btn:hover { transform: translateY(-1px); box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3); }

/* Section */
.wish-section { margin-bottom: 28px; }
.fulfilled-section { margin-top: 32px; }

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 14px;
  padding-left: 4px;
}
.section-title span:first-child { font-size: 18px; }
.section-count {
  font-size: 12px;
  background: rgba(232, 141, 125, 0.1);
  color: var(--color-primary-dark);
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 600;
}
.section-count.done {
  background: rgba(143, 188, 143, 0.12);
  color: #6B9B6B;
}

.fulfilled-title { color: var(--color-text-soft); }

/* Cards */
.wish-cards { display: flex; flex-direction: column; gap: 8px; }

.wish-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  padding: 16px 16px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition);
  animation: cardIn 0.35s ease-out both;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.wish-card:hover { box-shadow: var(--shadow-md); }

.wish-card.fulfilled {
  opacity: 0.55;
  text-decoration: none;
}

.card-check {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border: 2px solid var(--color-border-strong);
  border-radius: 50%;
  background: transparent;
  color: var(--color-text-muted);
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
  transition: all var(--transition);
  line-height: 1;
}

.card-check:hover { border-color: var(--color-success); color: var(--color-success); }

.card-check.checked {
  border-color: var(--color-success);
  background: var(--color-success);
  color: #fff;
  font-size: 10px;
}

.card-body { flex: 1; min-width: 0; }

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
  line-height: 1.5;
}

.card-title.done {
  text-decoration: line-through;
  color: var(--color-text-muted);
}

.card-desc {
  font-size: 13px;
  color: var(--color-text-soft);
  margin-top: 4px;
  line-height: 1.6;
}

.card-delete {
  flex-shrink: 0;
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
  margin-top: 2px;
}

.wish-card:hover .card-delete { opacity: 0.4; }
.card-delete:hover { opacity: 1 !important; background: rgba(212, 117, 106, 0.1); color: var(--color-danger); }

/* Dialog */
.dialog-overlay {
  position: fixed; inset: 0; z-index: 9999;
  background: rgba(74, 53, 48, 0.2); backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  display: flex; align-items: center; justify-content: center; padding: 20px;
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
.dialog-card h3 { font-size: 20px; font-weight: 700; color: var(--color-text); margin-bottom: 20px; text-align: center; }
.dialog-field { margin-bottom: 16px; }
.dialog-field label { display: block; font-size: 13px; font-weight: 600; color: var(--color-text-soft); margin-bottom: 6px; }
.required { color: var(--color-danger); }
.dialog-input {
  width: 100%; padding: 10px 14px;
  border: 1px solid var(--color-border); border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.5); font-size: 14px; color: var(--color-text);
  font-family: inherit; outline: none; transition: all var(--transition);
}
.dialog-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.1); }
.dialog-textarea {
  width: 100%; padding: 10px 14px;
  border: 1px solid var(--color-border); border-radius: var(--radius-sm);
  background: rgba(255, 248, 242, 0.5); font-size: 14px; color: var(--color-text);
  font-family: inherit; resize: none; outline: none; transition: all var(--transition);
}
.dialog-textarea:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.1); }
.field-error { font-size: 11px; color: var(--color-danger); margin-top: 4px; display: block; }
.dialog-actions { display: flex; gap: 10px; margin-top: 20px; }
.dialog-cancel, .dialog-confirm {
  flex: 1; padding: 11px; border-radius: var(--radius-sm);
  font-size: 14px; font-weight: 600; cursor: pointer; font-family: inherit; transition: all var(--transition);
}
.dialog-cancel { border: 1px solid var(--color-border); background: transparent; color: var(--color-text-soft); }
.dialog-cancel:hover { background: rgba(232, 141, 125, 0.04); }
.dialog-confirm {
  border: none; background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)); color: #fff;
}
.dialog-confirm:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(232, 141, 125, 0.3); }
.dialog-confirm:disabled { opacity: 0.6; cursor: not-allowed; }

.dialog-fade-enter-active, .dialog-fade-leave-active { transition: opacity 0.25s ease; }
.dialog-fade-enter-active .dialog-card, .dialog-fade-leave-active .dialog-card { transition: transform 0.25s ease; }
.dialog-fade-enter-from, .dialog-fade-leave-to { opacity: 0; }
.dialog-fade-enter-from .dialog-card { transform: scale(0.95) translateY(10px); }
.dialog-fade-leave-to .dialog-card { transform: scale(0.95) translateY(10px); }

@media (max-width: 768px) {
  .wish-view { padding: 20px 16px 40px; }
  .page-header { flex-direction: column; gap: 16px; }
  .page-header h1 { font-size: 22px; }
  .create-btn { align-self: stretch; justify-content: center; }
  .dialog-card { width: 100%; padding: 20px; }
}
</style>
