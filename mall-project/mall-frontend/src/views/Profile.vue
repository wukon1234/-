<template>
  <div class="profile-container">
    <h2 class="page-title">个人中心</h2>
    <el-row :gutter="20">
      <!-- 左侧导航 -->
      <el-col :span="6">
        <el-card shadow="hover" class="profile-sidebar">
          <el-menu
            :default-active="activeTab"
            class="profile-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item index="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-menu-item>
            <el-menu-item index="security">
              <el-icon><SwitchButton /></el-icon>
              <span>账号安全</span>
            </el-menu-item>
          </el-menu>
        </el-card>
      </el-col>
      
      <!-- 右侧内容 -->
      <el-col :span="18">
        <el-card shadow="hover" class="profile-content">
          <!-- 个人信息 -->
          <el-tabs v-model:active-name="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="个人信息" name="info">
              <el-form :model="userInfo" label-width="120px" class="profile-form">
                <el-form-item label="用户名">
                  <el-input v-model="userInfo.username" readonly placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="电话">
                  <el-input v-model="userInfo.phone" placeholder="请输入电话" />
                </el-form-item>
                <el-form-item label="注册时间">
                  <el-input v-model="userInfo.createTime" readonly placeholder="注册时间" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveUserInfo">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="120px" class="profile-form">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="updatePassword">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
            
            <!-- 账号安全 -->
            <el-tab-pane label="账号安全" name="security">
              <div class="security-section">
                <h3>登录历史</h3>
                <el-empty description="暂无登录历史" />
                
                <h3 class="mt-4">账号切换</h3>
                <el-button type="primary" @click="handleLogout">退出登录</el-button>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getUserInfo, updatePassword as updatePasswordApi } from '@/api/user'
import { User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const passwordFormRef = ref<FormInstance>()

// 标签页激活状态
const activeTab = ref('info')

// 用户信息
const userInfo = reactive({
  username: '',
  email: '',
  phone: '',
  createTime: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 初始化用户信息
const initUserInfo = async () => {
  if (userStore.token) {
    try {
      const res = await getUserInfo()
      Object.assign(userInfo, res.data)
    } catch (error) {
      console.error('获取用户信息失败', error)
    }
  }
}

// 保存用户信息
const saveUserInfo = () => {
  // 这里可以添加保存用户信息的逻辑
  console.log('保存用户信息', userInfo)
}

// 修改密码
const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await updatePasswordApi({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功')
        // 重置表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        passwordFormRef.value?.resetFields()
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      }
    }
  })
}

// 处理菜单选择
const handleMenuSelect = (index: string) => {
  activeTab.value = index
}

// 处理标签页切换
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 页面挂载时初始化用户信息
onMounted(() => {
  initUserInfo()
})
</script>

<style scoped lang="scss">
.profile-container {
  padding: 20px;
}

.page-title {
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 700;
  color: #333;
}

.profile-sidebar {
  height: fit-content;
}

.profile-menu {
  :deep(.el-menu-item) {
    padding: 12px 20px;
    font-size: 15px;
    font-weight: 600;
    transition: all 0.3s ease;
    color: #333;
    
    :deep(.el-icon) {
      margin-right: 8px;
    }
    
    &:hover {
      color: #667eea;
      background-color: rgba(102, 126, 234, 0.05);
    }
    
    &.is-active {
      color: #667eea;
      background-color: rgba(102, 126, 234, 0.1);
    }
  }
}

.profile-content {
  margin-left: 20px;
}

.profile-form {
  margin-top: 20px;
  
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}

.security-section {
  padding: 20px 0;
  
  h3 {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 20px;
    color: #333;
  }
  
  .mt-4 {
    margin-top: 24px;
  }
}
</style>