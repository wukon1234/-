<template>
  <el-container class="main-layout">
    <el-header :class="['header', { 'header--scrolled': navScrolled }]">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-mark">◈</span>
          <h2>MALL.OS</h2>
        </div>

        <el-menu
          v-show="!isMobile"
          :default-active="activeMenu"
          mode="horizontal"
          class="header-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/products">商品</el-menu-item>
          <el-menu-item index="/cart">购物车</el-menu-item>
          <el-menu-item index="/orders">订单</el-menu-item>
          <el-menu-item index="/address">地址</el-menu-item>
          <el-menu-item index="/profile">个人中心</el-menu-item>
          <el-menu-item index="/favorites">收藏</el-menu-item>
          <el-menu-item index="/assistant" class="menu-assistant">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能助手</span>
          </el-menu-item>
        </el-menu>

        <el-button v-show="isMobile" class="nav-toggle" text @click="mobileOpen = true">
          <el-icon :size="22"><Menu /></el-icon>
        </el-button>
      </div>
    </el-header>

    <el-drawer v-model="mobileOpen" direction="rtl" size="min(88vw, 320px)" title="导航">
      <el-menu :default-active="activeMenu" @select="handleMobileSelect">
        <el-menu-item index="/home">首页</el-menu-item>
        <el-menu-item index="/products">商品</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/orders">订单</el-menu-item>
        <el-menu-item index="/address">地址</el-menu-item>
        <el-menu-item index="/profile">个人中心</el-menu-item>
        <el-menu-item index="/favorites">收藏</el-menu-item>
        <el-menu-item index="/assistant">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能助手</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>

    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, Menu } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const navScrolled = ref(false)
const isMobile = ref(false)
const mobileOpen = ref(false)

const handleMenuSelect = (path: string) => {
  router.push(path)
}

const handleMobileSelect = (path: string) => {
  mobileOpen.value = false
  router.push(path)
}

const checkMobile = () => {
  isMobile.value = window.innerWidth <= 768
}

const onScroll = () => {
  navScrolled.value = window.scrollY > 8
}

onMounted(() => {
  checkMobile()
  onScroll()
  window.addEventListener('resize', checkMobile)
  window.addEventListener('scroll', onScroll, { passive: true })
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  --header-bg: rgba(7, 11, 20, 0.72);
  padding: 0;
  height: var(--header-height, 68px);
  transition: var(--transition, all 0.28s ease-out);
  position: sticky;
  top: 0;
  z-index: 1000;
  background: var(--header-bg);
  backdrop-filter: saturate(140%) blur(14px);
  -webkit-backdrop-filter: saturate(140%) blur(14px);
  border-bottom: 1px solid rgba(34, 211, 238, 0.12);
  box-shadow: 0 4px 28px rgba(0, 0, 0, 0.35);

  &.header--scrolled {
    --header-bg: rgba(7, 11, 20, 0.94);
    border-bottom-color: rgba(34, 211, 238, 0.28);
    box-shadow: 0 8px 36px rgba(0, 0, 0, 0.5);
  }

  .header-content {
    display: flex;
    align-items: center;
    height: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .logo {
    cursor: pointer;
    margin-right: 28px;
    display: flex;
    align-items: center;
    gap: 10px;
    transition: var(--transition, all 0.28s ease-out);

    &:hover {
      transform: translateY(-1px);
      filter: brightness(1.08);
    }

    .logo-mark {
      font-size: 14px;
      color: var(--color-accent-cyan, #22d3ee);
      text-shadow: 0 0 12px rgba(34, 211, 238, 0.65);
    }

    h2 {
      margin: 0;
      font-family: var(--font-mono, monospace);
      font-size: 17px;
      font-weight: 700;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--color-text-bold, #f1f5f9);
    }
  }

  .nav-toggle {
    margin-left: auto;
    color: var(--color-accent-cyan, #22d3ee);
  }

  .header-menu {
    flex: 1;
    border-bottom: none;
    background: transparent;
    min-width: 0;

    :deep(.el-menu-item) {
      position: relative;
      padding: 0 14px;
      font-size: 14px;
      font-weight: 600;
      transition: var(--transition, all 0.28s ease-out);
      color: var(--color-text, #cbd5e1) !important;
      background: transparent !important;
      border-radius: var(--radius-md, 8px);

      &:hover {
        color: var(--color-accent-cyan, #22d3ee) !important;
        background-color: rgba(34, 211, 238, 0.08) !important;
      }

      &.is-active {
        color: var(--color-accent-cyan, #22d3ee) !important;

        &::after {
          content: '';
          position: absolute;
          bottom: 2px;
          left: 50%;
          transform: translateX(-50%);
          width: 42px;
          height: 2px;
          background: var(--color-primary-gradient, linear-gradient(90deg, #22d3ee, #4ade80));
          border-radius: 2px;
          box-shadow: 0 0 10px rgba(34, 211, 238, 0.5);
        }
      }

      :deep(.el-icon) {
        margin-right: 6px;
      }
    }

    :deep(.menu-assistant) {
      border: 1px solid rgba(74, 222, 128, 0.35);
      margin-left: 12px;
      animation: assistant-glow 3s ease-in-out infinite;
    }
  }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  background: transparent;
  min-height: calc(100vh - var(--header-height, 68px));
  width: 100%;
  box-sizing: border-box;
}

@keyframes assistant-glow {
  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(74, 222, 128, 0.25);
  }
  50% {
    box-shadow: 0 0 0 7px rgba(74, 222, 128, 0);
  }
}
</style>
