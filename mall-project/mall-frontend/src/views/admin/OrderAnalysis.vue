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

        <el-date-picker
          v-if="analysisType === 'date'"
          v-model="analysisYear"
          type="year"
          placeholder="选择年份"
          style="width: 140px; margin-right: 10px"
          @change="handleYearChange"
        />
        
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

        <div class="ai-analysis">
          <div class="ai-header">AI分析结果</div>
          <el-empty v-if="!aiResult" description="点击“分析”生成AI结论" />
          <div v-else class="ai-content">{{ aiResult }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

// 日期范围
const dateRange = ref<any>(null)
// 分析类型
const analysisType = ref('date')
const analysisYear = ref(new Date())

// 统计数据
const stats = reactive({
  totalOrders: 0,
  totalAmount: 0,
  avgAmount: 0,
  conversionRate: 0
})

// 图表数据
const chartData = ref<Array<{ label: string; value: number }>>([])
const aiResult = ref('')

// 计算最大值用于图表显示
const maxValue = computed(() => {
  if (!chartData.value.length) {
    return 1
  }
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
  loadAnalysisData()
}

// 分析类型变更
const handleAnalysisTypeChange = () => {
  loadAnalysisData()
}

const handleYearChange = () => {
  loadAnalysisData()
}

const formatDate = (date: Date) => {
  const yyyy = date.getFullYear()
  const mm = `${date.getMonth() + 1}`.padStart(2, '0')
  const dd = `${date.getDate()}`.padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

const loadAnalysisData = async () => {
  try {
    const params: Record<string, string> = {
      type: analysisType.value
    }
    if (analysisType.value === 'date' && analysisYear.value) {
      params.year = `${new Date(analysisYear.value).getFullYear()}`
    }
    if (dateRange.value && Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      params.startDate = formatDate(new Date(dateRange.value[0]))
      params.endDate = formatDate(new Date(dateRange.value[1]))
    }

    const res = await request.get<any>('/admin/orders/analysis', { params })
    stats.totalOrders = Number(res.data.totalOrders || 0)
    stats.totalAmount = Number(res.data.totalAmount || 0)
    stats.avgAmount = Number(res.data.avgAmount || 0)
    stats.conversionRate = Number(res.data.conversionRate || 0)
    chartData.value = (res.data.chartData || []).map((item: any) => ({
      label: item.label,
      value: Number(item.value || 0)
    }))
  } catch (error) {
    ElMessage.error('订单分析失败')
    console.error('订单分析失败:', error)
  }
}

// 执行分析（统计 + AI洞察）
const handleAnalysis = async () => {
  await loadAnalysisData()
  try {
    const aiRes = await request.post<any>('/admin/orders/analysis/ai', {
      type: analysisType.value,
      totalOrders: stats.totalOrders,
      totalAmount: stats.totalAmount,
      avgAmount: stats.avgAmount,
      conversionRate: stats.conversionRate,
      chartData: chartData.value
    })
    aiResult.value = aiRes.data.content || ''
    ElMessage.success('分析完成')
  } catch (error) {
    aiResult.value = ''
    ElMessage.error('AI分析失败')
    console.error('AI分析失败:', error)
  }
}

onMounted(() => {
  loadAnalysisData()
})
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
                color: var(--color-text-weak, #64748b);
              }

              .bar-container {
                flex: 1;
                height: 20px;
                background-color: rgba(15, 23, 42, 0.75);
                border-radius: 10px;
                overflow: hidden;
                border: 1px solid rgba(34, 211, 238, 0.12);

                .bar-fill {
                  height: 100%;
                  background: linear-gradient(90deg, #22d3ee, #4ade80);
                  border-radius: 10px;
                  transition: width 0.5s ease;
                  box-shadow: 0 0 12px rgba(34, 211, 238, 0.25);
                }
              }

              .bar-value {
                width: 60px;
                font-size: 14px;
                font-weight: 500;
                color: var(--color-text-bold, #f1f5f9);
                text-align: right;
              }
            }
          }
        }
      }

      .ai-analysis {
        margin-top: 24px;
        padding: 16px;
        background: #fafafa;
        border: 1px solid #ebeef5;
        border-radius: 8px;

        .ai-header {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 12px;
        }

        .ai-content {
          white-space: pre-wrap;
          line-height: 1.8;
          color: #606266;
        }
      }
    }
  }
}
</style>