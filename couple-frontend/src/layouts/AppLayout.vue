<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCoupleStatus } from '@/api/couple'

const router = useRouter()
const route = useRoute()
const store = useUserStore()
const collapsed = ref(false)
const partnerInfo = ref<any>(null)

// Extensible navigation items — just add an entry here to create a new feature
const navItems = [
  { path: '/',         icon: '🏠', label: '首页',     badge: '' },
  { path: '/memorial', icon: '📅', label: '纪念日',   badge: '' },
  { path: '/wish',     icon: '💝', label: '心愿单',   badge: '' },
  { path: '/bind',     icon: '💑', label: '情侣绑定', badge: '' },
  { path: '/settings', icon: '⚙️', label: '设置',     badge: '' },
]

const isActive = (path: string) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}

function navigate(path: string) {
  router.push(path)
}

// Fetch partner info for sidebar
async function fetchPartner() {
  try {
    partnerInfo.value = await getCoupleStatus()
  } catch { /* ignore */ }
}
fetchPartner()
</script>

<template>
  <div class="app-shell">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed }">
      <!-- Brand -->
      <div class="sidebar-brand" @click="router.push('/')">
        <div class="brand-icon">💕</div>
        <div class="brand-text" v-show="!collapsed">
          <span class="brand-name">情侣空间</span>
          <span class="brand-sub">Our Sweet Space</span>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="sidebar-nav">
        <button
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="navigate(item.path)"
          :title="collapsed ? item.label : ''"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label" v-show="!collapsed">{{ item.label }}</span>
          <span
            v-if="item.badge && !collapsed"
            class="nav-badge"
            :class="{ soon: item.badge === 'soon' }"
          >
            {{ item.badge === 'soon' ? '即将' : item.badge }}
          </span>
        </button>
      </nav>

      <!-- Partner Status -->
      <div class="sidebar-partner" v-if="partnerInfo?.bound && !collapsed">
        <div class="partner-divider"></div>
        <div class="partner-card">
          <div class="partner-avatars">
            <div class="avatar-ring">
              <span class="avatar-placeholder">{{ store.user?.nickname?.charAt(0) || '我' }}</span>
            </div>
            <span class="partner-heart">💗</span>
            <div class="avatar-ring">
              <span class="avatar-placeholder">{{ partnerInfo.partnerNickname?.charAt(0) || 'TA' }}</span>
            </div>
          </div>
          <div class="partner-names">
            <span>{{ partnerInfo.partnerNickname || 'TA' }}</span>
          </div>
          <div class="partner-city" v-if="partnerInfo.partnerCity">
            <span class="city-dot"></span>{{ partnerInfo.partnerCity }}
          </div>
        </div>
      </div>

      <!-- User -->
      <div class="sidebar-user">
        <div class="user-avatar">
          {{ store.user?.nickname?.charAt(0) || '💕' }}
        </div>
        <div class="user-info" v-show="!collapsed">
          <div class="user-name">{{ store.user?.nickname || '用户' }}</div>
          <div class="user-role">{{ store.user?.city || '幸福进行中' }}</div>
        </div>
        <button
          class="collapse-btn"
          @click="collapsed = !collapsed"
          :title="collapsed ? '展开菜单' : '收起菜单'"
        >
          {{ collapsed ? '▶' : '◀' }}
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <main class="main-area">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
/* ===== Shell ===== */
.app-shell {
  display: flex;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* ===== Sidebar ===== */
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: 240px;
  background: linear-gradient(180deg,
    rgba(255, 250, 245, 0.88) 0%,
    rgba(255, 245, 238, 0.92) 50%,
    rgba(255, 240, 230, 0.9) 100%
  );
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  padding: 0;
  z-index: 200;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 2px 0 30px rgba(180, 120, 90, 0.06);
}

.sidebar.collapsed {
  width: 72px;
}

