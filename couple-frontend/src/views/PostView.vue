<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { uploadFile } from '@/api/file'
import { createPost } from '@/api/post'
import { ElMessage } from 'element-plus'

const router = useRouter()
const content = ref('')
const postType = ref<'image' | 'video'>('image')
const dragging = ref(false)
const submitting = ref(false)
const fileInput = ref<HTMLInputElement>()
const dropZone = ref<HTMLDivElement>()

// Track each file's upload state independently
interface FileItem {
  id: number
  file: File
  preview: string
  url?: string
  uploading: boolean
  progress: number
  error?: string
}

let nextId = 0
const fileItems = ref<FileItem[]>([])

const uploadedCount = computed(() => fileItems.value.filter(f => f.url).length)
const hasFiles = computed(() => fileItems.value.length > 0)
const allUploaded = computed(() => hasFiles.value && fileItems.value.every(f => f.url))
const anyUploading = computed(() => fileItems.value.some(f => f.uploading))
const canPublish = computed(() => allUploaded.value && !submitting.value)

// Validation
const MAX_IMAGE_SIZE = 10 * 1024 * 1024  // 10MB
const MAX_VIDEO_SIZE = 100 * 1024 * 1024 // 100MB
const MAX_IMAGE_COUNT = 9
const MAX_VIDEO_COUNT = 3

function validateFile(file: File): string | null {
  const maxSize = postType.value === 'image' ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE
  const maxCount = postType.value === 'image' ? MAX_IMAGE_COUNT : MAX_VIDEO_COUNT

  if (fileItems.value.length >= maxCount) {
    return `最多选择 ${maxCount} 个${postType.value === 'image' ? '图片' : '视频'}`
  }
  if (file.size > maxSize) {
    const sizeMB = (maxSize / 1024 / 1024).toFixed(0)
    return `文件过大，单个${postType.value === 'image' ? '图片' : '视频'}不超过 ${sizeMB}MB`
  }
  if (postType.value === 'image' && !file.type.startsWith('image/')) {
    return '仅支持图片格式'
  }
  if (postType.value === 'video' && !file.type.startsWith('video/')) {
    return '仅支持视频格式'
  }
  return null
}

function addFiles(fileList: FileList | File[]) {
  const files = Array.from(fileList)
  const newItems: FileItem[] = []

  for (const file of files) {
    const error = validateFile(file)
    if (error) {
      ElMessage.warning(error)
      if (files.length === 1) return // show single error
      continue
    }
    newItems.push({
      id: ++nextId,
      file,
      preview: URL.createObjectURL(file),
      uploading: false,
      progress: 0,
    })
  }

  if (newItems.length > 0) {
    fileItems.value.push(...newItems)
    // Auto-upload each file
    newItems.forEach(item => uploadSingle(item))
  }
}

function updateItem(id: number, patch: Partial<FileItem>) {
  const idx = fileItems.value.findIndex(f => f.id === id)
  if (idx !== -1) {
    fileItems.value[idx] = { ...fileItems.value[idx], ...patch }
  }
}

async function uploadSingle(item: FileItem) {
  updateItem(item.id, { uploading: true, progress: 0, error: undefined })

  const progressTimer = setInterval(() => {
    const current = fileItems.value.find(f => f.id === item.id)
    if (current && current.progress < 90) {
      const newProgress = Math.min(current.progress + Math.random() * 15 + 5, 90)
      updateItem(item.id, { progress: newProgress })
    }
  }, 300)

  try {
    const res = await uploadFile(item.file)
    clearInterval(progressTimer)
    updateItem(item.id, { progress: 100, url: res.url, uploading: false })
  } catch (e: any) {
    clearInterval(progressTimer)
    updateItem(item.id, { error: e?.message || '上传失败', uploading: false })
    ElMessage.error(e?.message || '上传失败')
  }
}

function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.files?.length) {
    addFiles(input.files)
  }
  input.value = ''
}

