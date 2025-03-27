<script setup>
import { ElConfigProvider } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const theme = ref(localStorage.getItem('settings.theme') || 'light')

// 切换主题
const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  localStorage.setItem('settings.theme', theme.value)
  document.documentElement.classList.remove('light', 'dark')
  document.documentElement.classList.add(theme.value)
}

// 退出登录
const handleLogout = () => {
  // TODO: 实现退出登录逻辑
  router.push('/login')
}

// 在组件挂载时应用保存的设置
onMounted(() => {
  // 应用主题设置
  document.documentElement.classList.remove('light', 'dark')
  document.documentElement.classList.add(theme.value)

  // 应用语言设置
  const language = localStorage.getItem('settings.language') || 'zh'
  document.documentElement.setAttribute('lang', language)

  // 应用系统名称
  const systemName = localStorage.getItem('settings.systemName') || '快递管理系统'
  document.title = systemName
})
</script>

<template>
  <el-config-provider>
    <div class="min-h-screen dark:bg-gray-900 transition-colors duration-200">
      <template v-if="$route.path !== '/login' && $route.path !== '/register'">
        <div class="bg-gray-100 dark:bg-gray-800 transition-colors duration-200">
          <header class="bg-white dark:bg-gray-900 shadow z-20 relative transition-colors duration-200">
            <div class="max-w-7xl mx-auto py-6 px-4 flex justify-between items-center">
              <h1 class="text-3xl font-bold text-gray-900 dark:text-white transition-colors duration-200">快递管理系统</h1>
              <div class="flex items-center space-x-4">
                <button
                  @click="toggleTheme"
                  class="p-2 rounded-lg bg-gray-200 dark:bg-gray-700 hover:bg-gray-300 dark:hover:bg-gray-600 transition-all duration-200 text-gray-700 dark:text-gray-200"
                  :title="theme === 'light' ? '切换到深色模式' : '切换到浅色模式'"
                >
                  <span v-if="theme === 'light'" class="text-gray-700">🌙</span>
                  <span v-else class="text-yellow-400">☀️</span>
                </button>
                <button
                  @click="handleLogout"
                  class="px-4 py-2 rounded-lg bg-red-500 hover:bg-red-600 text-white transition-colors duration-200"
                >
                  退出登录
                </button>
              </div>
            </div>
          </header>
          <main class="flex">
            <!-- 主要内容区域 -->
            <div class="flex-1 p-6 bg-gray-50 dark:bg-gray-800 transition-colors duration-200">
              <router-view />
            </div>
          </main>
        </div>
      </template>
      <template v-else>
        <router-view />
      </template>
    </div>
  </el-config-provider>
</template>

<style>
@import 'element-plus/dist/index.css';

/* 主题相关样式 */
:root.light {
  --bg-primary: #ffffff;
  --text-primary: #333333;
  --bg-secondary: #f9fafb;
  --text-secondary: #4b5563;
}

:root.dark {
  --bg-primary: #1a1a1a;
  --text-primary: #ffffff;
  --bg-secondary: #2d3748;
  --text-secondary: #a0aec0;
}

/* 应用主题变量 */
body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.2s ease-in-out, color 0.2s ease-in-out;
}

/* Element Plus 暗色模式适配 */
.dark .el-dialog,
.dark .el-message-box,
.dark .el-notification,
.dark .el-message {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  border-color: var(--bg-secondary);
}
</style>
