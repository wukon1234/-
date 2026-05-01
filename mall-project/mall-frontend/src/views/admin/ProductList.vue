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
          <el-option label="手机" :value="4" />
          <el-option label="电脑" :value="5" />
          <el-option label="男装" :value="6" />
          <el-option label="女装" :value="7" />
        </el-select>
        
        <el-select
          v-model="searchForm.status"
          placeholder="请选择状态"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table v-loading="loading" :data="productList" stripe style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="商品图片" width="120">
          <template #default="scope">
            <el-image
              :src="scope.row.imageUrl || '/images/default-product.jpg'"
              :preview-src-list="[scope.row.imageUrl || '/images/default-product.jpg']"
              fit="cover"
              style="width: 80px; height: 80px; border-radius: 4px"
            />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="price" label="价格" width="100" :formatter="formatPrice" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdminProductList, deleteProduct as deleteProductApi, updateProductStatus } from '@/api/product'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  category: undefined as number | undefined,
  status: undefined as number | undefined
})

// 商品列表数据
const productList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载状态
const loading = ref(false)
const deleteLoading = ref(false)

// 格式化价格
const formatPrice = (row: any, _column: any) => {
  return `¥${row.price}`
}

// 获取商品列表
const fetchProductList = async () => {
  loading.value = true
  try {
    const response = await getAdminProductList({
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: searchForm.keyword.trim() || undefined,
      categoryId: searchForm.category,
      status: searchForm.status
    })
    productList.value = (response.data.list || []).map((item: any) => ({
      ...item,
      categoryName: getCategoryName(item.categoryId)
    }))
    pagination.total = response.data.total || 0
  } catch (error) {
    ElMessage.error('获取商品列表失败')
    console.error('获取商品列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 根据分类ID获取分类名称
const getCategoryName = (categoryId: number) => {
  const categoryMap: Record<number, string> = {
    4: '手机',
    5: '电脑',
    6: '男装',
    7: '女装'
  }
  return categoryMap[categoryId] || '其他'
}

// 搜索
const handleSearch = () => {
  // 重置页码
  pagination.currentPage = 1
  // 重新获取商品列表
  fetchProductList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.category = undefined
  searchForm.status = undefined
  handleSearch()
}

// 编辑商品
const handleEdit = (row: any) => {
  // 导航到添加商品页面并传递商品ID
  router.push(`/admin/products/add?id=${row.id}`)
}

// 删除商品
const handleDelete = async (row: any) => {
  deleteLoading.value = true
  try {
    await deleteProductApi(row.id)
    ElMessage.success('商品删除成功')
    // 重新获取商品列表
    fetchProductList()
  } catch (error) {
    ElMessage.error('商品删除失败')
    console.error('删除商品失败:', error)
  } finally {
    deleteLoading.value = false
  }
}

// 状态变更
const handleStatusChange = async (row: any) => {
  try {
    await updateProductStatus(row.id, row.status)
    ElMessage.success('商品状态更新成功')
  } catch (error) {
    // 状态更新失败，恢复原来的状态
    row.status = row.status === 1 ? 0 : 1
    ElMessage.error('商品状态更新失败')
    console.error('更新商品状态失败:', error)
  }
}

// 分页大小变更
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchProductList()
}

// 当前页变更
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchProductList()
}

// 页面加载时获取商品列表
onMounted(() => {
  fetchProductList()
})
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