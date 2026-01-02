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
              <div class="card-value">128</div>
              <div class="card-desc">+20 本月新增</div>
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
              <div class="card-value">512</div>
              <div class="card-desc">+15 今日新增</div>
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
              <div class="card-value">1024</div>
              <div class="card-desc">+50 本月新增</div>
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
              <div class="card-value">¥25,600</div>
              <div class="card-desc">+12% 较上月</div>
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
              <el-button type="text" size="small">查看全部</el-button>
            </div>
          </template>
          <el-table :data="recentOrders" stripe style="width: 100%">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="userName" label="用户" />
            <el-table-column prop="totalAmount" label="金额" />
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="createTime" label="创建时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="hover" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span>热销商品</span>
              <el-button type="text" size="small">查看全部</el-button>
            </div>
          </template>
          <el-table :data="hotProducts" stripe style="width: 100%">
            <el-table-column prop="name" label="商品名称" />
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="price" label="价格" />
            <el-table-column prop="sales" label="销量" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Box, Document, User, Money } from '@element-plus/icons-vue'

// 最近订单数据
const recentOrders = ref([
  { orderNo: 'ORD20240101001', userName: 'test1', totalAmount: '¥128.00', status: '已完成', createTime: '2024-01-01 14:30:00' },
  { orderNo: 'ORD20240101002', userName: 'test2', totalAmount: '¥256.00', status: '待发货', createTime: '2024-01-01 15:20:00' },
  { orderNo: 'ORD20240101003', userName: 'test3', totalAmount: '¥99.00', status: '待支付', createTime: '2024-01-01 16:45:00' },
  { orderNo: 'ORD20240101004', userName: 'test4', totalAmount: '¥199.00', status: '已发货', createTime: '2024-01-01 17:10:00' },
  { orderNo: 'ORD20240101005', userName: 'test5', totalAmount: '¥328.00', status: '已完成', createTime: '2024-01-01 18:30:00' }
])

// 热销商品数据
const hotProducts = ref([
  { name: 'iPhone 15 Pro', category: '手机', price: '¥8999.00', sales: 120 },
  { name: 'MacBook Pro', category: '电脑', price: '¥14999.00', sales: 80 },
  { name: '休闲T恤', category: '男装', price: '¥99.00', sales: 500 },
  { name: '无线耳机', category: '电子产品', price: '¥1299.00', sales: 200 },
  { name: '运动鞋', category: '运动', price: '¥599.00', sales: 180 }
])
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
}
</style>