<template>
  <div class="admin-order-list">
    <div class="page-header">
      <h2 class="page-title">订单管理</h2>
    </div>

    <el-card shadow="hover" class="content-card">
      <div class="search-bar">
        <el-input
          v-model="searchForm.orderNo"
          placeholder="请输入订单号"
          style="width: 300px; margin-right: 10px"
          clearable
        >
          <template #append>
            <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>

        <el-input
          v-model="searchForm.userName"
          placeholder="请输入用户名"
          style="width: 200px; margin-right: 10px"
          clearable
        />

        <el-select
          v-model="searchForm.status"
          placeholder="请选择订单状态"
          style="width: 150px; margin-right: 10px"
          clearable
        >
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已发货" :value="2" />
          <el-option label="已完成" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>

        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="orderList" stripe style="width: 100%" border>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column label="总金额" width="120">
          <template #default="scope">
            ¥{{ Number(scope.row.totalAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="payTime" label="支付时间" width="180" />
        <el-table-column prop="deliveryTime" label="发货时间" width="180" />
        <el-table-column prop="completeTime" label="完成时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button v-if="scope.row.status === 1" type="success" size="small" @click="handleShip(scope.row)">
              <el-icon><Van /></el-icon> 发货
            </el-button>
            <el-button v-if="scope.row.status === 0" type="danger" size="small" @click="handleCancel(scope.row)">
              <el-icon><Close /></el-icon> 取消
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

    <el-dialog v-model="viewDialogVisible" title="订单详情" width="800px">
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ Number(currentOrder.totalAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusTagType(currentOrder.status)">
              {{ getStatusText(currentOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">{{ currentOrder.createTime }}</el-descriptions-item>
          <el-descriptions-item label="支付时间" :span="2">{{ currentOrder.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间" :span="2">{{ currentOrder.deliveryTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ currentOrder.completeTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div class="mt-20">
          <h3>订单商品</h3>
          <el-table :data="orderItems" stripe style="width: 100%">
            <el-table-column prop="productName" label="商品名称" min-width="200" />
            <el-table-column label="单价" width="100">
              <template #default="scope">¥{{ Number(scope.row.price || 0).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column label="小计" width="120">
              <template #default="scope">¥{{ Number(scope.row.subtotal || 0).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, View, Van, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { cancelOrderByAdmin, getAdminOrderDetail, getAdminOrderList, shipOrderByAdmin } from '@/api/order'

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  userName: '',
  status: undefined as number | undefined
})

const loading = ref(false)
const orderList = ref<any[]>([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 查看订单相关
const viewDialogVisible = ref(false)
const currentOrder = ref<any>(null)
const orderItems = ref<any[]>([])

// 获取订单状态文本
const getStatusText = (status: number) => {
  const statusMap: any = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取订单状态标签类型
const getStatusTagType = (status: number) => {
  const typeMap: any = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

// 搜索
const fetchOrderList = async () => {
  loading.value = true
  try {
    const res = await getAdminOrderList({
      page: pagination.currentPage,
      size: pagination.pageSize,
      orderNo: searchForm.orderNo.trim() || undefined,
      userName: searchForm.userName.trim() || undefined,
      status: searchForm.status
    })
    orderList.value = res.data.list || []
    pagination.total = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取订单列表失败')
    console.error('获取订单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  fetchOrderList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.userName = ''
  searchForm.status = undefined
  handleSearch()
}

// 查看订单
const handleView = async (row: any) => {
  try {
    const res = await getAdminOrderDetail(row.id)
    currentOrder.value = res.data
    orderItems.value = res.data.items || []
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    console.error('获取订单详情失败:', error)
  }
}

// 发货
const handleShip = async (row: any) => {
  try {
    await shipOrderByAdmin(row.id)
    ElMessage.success('发货成功')
    fetchOrderList()
  } catch (error) {
    ElMessage.error('发货失败')
    console.error('发货失败:', error)
  }
}

// 取消订单
const handleCancel = async (row: any) => {
  try {
    await cancelOrderByAdmin(row.id)
    ElMessage.success('订单已取消')
    fetchOrderList()
  } catch (error) {
    ElMessage.error('取消订单失败')
    console.error('取消订单失败:', error)
  }
}

// 分页大小变更
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  fetchOrderList()
}

// 当前页变更
const handleCurrentChange = (current: number) => {
  pagination.currentPage = current
  fetchOrderList()
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped lang="scss">
.admin-order-list {
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