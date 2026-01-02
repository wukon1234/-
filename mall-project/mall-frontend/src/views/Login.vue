<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>欢迎登录</h2>
          <p>商城智能助手系统</p>
        </div>
      </template>
      
      <div class="login-form">
        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">
              登录
            </el-button>
          </el-form-item>
          
          <div class="register-link">
            <span>还没有账号？</span>
            <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const res = await login(loginForm)
    userStore.setToken(res.data.token)
    // 后端返回的是直接的用户信息，不是包含在user对象中
    userStore.setUserInfo({
      id: res.data.userId,
      username: res.data.username,
      avatar: res.data.avatar
    })
    localStorage.setItem('userId', String(res.data.userId))
    
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  
  .card-header {
    text-align: center;
    padding: 20px 0;
    
    h2 {
      margin: 0 0 10px 0;
      color: #333;
      font-size: 24px;
    }
    
    p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }
  }
  
  .login-form {
    padding: 20px 30px 30px;
  }
  
  .login-btn {
    width: 100%;
    height: 40px;
    font-size: 16px;
  }
  
  .register-link {
    text-align: center;
    margin-top: 20px;
    color: #666;
    
    span {
      margin-right: 8px;
    }
  }
}
</style>