function removeFile(item: FileItem) {
  const idx = fileItems.value.indexOf(item)
  if (idx === -1) return
  URL.revokeObjectURL(item.preview)
  fileItems.value.splice(idx, 1)
}

function retryUpload(item: FileItem) {
  item.error = undefined
  uploadSingle(item)
}

// Drag & Drop
function onDragEnter(e: DragEvent) {
  e.preventDefault()
  dragging.value = true
}

function onDragOver(e: DragEvent) {
  e.preventDefault()
  dragging.value = true
}

function onDragLeave(e: DragEvent) {
  // Only set false if leaving the drop zone entirely
  if (dropZone.value && !dropZone.value.contains(e.relatedTarget as Node)) {
    dragging.value = false
  }
}

function onDrop(e: DragEvent) {
  e.preventDefault()
  dragging.value = false
  if (e.dataTransfer?.files?.length) {
    addFiles(e.dataTransfer.files)
  }
}

// Paste from clipboard
function onPaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return

  const files: File[] = []
  for (let i = 0; i < items.length; i++) {
    const blob = items[i].getAsFile()
    if (blob && (blob.type.startsWith('image/') || blob.type.startsWith('video/'))) {
      files.push(blob)
    }
  }
  if (files.length > 0) {
    e.preventDefault()
    addFiles(files)
  }
}

function switchType(type: 'image' | 'video') {
  if (type === postType.value) return
  if (fileItems.value.length > 0) {
    // Clear all files when switching
    fileItems.value.forEach(f => URL.revokeObjectURL(f.preview))
    fileItems.value = []
  }
  postType.value = type
}

