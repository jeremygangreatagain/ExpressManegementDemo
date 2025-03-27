import { createRouter, createWebHistory } from 'vue-router'
import Login from '../components/Login.vue'
import Register from '../components/Register.vue'
import Home from '../components/Home.vue'
import Users from '../components/Users.vue'
import Stores from '../components/Stores.vue'
import WorkLogs from '../components/WorkLogs.vue'
import StaffManagement from '../components/StaffManagement.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/home',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../components/Dashboard.vue')
      },
      {
        path: '/orders',
        name: 'Orders',
        component: () => import('../components/Orders.vue')
      },
      {
        path: '/stores',
        name: 'Stores',
        component: Stores
      },
      {
        path: '/users',
        name: 'Users',
        component: Users
      },
      {
        path: '/staff',
        name: 'Staff',
        component: StaffManagement
      },
      {
        path: '/worklogs',
        name: 'WorkLogs',
        component: WorkLogs
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

import { hasPermission } from '../utils/permission'

router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 检查是否已登录
    const token = localStorage.getItem('token')
    const userRole = localStorage.getItem('userRole')

    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    } else if (!hasPermission(userRole, to.name)) {
      // 如果用户没有权限访问该路由，根据角色重定向到相应页面
      next({ name: userRole === 'user' ? 'Orders' : 'Dashboard' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router