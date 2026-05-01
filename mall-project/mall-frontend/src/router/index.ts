import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  // 前端用户路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页', requiresAuth: true }
      },
      {
        path: 'products',
        name: 'ProductList',
        component: () => import('@/views/ProductList.vue'),
        meta: { title: '商品列表', requiresAuth: true }
      },
      {
        path: 'products/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '商品详情', requiresAuth: true }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: () => import('@/views/OrderList.vue'),
        meta: { title: '我的订单', requiresAuth: true }
      },
      {
        path: 'address',
        name: 'AddressList',
        component: () => import('@/views/AddressList.vue'),
        meta: { title: '收货地址', requiresAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', requiresAuth: true }
      },
      {
        path: 'assistant',
        name: 'Assistant',
        component: () => import('@/views/Assistant.vue'),
        meta: { title: '智能助手', requiresAuth: true }
      },
      {
        path: 'favorites',
        name: 'FavoriteList',
        component: () => import('@/views/FavoriteList.vue'),
        meta: { title: '我的收藏', requiresAuth: true }
      }
    ]
  },
  
  // 管理端路由
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/Login.vue'),
    meta: { title: '管理员登录', requiresAuth: false }
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '仪表盘', requiresAuth: true }
      },
      {
        path: 'products',
        name: 'AdminProductList',
        component: () => import('@/views/admin/ProductList.vue'),
        meta: { title: '商品列表', requiresAuth: true }
      },
      {
        path: 'products/add',
        name: 'AdminAddProduct',
        component: () => import('@/views/admin/AddProduct.vue'),
        meta: { title: '添加商品', requiresAuth: true }
      },
      {
        path: 'orders',
        name: 'AdminOrderList',
        component: () => import('@/views/admin/OrderList.vue'),
        meta: { title: '订单列表', requiresAuth: true }
      },
      {
        path: 'orders/analysis',
        name: 'AdminOrderAnalysis',
        component: () => import('@/views/admin/OrderAnalysis.vue'),
        meta: { title: '订单分析', requiresAuth: true }
      },
      {
        path: 'users',
        name: 'AdminUserList',
        component: () => import('@/views/admin/UserList.vue'),
        meta: { title: '用户列表', requiresAuth: true }
      },
      {
        path: 'assistant',
        name: 'AdminAssistant',
        component: () => import('@/views/admin/AdminAssistant.vue'),
        meta: { title: '智能助手管理', requiresAuth: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '商城智能助手系统'} - 商城智能助手系统`
  
  const rawToken = localStorage.getItem('token')
  const token = rawToken && rawToken !== 'null' && rawToken !== 'undefined' ? rawToken : ''
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth === true)
  
  // 如果需要认证但没有token，重定向到登录页面
  if (requiresAuth && !token) {
    if (to.path.startsWith('/admin')) {
      next('/admin/login')
      return
    }
    next('/login')
    return
  } else {
    next()
  }
})

export default router
