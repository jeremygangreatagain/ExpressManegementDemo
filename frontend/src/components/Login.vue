<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const isLogin = ref(true)
const loginForm = ref({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
  role: 'user' // 默认角色为普通用户
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: ''
})

const captchaImage = ref('')

// 获取验证码
const getCaptcha = async () => {
  try {
    const response = await axios.get('/api/auth/captcha')
    if (response.data.code === 200) {
      captchaImage.value = response.data.data.captchaImage
      loginForm.value.captchaKey = response.data.data.captchaKey
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  }
}

// 页面加载时获取验证码
onMounted(() => {
  getCaptcha()
})

const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  if (!loginForm.value.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  
  try {
    const response = await axios.post('/api/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password,
      captcha: loginForm.value.captcha,
      captchaKey: loginForm.value.captchaKey
    })
    
    if (response.data.code === 200) {
      localStorage.setItem('token', response.data.data.token)
      localStorage.setItem('userRole', response.data.data.user.role)
      localStorage.setItem('userId', response.data.data.user.userId)
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(response.data.message || '登录失败')
      // 刷新验证码
      getCaptcha()
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请稍后重试')
    // 刷新验证码
    getCaptcha()
  }
}

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
    const response = await axios.post('/api/auth/register', {
      username: registerForm.value.username,
      password: registerForm.value.password,
      phone: registerForm.value.phone
    })
    
    if (response.data.code === 200) {
      ElMessage.success('注册成功')
      isLogin.value = true
      // 注册成功后刷新验证码
      getCaptcha()
    } else {
      ElMessage.error(response.data.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    ElMessage.error('注册失败，请稍后重试')
  }
}

const toggleForm = () => {
  isLogin.value = !isLogin.value
  if (isLogin.value) {
    // 切换到登录页面时刷新验证码
    getCaptcha()
  }
}
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <div class="relative w-full flex">
      <!-- 登录表单 -->
      <div :class="['w-1/2 flex items-center justify-center bg-white dark:bg-gray-800 transition-all duration-500 ease-in-out h-screen', !isLogin ? '-translate-x-full' : '']">
        <div class="w-full max-w-md space-y-6 px-8">
          <div class="text-center">
            <h2 class="text-2xl font-bold text-gray-900 dark:text-white">用户登录</h2>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">欢迎使用快递管理系统</p>
          </div>

          <form @submit.prevent="handleLogin" class="space-y-4">
            <div>
              <el-select
                v-model="loginForm.role"
                class="w-full mb-4 dark:bg-gray-700 dark:text-white dark:border-gray-600"
                size="large"
              >
                <el-option label="管理员" value="admin" />
                <el-option label="员工" value="staff" />
                <el-option label="用户" value="customer" />
              </el-select>
            </div>
            <div>
              <el-input
                v-model="loginForm.username"
                type="text"
                required
                size="large"
                placeholder="用户名"
                class="w-full dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
            </div>
            <div>
              <el-input
                v-model="loginForm.password"
                type="password"
                required
                size="large"
                placeholder="密码"
                class="w-full dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
            </div>
            
            <!-- 验证码区域 -->
            <div class="flex space-x-2">
              <el-input
                v-model="loginForm.captcha"
                type="text"
                required
                size="large"
                placeholder="验证码"
                class="w-2/3 dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
              <div 
                class="w-1/3 h-10 overflow-hidden rounded cursor-pointer"
                @click="getCaptcha"
              >
                <img 
                  v-if="captchaImage" 
                  :src="captchaImage" 
                  alt="验证码" 
                  class="w-full h-full object-cover"
                />
                <div v-else class="w-full h-full flex items-center justify-center bg-gray-200 dark:bg-gray-700">
                  <span class="text-sm text-gray-500 dark:text-gray-400">加载中...</span>
                </div>
              </div>
            </div>

            <div class="flex items-center justify-between text-sm">
              <div class="flex items-center">
                <input type="checkbox" class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600 rounded dark:bg-gray-700">
                <label class="ml-2 text-gray-600 dark:text-gray-400">记住密码？</label>
              </div>
              <a href="#" class="text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300">忘记密码</a>
            </div>

            <el-button
              type="primary"
              native-type="submit"
              size="large"
              class="w-full bg-blue-600 hover:bg-blue-500 dark:bg-blue-700 dark:hover:bg-blue-600"
            >
              登录
            </el-button>
          </form>

          <div class="text-center">
            <button
              @click="toggleForm"
              class="text-sm text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300"
            >
              没有账号？立即注册
            </button>
          </div>
        </div>
      </div>

      <!-- 注册表单 -->
      <div :class="['w-1/2 flex items-center justify-center bg-white dark:bg-gray-800 transition-all duration-500 ease-in-out absolute right-0 h-screen', isLogin ? 'translate-x-full' : '']">
        <div class="w-full max-w-md space-y-6 px-8">
          <div class="text-center">
            <h2 class="text-2xl font-bold text-gray-900 dark:text-white">用户注册</h2>
            <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">创建您的账号</p>
          </div>

          <form @submit.prevent="handleRegister" class="space-y-4">
            <div>
              <el-input
                v-model="registerForm.username"
                type="text"
                required
                size="large"
                placeholder="用户名"
                class="w-full dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
            </div>
            <div>
              <el-input
                v-model="registerForm.phone"
                type="text"
                required
                size="large"
                placeholder="手机号码"
                class="w-full dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
            </div>
            <div>
              <el-input
                v-model="registerForm.password"
                type="password"
                required
                size="large"
                placeholder="密码"
                class="w-full dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
            </div>
            <div>
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                required
                size="large"
                placeholder="确认密码"
                class="w-full dark:bg-gray-700 dark:text-white dark:border-gray-600"
              />
            </div>

            <el-button
              type="primary"
              native-type="submit"
              size="large"
              class="w-full bg-blue-600 hover:bg-blue-500 dark:bg-blue-700 dark:hover:bg-blue-600"
            >
              注册
            </el-button>
          </form>

          <div class="text-center">
            <button
              @click="toggleForm"
              class="text-sm text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300"
            >
              已有账号？立即登录
            </button>
          </div>
        </div>
      </div>

      <!-- 滑动背景 -->
      <div :class="['absolute top-0 w-1/2 h-full bg-gradient-to-br from-blue-500 to-blue-600 dark:from-blue-600 dark:to-blue-700 transition-all duration-500 ease-in-out shadow-2xl transform flex items-center justify-center', isLogin ? 'translate-x-full scale-[1.02]' : 'scale-[1.02]']">
        <div class="text-center text-white space-y-4">
          <h3 class="text-3xl font-bold" v-if="isLogin">还没有账号？</h3>
          <h3 class="text-3xl font-bold" v-else>已经有账号了？</h3>
          <p class="text-lg" v-if="isLogin">立即注册，开始使用快递管理系统</p>
          <p class="text-lg" v-else>立即登录，继续使用快递管理系统</p>
          <button
            @click="toggleForm"
            class="mt-4 px-8 py-2 border-2 border-white rounded-full text-white hover:bg-white hover:text-blue-600 transition-colors duration-300"
          >
            {{ isLogin ? '注册' : '登录' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.el-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.el-button {
  border-radius: 8px;
}
</style>