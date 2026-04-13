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
          <el-option label="待支付" value="0" />
          <el-option label="已支付" value="1" />
          <el-option label="已发货" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已取消" value="4" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
      
      <el-table :data="orderList" stripe style="width: 100%" border>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="用户名" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="120" />
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
    
    <!-- 查看订单详情对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="订单详情"
      width="800px"
    >
      <div v-if="currentOrder">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ currentOrder.userName }}</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ currentOrder.totalAmount }}</el-descriptions-item>
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
            <el-table-column prop="price" label="单价" width="100" />
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="subtotal" label="小计" width="120" />
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
import { ref, reactive } from 'vue'
import { Search, View, Van, Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  orderNo: '',
  userName: '',
  status: ''
})

// 原始订单列表数据
const originalOrderList = ref([
  { orderNo: 'ORD20240101001', userName: 'test1', totalAmount: '¥128.00', status: 3, createTime: '2024-01-01 14:30:00', payTime: '2024-01-01 14:35:00', deliveryTime: '2024-01-02 10:00:00', completeTime: '2024-01-03 15:30:00' },
  { orderNo: 'ORD20240101002', userName: 'test2', totalAmount: '¥256.00', status: 2, createTime: '2024-01-01 15:20:00', payTime: '2024-01-01 15:25:00', deliveryTime: '2024-01-02 11:00:00', completeTime: '' },
  { orderNo: 'ORD20240101003', userName: 'test3', totalAmount: '¥99.00', status: 0, createTime: '2024-01-01 16:45:00', payTime: '', deliveryTime: '', completeTime: '' },
  { orderNo: 'ORD20240101004', userName: 'test4', totalAmount: '¥199.00', status: 1, createTime: '2024-01-01 17:10:00', payTime: '2024-01-01 17:15:00', deliveryTime: '', completeTime: '' },
  { orderNo: 'ORD20240101005', userName: 'test5', totalAmount: '¥328.00', status: 3, createTime: '2024-01-01 18:30:00', payTime: '2024-01-01 18:35:00', deliveryTime: '2024-01-02 14:00:00', completeTime: '2024-01-04 10:30:00' }
])

// 订单列表数据
const orderList = ref([...originalOrderList.value])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 5
})

// 查看订单相关
const viewDialogVisible = ref(false)
const currentOrder = ref<any>(null)
const orderItems = ref([
  { productName: 'iPhone 15 Pro', price: '¥8999.00', quantity: 1, subtotal: '¥8999.00' },
  { productName: '无线耳机', price: '¥1299.00', quantity: 1, subtotal: '¥1299.00' }
])

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
const handleSearch = () => {
  console.log('搜索条件', searchForm)
  // 模拟根据搜索条件过滤订单列表
  const filteredList = originalOrderList.value.filter(order => {
    const orderNoMatch = !searchForm.orderNo || order.orderNo.includes(searchForm.orderNo)
    const userNameMatch = !searchForm.userName || order.userName.includes(searchForm.userName)
    const statusMatch = !searchForm.status || order.status.toString() === searchForm.status
    return orderNoMatch && userNameMatch && statusMatch
  })
  // 更新订单列表和分页信息
  orderList.value = filteredList
  pagination.total = filteredList.length
  pagination.currentPage = 1
  // 这里可以替换为实际的API调用
  // 例如：orderService.getOrders(searchForm)
}

// 重置搜索
const resetSearch = () => {
  searchForm.orderNo = ''
  searchForm.userName = ''
  searchForm.status = ''
  handleSearch()
}

// 查看订单
const handleView = (row: any) => {
  console.log('查看订单', row)
  currentOrder.value = row
  // 模拟订单商品数据
  orderItems.value = [
    { productName: '商品1', price: '¥50.00', quantity: 2, subtotal: '¥100.00' },
    { productName: '商品2', price: '¥28.00', quantity: 1, subtotal: '¥28.00' }
  ]
  viewDialogVisible.value = true
}

// 发货
const handleShip = (row: any) => {
  // 更新订单状态为已发货
  row.status = 2
  // 添加发货时间
  row.deliveryTime = new Date().toLocaleString()
  ElMessage.success('发货成功')
  // 这里可以添加发货逻辑
  // 例如：orderService.shipOrder(row.orderNo)
}

// 取消订单
const handleCancel = (row: any) => {
  // 更新订单状态为已取消
  row.status = 4
  ElMessage.success('订单已取消')
  // 这里可以添加取消订单逻辑
  // 例如：orderService.cancelOrder(row.orderNo)
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