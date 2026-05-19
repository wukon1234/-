<template>
  <div class="product-detail">
    <el-card v-if="product" class="detail-card">
      <div class="detail-wrap">
        <el-image :src="product.imageUrl || '/placeholder.png'" class="img" fit="cover">
          <template #error>
            <div class="image-slot">暂无图片</div>
          </template>
        </el-image>

        <div class="info">
          <h2 class="name">{{ product.name }}</h2>
          <div class="price">¥{{ Number(product.price).toFixed(2) }}</div>
          <div class="stock">库存：{{ product.stock }}</div>
          <div class="rating" v-if="averageRating">
            <span class="rating-label">评分：</span>
            <el-rate v-model="averageRating" disabled />
            <span class="rating-count">({{ reviewCount }}条评价)</span>
          </div>
          <p class="desc">{{ product.description || '暂无描述' }}</p>

          <div class="actions">
            <el-input-number v-model="qty" :min="1" :max="product.stock" />
            <el-button type="primary" :disabled="product.stock <= 0" @click="handleAddCart">
              加入购物车
            </el-button>
            <el-button @click="$router.push('/cart')">去购物车</el-button>
            <el-button 
              :type="isFavorite ? 'warning' : 'info'" 
              @click="toggleFavorite"
            >
              {{ isFavorite ? '取消收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 评价部分 -->
    <div v-if="product" class="reviews-section">
      <h3>商品评价</h3>
      
      <!-- 添加评价 -->
      <el-card class="add-review-card">
        <h4>发表评价</h4>
        <el-form :model="reviewForm" label-width="80px">
          <el-form-item label="评分">
            <el-rate v-model="reviewForm.rating" />
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              rows="4"
              placeholder="请输入您的评价..."
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitReview" :disabled="!reviewForm.rating">
              提交评价
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 评价列表 -->
      <div class="reviews-list">
        <div v-if="reviews.length === 0" class="empty-reviews">
          <el-empty description="暂无评价" />
        </div>
        <el-card v-for="review in reviews" :key="review.id" class="review-card">
          <div class="review-header">
            <span class="review-username">{{ review.username }}</span>
            <el-rate v-model="review.rating" disabled />
            <span class="review-time">{{ formatTime(review.createTime) }}</span>
          </div>
          <div class="review-content">{{ review.content }}</div>
        </el-card>
      </div>
    </div>

    <el-empty v-else description="商品不存在或已下架" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById, type Product } from '@/api/product'
import { addCart } from '@/api/cart'
import { createReview, getProductReviews, getAverageRating, getReviewCount } from '@/api/review'
import { addFavorite, removeFavorite, checkFavorite } from '@/api/favorite'

const route = useRoute()
const product = ref<Product | null>(null)
const qty = ref(1)
const averageRating = ref<number>(0)
const reviewCount = ref<number>(0)
const reviews = ref<any[]>([])
const isFavorite = ref<boolean>(false)

const reviewForm = ref({
  rating: 0,
  content: ''
})

const load = async () => {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const res = await getProductById(id)
    product.value = res.data
    qty.value = 1
    
    // 加载评价信息
    await loadReviews(id)
    
    // 加载收藏状态
    await checkFavoriteStatus(id)
  } catch (e: any) {
    product.value = null
    ElMessage.error(e.message || '加载商品失败')
  }
}

const loadReviews = async (productId: number) => {
  try {
    // 获取平均评分
    const ratingRes = await getAverageRating(productId)
    averageRating.value = ratingRes.data || 0
    
    // 获取评价数量
    const countRes = await getReviewCount(productId)
    reviewCount.value = countRes.data || 0
    
    // 获取评价列表
    const reviewsRes = await getProductReviews(productId, 1, 10)
    reviews.value = reviewsRes.data || []
  } catch (error) {
    console.error('加载评价失败:', error)
  }
}

const checkFavoriteStatus = async (productId: number) => {
  try {
    const res = await checkFavorite(productId)
    isFavorite.value = res.data || false
  } catch (error) {
    console.error('检查收藏状态失败:', error)
  }
}

const handleAddCart = async () => {
  if (!product.value) return
  try {
    await addCart({ productId: product.value.id, quantity: qty.value })
    ElMessage.success('已加入购物车')
  } catch (e: any) {
    ElMessage.error(e.message || '加入购物车失败')
  }
}

const toggleFavorite = async () => {
  if (!product.value) return
  try {
    if (isFavorite.value) {
      await removeFavorite(product.value.id)
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(product.value.id)
      ElMessage.success('已添加到收藏')
    }
    isFavorite.value = !isFavorite.value
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  }
}

const submitReview = async () => {
  if (!product.value || !reviewForm.value.rating) return
  try {
    await createReview({
      productId: product.value.id,
      rating: reviewForm.value.rating,
      content: reviewForm.value.content
    })
    ElMessage.success('评价成功')
    // 重新加载评价
    await loadReviews(product.value.id)
    // 重置表单
    reviewForm.value = {
      rating: 0,
      content: ''
    }
  } catch (e: any) {
    ElMessage.error(e.message || '评价失败')
  }
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

onMounted(() => load())
</script>

<style scoped lang="scss">
.product-detail {
  .detail-card {
    border-radius: var(--radius-lg, 12px);
    border: 1px solid var(--color-border, #e5e7eb);
    box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));

    .detail-wrap {
      display: flex;
      gap: 24px;
      flex-wrap: wrap;
    }

    .img {
      width: 360px;
      max-width: 100%;
      height: 360px;
      border-radius: var(--radius-lg, 12px);
      overflow: hidden;
      background: var(--color-bg-200, #f1f3f5);

      .image-slot {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100%;
        color: #999;
      }
    }

    .info {
      flex: 1;

      .name {
        margin: 0 0 10px 0;
        color: var(--color-text-bold, #222);
        font-size: 24px;
        font-weight: 700;
      }

      .price {
        color: var(--color-danger, #ff4d4f);
        font-size: 26px;
        font-weight: 700;
        margin-bottom: 10px;
      }

      .stock {
        color: var(--color-text, #444);
        margin-bottom: 10px;
      }

      .rating {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        
        .rating-label {
          color: #666;
          margin-right: 8px;
        }
        
        .rating-count {
          margin-left: 8px;
          color: #999;
          font-size: 14px;
        }
      }

      .desc {
        color: var(--color-text-weak, #888);
        line-height: 1.7;
        margin: 10px 0 16px 0;
      }

      .actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }
  }

  .reviews-section {
    margin-top: 30px;
    
    h3 {
      margin-bottom: 20px;
      color: var(--color-text-bold, #222);
      font-weight: 700;
    }
    
    .add-review-card {
      margin-bottom: 30px;
      
      h4 {
        margin-bottom: 15px;
        color: #666;
      }
    }
    
    .reviews-list {
      .empty-reviews {
        padding: 40px 0;
      }
      
      .review-card {
        margin-bottom: 15px;
        
        .review-header {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          
          .review-username {
            font-weight: 600;
            margin-right: 15px;
          }
          
          .review-time {
            margin-left: auto;
            color: #999;
            font-size: 14px;
          }
        }
        
        .review-content {
          color: #666;
          line-height: 1.6;
        }
      }
    }
  }
}
</style>
