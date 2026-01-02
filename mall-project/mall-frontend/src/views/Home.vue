<template>
  <div class="home">
    <el-card class="hero">
      <div class="hero-wrap">
        <div>
          <h1>商城智能助手系统</h1>
          <p>边逛边问：用智能助手快速对比参数、推荐商品、解决购物疑问。</p>
          <div class="hero-actions">
            <el-button type="primary" @click="$router.push('/assistant')">
              <el-icon><ChatDotRound /></el-icon>
              开始对话
            </el-button>
            <el-button @click="$router.push('/products')">去逛商品</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <div class="section">
      <div class="section-title">
        <h2>热门商品</h2>
        <el-button link type="primary" @click="$router.push('/products')">查看更多</el-button>
      </div>
      <el-row :gutter="16" v-if="hot.length > 0">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="p in hot" :key="p.id">
          <el-card class="p-card" shadow="hover" @click="$router.push(`/products/${p.id}`)">
            <el-image :src="p.imageUrl || '/placeholder.png'" class="p-img" fit="cover" />
            <div class="p-name">{{ p.name }}</div>
            <div class="p-bottom">
              <div class="p-price">¥{{ Number(p.price).toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无热门商品" />
    </div>

    <div class="section">
      <div class="section-title">
        <h2>最新上架</h2>
        <el-button link type="primary" @click="$router.push('/products')">查看更多</el-button>
      </div>
      <el-row :gutter="16" v-if="news.length > 0">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="p in news" :key="p.id">
          <el-card class="p-card" shadow="hover" @click="$router.push(`/products/${p.id}`)">
            <el-image :src="p.imageUrl || '/placeholder.png'" class="p-img" fit="cover" />
            <div class="p-name">{{ p.name }}</div>
            <div class="p-bottom">
              <div class="p-price">¥{{ Number(p.price).toFixed(2) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <el-empty v-else description="暂无最新商品" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ChatDotRound } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { getHotProducts, getNewProducts, type Product } from '@/api/product'

const hot = ref<Product[]>([])
const news = ref<Product[]>([])

const load = async () => {
  const [hotRes, newRes] = await Promise.all([getHotProducts(8), getNewProducts(8)])
  hot.value = hotRes.data || []
  news.value = newRes.data || []
}

onMounted(() => load())
</script>

<style scoped lang="scss">
.home {
  .hero {
    margin-bottom: 32px;
    border-radius: 12px;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
    transition: all 0.3s ease;
    
    &:hover {
      box-shadow: 0 15px 40px rgba(102, 126, 234, 0.4);
    }

    .hero-wrap {
      padding: 60px 40px;
      color: white;

      h1 {
        margin: 0 0 16px 0;
        font-size: 36px;
        font-weight: 700;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      p {
        margin: 0 0 24px 0;
        font-size: 16px;
        opacity: 0.9;
        max-width: 600px;
      }

      .hero-actions {
        display: flex;
        gap: 16px;
        flex-wrap: wrap;
        
        .el-button {
          padding: 12px 24px;
          font-size: 16px;
          font-weight: 600;
          border-radius: 8px;
          transition: all 0.3s ease;
          
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
          }
        }
      }
    }
  }

  .section {
    margin-bottom: 40px;

    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 12px;
      border-bottom: 2px solid #f0f0f0;

      h2 {
        margin: 0;
        color: #333;
        font-size: 22px;
        font-weight: 700;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          bottom: -14px;
          left: 0;
          width: 40px;
          height: 2px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
      }
      
      .el-button {
        font-size: 14px;
        font-weight: 600;
      }
    }

    .p-card {
      margin-bottom: 20px;
      cursor: pointer;
      border-radius: 12px;
      border: 1px solid #f0f0f0;
      transition: all 0.3s ease;
      overflow: hidden;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
        border-color: #e0e7ff;
      }

      .p-img {
        width: 100%;
        height: 180px;
        border-radius: 12px 12px 0 0;
        background: #f5f5f5;
        transition: all 0.3s ease;
        
        &:hover {
          transform: scale(1.05);
        }
      }
      
      :deep(.el-card__body) {
        padding: 16px;
      }

      .p-name {
        margin-top: 0;
        margin-bottom: 12px;
        font-weight: 600;
        color: #222;
        font-size: 16px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        line-height: 1.5;
      }

      .p-bottom {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 12px;

        .p-price {
          color: #f56c6c;
          font-weight: 700;
          font-size: 18px;
        }
      }
    }
  }
}
</style>
