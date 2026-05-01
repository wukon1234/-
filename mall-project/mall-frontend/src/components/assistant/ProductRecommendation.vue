<template>
  <div class="product-recommendation" v-if="products && products.length > 0">
    <div class="recommendation-title">
      <el-icon><ShoppingBag /></el-icon>
      <span>HUD · 候选商品阵列</span>
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
  margin-top: 12px;
  padding: 14px;
  background: rgba(4, 6, 14, 0.88);
  border-radius: var(--radius-md, 10px);
  border: 1px solid rgba(34, 211, 238, 0.2);
  box-shadow: inset 0 0 40px rgba(34, 211, 238, 0.04);

  .recommendation-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-family: var(--font-mono, monospace);
    font-weight: 600;
    font-size: 12px;
    letter-spacing: 0.06em;
    margin-bottom: 12px;
    color: var(--color-accent-green, #4ade80);
  }

  .product-list {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;

    .product-item {
      flex: 1;
      min-width: 150px;
      max-width: 200px;
      background: rgba(15, 23, 42, 0.75);
      border-radius: var(--radius-md, 10px);
      padding: 12px;
      cursor: pointer;
      transition: var(--transition, all 0.28s ease-out);
      border: 1px solid rgba(148, 163, 184, 0.12);

      &:hover {
        border-color: rgba(34, 211, 238, 0.45);
        box-shadow: 0 0 20px rgba(34, 211, 238, 0.12);
        transform: translateY(-2px);
      }

      .product-image {
        width: 100%;
        height: 120px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: rgba(3, 7, 18, 0.6);
        border-radius: var(--radius-sm, 6px);
        margin-bottom: 8px;
        overflow: hidden;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .el-icon {
          font-size: 42px;
          color: rgba(148, 163, 184, 0.45);
        }
      }

      .product-info {
        .product-name {
          font-size: 13px;
          color: var(--color-text-bold, #f1f5f9);
          margin-bottom: 6px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .product-price {
          font-size: 15px;
          font-weight: 700;
          color: var(--color-danger, #f87171);
        }
      }
    }
  }
}
</style>
