<template>
  <el-container class="admin-layout">
    <el-header class="admin-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/admin')">
          <span class="logo-mark">▍</span>
          <h2>ADMIN.OS</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ userStore.userInfo?.username }}
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-container class="admin-body">
      <el-aside width="220px" class="admin-aside">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-sub-menu index="products">
            <template #title>
              <el-icon><Box /></el-icon>
              <span>商品管理</span>
            </template>
            <el-menu-item index="/admin/products">商品列表</el-menu-item>
            <el-menu-item index="/admin/products/add">添加商品</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="orders">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </template>
            <el-menu-item index="/admin/orders">订单列表</el-menu-item>
            <el-menu-item index="/admin/orders/analysis">订单分析</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="users">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/users">用户列表</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/assistant">
            <el-icon><ChatLineSquare /></el-icon>
            <span>智能助手管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { DataAnalysis, Box, Document, User, ArrowDown, ChatLineSquare } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const handleMenuSelect = (key: string, _keyPath: string[]) => {
  if (typeof key === 'string' && key.startsWith('/')) {
    router.push(key)
  }
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/admin/login')
  } else if (command === 'profile') {
    router.push('/admin/profile')
  }
}
</script>

<style scoped lang="scss">
.admin-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  height: var(--header-height, 68px);
  padding: 0 24px;
  flex-shrink: 0;
  background: rgba(7, 11, 20, 0.88);
  backdrop-filter: saturate(140%) blur(12px);
  -webkit-backdrop-filter: saturate(140%) blur(12px);
  border-bottom: 1px solid rgba(34, 211, 238, 0.14);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1760px;
    margin: 0 auto;
    height: 100%;
  }

  .logo {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;

    .logo-mark {
      color: var(--color-accent-cyan, #22d3ee);
      opacity: 0.75;
      font-size: 20px;
    }

    h2 {
      margin: 0;
      font-family: var(--font-mono, monospace);
      font-size: 16px;
      font-weight: 700;
      letter-spacing: 0.12em;
      color: var(--color-text-bold, #f1f5f9);
    }
  }

  .header-right {
    .el-dropdown-link {
      color: var(--color-accent-cyan, #22d3ee);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      font-family: var(--font-mono, monospace);
      font-weight: 600;
      font-size: 13px;
      letter-spacing: 0.06em;
      padding: 8px 12px;
      border-radius: var(--radius-md, 8px);
      border: 1px solid rgba(34, 211, 238, 0.18);
      background: rgba(15, 23, 42, 0.6);
      transition: var(--transition, all 0.28s ease-out);

      &:hover {
        border-color: rgba(34, 211, 238, 0.45);
        box-shadow: 0 0 16px rgba(34, 211, 238, 0.2);
      }
    }
  }
}

.admin-body {
  flex: 1;
  min-height: 0;
}

.admin-aside {
  background: rgba(11, 18, 36, 0.95);
  border-right: 1px solid rgba(34, 211, 238, 0.1);

  .admin-menu {
    height: 100%;
    border-right: none;
    padding: 14px 8px;
    background: transparent;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      border-radius: var(--radius-md, 8px);
      margin: 4px 0;
      color: var(--color-text, #cbd5e1);
      font-weight: 500;
    }

    :deep(.el-menu-item:hover),
    :deep(.el-sub-menu__title:hover) {
      background: rgba(34, 211, 238, 0.08) !important;
      color: var(--color-accent-cyan, #22d3ee) !important;
    }

    :deep(.el-menu-item.is-active) {
      background: linear-gradient(
        135deg,
        rgba(34, 211, 238, 0.16) 0%,
        rgba(74, 222, 128, 0.1) 100%
      ) !important;
      color: var(--color-accent-green, #4ade80) !important;
      font-weight: 700;
      box-shadow: inset 0 0 0 1px rgba(34, 211, 238, 0.2);
    }

    :deep(.el-menu--inline) {
      background: transparent !important;
    }

    :deep(.el-menu-item) {
      min-width: auto;
    }
  }
}

.admin-main {
  background: transparent;
  padding: 24px;
  min-height: calc(100vh - var(--header-height, 68px));
  overflow: auto;
}
</style>
