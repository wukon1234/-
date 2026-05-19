<template>
  <div class="product-list page-fade">
    <div class="toolbar card-surface">
      <div class="toolbar-head">
        <h2>商品列表</h2>
        <p class="toolbar-hint">搜索名称、描述或规格，支持分页浏览</p>
      </div>
      <div class="search">
        <el-input
          v-model="keyword"
          placeholder="搜索商品（名称/描述/规格）"
          clearable
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
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
.page-fade {
  animation: pageIn 0.4s ease-out both;
}

@keyframes pageIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-surface {
  background: var(--surface-panel, rgba(15, 23, 42, 0.86));
  border: 1px solid var(--tech-panel-border, rgba(34, 211, 238, 0.22));
  border-radius: var(--radius-lg, 12px);
  box-shadow: var(--shadow-sm);
  padding: 20px 24px;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
}

.product-list {
  .toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;

    .toolbar-head {
      flex: 1;
      min-width: 200px;

      h2 {
        margin: 0 0 6px 0;
        color: var(--color-text-bold, #222);
        font-size: 22px;
        font-weight: 700;
      }

      .toolbar-hint {
        margin: 0;
        font-size: 13px;
        color: var(--color-text-weak, #888);
      }
    }

    .search {
      width: 100%;
      max-width: 440px;
      flex: 1;
    }
  }

  .p-card {
    margin-bottom: 16px;
    cursor: pointer;
    border-radius: var(--radius-lg, 12px);
    border: 1px solid var(--color-border, #e5e7eb);
    transition: var(--transition, all 0.28s ease-out);

    &:hover {
      transform: translateY(-3px);
      border-color: rgba(34, 211, 238, 0.42);
      box-shadow: var(--shadow-hover);
    }

    .p-img {
      width: 100%;
      height: 160px;
      border-radius: var(--radius-lg, 12px) var(--radius-lg, 12px) 0 0;
      overflow: hidden;
      background: var(--color-bg-200, #f1f3f5);
      transition: var(--transition, all 0.28s ease-out);

      &:hover {
        transform: scale(1.03);
      }

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
      margin-top: 12px;
      font-weight: 600;
      color: var(--color-text-bold, #222);
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .p-desc {
      margin-top: 6px;
      color: var(--color-text-weak, #888);
      font-size: 13px;
      height: 34px;
      overflow: hidden;
    }

    .p-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 12px;

      .p-price {
        color: var(--color-danger, #ff4d4f);
        font-weight: 700;
        font-size: 16px;
      }
    }
  }

  .pager {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }
}
</style>
