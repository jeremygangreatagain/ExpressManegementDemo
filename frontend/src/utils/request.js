import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: '/api', // 设置统一的请求前缀
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    // 如果token存在，则添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果返回的状态码为200，直接返回数据
    if (res.code === 200) {
      return res
    }

    // 401: 未授权或token过期
    if (res.code === 401 || response.status === 401) {
      handleUnauthorized()
      return Promise.reject(new Error('未授权或登录已过期'))
    }

    // 显示其他错误消息
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(res)
  },
  error => {
    console.error('响应错误:', error)

    // 处理401未授权错误
    if (error.response) {
      if (error.response.status === 401 || error.response.data?.code === 401) {
        handleUnauthorized()
        return Promise.reject(new Error('未授权或登录已过期'))
      }
      // 显示服务器返回的错误消息
      const errorMessage = error.response.data?.message || '请求失败'
      ElMessage.error(errorMessage)
    } else if (error.request) {
      // 请求已发出但未收到响应
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误')
    }
    return Promise.reject(error)
  }
)

// 处理未授权情况的统一方法
const handleUnauthorized = () => {
  // 清除所有认证相关的本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  localStorage.removeItem('userId')
  ElMessage.error('登录已过期，请重新登录')
  router.push('/login')
}

export default service