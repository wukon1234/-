<template>
  <div class="admin-user-list">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
    </div>
    
    <el-card shadow="hover" class="content-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入用户名或邮箱"
          style="width: 300px; margin-right: 10px"
          clearable
        >
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        
        <el-select
          v-model="searchForm.status"
          placeholder="请选择状态"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        
        <el-select
          v-model="searchForm.role"
          placeholder="请选择角色"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="普通用户" :value="1" />
          <el-option label="管理员" :value="2" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="userList" stripe style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 2 ? 'warning' : 'primary'">
              {{ scope.row.role === 2 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="warning" size="small" @click="handleRoleChange(scope.row)">
              <el-icon><SwitchButton /></el-icon> {{ scope.row.role === 2 ? '取消管理员' : '设为管理员' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户"
      width="500px"
    >
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role">
            <el-option label="普通用户" :value="1" />
            <el-option label="管理员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="editForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveEdit">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Edit, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserList, updateUser, updateUserStatus, updateUserRole } from '@/api/user'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: undefined as number | undefined,
  role: undefined as number | undefined
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 5
})

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = reactive({
  id: 0,
  username: '',
  email: '',
  phone: '',
  role: 1,
  status: 1
})

// 加载状态
const loading = ref(false)

// 用户列表数据
const userList = ref([])

// 初始化加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const response = await getUserList({
      keyword: searchForm.keyword,
      status: searchForm.status,
      role: searchForm.role,
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    })
    userList.value = response.data
    pagination.total = response.data.length
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  loadUserList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = undefined
  searchForm.role = undefined
  handleSearch()
}

// 编辑用户
const handleEdit = (row: any) => {
  // 填充编辑表单
  editForm.id = row.id
  editForm.username = row.username
  editForm.email = row.email
  editForm.phone = row.phone
  editForm.role = row.role
  editForm.status = row.status
  // 显示编辑对话框
  editDialogVisible.value = true
}

// 保存编辑
const handleSaveEdit = async () => {
  loading.value = true
  try {
    await updateUser({
      id: editForm.id,
      email: editForm.email,
      phone: editForm.phone,
      role: editForm.role,
      status: editForm.status
    })
    ElMessage.success('用户信息更新成功')
    // 重新加载用户列表
    loadUserList()
  } catch (error) {
    ElMessage.error('更新用户信息失败')
    console.error('更新用户信息失败:', error)
  } finally {
    loading.value = false
    editDialogVisible.value = false
  }
}

// 切换用户状态
const handleStatusChange = async (row: any) => {
  loading.value = true
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success(`用户 ${row.username} 状态已更新`)
  } catch (error) {
    ElMessage.error('更新用户状态失败')
    console.error('更新用户状态失败:', error)
    // 恢复原始状态
    loadUserList()
  } finally {
    loading.value = false
  }
}

// 切换用户角色
const handleRoleChange = async (row: any) => {
  const newRole = row.role === 2 ? 1 : 2
  loading.value = true
  try {
    await updateUserRole(row.id, newRole)
    row.role = newRole
    ElMessage.success(`用户 ${row.username} 已${newRole === 2 ? '设为管理员' : '取消管理员'}`)
  } catch (error) {
    ElMessage.error('更新用户角色失败')
    console.error('更新用户角色失败:', error)
    // 重新加载用户列表
    loadUserList()
  } finally {
    loading.value = false
  }
}

// 分页大小变更
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadUserList()
}

// 当前页变更
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  loadUserList()
}

// 初始化加载
onMounted(() => {
  loadUserList()
})
</script>

<style scoped lang="scss">
.admin-user-list {
  .page-header {
    margin-bottom: 20px;
    
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: #333;
    }
  }
  
  .content-card {
    
    .search-bar {
      margin-bottom: 20px;
      display: flex;
      align-items: center;
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>