async function handleSubmit() {
  if (!canPublish.value) return
  submitting.value = true
  try {
    const urls = fileItems.value.map(f => f.url!)
    await createPost({
      postType: postType.value,
      content: content.value || undefined,
      files: urls,
    })
    ElMessage.success('发布成功 💕')
    router.push('/')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="post-view fade-in" @paste="onPaste">
    <header class="post-header">
      <button class="back-link" @click="router.back()">
        <span>←</span> 返回
      </button>
      <h1>发布动态</h1>
    </header>

    <div class="post-card">
      <!-- Type Switcher -->
      <div class="type-switch">
        <label class="type-option" :class="{ active: postType === 'image' }">
          <input type="radio" name="postType" value="image" v-model="postType" @change="switchType('image')" />
          <span class="type-icon">🖼️</span>
          <span class="type-label">图片</span>
        </label>
        <label class="type-option" :class="{ active: postType === 'video' }">
          <input type="radio" name="postType" value="video" v-model="postType" @change="switchType('video')" />
          <span class="type-icon">🎬</span>
          <span class="type-label">视频</span>
        </label>
      </div>

      <!-- Upload Area -->
      <div class="upload-section">
        <input
          type="file"
          :accept="postType === 'image' ? 'image/*' : 'video/*'"
          multiple
          @change="handleFileChange"
          ref="fileInput"
          style="display:none"
        />

        <!-- Drop Zone (always visible for drag & drop + paste) -->
        <div
          ref="dropZone"
          class="drop-zone"
          :class="{
            dragging,
            'has-files': hasFiles,
            'all-done': allUploaded
          }"
          @click="!hasFiles && fileInput?.click()"
          @dragenter="onDragEnter"
          @dragover="onDragOver"
          @dragleave="onDragLeave"
          @drop="onDrop"
        >
          <!-- File Grid -->
          <div v-if="hasFiles" class="preview-grid" :class="{ video: postType === 'video' }">
            <div
              v-for="item in fileItems"
              :key="item.id"
              class="preview-item"
              :class="{ uploading: item.uploading, error: !!item.error }"
            >
              <img v-if="postType === 'image'" :src="item.preview" :alt="item.file.name" />
              <video v-else :src="item.preview" />

              <!-- Upload overlay -->
              <div v-if="item.uploading" class="upload-overlay">
                <div class="progress-ring">
                  <svg viewBox="0 0 36 36">
                    <circle cx="18" cy="18" r="15" fill="none" stroke="rgba(255,255,255,0.2)" stroke-width="3" />
                    <circle
                      cx="18" cy="18" r="15"
                      fill="none"
                      stroke="#fff"
                      stroke-width="3"
                      stroke-linecap="round"
                      :stroke-dasharray="94.2"
                      :stroke-dashoffset="94.2 - (94.2 * item.progress) / 100"
                      transform="rotate(-90 18 18)"
                    />
                  </svg>
                  <span class="progress-text">{{ Math.round(item.progress) }}%</span>
                </div>
              </div>

              <!-- Error overlay -->
              <div v-if="item.error" class="error-overlay" @click.stop="retryUpload(item)">
                <span class="error-icon">⚠️</span>
                <span class="error-text">点击重试</span>
              </div>

              <!-- Done check -->
              <div v-if="item.url && !item.uploading" class="done-badge">✓</div>

              <!-- Remove -->
              <button v-if="!item.uploading" class="remove-file" @click.stop="removeFile(item)" title="移除">✕</button>
            </div>

            <!-- Add more -->
            <button
              v-if="!allUploaded"
              class="add-more"
              @click.stop="fileInput?.click()"
            >
              <span>+</span>
              <span>添加</span>
            </button>
          </div>

          <!-- Empty state -->
          <div v-else class="upload-placeholder">
            <div class="upload-icon">📁</div>
            <div class="upload-text">点击选择或拖拽{{ postType === 'image' ? '图片' : '视频' }}到此处</div>
            <div class="upload-hint">
              {{ postType === 'image'
                ? `支持 jpg / png / gif，单张 ≤ 10MB，最多 9 张`
                : `支持 mp4 / webm，单个 ≤ 100MB，最多 3 个`
              }}
            </div>
            <div class="upload-hint paste-hint">也支持 Ctrl+V 粘贴</div>
          </div>
        </div>
      </div>

      <!-- Upload status bar -->
      <div v-if="anyUploading" class="status-bar">
        <span class="status-icon">☁️</span>
        <span>正在上传 {{ uploadedCount }}/{{ fileItems.length }} ...</span>
      </div>
      <div v-else-if="allUploaded && hasFiles" class="status-bar done">
        <span class="status-icon">✅</span>
        <span>全部上传完成，可以发布啦</span>
      </div>

      <div class="divider"></div>

      <!-- Content -->
      <div class="content-section">
        <label class="section-label">配文</label>
        <textarea
          v-model="content"
          placeholder="写下你想说的话... 💌"
          maxlength="500"
          rows="4"
          class="content-textarea"
        ></textarea>
        <div class="char-count">{{ content.length }} / 500</div>
      </div>

      <!-- Submit -->
      <button
        class="publish-btn"
        :disabled="!canPublish"
        @click="handleSubmit"
      >
        <span v-if="submitting" class="spinner"></span>
        <span v-else-if="anyUploading">⏳ 等待上传完成...</span>
        <span v-else-if="!hasFiles">👆 请先选择文件</span>
        <span v-else-if="!allUploaded">☁️ 文件上传中...</span>
        <span v-else>✨ 发布动态</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.post-view {
  max-width: 600px;
  margin: 0 auto;
  padding: 32px 28px 60px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
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

.post-header h1 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: 1px;
}

.post-card {
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-xl);
  padding: 28px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-md);
}

/* Type Switcher */
.type-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  background: rgba(232, 141, 125, 0.04);
  padding: 4px;
  border-radius: var(--radius-sm);
}

.type-option {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-soft);
  transition: all var(--transition);
}

.type-option input { display: none; }

.type-option.active {
  background: #fff;
  color: var(--color-primary-dark);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(180, 120, 90, 0.1);
}

.type-option:hover:not(.active) { color: var(--color-text); }

/* ===== Drop Zone ===== */
.drop-zone {
  border: 2px dashed var(--color-border-strong);
  border-radius: var(--radius-lg);
  background: rgba(255, 248, 242, 0.3);
  transition: all var(--transition);
  min-height: 140px;
}

