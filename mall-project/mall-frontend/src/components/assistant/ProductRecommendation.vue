<template>
  <div class="product-recommendation" v-if="products && products.length > 0">
    <div class="recommendation-title">
      <el-icon><ShoppingBag /></el-icon>
      <span>推荐商品</span>
    </div>
    <div class="product-list">
      <div
        v-for="product in products"
        :key="product.id"
        class="product-item"
        @click="handleProductClick(product.id)"
      >
        <div class="product-image">
          <img
            v-if="product.imageUrl"
            :src="product.imageUrl"
            :alt="product.name"
            @error="handleImageError"
          />
          <el-icon v-else><Picture /></el-icon>
        </div>
        <div class="product-info">
          <div class="product-name">{{ product.name }}</div>
          <div class="product-price">¥{{ product.price?.toFixed(2) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ShoppingBag, Picture } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

interface Props {
  products?: Array<{
    id: number
    name: string
    price?: number
    imageUrl?: string
  }>
}

defineProps<Props>()

const router = useRouter()

const handleProductClick = (productId: number) => {
  router.push(`/products/${productId}`)
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}
</script>

<style scoped lang="scss">
.product-recommendation {
  margin-top: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;

  .recommendation-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    margin-bottom: 12px;
    color: #409eff;
  }

  .product-list {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;

    .product-item {
      flex: 1;
      min-width: 150px;
      max-width: 200px;
      background: #fff;
      border-radius: 8px;
      padding: 12px;
      cursor: pointer;
      transition: all 0.3s;
      border: 1px solid #e4e7ed;

      &:hover {
        box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
        transform: translateY(-2px);
      }

      .product-image {
        width: 100%;
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f7fa;
        border-radius: 6px;
        margin-bottom: 8px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .el-icon {
          font-size: 48px;
          color: #c0c4cc;
        }
      }

      .product-info {
        .product-name {
          font-size: 14px;
          color: #303133;
          margin-bottom: 6px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .product-price {
          font-size: 16px;
          font-weight: 600;
          color: #f56c6c;
        }
      }
    }
  }
}
</style>

