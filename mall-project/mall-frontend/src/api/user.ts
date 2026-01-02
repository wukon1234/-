import request from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  avatar?: string
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  phone?: string
}

/**
 * 用户登录
 */
export const login = (data: LoginRequest) => {
  return request.post<LoginResponse>('/user/login', data)
}

/**
 * 用户注册
 */
export const register = (data: RegisterRequest) => {
  return request.post('/user/register', data)
}

/**
 * 获取当前用户信息
 */
export const getUserInfo = () => {
  return request.get('/user/info')
}

/**
 * 修改密码
 */
export const updatePassword = (data: {
  oldPassword: string;
  newPassword: string;
}) => {
  return request.post('/user/update-password', data)
}

/**
 * 管理员登录
 */
export const adminLogin = (data: LoginRequest) => {
  return request.post<LoginResponse>('/user/admin/login', data)
}