.drop-zone.dragging {
  border-color: var(--color-primary);
  background: rgba(232, 141, 125, 0.08);
  transform: scale(1.01);
}

.drop-zone.has-files {
  border-style: solid;
  border-color: var(--color-border);
  background: transparent;
  min-height: auto;
  padding: 12px;
}

.drop-zone.all-done {
  border-color: rgba(143, 188, 143, 0.3);
  background: rgba(143, 188, 143, 0.04);
}

/* Upload Placeholder */
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 36px;
  cursor: pointer;
  color: var(--color-text-soft);
}

.upload-icon { font-size: 36px; }
.upload-text { font-size: 14px; font-weight: 500; }
.upload-hint { font-size: 12px; color: var(--color-text-muted); }

.paste-hint {
  margin-top: -4px;
  background: rgba(232, 141, 125, 0.08);
  padding: 3px 12px;
  border-radius: 10px;
  color: var(--color-primary-dark);
  font-weight: 500;
}

/* ===== Preview Grid ===== */
.preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.preview-grid.video { grid-template-columns: 1fr; }

.preview-item {
  position: relative;
  border-radius: var(--radius-md);
  overflow: hidden;
  aspect-ratio: 1;
  background: rgba(232, 141, 125, 0.06);
  border: 1px solid var(--color-border);
  transition: all var(--transition);
}

.preview-item img,
.preview-item video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-item.uploading {
  border-color: var(--color-accent);
}

.preview-item.error {
  border-color: var(--color-danger);
}

/* Upload Overlay */
.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(40, 28, 24, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-ring {
  position: relative;
  width: 48px;
  height: 48px;
}

.progress-ring svg {
  width: 100%;
  height: 100%;
}

.progress-text {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
}

/* Error Overlay */
.error-overlay {
  position: absolute;
  inset: 0;
  background: rgba(212, 117, 106, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  cursor: pointer;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.error-icon { font-size: 22px; }

/* Done Badge */
.done-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--color-success);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}

/* Remove */
.remove-file {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: none;
  background: rgba(74, 53, 48, 0.55);
  color: #fff;
  font-size: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.remove-file:hover {
  background: rgba(212, 117, 106, 0.85);
  transform: scale(1.1);
}

/* Add More */
.add-more {
  aspect-ratio: 1;
  border: 2px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-soft);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 18px;
  font-family: inherit;
  transition: all var(--transition);
}

.add-more:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: rgba(232, 141, 125, 0.04);
}

.add-more span:last-child { font-size: 11px; }

/* Status Bar */
.status-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 16px;
  border-radius: var(--radius-sm);
  background: rgba(242, 185, 135, 0.1);
  color: var(--color-text-soft);
  font-size: 13px;
  font-weight: 500;
}

.status-bar.done {
  background: rgba(143, 188, 143, 0.1);
  color: #6B9B6B;
}

.status-icon { font-size: 16px; }

/* Divider */
.divider {
  height: 1px;
  background: var(--color-border);
  margin: 20px 0;
}

/* Content */
.content-section { margin-bottom: 20px; }

.section-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text-soft);
  margin-bottom: 8px;
  letter-spacing: 0.5px;
}

.content-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  background: rgba(255, 248, 242, 0.5);
  font-size: 14px;
  color: var(--color-text);
  font-family: inherit;
  resize: none;
  outline: none;
  transition: all var(--transition);
  line-height: 1.7;
}

.content-textarea:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(232, 141, 125, 0.1);
  background: rgba(255, 252, 248, 0.8);
}

.content-textarea::placeholder { color: var(--color-text-muted); }

.char-count {
  text-align: right;
  font-size: 11px;
  color: var(--color-text-muted);
  margin-top: 6px;
}

/* Publish */
.publish-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  letter-spacing: 1px;
  transition: all var(--transition);
}

.publish-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}

.publish-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 768px) {
  .post-view {
    padding: 20px 16px 40px;
  }
  .post-card {
    padding: 20px;
  }
  .preview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
