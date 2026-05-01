<template>
  <div class="cart">
    <h2>购物车</h2>
    
    <el-card v-if="cartList.length > 0" class="cart-card">
      <template #header>
        <div class="cart-header">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <div class="cart-actions">
            <span>已选择 {{ selectedItems.length }} 件商品</span>
            <el-button type="danger" :disabled="selectedItems.length === 0" @click="handleBatchDelete">
              批量删除
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="cart-list">
        <div v-for="item in cartList" :key="item.id" class="cart-item">
          <el-checkbox v-model="item.selected" @change="handleItemSelect(item)"></el-checkbox>
          
          <el-image
            :src="item.product.imageUrl || '/placeholder.png'"
            class="product-image"
            fit="cover"
            @click="$router.push(`/products/${item.productId}`)"
          >
            <template #error>
              <div class="image-slot">暂无图片</div>
            </template>
          </el-image>
          
          <div class="product-info">
            <h3 class="product-name" @click="$router.push(`/products/${item.productId}`)">
              {{ item.product.name }}
            </h3>
            <p class="product-desc">{{ item.product.description }}</p>
            <div class="product-price">
              <span class="price">¥{{ item.product.price }}</span>
              <span class="stock">库存: {{ item.product.stock }}</span>
            </div>
          </div>
          
          <div class="quantity-control">
            <el-input-number
              v-model="item.quantity"
              :min="1"
              :max="item.product.stock"
              size="small"
              @change="handleQuantityChange(item)"
            />
          </div>
          
          <div class="subtotal">
            <span class="subtotal-label">小计:</span>
            <span class="subtotal-value">¥{{ Number(item.subtotal).toFixed(2) }}</span>
          </div>
          
          <el-button
            type="danger"
            :icon="Delete"
            circle
            size="small"
            @click="handleDelete(item)"
          />
        </div>
      </div>
      
      <div class="cart-footer">
        <div class="total-info">
          <span>合计: <strong class="total-price">¥{{ totalAmount.toFixed(2) }}</strong></span>
        </div>
        <el-button type="primary" size="large" :disabled="selectedItems.length === 0" @click="handleCheckout">
          结算({{ selectedItems.length }})
        </el-button>
      </div>
    </el-card>
    
    <el-empty v-else description="购物车是空的，快去选购商品吧~">
      <el-button type="primary" @click="$router.push('/products')">去购物</el-button>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { getCartList, updateCartQuantity, deleteCartItem, type CartItem } from '@/api/cart'
import { createOrder } from '@/api/order'
import { getDefaultAddress } from '@/api/address'

const router = useRouter()

type CartItemWithSelected = CartItem & { selected: boolean }
const cartList = ref<CartItemWithSelected[]>([])
const selectAll = ref(false)

// 为每个购物车项添加selected属性
const initCartList = (items: CartItem[]): CartItemWithSelected[] => {
  return items.map(item => ({
    ...item,
    selected: false
  }))
}

// 已选中的商品
const selectedItems = computed(() => {
  return cartList.value.filter(item => item.selected)
})

// 总金额
const totalAmount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + Number(item.subtotal), 0)
})

// 加载购物车列表
const loadCartList = async () => {
  try {
    const res = await getCartList()
    cartList.value = initCartList(res.data || [])
  } catch (error) {
    ElMessage.error('加载购物车失败')
  }
}

// 全选/取消全选
const handleSelectAll = (checked: boolean) => {
  cartList.value.forEach(item => {
    item.selected = checked
  })
}

// 单个商品选择
const handleItemSelect = (_item: CartItemWithSelected) => {
  selectAll.value = cartList.value.every(cartItem => cartItem.selected)
}

// 更新商品数量
const handleQuantityChange = async (item: CartItemWithSelected) => {
  try {
    await updateCartQuantity(item.id, { quantity: item.quantity })
    // 更新小计
    item.subtotal = item.product.price * item.quantity
    ElMessage.success('更新成功')
  } catch (error: any) {
    ElMessage.error(error.message || '更新失败')
    // 恢复原数量
    await loadCartList()
  }
}

// 删除商品
const handleDelete = async (item: CartItemWithSelected) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCartItem(item.id)
    ElMessage.success('删除成功')
    await loadCartList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedItems.value.length} 件商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const deletePromises = selectedItems.value.map(item => deleteCartItem(item.id))
    await Promise.all(deletePromises)
    ElMessage.success('删除成功')
    await loadCartList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 结算
const handleCheckout = async () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }
  
  try {
    // 订单创建需要地址：优先使用默认地址；没有则引导去新增
    const defaultRes = await getDefaultAddress()
    const defaultAddress = defaultRes.data
    if (!defaultAddress) {
      await ElMessageBox.confirm('你还没有收货地址，是否先去新增地址？', '提示', {
        confirmButtonText: '去新增',
        cancelButtonText: '取消',
        type: 'warning'
      })
      router.push('/address')
      return
    }

    const cartItemIds = selectedItems.value.map(item => item.id)
    await createOrder({ cartItemIds, addressId: defaultAddress.id })
    ElMessage.success('订单创建成功')
    router.push(`/orders`)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '创建订单失败')
    }
  }
}

onMounted(() => {
  loadCartList()
})
</script>

<style scoped lang="scss">
.cart {
  h2 {
    margin-bottom: 20px;
    color: var(--color-text-bold, #222);
    font-size: 24px;
    font-weight: 700;
  }
}

.cart-card {
  border-radius: var(--radius-lg, 12px);
  border: 1px solid var(--color-border, #e5e7eb);
  box-shadow: var(--shadow-sm, 0 2px 8px rgba(0, 0, 0, 0.04));

  .cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .cart-actions {
      display: flex;
      align-items: center;
      gap: 20px;
    }
  }
  
  .cart-list {
    .cart-item {
      display: flex;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid var(--color-border, #e5e7eb);
      
      &:last-child {
        border-bottom: none;
      }
      
      .product-image {
        width: 120px;
        height: 120px;
        margin: 0 20px;
        cursor: pointer;
        border-radius: var(--radius-md, 8px);
        overflow: hidden;
        
        .image-slot {
          display: flex;
          justify-content: center;
          align-items: center;
          width: 100%;
          height: 100%;
          background-color: #f5f5f5;
          color: #999;
        }
      }
      
      .product-info {
        flex: 1;
        margin-right: 20px;
        
        .product-name {
          margin: 0 0 10px 0;
          font-size: 16px;
          cursor: pointer;
          color: #333;
          
          &:hover {
            color: var(--color-accent-cyan, #22d3ee);
          }
        }
        
        .product-desc {
          margin: 0 0 10px 0;
          color: #999;
          font-size: 14px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .product-price {
          display: flex;
          align-items: center;
          gap: 20px;
          
          .price {
            color: var(--color-danger, #ff4d4f);
            font-size: 18px;
            font-weight: bold;
          }
          
          .stock {
            color: #999;
            font-size: 14px;
          }
        }
      }
      
      .quantity-control {
        width: 120px;
        margin-right: 20px;
      }
      
      .subtotal {
        width: 120px;
        text-align: right;
        margin-right: 20px;
        
        .subtotal-label {
          color: #999;
          font-size: 14px;
        }
        
        .subtotal-value {
          color: var(--color-danger, #ff4d4f);
          font-size: 16px;
          font-weight: bold;
          margin-left: 10px;
        }
      }
    }
  }
  
  .cart-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid var(--color-border, #e5e7eb);
    
    .total-info {
      font-size: 16px;
      
      .total-price {
        color: var(--color-danger, #ff4d4f);
        font-size: 24px;
        margin-left: 10px;
      }
    }
  }
}
</style>
