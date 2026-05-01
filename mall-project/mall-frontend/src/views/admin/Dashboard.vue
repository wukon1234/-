<template>
  <div class="admin-dashboard">
    <h2 class="page-title">仪表盘</h2>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-content">
            <div class="card-icon product-icon">
              <el-icon><Box /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">商品总数</div>
              <div class="card-value">{{ dashboard.productTotal }}</div>
              <div class="card-desc">+{{ dashboard.monthNewProducts }} 本月新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-content">
            <div class="card-icon order-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">订单总数</div>
              <div class="card-value">{{ dashboard.orderTotal }}</div>
              <div class="card-desc">+{{ dashboard.todayNewOrders }} 今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-content">
            <div class="card-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">用户总数</div>
              <div class="card-value">{{ dashboard.userTotal }}</div>
              <div class="card-desc">+{{ dashboard.monthNewUsers }} 本月新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-content">
            <div class="card-icon revenue-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">本月销售额</div>
              <div class="card-value">¥{{ formatAmount(dashboard.monthSalesAmount) }}</div>
              <div class="card-desc">来自本月订单累计金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>最近订单</span>
              <el-button type="text" size="small" @click="goToAllOrders">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" stripe style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="userName" label="用户" />
            <el-table-column label="金额">
              <template #default="scope">
                ¥{{ formatAmount(scope.row.totalAmount) }}
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="scope">
                {{ getOrderStatusText(scope.row.status) }}
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>热销商品</span>
              <el-button type="text" size="small" @click="goToAllProducts">查看全部</el-button>
            </div>
          </template>
          <el-table :data="hotProducts" stripe style="width: 100%">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column label="分类">
              <template #default="scope">
                {{ getCategoryName(scope.row.categoryId) }}
              </template>
            </el-table-column>
            <el-table-column label="价格">
              <template #default="scope">
                ¥{{ formatAmount(scope.row.price) }}
              </template>
            </el-table-column>
            <el-table-column prop="sales" label="销量" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Box, Document, User, Money } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()

const dashboard = reactive({
  productTotal: 0,
  orderTotal: 0,
  userTotal: 0,
  monthSalesAmount: 0,
  monthNewProducts: 0,
  todayNewOrders: 0,
  monthNewUsers: 0
})

const recentOrders = ref<any[]>([])
const hotProducts = ref<any[]>([])

const getDashboardData = async () => {
  try {
    const res = await request.get<any>('/admin/dashboard')
    Object.assign(dashboard, {
      productTotal: res.data.productTotal || 0,
      orderTotal: res.data.orderTotal || 0,
      userTotal: res.data.userTotal || 0,
      monthSalesAmount: res.data.monthSalesAmount || 0,
      monthNewProducts: res.data.monthNewProducts || 0,
      todayNewOrders: res.data.todayNewOrders || 0,
      monthNewUsers: res.data.monthNewUsers || 0
    })
    recentOrders.value = res.data.recentOrders || []
    hotProducts.value = res.data.hotProducts || []
  } catch (error) {
    ElMessage.error('加载仪表盘数据失败')
    console.error('加载仪表盘数据失败:', error)
  }
}

const formatAmount = (amount: number | string) => {
  const num = Number(amount || 0)
  return num.toFixed(2)
}

const getOrderStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const getCategoryName = (categoryId: number) => {
  const categoryMap: Record<number, string> = {
    1: '电子产品',
    2: '服装配饰',
    3: '食品饮料',
    4: '手机',
    5: '电脑',
    6: '男装',
    7: '女装'
  }
  return categoryMap[categoryId] || '其他'
}

const goToAllOrders = () => {
  router.push('/admin/orders')
}

const goToAllProducts = () => {
  router.push('/admin/products')
}

onMounted(() => {
  getDashboardData()
})
</script>

<style scoped lang="scss">
.admin-dashboard {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 700;
    color: #333;
  }
  
  .dashboard-card {
    height: 150px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .card-content {
      display: flex;
      align-items: center;
      height: calc(100% - 40px);
      
      .card-icon {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        display: flex;
        justify-content: center;
        align-items: center;
        font-size: 24px;
        margin-right: 20px;
        
        &.product-icon {
          background-color: rgba(102, 126, 234, 0.1);
          color: #667eea;
        }
        
        &.order-icon {
          background-color: rgba(255, 159, 64, 0.1);
          color: #ff9f43;
        }
        
        &.user-icon {
          background-color: rgba(52, 199, 89, 0.1);
          color: #34c759;
        }
        
        &.revenue-icon {
          background-color: rgba(255, 45, 85, 0.1);
          color: #ff2d55;
        }
      }
      
      .card-info {
        flex: 1;
        
        .card-title {
          font-size: 14px;
          color: #666;
          margin-bottom: 8px;
        }
        
        .card-value {
          font-size: 28px;
          font-weight: 700;
          color: #333;
          margin-bottom: 4px;
        }
        
        .card-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
  
  .mt-20 {
    margin-top: 20px;
  }

  .mt-20 .dashboard-card {
    height: auto;
    min-height: 420px;
  }
}
</style>