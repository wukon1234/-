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
          <p class="desc">{{ product.description || '暂无描述' }}</p>

          <div class="actions">
            <el-input-number v-model="qty" :min="1" :max="product.stock" />
            <el-button type="primary" :disabled="product.stock <= 0" @click="handleAddCart">
              加入购物车
            </el-button>
            <el-button @click="$router.push('/cart')">去购物车</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-empty v-else description="商品不存在或已下架" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductById, type Product } from '@/api/product'
import { addCart } from '@/api/cart'

const route = useRoute()
const product = ref<Product | null>(null)
const qty = ref(1)

const load = async () => {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const res = await getProductById(id)
    product.value = res.data
    qty.value = 1
  } catch (e: any) {
    product.value = null
    ElMessage.error(e.message || '加载商品失败')
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

onMounted(() => load())
</script>

<style scoped lang="scss">
.product-detail {
  .detail-card {
    .detail-wrap {
      display: flex;
      gap: 20px;
    }

    .img {
      width: 360px;
      height: 360px;
      border-radius: 8px;
      overflow: hidden;
      background: #f5f5f5;

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
        color: #333;
      }

      .price {
        color: #f56c6c;
        font-size: 26px;
        font-weight: 700;
        margin-bottom: 10px;
      }

      .stock {
        color: #666;
        margin-bottom: 10px;
      }

      .desc {
        color: #888;
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
}
</style>
