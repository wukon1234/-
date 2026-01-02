<template>
  <div class="admin-product-list">
    <div class="page-header">
      <h2 class="page-title">商品管理</h2>
      <el-button type="primary" @click="$router.push('/admin/products/add')">
        <el-icon><Plus /></el-icon> 添加商品
      </el-button>
    </div>
    
    <el-card shadow="hover" class="content-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.keyword"
          placeholder="请输入商品名称"
          style="width: 300px; margin-right: 10px"
          clearable
        >
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
        
        <el-select
          v-model="searchForm.category"
          placeholder="请选择分类"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="手机" value="1" />
          <el-option label="电脑" value="2" />
          <el-option label="服装" value="3" />
          <el-option label="家居" value="4" />
        </el-select>
        
        <el-select
          v-model="searchForm.status"
          placeholder="请选择状态"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="上架" value="1" />
          <el-option label="下架" value="0" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="productList" stripe style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="120">
          <template #default="scope">
            <el-image
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100" formatter="formatPrice" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-value="1"
              inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)" :loading="deleteLoading">
              <el-icon><Delete /></el-icon> 删除
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
import { useRouter } from 'vue-router'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: '',
  status: ''
})

// 商品列表数据
const productList = ref([
  { id: 1, name: 'iPhone 15 Pro', categoryName: '手机', price: 8999, stock: 100, status: 1, imageUrl: '/images/iphone15.jpg', createTime: '2024-01-01 10:00:00' },
  { id: 2, name: 'MacBook Pro', categoryName: '电脑', price: 14999, stock: 50, status: 1, imageUrl: '/images/macbook.jpg', createTime: '2024-01-02 14:30:00' },
  { id: 3, name: '休闲T恤', categoryName: '男装', price: 99, stock: 200, status: 1, imageUrl: '/images/tshirt.jpg', createTime: '2024-01-03 09:15:00' },
  { id: 4, name: '无线耳机', categoryName: '电子产品', price: 1299, stock: 150, status: 1, imageUrl: '/images/earphone.jpg', createTime: '2024-01-04 16:45:00' },
  { id: 5, name: '运动鞋', categoryName: '运动', price: 599, stock: 180, status: 1, imageUrl: '/images/shoes.jpg', createTime: '2024-01-05 11:20:00' }
])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 5
})

// 删除加载状态
const deleteLoading = ref(false)

// 格式化价格
const formatPrice = (row: any, column: any) => {
  return `¥${row.price}`
}

// 搜索
const handleSearch = () => {
  console.log('搜索条件', searchForm)
  // 这里可以添加搜索逻辑
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = ''
  searchForm.status = ''
  handleSearch()
}

// 编辑商品
const handleEdit = (row: any) => {
  router.push(`/admin/products/edit/${row.id}`)
}

// 删除商品
const handleDelete = (row: any) => {
  deleteLoading.value = true
  setTimeout(() => {
    ElMessage.success('商品删除成功')
    deleteLoading.value = false
    // 这里可以添加删除逻辑
  }, 1000)
}

// 状态变更
const handleStatusChange = (row: any) => {
  console.log('商品状态变更', row)
  // 这里可以添加状态变更逻辑
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
.admin-product-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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