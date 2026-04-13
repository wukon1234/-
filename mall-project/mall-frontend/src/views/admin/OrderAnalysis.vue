<template>
  <div class="admin-order-analysis">
    <h2 class="page-title">订单分析</h2>
    
    <el-card shadow="hover" class="content-card">
      <div class="analysis-filters">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 300px; margin-right: 10px"
          @change="handleDateChange"
        />
        
        <el-select
          v-model="analysisType"
          placeholder="分析类型"
          style="width: 150px; margin-right: 10px"
          @change="handleAnalysisTypeChange"
        >
          <el-option label="按日期" value="date" />
          <el-option label="按商品" value="product" />
          <el-option label="按用户" value="user" />
        </el-select>
        
        <el-button type="primary" @click="handleAnalysis">分析</el-button>
      </div>
      
      <div class="analysis-result">
        <div class="result-stats">
          <el-statistic title="订单总数" :value="stats.totalOrders" />
          <el-statistic title="总销售额" :value="stats.totalAmount" prefix="¥" />
          <el-statistic title="平均订单金额" :value="stats.avgAmount" prefix="¥" />
          <el-statistic title="订单转化率" :value="stats.conversionRate" suffix="%" />
        </div>
        
        <div class="chart-container">
          <el-empty v-if="!chartData.length" description="暂无数据" />
          <div v-else class="chart">
            <!-- 这里可以集成 echarts 或其他图表库 -->
            <div class="mock-chart">
              <h3>{{ chartTitle }}</h3>
              <div class="chart-bars">
                <div 
                  v-for="(item, index) in chartData" 
                  :key="index"
                  class="chart-bar"
                >
                  <div class="bar-label">{{ item.label }}</div>
                  <div class="bar-container">
                    <div 
                      class="bar-fill"
                      :style="{ width: (item.value / maxValue * 100) + '%' }"
                    ></div>
                  </div>
                  <div class="bar-value">{{ item.value }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

// 日期范围
const dateRange = ref<any>(null)
// 分析类型
const analysisType = ref('date')

// 统计数据
const stats = reactive({
  totalOrders: 512,
  totalAmount: 25600,
  avgAmount: 50,
  conversionRate: 12.5
})

// 图表数据
const chartData = ref([
  { label: '1月', value: 120 },
  { label: '2月', value: 190 },
  { label: '3月', value: 150 },
  { label: '4月', value: 210 },
  { label: '5月', value: 180 },
  { label: '6月', value: 250 }
])

// 计算最大值用于图表显示
const maxValue = computed(() => {
  return Math.max(...chartData.value.map(item => item.value))
})

// 图表标题
const chartTitle = computed(() => {
  switch (analysisType.value) {
    case 'date': return '订单趋势分析'
    case 'product': return '商品销售分析'
    case 'user': return '用户购买分析'
    default: return '订单分析'
  }
})

// 日期变更
const handleDateChange = () => {
  console.log('日期变更', dateRange.value)
}

// 分析类型变更
const handleAnalysisTypeChange = () => {
  console.log('分析类型变更', analysisType.value)
  // 模拟不同分析类型的数据
  if (analysisType.value === 'date') {
    chartData.value = [
      { label: '1月', value: 120 },
      { label: '2月', value: 190 },
      { label: '3月', value: 150 },
      { label: '4月', value: 210 },
      { label: '5月', value: 180 },
      { label: '6月', value: 250 }
    ]
  } else if (analysisType.value === 'product') {
    chartData.value = [
      { label: 'iPhone 15 Pro', value: 120 },
      { label: 'MacBook Pro', value: 80 },
      { label: '休闲T恤', value: 500 },
      { label: '无线耳机', value: 200 },
      { label: '运动鞋', value: 180 }
    ]
  } else if (analysisType.value === 'user') {
    chartData.value = [
      { label: 'test1', value: 10 },
      { label: 'test2', value: 8 },
      { label: 'test3', value: 15 },
      { label: 'test4', value: 12 },
      { label: 'test5', value: 20 }
    ]
  }
}

// 执行分析
const handleAnalysis = () => {
  console.log('执行分析', { dateRange: dateRange.value, analysisType: analysisType.value })
  ElMessage.success('分析完成')
  // 这里可以添加实际的分析逻辑
  // 例如：orderService.analysis(dateRange.value, analysisType.value)
}
</script>

<style scoped lang="scss">
.admin-order-analysis {
  .page-title {
    margin-bottom: 20px;
    font-size: 24px;
    font-weight: 700;
    color: #333;
  }
  
  .content-card {
    .analysis-filters {
      margin-bottom: 20px;
      display: flex;
      align-items: center;
    }
    
    .analysis-result {
      .result-stats {
        display: flex;
        justify-content: space-around;
        margin-bottom: 30px;
        padding: 20px;
        background-color: #f9f9f9;
        border-radius: 8px;
      }
      
      .chart-container {
        margin-top: 20px;
        
        .mock-chart {
          h3 {
            margin-bottom: 20px;
            color: #333;
          }
          
          .chart-bars {
            display: flex;
            flex-direction: column;
            gap: 15px;
            
            .chart-bar {
              display: flex;
              align-items: center;
              gap: 10px;
              
              .bar-label {
                width: 100px;
                font-size: 14px;
                color: #666;
              }
              
              .bar-container {
                flex: 1;
                height: 20px;
                background-color: #f0f0f0;
                border-radius: 10px;
                overflow: hidden;
                
                .bar-fill {
                  height: 100%;
                  background-color: #409eff;
                  border-radius: 10px;
                  transition: width 0.5s ease;
                }
              }
              
              .bar-value {
                width: 60px;
                font-size: 14px;
                font-weight: 500;
                color: #333;
                text-align: right;
              }
            }
          }
        }
      }
    }
  }
}
</style>