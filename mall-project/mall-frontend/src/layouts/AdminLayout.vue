<template>
  <el-container class="admin-layout">
    <el-header class="admin-header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/admin')">
          <h2>商城管理系统</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              {{ userStore.userInfo?.username }} <el-icon class="el-icon--right"><arrow-down /></el-icon>
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
    
    <el-container>
      <el-aside width="200px" class="admin-aside">
        <el-menu
          :default-active="activeMenu"
          class="admin-menu"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
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
  // 确保key是字符串类型的路由路径
  if (typeof key === 'string') {
    if (key.startsWith('/')) {
      router.push(key)
    } else {
      // 对于非路由路径的key，忽略处理
      console.log('Non-route key selected:', key)
    }
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
  background-color: #363d44;
  color: white;
  height: 60px;
  line-height: 60px;
  padding: 0 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1400px;
    margin: 0 auto;
  }
  
  .logo {
    cursor: pointer;
    
    h2 {
      margin: 0;
      font-size: 20px;
      font-weight: 700;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }
  
  .header-right {
    .el-dropdown-link {
      color: white;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 8px;
      
      &:hover {
        color: #ffd04b;
      }
    }
  }
}

.admin-aside {
  background-color: #545c64;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  
  .admin-menu {
    height: 100%;
    border-right: none;
    
    :deep(.el-sub-menu__title) {
      padding: 0 20px;
      height: 60px;
      line-height: 60px;
      
      &:hover {
        background-color: #464c52;
      }
    }
    
    :deep(.el-menu-item) {
      padding: 0 20px 0 40px;
      height: 60px;
      line-height: 60px;
      
      &:hover {
        background-color: #464c52;
      }
    }
  }
}

.admin-main {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 60px);
}
</style>