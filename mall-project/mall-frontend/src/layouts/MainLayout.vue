<template>
  <el-container class="main-layout">
    <el-header class="header">
      <div class="header-content">
        <div class="logo" @click="$router.push('/')">
          <h2>商城智能助手系统</h2>
        </div>
        <el-menu
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
          <el-menu-item index="/assistant">
            <el-icon><ChatDotRound /></el-icon>
            <span>智能助手</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const handleMenuSelect = (path: string) => {
  router.push(path)
}
</script>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background-color: #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 70px;
  transition: all 0.3s ease;
  position: sticky;
  top: 0;
  z-index: 1000;

  .header-content {
    display: flex;
    align-items: center;
    height: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;

    .logo {
      cursor: pointer;
      margin-right: 50px;
      transition: all 0.3s ease;

      h2 {
        margin: 0;
        color: #667eea;
        font-size: 22px;
        font-weight: 700;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        
        &:hover {
          opacity: 0.9;
        }
      }
    }

    .header-menu {
      flex: 1;
      border-bottom: none;
      background: transparent;
      
      :deep(.el-menu-item) {
        position: relative;
        padding: 0 20px;
        font-size: 15px;
        font-weight: 600;
        transition: all 0.3s ease;
        color: #333;
        
        &:hover {
          color: #667eea;
          background-color: rgba(102, 126, 234, 0.05);
        }
        
        &.is-active {
          color: #667eea;
          font-weight: 700;
          
          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 20px;
            height: 3px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 2px;
          }
        }
        
        :deep(.el-icon) {
          margin-right: 6px;
        }
      }
    }
  }
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #ffffff;
  min-height: calc(100vh - 70px);
  width: 100%;
  box-sizing: border-box;
}
</style>
