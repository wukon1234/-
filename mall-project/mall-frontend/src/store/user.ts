import { defineStore } from 'pinia'
import { ref, onMounted } from 'vue'
import { getUserInfo } from '@/api/user'

export interface UserInfo {
  id: number
  username: string
  email?: string
  avatar?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const token = ref<string>(localStorage.getItem('token') || '')

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    localStorage.setItem('userId', String(info.id))
  }

  const setToken = (t: string) => {
    token.value = t
    localStorage.setItem('token', t)
  }

  const logout = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
  }

  // 初始化用户信息
  const initUserInfo = async () => {
    if (token.value && !userInfo.value) {
      try {
        const res = await getUserInfo()
        setUserInfo(res.data)
      } catch (error) {
        // 获取失败，清除token
        logout()
      }
    }
  }

  return {
    userInfo,
    token,
    setUserInfo,
    setToken,
    logout,
    initUserInfo
  }
})
