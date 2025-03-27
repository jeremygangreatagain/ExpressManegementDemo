<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const handleRegister = async () => {
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.confirmPassword) {
    ElMessage.warning('请填写完整的注册信息')
    return
  }
  
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  try {
    const response = await request.post('/auth/register', {
      username: registerForm.value.username,
      password: registerForm.value.password,
      phone: registerForm.value.phone
    })
    
    if (response.code === 200) {
      ElMessage.success('注册成功')
      // 注册成功后跳转到登录页面
      router.push('/login')
    } else {
      ElMessage.error(response.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请稍后重试')
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          快递管理系统
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          创建新账号
        </p>
      </div>
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <el-input
              v-model="registerForm.username"
              type="text"
              required
              placeholder="用户名"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-orange-500 focus:border-orange-500 focus:z-10 sm:text-sm"
            />
          </div>
          <div>
            <el-input
              v-model="registerForm.phone"
              type="text"
              required
              placeholder="手机号码"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-orange-500 focus:border-orange-500 focus:z-10 sm:text-sm"
            />
          </div>
          <div>
            <el-input
              v-model="registerForm.password"
              type="password"
              required
              placeholder="密码"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-orange-500 focus:border-orange-500 focus:z-10 sm:text-sm"
            />
          </div>
          <div>
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              required
              placeholder="确认密码"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-orange-500 focus:border-orange-500 focus:z-10 sm:text-sm"
            />
          </div>
        </div>

        <div class="flex items-center justify-between">
          <router-link to="/login" class="text-sm text-[#FF7E00] hover:text-[#ff9933]">
            已有账号？立即登录
          </router-link>
        </div>

        <div>
          <el-button
            type="primary"
            native-type="submit"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-[#FF7E00] hover:bg-[#ff9933] focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500"
          >
            注册
          </el-button>
        </div>
      </form>
    </div>
  </div>
</template>