/* Brand */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
  cursor: pointer;
  user-select: none;
  transition: opacity var(--transition);
}
.sidebar-brand:hover {
  opacity: 0.8;
}
.brand-icon {
  font-size: 32px;
  flex-shrink: 0;
  animation: heartbeat 2s ease-in-out infinite;
}
@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  10% { transform: scale(1.12); }
  20% { transform: scale(1); }
  30% { transform: scale(1.08); }
  40% { transform: scale(1); }
}
.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  white-space: nowrap;
  overflow: hidden;
}
.brand-name {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: 1px;
}
.brand-sub {
  font-size: 11px;
  color: var(--color-text-muted);
  letter-spacing: 0.5px;
}

/* Navigation */
.sidebar-nav {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 11px 14px;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  cursor: pointer;
  transition: all var(--transition);
  color: var(--color-text-soft);
  font-size: 14px;
  font-family: inherit;
  position: relative;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(232, 141, 125, 0.08);
  color: var(--color-text);
}

.nav-item.active {
  background: linear-gradient(135deg, rgba(232, 141, 125, 0.12), rgba(242, 185, 135, 0.1));
  color: var(--color-primary-dark);
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(232, 141, 125, 0.15);
}

.nav-item:active {
  transform: scale(0.97);
}

.nav-icon {
  font-size: 20px;
  flex-shrink: 0;
  width: 28px;
  text-align: center;
  transition: transform var(--transition);
}

.nav-item.active .nav-icon {
  transform: scale(1.1);
}

.nav-label {
  flex: 1;
  text-align: left;
}

.nav-badge {
  font-size: 10px;
  padding: 2px 8px;
  border-radius: 10px;
  background: var(--color-primary-light);
  color: var(--color-primary-dark);
  font-weight: 600;
}
.nav-badge.soon {
  background: rgba(242, 185, 135, 0.2);
  color: #B88A6A;
}

/* Partner card in sidebar */
.sidebar-partner {
  padding: 0 16px;
}
.partner-divider {
  height: 1px;
  background: var(--color-border);
  margin-bottom: 12px;
}
.partner-card {
  background: linear-gradient(135deg,
    rgba(232, 141, 125, 0.08),
    rgba(242, 185, 135, 0.06)
  );
  border-radius: var(--radius-md);
  padding: 14px;
  text-align: center;
  border: 1px solid var(--color-border);
  margin-bottom: 12px;
}
.partner-avatars {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 8px;
}
.avatar-ring {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(232, 141, 125, 0.25);
}
.partner-heart {
  font-size: 14px;
  animation: heartbeat 2s ease-in-out infinite;
}
.partner-names {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}
.partner-city {
  font-size: 11px;
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
.city-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 0.4; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

/* User section */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-top: 1px solid var(--color-border);
  margin-top: auto;
}
.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent), var(--color-primary));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 600;
  flex-shrink: 0;
}
.user-info {
  flex: 1;
  overflow: hidden;
}
.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.user-role {
  font-size: 11px;
  color: var(--color-text-soft);
}
.collapse-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--color-text-muted);
  font-size: 10px;
  padding: 4px;
  border-radius: 6px;
  transition: all var(--transition);
  flex-shrink: 0;
}
.collapse-btn:hover {
  background: rgba(232, 141, 125, 0.08);
  color: var(--color-text-soft);
}

/* ===== Main Area ===== */
.main-area {
  flex: 1;
  margin-left: 240px;
  min-height: 100vh;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.sidebar.collapsed ~ .main-area {
  margin-left: 72px;
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .sidebar {
    width: 72px;
  }
  .sidebar .brand-text,
  .sidebar .nav-label,
  .sidebar .nav-badge,
  .sidebar .user-info,
  .sidebar .sidebar-partner,
  .sidebar .collapse-btn {
    display: none;
  }
  .sidebar-brand {
    padding: 16px;
    justify-content: center;
  }
  .nav-item {
    justify-content: center;
    padding: 12px;
  }
  .sidebar-user {
    justify-content: center;
    padding: 12px;
  }
  .main-area {
    margin-left: 72px;
  }
}
</style>
