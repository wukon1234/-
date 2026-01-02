import request from './request'

export interface Address {
  id: number
  userId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
  fullAddress?: string
  createTime?: string
  updateTime?: string
}

export interface SaveAddressRequest {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: number
}

export const getAddressList = () => request.get<Address[]>('/address/list')

export const getDefaultAddress = () => request.get<Address | null>('/address/default')

export const createAddress = (data: SaveAddressRequest) => request.post<number>('/address', data)

export const updateAddress = (id: number, data: SaveAddressRequest) => request.put(`/address/${id}`, data)

export const deleteAddress = (id: number) => request.delete(`/address/${id}`)

export const setDefaultAddress = (id: number) => request.put(`/address/${id}/default`)


