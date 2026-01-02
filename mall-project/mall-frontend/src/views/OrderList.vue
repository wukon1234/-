<template>
  <div class="order-list">
    <h2>我的订单</h2>
    
    <el-tabs v-model="activeStatus" @tab-change="handleStatusChange">
      <el-tab-pane label="全部" :name="undefined"></el-tab-pane>
      <el-tab-pane label="待支付" :name="0"></el-tab-pane>
      <el-tab-pane label="已支付" :name="1"></el-tab-pane>
      <el-tab-pane label="已发货" :name="2"></el-tab-pane>
      <el-tab-pane label="已完成" :name="3"></el-tab-pane>
      <el-tab-pane label="已取消" :name="4"></el-tab-pane>
    </el-tabs>
    
    <div v-if="orderList.length > 0" class="order-list-content">
      <el-card v-for="order in orderList" :key="order.id" class="order-card" shadow="hover">
        <template #header>
          <div class="order-header">
            <div class="order-info">
              <span class="order-no">订单号: {{ order.orderNo }}</span>
              <span class="order-time">{{ formatTime(order.createTime) }}</span>
            </div>
            <el-tag :type="orderStatusMap[order.status].type as any">
              {{ orderStatusMap[order.status].text }}
            </el-tag>
          </div>
        </template>
        
        <div class="order-items">
          <div
            v-for="item in order.items"
            :key="item.id"
            class="order-item"
            @click="viewOrderDetail(order.id)"
          >
            <el-image
              :src="'/placeholder.png'"
              class="item-image"
              fit="cover"
            >
              <template #error>
                <div class="image-slot">暂无图片</div>
              </template>
            </el-image>
            
            <div class="item-info">
              <h4 class="item-name">{{ item.productName }}</h4>
              <div class="item-specs">
                <span>数量: {{ item.quantity }}</span>
                <span>单价: ¥{{ Number(item.price).toFixed(2) }}</span>
              </div>
            </div>
            
            <div class="item-subtotal">
              ¥{{ Number(item.subtotal).toFixed(2) }}
            </div>
          </div>
        </div>
        
        <div class="order-footer">
          <div class="order-total">
            共 {{ order.items.length }} 件商品，合计:
            <strong class="total-price">¥{{ Number(order.totalAmount).toFixed(2) }}</strong>
          </div>
          <div class="order-actions">
            <el-button
              v-if="order.status === 0"
              type="primary"
              @click.stop="handlePay(order.id)"
            >
              立即支付
            </el-button>
            <el-button
              v-if="order.status === 0"
              @click.stop="handleCancel(order.id)"
            >
              取消订单
            </el-button>
            <el-button
              type="info"
              @click.stop="viewOrderDetail(order.id)"
            >
              查看详情
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <el-empty v-else description="暂无订单">
      <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
    </el-empty>
    
    <!-- 订单详情对话框 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="800px">
      <div v-if="orderDetail" class="order-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="orderStatusMap[orderDetail.status].type as any">
              {{ orderStatusMap[orderDetail.status].text }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ Number(orderDetail.totalAmount).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(orderDetail.createTime) }}</el-descriptions-item>
        </el-descriptions>
        
        <h3 style="margin: 20px 0 10px 0">订单商品</h3>
        <el-table :data="orderDetail.items" border>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="price" label="单价" width="120">
            <template #default="{ row }">
              ¥{{ Number(row.price).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计" width="120">
            <template #default="{ row }">
              ¥{{ Number(row.subtotal).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOrderList,
  getOrderDetail,
  cancelOrder,
  payOrder,
  orderStatusMap,
  type Order
} from '@/api/order'

const router = useRouter()

const activeStatus = ref<number | undefined>(undefined)
const orderList = ref<Order[]>([])
const detailVisible = ref(false)
const orderDetail = ref<Order | null>(null)

// 加载订单列表
const loadOrderList = async () => {
  try {
    const res = await getOrderList(activeStatus.value)
    orderList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载订单列表失败')
  }
}

// 状态切换
const handleStatusChange = () => {
  loadOrderList()
}

// 查看订单详情
const viewOrderDetail = async (orderId: number) => {
  try {
    const res = await getOrderDetail(orderId)
    orderDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载订单详情失败')
  }
}

// 支付订单
const handlePay = async (orderId: number) => {
  try {
    await ElMessageBox.confirm('确定要支付该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await payOrder(orderId)
    ElMessage.success('支付成功')
    await loadOrderList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '支付失败')
    }
  }
}

// 取消订单
const handleCancel = async (orderId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(orderId)
    ElMessage.success('取消成功')
    await loadOrderList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

// 格式化时间
const formatTime = (time: string) => {
  return new Date(time).toLocaleString('zh-CN')
}

onMounted(() => {
  loadOrderList()
})
</script>

<style scoped lang="scss">
.order-list {
  h2 {
    margin-bottom: 20px;
    color: #333;
  }
}

.order-list-content {
  margin-top: 20px;
}

.order-card {
  margin-bottom: 20px;
  
  .order-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .order-info {
      display: flex;
      flex-direction: column;
      gap: 5px;
      
      .order-no {
        font-weight: bold;
        color: #333;
      }
      
      .order-time {
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .order-items {
    .order-item {
      display: flex;
      align-items: center;
      padding: 15px 0;
      border-bottom: 1px solid #f0f0f0;
      cursor: pointer;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        background-color: #f5f5f5;
      }
      
      .item-image {
        width: 80px;
        height: 80px;
        margin-right: 15px;
        border-radius: 4px;
        overflow: hidden;
        
        .image-slot {
          display: flex;
          justify-content: center;
          align-items: center;
          width: 100%;
          height: 100%;
          background-color: #f5f5f5;
          color: #999;
          font-size: 12px;
        }
      }
      
      .item-info {
        flex: 1;
        
        .item-name {
          margin: 0 0 10px 0;
          font-size: 16px;
          color: #333;
        }
        
        .item-specs {
          display: flex;
          gap: 20px;
          font-size: 14px;
          color: #999;
        }
      }
      
      .item-subtotal {
        font-size: 16px;
        font-weight: bold;
        color: #f56c6c;
        width: 100px;
        text-align: right;
      }
    }
  }
  
  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #f0f0f0;
    
    .order-total {
      font-size: 16px;
      
      .total-price {
        color: #f56c6c;
        font-size: 20px;
        margin-left: 10px;
      }
    }
    
    .order-actions {
      display: flex;
      gap: 10px;
    }
  }
}

.order-detail {
  h3 {
    margin: 20px 0 10px 0;
    color: #333;
  }
}
</style>
