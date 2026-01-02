import request from './request'

export interface CartItem {
  id: number
  userId: number
  productId: number
  quantity: number
  product: {
    id: number
    name: string
    description: string
    price: number
    stock: number
    imageUrl?: string
    status: number
  }
  subtotal: number
}

export interface AddCartRequest {
  productId: number
  quantity: number
}

export interface UpdateCartRequest {
  quantity: number
}

/**
 * 添加商品到购物车
 */
export const addCart = (data: AddCartRequest) => {
  return request.post('/cart/add', data)
}

/**
 * 获取购物车列表
 */
export const getCartList = () => {
  return request.get<CartItem[]>('/cart/list')
}

/**
 * 更新购物车商品数量
 */
export const updateCartQuantity = (cartId: number, data: UpdateCartRequest) => {
  return request.put(`/cart/${cartId}`, data)
}

/**
 * 删除购物车商品
 */
export const deleteCartItem = (cartId: number) => {
  return request.delete(`/cart/${cartId}`)
}

/**
 * 清空购物车
 */
export const clearCart = () => {
  return request.delete('/cart/clear')
}

