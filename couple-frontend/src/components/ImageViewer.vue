<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps<{
  images: string[]
  initialIndex?: number
}>()

const emit = defineEmits<{
  close: []
}>()

const visible = ref(false)
const current = ref(props.initialIndex || 0)
const touching = ref(false)
const touchStart = ref(0)
const touchDelta = ref(0)
const swiping = ref(false)

const canPrev = computed(() => current.value > 0)
const canNext = computed(() => current.value < props.images.length - 1)

function open(index: number = 0) {
  current.value = index
  visible.value = true
  document.body.style.overflow = 'hidden'
}

function close() {
  visible.value = false
  document.body.style.overflow = ''
  emit('close')
}

function goPrev() {
  if (canPrev.value) {
    swiping.value = true
    setTimeout(() => {
      current.value--
      swiping.value = false
    }, 50)
  }
}

function goNext() {
  if (canNext.value) {
    swiping.value = true
    setTimeout(() => {
      current.value++
      swiping.value = false
    }, 50)
  }
}

function goTo(index: number) {
  current.value = index
}

// Keyboard
function onKeydown(e: KeyboardEvent) {
  if (!visible.value) return
  switch (e.key) {
    case 'Escape': close(); break
    case 'ArrowLeft': goPrev(); break
    case 'ArrowRight': goNext(); break
  }
}

// Touch
function onTouchStart(e: TouchEvent) {
  touchStart.value = e.touches[0].clientX
  touching.value = true
  touchDelta.value = 0
}

function onTouchMove(e: TouchEvent) {
  if (!touching.value) return
  touchDelta.value = e.touches[0].clientX - touchStart.value
}

function onTouchEnd() {
  if (Math.abs(touchDelta.value) > 80) {
    if (touchDelta.value > 0) goPrev()
    else goNext()
  }
  touching.value = false
  touchDelta.value = 0
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
  document.body.style.overflow = ''
})

defineExpose({ open, close })
</script>

<template>
  <Teleport to="body">
    <Transition name="viewer-fade">
      <div v-if="visible" class="image-viewer" @click.self="close">
        <!-- Close button -->
        <button class="viewer-close" @click="close" title="关闭">
          <span>✕</span>
        </button>

        <!-- Counter -->
        <div class="viewer-counter">{{ current + 1 }} / {{ images.length }}</div>

        <!-- Prev -->
        <button v-if="canPrev" class="viewer-arrow prev" @click.stop="goPrev">
          <span>‹</span>
        </button>

        <!-- Image -->
        <div
          class="viewer-stage"
          @touchstart="onTouchStart"
          @touchmove="onTouchMove"
          @touchend="onTouchEnd"
        >
          <Transition :name="swiping ? '' : 'viewer-slide'" mode="out-in">
            <div class="viewer-image-wrap" :key="current">
              <img
                :src="images[current]"
                :alt="'image ' + (current + 1)"
                class="viewer-image"
                :style="touching ? { transform: `translateX(${touchDelta}px)` } : {}"
                draggable="false"
              />
            </div>
          </Transition>
        </div>

        <!-- Next -->
        <button v-if="canNext" class="viewer-arrow next" @click.stop="goNext">
          <span>›</span>
        </button>

        <!-- Thumbnails -->
        <div class="viewer-thumbs" v-if="images.length > 1">
          <button
            v-for="(url, i) in images"
            :key="i"
            class="viewer-thumb"
            :class="{ active: i === current }"
            @click="goTo(i)"
          >
            <img :src="url" :alt="'thumb ' + i" loading="lazy" />
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.image-viewer {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(40, 28, 24, 0.94);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  user-select: none;
  -webkit-user-select: none;
}

/* Close */
.viewer-close {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
}

.viewer-close:hover {
  background: rgba(255, 255, 255, 0.22);
  transform: scale(1.08);
}

/* Counter */
.viewer-counter {
  position: fixed;
  top: 24px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  background: rgba(255, 255, 255, 0.08);
  padding: 6px 16px;
  border-radius: 20px;
}

/* Arrows */
.viewer-arrow {
  position: fixed;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition);
  font-family: 'Georgia', serif;
  line-height: 1;
  padding-bottom: 2px;
}

.viewer-arrow:hover {
  background: rgba(255, 255, 255, 0.22);
  transform: translateY(-50%) scale(1.08);
}

.viewer-arrow.prev { left: 20px; }
.viewer-arrow.next { right: 20px; }

.viewer-arrow.prev span { margin-right: 2px; }
.viewer-arrow.next span { margin-left: 2px; }

/* Stage */
.viewer-stage {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  padding: 60px 80px 100px;
}

.viewer-image-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 100%;
  max-height: 100%;
}

.viewer-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 16px 60px rgba(0, 0, 0, 0.4);
  transition: transform 0.15s ease;
}

/* Thumbnails */
.viewer-thumbs {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.viewer-thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  padding: 0;
  background: none;
  transition: all var(--transition);
  opacity: 0.5;
  flex-shrink: 0;
}

.viewer-thumb.active {
  border-color: var(--color-primary);
  opacity: 1;
}

.viewer-thumb:hover {
  opacity: 0.85;
}

.viewer-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* Transitions */
.viewer-fade-enter-active,
.viewer-fade-leave-active {
  transition: opacity 0.3s ease;
}

.viewer-fade-enter-from,
.viewer-fade-leave-to {
  opacity: 0;
}

.viewer-slide-enter-active {
  transition: all 0.25s ease-out;
}

.viewer-slide-leave-active {
  transition: all 0.2s ease-in;
}

.viewer-slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.viewer-slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* Responsive */
@media (max-width: 768px) {
  .viewer-stage {
    padding: 60px 16px 100px;
  }
  .viewer-arrow {
    width: 36px;
    height: 36px;
    font-size: 22px;
  }
  .viewer-arrow.prev { left: 8px; }
  .viewer-arrow.next { right: 8px; }
  .viewer-image {
    max-height: 60vh;
  }
  .viewer-thumbs {
    bottom: 16px;
    gap: 6px;
    padding: 6px 10px;
  }
  .viewer-thumb {
    width: 40px;
    height: 40px;
  }
}
</style>
