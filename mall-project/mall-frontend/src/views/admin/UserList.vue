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
          <el-option label="启用" value="1" />
          <el-option label="禁用" value="0" />
        </el-select>
        
        <el-select
          v-model="searchForm.role"
          placeholder="请选择角色"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="普通用户" value="1" />
          <el-option label="管理员" value="2" />
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
              active-value="1"
              inactive-value="0"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { Search, Edit, SwitchButton } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  role: ''
})

// 用户列表数据
const userList = ref([
  { id: 1, username: 'admin', email: 'admin@example.com', phone: '13800138000', role: 2, status: 1, createTime: '2024-01-01 10:00:00' },
  { id: 2, username: 'test1', email: 'test1@example.com', phone: '13800138001', role: 1, status: 1, createTime: '2024-01-02 14:30:00' },
  { id: 3, username: 'test2', email: 'test2@example.com', phone: '13800138002', role: 1, status: 1, createTime: '2024-01-03 09:15:00' },
  { id: 4, username: 'test3', email: 'test3@example.com', phone: '13800138003', role: 1, status: 0, createTime: '2024-01-04 16:45:00' },
  { id: 5, username: 'test4', email: 'test4@example.com', phone: '13800138004', role: 1, status: 1, createTime: '2024-01-05 11:20:00' }
])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 5
})

// 搜索
const handleSearch = () => {
  console.log('搜索条件', searchForm)
  // 这里可以添加搜索逻辑
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.role = ''
  handleSearch()
}

// 编辑用户
const handleEdit = (row: any) => {
  console.log('编辑用户', row)
  // 这里可以添加编辑用户逻辑
}

// 切换用户状态
const handleStatusChange = (row: any) => {
  console.log('用户状态变更', row)
  // 这里可以添加状态变更逻辑
}

// 切换用户角色
const handleRoleChange = (row: any) => {
  row.role = row.role === 2 ? 1 : 2
  ElMessage.success(`用户 ${row.username} 已${row.role === 2 ? '设为管理员' : '取消管理员'}`)
  // 这里可以添加角色变更逻辑
}

// 分页大小变更
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  // 这里可以添加分页逻辑
}

// 当前页变更
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  // 这里可以添加分页逻辑
}
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