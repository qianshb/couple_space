<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPostList, deletePost } from '@/api/post'
import { ElMessage, ElMessageBox } from 'element-plus'
import ImageViewer from '@/components/ImageViewer.vue'

const router = useRouter()
const store = useUserStore()
const posts = ref<any[]>([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

// Lightbox
const viewer = ref<InstanceType<typeof ImageViewer>>()
const viewerImages = ref<string[]>([])
const viewerIndex = ref(0)

function openViewer(images: string[], index: number) {
  viewerImages.value = images
  viewerIndex.value = index
  viewer.value?.open(index)
}

async function fetchPosts() {
  if (loading.value) return
  loading.value = true
  try {
    const list = await getPostList(page.value)
    if (list.length > 0) {
      posts.value.push(...list)
      page.value++
    } else {
      noMore.value = true
    }
  } catch {
    posts.value = []
  } finally {
    loading.value = false
  }
}

async function handleDelete(post: any) {
  try {
    await ElMessageBox.confirm('确定删除这条动态吗？', '提示', { type: 'warning' })
    await deletePost(post.id)
    posts.value = posts.value.filter(p => p.id !== post.id)
    ElMessage.success('已删除')
  } catch {
    // cancel
  }
}

// Get grid class based on image count
function gridClass(count: number) {
  if (count === 1) return 'grid-1'
  if (count === 2) return 'grid-2'
  if (count === 3) return 'grid-3'
  if (count === 4) return 'grid-4'
  return 'grid-multi'
}

onMounted(fetchPosts)
</script>

<template>
  <div class="home-view fade-in">
    <header class="page-header">
      <div>
        <h1>甜蜜动态</h1>
        <p>记录我们的每一个瞬间</p>
      </div>
      <button class="create-btn" @click="router.push('/post')">
        <span>+</span> 发布动态
      </button>
    </header>

    <!-- Empty State -->
    <div v-if="posts.length === 0 && !loading" class="empty-state">
      <div class="empty-illustration">
        <div class="empty-frame">📷</div>
        <div class="empty-sparkles">✨</div>
      </div>
      <h2>还没有动态</h2>
      <p>发布属于你们的第一张照片吧</p>
      <button class="empty-btn" @click="router.push('/post')">✨ 发布第一条动态</button>
    </div>

    <!-- Timeline Feed -->
    <div v-if="posts.length > 0" class="feed-timeline">
      <div class="timeline-line"></div>
      <div
        v-for="(post, idx) in posts"
        :key="post.id"
        class="post-card"
        :style="{ animationDelay: idx * 0.05 + 's' }"
      >
        <div class="timeline-dot">
          <span>{{ idx === 0 ? '💕' : '🌸' }}</span>
        </div>

        <div class="post-inner">
          <!-- Post Header -->
          <div class="post-head">
            <div class="post-author">
              <div class="author-avatar">
                {{ post.nickname?.charAt(0) || '?' }}
              </div>
              <div>
                <div class="author-name">{{ post.nickname }}</div>
                <div class="author-time">{{ post.createdAt?.slice(0, 10) }}</div>
              </div>
            </div>
            <button
              v-if="post.userId === store.user?.id"
              class="delete-btn"
              @click="handleDelete(post)"
              title="删除"
            >
              🗑
            </button>
          </div>

          <!-- Content -->
          <div class="post-body" v-if="post.content">{{ post.content }}</div>

          <!-- Images -->
          <div class="post-media" v-if="post.files?.length && post.postType === 'image'">
            <div class="media-grid" :class="gridClass(post.files.length)">
              <div
                v-for="(url, i) in post.files"
                :key="i"
                class="media-item"
                @click="openViewer(post.files, i)"
              >
                <img :src="url" :alt="'photo ' + (i + 1)" loading="lazy" />
                <!-- Show "+N" overlay on the last visible image when > 4 -->
                <div
                  v-if="post.files.length > 4 && i === 3"
                  class="more-overlay"
                >
                  <span>+{{ post.files.length - 4 }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Videos -->
          <div class="post-media" v-if="post.files?.length && post.postType === 'video'">
            <div class="video-list">
              <video
                v-for="(url, i) in post.files"
                :key="i"
                :src="url"
                controls
                preload="metadata"
                class="post-video"
              />
            </div>
          </div>
        </div>
      </div>

      <!-- Load More -->
      <div class="load-more" v-if="posts.length > 0">
        <button v-if="!noMore" :disabled="loading" class="load-btn" @click="fetchPosts">
          {{ loading ? '加载中...' : '查看更多 💕' }}
        </button>
        <span v-else class="no-more">— 已经到底啦 —</span>
      </div>
    </div>

    <!-- Lightbox -->
    <ImageViewer
      ref="viewer"
      :images="viewerImages"
      :initial-index="viewerIndex"
    />
  </div>
</template>

<style scoped>
.home-view {
  max-width: 680px;
  margin: 0 auto;
  padding: 32px 28px 60px;
}

/* Page Header */
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
  transition: all var(--transition);
  font-family: inherit;
  white-space: nowrap;
}

.create-btn span { font-size: 18px; line-height: 1; }

.create-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.3);
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 24px;
  background: var(--color-surface);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border);
}

