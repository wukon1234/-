<template>
  <div class="product-list">
    <div class="header">
      <h2>商品列表</h2>
      <div class="search">
        <el-input
          v-model="keyword"
          placeholder="搜索商品（名称/描述/规格）"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <el-row :gutter="16" v-if="products.length > 0">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="p in products" :key="p.id">
        <el-card class="p-card" shadow="hover" @click="goDetail(p.id)">
          <el-image :src="p.imageUrl || '/placeholder.png'" class="p-img" fit="cover">
            <template #error>
              <div class="image-slot">暂无图片</div>
            </template>
          </el-image>
          <div class="p-name">{{ p.name }}</div>
          <div class="p-desc">{{ p.description || '暂无描述' }}</div>
          <div class="p-bottom">
            <div class="p-price">¥{{ Number(p.price).toFixed(2) }}</div>
            <el-button type="primary" size="small" @click.stop="handleAddCart(p.id)">加入购物车</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-else description="暂无商品" />

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[8, 12, 16, 24]"
        layout="total, sizes, prev, pager, next"
        :total="9999"
        @size-change="load"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductList, searchProducts, type Product } from '@/api/product'
import { addCart } from '@/api/cart'

const router = useRouter()

const products = ref<Product[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(12)

const load = async () => {
  try {
    if (keyword.value.trim()) {
      const res = await searchProducts(keyword.value.trim(), page.value, size.value)
      products.value = res.data || []
    } else {
      const res = await getProductList(page.value, size.value)
      products.value = res.data || []
    }
  } catch (e: any) {
    ElMessage.error(e.message || '加载商品失败')
  }
}

const handleSearch = () => {
  page.value = 1
  load()
}

const goDetail = (id: number) => {
  router.push(`/products/${id}`)
}

const handleAddCart = async (productId: number) => {
  try {
    await addCart({ productId, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (e: any) {
    ElMessage.error(e.message || '加入购物车失败')
  }
}

onMounted(() => load())
</script>

<style scoped lang="scss">
.product-list {
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      margin: 0;
      color: #333;
    }

    .search {
      width: 420px;
      max-width: 60vw;
    }
  }

  .p-card {
    margin-bottom: 16px;
    cursor: pointer;

    .p-img {
      width: 100%;
      height: 160px;
      border-radius: 6px;
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

    .p-name {
      margin-top: 10px;
      font-weight: 600;
      color: #222;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .p-desc {
      margin-top: 6px;
      color: #888;
      font-size: 13px;
      height: 34px;
      overflow: hidden;
    }

    .p-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;

      .p-price {
        color: #f56c6c;
        font-weight: 700;
        font-size: 16px;
      }
    }
  }

  .pager {
    display: flex;
    justify-content: flex-end;
    margin-top: 10px;
  }
}
</style>
