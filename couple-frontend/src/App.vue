<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  const container = document.createElement('div')
  container.className = 'ambient-layer'
  document.body.appendChild(container)

  // Warm floating particles (firefly-like)
  for (let i = 0; i < 18; i++) {
    const glow = document.createElement('div')
    glow.className = 'glow-particle'
    glow.style.left = Math.random() * 100 + '%'
    glow.style.top = Math.random() * 100 + '%'
    glow.style.animationDelay = Math.random() * 8 + 's'
    glow.style.animationDuration = (Math.random() * 5 + 5) + 's'
    glow.style.opacity = String(Math.random() * 0.25 + 0.06)
    glow.style.width = glow.style.height = (Math.random() * 60 + 20) + 'px'
    container.appendChild(glow)
  }

  // Gentle falling petals
  for (let i = 0; i < 14; i++) {
    const petal = document.createElement('div')
    petal.className = 'petal'
    petal.style.left = Math.random() * 100 + '%'
    petal.style.animationDelay = Math.random() * 12 + 's'
    petal.style.animationDuration = (Math.random() * 8 + 8) + 's'
    petal.style.opacity = String(Math.random() * 0.3 + 0.08)
    petal.style.fontSize = (Math.random() * 12 + 8) + 'px'
    petal.textContent = ['🌸', '💛', '🌼', '✨', '🧡'][Math.floor(Math.random() * 5)]
    container.appendChild(petal)
  }
})
</script>

<template>
  <router-view />
</template>

<style>
/* ===== Design Tokens ===== */
:root {
  --color-bg-from: #FFF5EE;
  --color-bg-via: #FFF0E8;
  --color-bg-to: #FFE8DC;
  --color-primary: #E88D7D;
  --color-primary-dark: #D4756A;
  --color-primary-soft: #F5C4B3;
  --color-primary-light: #FDE8E0;
  --color-accent: #F2B987;
  --color-accent-soft: #FDE4CF;
  --color-accent-warm: #F5D08C;
  --color-surface: rgba(255, 252, 248, 0.78);
  --color-surface-hover: rgba(255, 252, 248, 0.92);
  --color-border: rgba(200, 155, 130, 0.15);
  --color-border-strong: rgba(200, 155, 130, 0.25);
  --color-text: #4A3530;
  --color-text-soft: #8B6F66;
  --color-text-muted: #B0A098;
  --color-danger: #D4756A;
  --color-success: #8FBC8F;
  --radius-sm: 10px;
  --radius-md: 16px;
  --radius-lg: 22px;
  --radius-xl: 28px;
  --shadow-sm: 0 2px 12px rgba(180, 120, 90, 0.06);
  --shadow-md: 0 4px 24px rgba(180, 120, 90, 0.08);
  --shadow-lg: 0 8px 40px rgba(180, 120, 90, 0.12);
  --shadow-glow: 0 0 30px rgba(232, 141, 125, 0.18);
  --transition: 0.25s cubic-bezier(0.4, 0, 0.2, 1);

  /* Element Plus overrides */
  --el-color-primary: #E88D7D;
  --el-color-primary-light-3: #F0B0A3;
  --el-color-primary-light-5: #F5C9BF;
  --el-color-primary-light-7: #FAE2DB;
  --el-color-primary-light-9: #FDF3F0;
  --el-color-primary-dark-2: #D4756A;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'PingFang SC', 'Hiragino Sans GB', 'Noto Sans SC', 'Microsoft YaHei', sans-serif;
  background: linear-gradient(160deg, var(--color-bg-from) 0%, var(--color-bg-via) 40%, var(--color-bg-to) 100%);
  min-height: 100vh;
  color: var(--color-text);
  overflow-x: hidden;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
  position: relative;
}

/* ===== Ambient Layer ===== */
.ambient-layer {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

/* Soft glowing orbs */
.glow-particle {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle,
    rgba(242, 185, 135, 0.5) 0%,
    rgba(242, 185, 135, 0.15) 40%,
    transparent 70%
  );
  animation: float-glow ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes float-glow {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.06;
  }
  33% {
    transform: translateY(-18px) scale(1.15);
    opacity: 0.14;
  }
  66% {
    transform: translateY(8px) scale(0.9);
    opacity: 0.08;
  }
}

/* Falling petals */
.petal {
  position: absolute;
  top: -30px;
  animation: petal-fall linear infinite;
  will-change: transform;
}

@keyframes petal-fall {
  0% {
    transform: translateY(-30px) rotate(0deg) translateX(0);
  }
  25% {
    transform: translateY(25vh) rotate(90deg) translateX(15px);
  }
  50% {
    transform: translateY(50vh) rotate(180deg) translateX(-10px);
  }
  75% {
    transform: translateY(75vh) rotate(270deg) translateX(12px);
  }
  100% {
    transform: translateY(105vh) rotate(360deg) translateX(0);
  }
}

/* ===== Scrollbar ===== */
::-webkit-scrollbar {
  width: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: var(--color-primary-soft);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: var(--color-primary);
}

/* ===== Element Plus Global Overrides ===== */
.el-button--primary {
  --el-button-bg-color: linear-gradient(135deg, #E88D7D, #D4756A);
  --el-button-border-color: transparent;
  --el-button-hover-bg-color: linear-gradient(135deg, #F0A090, #E08075);
  --el-button-hover-border-color: transparent;
  --el-button-active-bg-color: linear-gradient(135deg, #D4756A, #C0655A);
  border: none;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.el-button--primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(232, 141, 125, 0.35);
}

.el-button--primary:active {
  transform: translateY(0);
}

.el-input {
  --el-input-focus-border-color: #E88D7D;
}

.el-input__wrapper {
  border-radius: var(--radius-sm) !important;
  background: rgba(255, 248, 242, 0.7) !important;
  box-shadow: 0 0 0 1px var(--color-border) !important;
  transition: all var(--transition) !important;
}

.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--color-border-strong) !important;
}

.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px rgba(232, 141, 125, 0.3) !important;
  background: rgba(255, 252, 248, 0.9) !important;
}

.el-message {
  --el-message-bg-color: var(--color-surface);
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-md) !important;
  border: 1px solid var(--color-border) !important;
}

.el-message-box {
  border-radius: var(--radius-lg) !important;
  background: var(--color-surface) !important;
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-border) !important;
}

.el-overlay {
  background: rgba(74, 53, 48, 0.15) !important;
  backdrop-filter: blur(4px);
}

/* ===== Utility Classes ===== */
.fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
