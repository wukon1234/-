<template>
  <div class="favorite-list">
    <h2>我的收藏</h2>
    <div v-if="favorites.length === 0" class="empty">
      <p>暂无收藏商品</p>
    </div>
    <div v-else class="favorite-grid">
      <div v-for="item in favorites" :key="item.id" class="favorite-item">
        <div class="product-image">
          <img :src="item.productImage" :alt="item.productName" />
        </div>
        <div class="product-info">
          <h3>{{ item.productName }}</h3>
          <p class="price">¥{{ item.productPrice }}</p>
          <button class="remove-btn" @click="removeFavorite(item.productId)">
            取消收藏
          </button>
          <router-link :to="`/products/${item.productId}`" class="view-btn">
            查看详情
          </router-link>
        </div>
      </div>
    </div>
    <div class="pagination" v-if="total > pageSize">
      <button 
        @click="changePage(page - 1)" 
        :disabled="page === 1"
      >
        上一�?      </button>
      <span>{{ page }} / {{ Math.ceil(total / pageSize) }}</span>
      <button 
        @click="changePage(page + 1)" 
        :disabled="page >= Math.ceil(total / pageSize)"
      >
        下一�?      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getFavoriteList, removeFavorite as apiRemoveFavorite } from '../api/favorite';

const router = useRouter();
const favorites = ref<any[]>([]);
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);

const loadFavorites = async () => {
  try {
    const response = await getFavoriteList(page.value, pageSize.value);
    favorites.value = response.data || [];
    // 这里简化处理，实际应该从响应中获取total
  } catch (error) {
    console.error('获取收藏列表失败:', error);
  }
};

const removeFavorite = async (productId: number) => {
  try {
    await apiRemoveFavorite(productId);
    // 重新加载收藏列表
    await loadFavorites();
  } catch (error) {
    console.error('取消收藏失败:', error);
  }
};

const changePage = (newPage: number) => {
  if (newPage >= 1) {
    page.value = newPage;
    loadFavorites();
  }
};

onMounted(() => {
  loadFavorites();
});
</script>

<style scoped>
.favorite-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.empty {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.favorite-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.favorite-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.product-image {
  height: 200px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
}

.price {
  margin: 0 0 15px 0;
  font-size: 18px;
  color: #ff4d4f;
  font-weight: bold;
}

.remove-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
  font-size: 14px;
}

.view-btn {
  background-color: #1890ff;
  color: white;
  padding: 6px 12px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 14px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.pagination button {
  padding: 6px 12px;
  border: 1px solid #d9d9d9;
  background-color: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  color: #d9d9d9;
  cursor: not-allowed;
}

.pagination span {
  padding: 0 10px;
}
</style>