.empty-illustration {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.empty-frame { font-size: 64px; filter: grayscale(0.2); }

.empty-sparkles {
  position: absolute;
  top: -8px;
  right: -12px;
  font-size: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

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

/* Timeline */
.feed-timeline {
  position: relative;
  padding-left: 32px;
}

.timeline-line {
  position: absolute;
  left: 11px;
  top: 18px;
  bottom: 18px;
  width: 2px;
  background: linear-gradient(180deg,
    var(--color-primary-soft) 0%,
    var(--color-border) 50%,
    var(--color-border) 100%
  );
  border-radius: 1px;
}

.post-card {
  position: relative;
  margin-bottom: 18px;
  animation: cardSlideIn 0.4s ease-out both;
}

@keyframes cardSlideIn {
  from { opacity: 0; transform: translateX(-8px); }
  to { opacity: 1; transform: translateX(0); }
}

.timeline-dot {
  position: absolute;
  left: -29px;
  top: 14px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-surface);
  border: 2px solid var(--color-primary-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  z-index: 2;
}

.post-inner {
  background: var(--color-surface);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: var(--radius-lg);
  padding: 18px 20px;
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition);
}

.post-inner:hover {
  box-shadow: var(--shadow-md);
  border-color: var(--color-border-strong);
}

/* Post Head */
.post-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), var(--color-primary));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(232, 141, 125, 0.2);
}

.author-name { font-weight: 600; font-size: 14px; color: var(--color-text); }
.author-time { font-size: 12px; color: var(--color-text-muted); margin-top: 2px; }

.delete-btn {
  background: none;
  border: none;
  font-size: 15px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  opacity: 0;
  transition: all var(--transition);
}

.post-inner:hover .delete-btn { opacity: 0.5; }

.delete-btn:hover {
  opacity: 1 !important;
  background: rgba(212, 117, 106, 0.08);
}

/* Post Body */
.post-body {
  margin-top: 14px;
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text);
  white-space: pre-wrap;
}

/* Media Grid */
.post-media { margin-top: 14px; }

.media-grid {
  display: grid;
  gap: 6px;
}

/* 1 image: full width, taller */
.media-grid.grid-1 {
  grid-template-columns: 1fr;
}
.media-grid.grid-1 .media-item {
  aspect-ratio: 4 / 3;
}

/* 2 images: side by side */
.media-grid.grid-2 {
  grid-template-columns: repeat(2, 1fr);
}
.media-grid.grid-2 .media-item {
  aspect-ratio: 1;
}

/* 3 images: one large left, two stacked right */
.media-grid.grid-3 {
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
}
.media-grid.grid-3 .media-item:first-child {
  grid-row: 1 / 3;
  aspect-ratio: auto;
}
.media-grid.grid-3 .media-item:not(:first-child) {
  aspect-ratio: 1;
}

/* 4 images: 2x2 */
.media-grid.grid-4 {
  grid-template-columns: repeat(2, 1fr);
}
.media-grid.grid-4 .media-item {
  aspect-ratio: 1;
}

/* 5+: 3 column with +N overlay */
.media-grid.grid-multi {
  grid-template-columns: repeat(3, 1fr);
}
.media-grid.grid-multi .media-item {
  aspect-ratio: 1;
}

.media-item {
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: transform var(--transition);
  background: rgba(232, 141, 125, 0.06);
}

.media-item:hover {
  transform: scale(1.02);
}

.media-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.more-overlay {
  position: absolute;
  inset: 0;
  background: rgba(40, 28, 24, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  font-weight: 700;
}

/* Videos */
.video-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.post-video {
  width: 100%;
  border-radius: var(--radius-md);
  max-height: 380px;
  background: #000;
  outline: none;
}

/* Load More */
.load-more {
  text-align: center;
  padding: 24px 0 0;
  margin-left: -32px;
}

.load-btn {
  padding: 10px 28px;
  border: 1px solid var(--color-border-strong);
  border-radius: 20px;
  background: var(--color-surface);
  color: var(--color-text-soft);
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition);
}

.load-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: rgba(232, 141, 125, 0.04);
}

.load-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.no-more {
  color: var(--color-text-muted);
  font-size: 13px;
}

/* Responsive */
@media (max-width: 768px) {
  .home-view {
    padding: 20px 16px 40px;
  }
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  .page-header h1 { font-size: 22px; }
  .create-btn {
    align-self: stretch;
    justify-content: center;
  }
  .feed-timeline { padding-left: 24px; }
  .timeline-dot {
    left: -21px;
    width: 20px;
    height: 20px;
    font-size: 10px;
  }
  .timeline-line { left: 9px; }
  .post-inner { padding: 14px 16px; }
}
</style>
