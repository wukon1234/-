import request from './request'

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  quantity: number
  price: number
  subtotal: number
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: number  // 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消
  addressId?: number
  createTime: string
  updateTime: string
  items: OrderItem[]
}

export interface CreateOrderRequest {
  cartItemIds: number[]
  addressId?: number
}

export const orderStatusMap: Record<number, { text: string; type: string }> = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'info' }
}

/**
 * 创建订单
 */
export const createOrder = (data: CreateOrderRequest) => {
  return request.post<Order>('/order/create', data)
}

/**
 * 获取订单列表
 */
export const getOrderList = (status?: number) => {
  const params = status !== undefined ? { status } : {}
  return request.get<Order[]>('/order/list', { params })
}

/**
 * 获取订单详情
 */
export const getOrderDetail = (orderId: number) => {
  return request.get<Order>(`/order/${orderId}`)
}

/**
 * 取消订单
 */
export const cancelOrder = (orderId: number) => {
  return request.put(`/order/${orderId}/cancel`)
}

/**
 * 支付订单
 */
export const payOrder = (orderId: number) => {
  return request.put(`/order/${orderId}/pay`)
}

/**
 * 管理端：订单列表
 */
export const getAdminOrderList = (params: {
  page?: number
  size?: number
  orderNo?: string
  userName?: string
  status?: number
}) => {
  return request.get<{
    list: any[]
    total: number
  }>('/admin/orders', { params })
}

/**
 * 管理端：订单详情
 */
export const getAdminOrderDetail = (orderId: number) => {
  return request.get<any>(`/admin/orders/${orderId}`)
}

/**
 * 管理端：发货
 */
export const shipOrderByAdmin = (orderId: number) => {
  return request.put(`/admin/orders/${orderId}/ship`)
}

/**
 * 管理端：取消订单
 */
export const cancelOrderByAdmin = (orderId: number) => {
  return request.put(`/admin/orders/${orderId}/cancel`)
}

