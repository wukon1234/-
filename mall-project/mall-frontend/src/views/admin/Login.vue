<template>
  <div class="sci-auth-scene admin-login-root">
    <div class="hud-corner hud-corner--tl" aria-hidden="true" />
    <div class="hud-corner hud-corner--br" aria-hidden="true" />

    <div class="login-form-wrapper hud-admin">
      <div class="login-header">
        <div class="code-tag">CONTROL_PLANE // PRIV_ROOT</div>
        <h2>管理轴心</h2>
        <p>高权限通道 · 请谨慎核对身份</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item prop="username" label="管理员 ID">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item prop="password" label="访问令牌">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading" block>
            授权进入
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { adminLogin } from '@/api/user'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await adminLogin(loginForm)
        userStore.setToken(res.data.token)
        userStore.setUserInfo({
          id: res.data.userId,
          username: res.data.username,
          avatar: res.data.avatar
        })
        ElMessage.success('登录成功')
        router.push('/admin/dashboard')
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.admin-login-root {
  min-height: 100vh;
}

.hud-corner {
  position: fixed;
  width: 120px;
  height: 120px;
  pointer-events: none;
  opacity: 0.4;

  &::before,
  &::after {
    content: '';
    position: absolute;
    background: var(--color-anomaly, #f59e0b);
    box-shadow: 0 0 12px rgba(245, 158, 11, 0.45);
  }

  &--tl {
    top: 22px;
    left: 22px;

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
    bottom: 22px;
    right: 22px;

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

.hud-admin {
  border: 1px solid rgba(245, 158, 11, 0.25);
  background: rgba(3, 7, 18, 0.82) !important;
  backdrop-filter: blur(22px);
  border-radius: var(--radius-xl, 16px);
  box-shadow:
    inset 0 0 72px rgba(245, 158, 11, 0.04),
    var(--shadow-lg);
  padding: 40px 36px;
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: left;
  margin-bottom: 24px;

  .code-tag {
    font-family: var(--font-mono, monospace);
    font-size: 11px;
    letter-spacing: 0.1em;
    color: var(--color-anomaly, #fbbf24);
    margin-bottom: 14px;
  }

  h2 {
    margin: 0 0 8px 0;
    font-size: 26px;
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
  width: 100%;

  .login-btn {
    margin-top: 8px;
    padding: 13px 0;
    font-size: 15px;
    font-weight: 700;
  }
}
</style>
