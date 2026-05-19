<template>

  <div class="sci-auth-scene login-full">

    <div class="hud-corner hud-corner--tl" aria-hidden="true" />

    <div class="hud-corner hud-corner--br" aria-hidden="true" />



    <el-card class="login-card hud-panel" shadow="never">

      <template #header>

        <div class="card-header">

          <div class="code-tag">ACCESS_NODE // SECURE_TUNNEL</div>

          <h2>接入地面站</h2>

          <p>商城助手 · 低干扰链路</p>

        </div>

      </template>



      <div class="login-form">

        <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">

          <el-form-item label="用户名" prop="username">

            <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />

          </el-form-item>



          <el-form-item label="密码" prop="password">

            <el-input

              v-model="loginForm.password"

              type="password"

              placeholder="请输入密码"

              prefix-icon="Lock"

              show-password

            />

          </el-form-item>



          <el-form-item>

            <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">

              建立会话

            </el-button>

          </el-form-item>



          <div class="register-link">

            <span>无本地身份?</span>

            <el-link type="primary" @click="$router.push('/register')">注册节点</el-link>

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

  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],

  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]

})



const handleLogin = async () => {

  if (!loginFormRef.value) return



  try {

    await loginFormRef.value.validate()

    loading.value = true



    const res = await login(loginForm)

    userStore.setToken(res.data.token)

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

.login-full {

  min-height: 100vh;

}



.hud-corner {

  position: fixed;

  width: 120px;

  height: 120px;

  pointer-events: none;

  opacity: 0.45;



  &::before,

  &::after {

    content: '';

    position: absolute;

    background: var(--color-accent-cyan, #22d3ee);

    box-shadow: 0 0 14px rgba(34, 211, 238, 0.4);

  }



  &--tl {

    top: 24px;

    left: 24px;



    &::before {

      top: 0;

      left: 0;

      width: 100%;

      height: 2px;

    }



    &::after {

      top: 0;

      left: 0;

      width: 2px;

      height: 100%;

    }

  }



  &--br {

    bottom: 24px;

    right: 24px;



    &::before {

      bottom: 0;

      right: 0;

      width: 100%;

      height: 2px;

    }



    &::after {

      bottom: 0;

      right: 0;

      width: 2px;

      height: 100%;

    }

  }

}



.hud-panel {

  border: 1px solid var(--tech-panel-border, rgba(34, 211, 238, 0.22));

  border-radius: var(--radius-xl, 16px);

  background: rgba(3, 7, 18, 0.78) !important;

  backdrop-filter: blur(22px);

  box-shadow:

    inset 0 0 80px rgba(34, 211, 238, 0.04),

    var(--shadow-lg);

  overflow: visible;

}



.login-card {

  width: 100%;

  max-width: 420px;

  position: relative;

  z-index: 1;



  :deep(.el-card__header) {

    background: transparent;

    padding: 20px 24px 0;

    border-bottom: none !important;

  }



  .card-header {

    text-align: left;



    .code-tag {

      font-family: var(--font-mono, monospace);

      font-size: 11px;

      letter-spacing: 0.12em;

      color: var(--color-accent-green, #4ade80);

      margin-bottom: 12px;

      opacity: 0.92;

    }



    h2 {

      margin: 0 0 8px 0;

      font-size: 24px;

      font-weight: 700;

      color: var(--color-text-bold, #f1f5f9);

    }



    p {

      margin: 0;

      color: var(--color-text-weak, #64748b);

      font-size: 14px;

    }

  }



  .login-form {

    padding: 8px 24px 28px;

  }



  .login-btn {

    width: 100%;

    height: 46px;

    font-size: 15px;

  }



  .register-link {

    text-align: center;

    margin-top: 20px;

    color: var(--color-text, #cbd5e1);

    font-size: 13px;



    span {

      margin-right: 8px;

      color: var(--color-text-weak, #64748b);

    }

  }

}

</style>

