import request from './request'

export interface Product {
  id: number
  name: string
  description?: string
  categoryId?: number
  price: number
  stock: number
  imageUrl?: string
  detailImages?: string
  specs?: string
  status: number
  createTime?: string
  updateTime?: string
}

export const getProductList = (page = 1, size = 12) => {
  return request.get<Product[]>('/products', { params: { page, size } })
}

export const searchProducts = (keyword: string, page = 1, size = 12) => {
  return request.get<Product[]>('/products/search', { params: { keyword, page, size } })
}

export const getProductById = (id: number) => {
  return request.get<Product>(`/products/${id}`)
}

export const getHotProducts = (limit = 8) => {
  return request.get<Product[]>('/products/hot', { params: { limit } })
}

export const getNewProducts = (limit = 8) => {
  return request.get<Product[]>('/products/new', { params: { limit } })
}

export interface AIProductRequest {
  productName: string
  keywords: string
  targetAudience: string
}

export interface AIProductResponse {
  title: string
  summary: string
  description: string
  sellingPoints: string
}

export const generateProductContent = (data: AIProductRequest) => {
  return request.post<AIProductResponse>('/products/generate-content', data)
}

export const addProduct = (product: Product) => {
  return request.post<boolean>('/products', product)
}

export const updateProduct = (product: Product) => {
  return request.put<boolean>('/products', product)
}

export const deleteProduct = (id: number) => {
  return request.delete<boolean>(`/products/${id}`)
}

export const updateProductStatus = (id: number, status: number) => {
  return request.put<boolean>(`/products/${id}/status`, null, { params: { status } })
}

export const getAdminProductList = (params: {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  status?: number
}) => {
  return request.get<{
    list: Product[]
    total: number
  }>('/products/admin/list', { params })
}


