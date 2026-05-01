<template>
  <div class="sci-auth-scene login-full">
    <div class="hud-corner hud-corner--tl" aria-hidden="true" />
    <div class="hud-corner hud-corner--br" aria-hidden="true" />

    <el-card class="register-card hud-panel" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="code-tag">PROVISION_ACCOUNT // SYNC_TRUST_ZONE</div>
          <h2>登记新节点</h2>
          <p>生成本地身份并完成握手</p>
        </div>
      </template>

      <div class="register-form">
        <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-position="top">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="registerForm.username" placeholder="请输入用户名" prefix-icon="User" />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="Message" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" class="register-btn" @click="handleRegister" :loading="loading">
              完成注册
            </el-button>
          </el-form-item>

          <div class="login-link">
            <span>已有会话?</span>
            <el-link type="primary" @click="$router.push('/login')">返回接入</el-link>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const registerRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度在 6 到 30 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
    loading.value = true

    await register({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email
    })

    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '注册失败')
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
    inset 0 0 80px rgba(74, 222, 128, 0.03),
    var(--shadow-lg);
  overflow: visible;
}

.register-card {
  width: 100%;
  max-width: 440px;

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
      letter-spacing: 0.1em;
      color: var(--color-accent-green, #4ade80);
      margin-bottom: 12px;
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

  .register-form {
    padding: 8px 24px 28px;
  }

  .register-btn {
    width: 100%;
    height: 46px;
    font-size: 15px;
  }

  .login-link {
    text-align: center;
    margin-top: 20px;
    color: var(--color-text-weak, #64748b);
    font-size: 13px;

    span {
      margin-right: 8px;
    }
  }
}
</style>
