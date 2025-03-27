<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-800 transition-colors duration-200">
    <!-- 管理员和员工的侧边栏 -->
    <div v-if="userRole !== 'customer'" class="fixed inset-y-0 left-0 w-64 bg-white dark:bg-gray-900 shadow-lg z-10 transition-colors duration-200">
      <div class="flex items-center justify-center h-16 border-b dark:border-gray-700">
        <h1 class="text-xl font-bold text-[#FF7E00] dark:text-[#ff9933]">快递管理系统</h1>
      </div>
      <nav class="mt-6">
        <el-menu :default-active="activeMenu" class="border-0 !bg-transparent" @select="handleMenuSelect">
          <template v-if="accessibleRoutes.includes('Dashboard')">
            <el-menu-item index="dashboard" class="dark:text-gray-300 dark:hover:bg-gray-800">
              <el-icon><i class="el-icon-menu"></i></el-icon>
              <span>数据统计</span>
            </el-menu-item>
          </template>
          <template v-if="accessibleRoutes.includes('Orders')">
            <el-menu-item index="orders" class="dark:text-gray-300 dark:hover:bg-gray-800">
              <el-icon><i class="el-icon-document"></i></el-icon>
              <span>订单管理</span>
            </el-menu-item>
          </template>
          <template v-if="accessibleRoutes.includes('Users')">
            <el-menu-item index="users" class="dark:text-gray-300 dark:hover:bg-gray-800">
              <el-icon><i class="el-icon-user"></i></el-icon>
              <span>用户管理</span>
            </el-menu-item>
          </template>
          <template v-if="accessibleRoutes.includes('Staff')">
            <el-menu-item index="staff" class="dark:text-gray-300 dark:hover:bg-gray-800">
              <el-icon><i class="el-icon-user-filled"></i></el-icon>
              <span>员工管理</span>
            </el-menu-item>
          </template>
          <template v-if="accessibleRoutes.includes('Stores')">
            <el-menu-item index="stores" class="dark:text-gray-300 dark:hover:bg-gray-800">
              <el-icon><i class="el-icon-location"></i></el-icon>
              <span>门店管理</span>
            </el-menu-item>
          </template>
          <template v-if="accessibleRoutes.includes('WorkLogs')">
            <el-menu-item index="worklogs" class="dark:text-gray-300 dark:hover:bg-gray-800">
              <el-icon><i class="el-icon-document"></i></el-icon>
              <span>工作日志</span>
            </el-menu-item>
          </template>
        </el-menu>
      </nav>
    </div>

    <!-- 主要内容区域 -->
    <div class="min-h-screen bg-gray-100 dark:bg-gray-800 transition-colors duration-200" :class="{'ml-64': userRole !== 'customer'}">
      <!-- 普通用户的卡片式布局 -->
      <div v-if="userRole === 'customer'" class="p-4 max-w-6xl mx-auto">
        <div class="grid grid-cols-2 gap-4">
          <div @click="handleCreateOrder" class="cursor-pointer bg-gradient-to-br from-[#3376f2] to-[#4a8bff] rounded-xl shadow-lg p-8 hover:shadow-2xl hover:scale-105 transition-all duration-300 flex flex-col items-center justify-center h-[500px] group">
            <div class="text-center space-y-8">
              <div class="w-32 h-32 bg-white/20 rounded-full flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <el-icon class="text-6xl text-white"><i class="el-icon-plus"></i></el-icon>
              </div>
              <div class="space-y-3">
                <h3 class="text-4xl font-bold text-white">发布订单</h3>
                <p class="text-white/80 text-xl">快速发布新的快递订单</p>
              </div>
            </div>
          </div>
          <div @click="handleViewOrders" class="cursor-pointer bg-gradient-to-br from-[#3376f2] to-[#4a8bff] rounded-xl shadow-lg p-8 hover:shadow-2xl hover:scale-105 transition-all duration-300 flex flex-col items-center justify-center h-[500px] group">
            <div class="text-center space-y-8">
              <div class="w-32 h-32 bg-white/20 rounded-full flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300">
                <el-icon class="text-6xl text-white"><i class="el-icon-document"></i></el-icon>
              </div>
              <div class="space-y-3">
                <h3 class="text-4xl font-bold text-white">查看订单</h3>
                <p class="text-white/80 text-xl">管理和追踪您的订单</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 管理员和员工的页面内容 -->
      <div v-else class="p-8">
        <router-view />
      </div>
    </div>

    <!-- 发布订单对话框 -->
    <el-dialog
      v-model="createOrderDialogVisible"
      title="发布订单"
      width="60%"
      class="order-dialog"
    >
      <div class="p-8 space-y-8 bg-gradient-to-br from-[#3376f2]/10 to-[#4a8bff]/10 rounded-xl">
        <!-- 发货人信息 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">发货人信息</h3>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="姓名" required>
              <el-input v-model="senderInfo.name" placeholder="请输入发货人姓名" />
            </el-form-item>
            <el-form-item label="电话" required>
              <el-input v-model="senderInfo.phone" placeholder="请输入发货人电话" />
            </el-form-item>
          </div>
          <el-form-item label="地址" required>
            <el-cascader
              v-model="senderInfo.region"
              :options="regionOptions"
              placeholder="请选择省/市/区"
              class="w-full"
            />
            <el-input
              v-model="senderInfo.address"
              placeholder="请输入详细地址"
              class="mt-2"
            />
          </el-form-item>
        </div>

        <!-- 收货人信息 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">收货人信息</h3>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="姓名" required>
              <el-input v-model="receiverInfo.name" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="电话" required>
              <el-input v-model="receiverInfo.phone" placeholder="请输入收货人电话" />
            </el-form-item>
          </div>
          <el-form-item label="地址" required>
            <el-cascader
              v-model="receiverInfo.region"
              :options="regionOptions"
              placeholder="请选择省/市/区"
              class="w-full"
            />
            <el-input
              v-model="receiverInfo.address"
              placeholder="请输入详细地址"
              class="mt-2"
            />
          </el-form-item>
        </div>

        <!-- 物品信息 -->
        <div class="space-y-4">
          <h3 class="text-lg font-medium text-gray-900 dark:text-white">物品信息</h3>
          <el-form-item label="物品种类" required>
            <el-select v-model="itemInfo.type" placeholder="请选择物品种类" class="w-full">
              <el-option label="玻璃制品" value="glass" />
              <el-option label="陶瓷制品" value="ceramic" />
              <el-option label="电子产品" value="electronics" />
              <el-option label="易碎品" value="fragile" />
              <el-option label="文件资料" value="documents" />
              <el-option label="服装鞋帽" value="clothing" />
              <el-option label="食品生鲜" value="food" />
              <el-option label="医疗用品" value="medical" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input
              v-model="itemInfo.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入备注信息（选填）"
            />
          </el-form-item>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createOrderDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOrder">确认发布</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看订单对话框 -->
    <el-dialog
      v-model="viewOrdersDialogVisible"
      title="我的订单"
      width="80%"
      class="order-dialog"
    >
      <div class="p-8 bg-gradient-to-br from-[#3376f2]/10 to-[#4a8bff]/10 rounded-xl">
        <!-- 订单列表 -->
        <div class="bg-white dark:bg-gray-700 rounded-lg shadow overflow-hidden overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
            <thead class="bg-gray-50 dark:bg-gray-800">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">订单号</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">寄件地址</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">收件地址</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">状态</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">时间</th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-gray-700 divide-y divide-gray-200 dark:divide-gray-600">
              <tr v-for="order in userOrders" :key="order.id">
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">#{{ order.id }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ order.senderAddress }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ order.receiverAddress }}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span :class="{
                    'px-2 py-1 text-xs font-medium rounded-full': true,
                    'bg-yellow-100 text-yellow-800 dark:bg-yellow-800 dark:text-yellow-100': order.status === '待处理',
                    'bg-blue-100 text-blue-800 dark:bg-blue-800 dark:text-blue-100': order.status === '配送中',
                    'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100': order.status === '已完成'
                  }">
                    {{ order.status }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ order.time }}</td>
              </tr>
              <tr v-if="userOrders.length === 0">
                <td colspan="5" class="px-6 py-4 text-center text-sm text-gray-500 dark:text-gray-400">暂无订单数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="viewOrdersDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

// 用户角色（从登录状态获取）
const userRole = ref(localStorage.getItem('userRole') || 'customer') // 从localStorage获取用户角色，默认为普通用户

// 根据用户角色设置可访问的路由
const accessibleRoutes = computed(() => {
  switch (userRole.value) {
    case 'admin':
      return ['Dashboard', 'Orders', 'Users', 'Staff', 'Stores', 'WorkLogs']
    case 'staff':
      return ['Dashboard', 'Orders', 'WorkLogs']
    case 'customer':
      return ['Orders']
    default:
      return []
  }
})

// 路由相关
const router = useRouter()
const activeMenu = ref('dashboard')

// 页面标题
const getPageTitle = computed(() => {
  // 如果是普通用户，始终显示快递管理系统
  if (userRole.value === 'customer') {
    return '快递管理系统'
  }
  
  // 管理员和员工根据当前菜单显示对应标题
  const routeMap = {
    dashboard: '数据统计',
    orders: '订单管理',
    users: '用户管理',
    staff: '员工管理',
    stores: '门店管理',
    worklogs: '工作日志'
  }
  return routeMap[activeMenu.value] || '快递管理系统'
})

// 菜单选择处理
const handleMenuSelect = (index) => {
  activeMenu.value = index
  router.push(`/${index}`)
}

// 对话框状态
const createOrderDialogVisible = ref(false)
const viewOrdersDialogVisible = ref(false)

// 表单数据
const senderInfo = ref({
  name: '',
  phone: '',
  region: [],
  address: ''
})

const receiverInfo = ref({
  name: '',
  phone: '',
  region: [],
  address: ''
})

const itemInfo = ref({
  type: '',
  remark: ''
})

// 用户订单数据
const userOrders = ref([])

// 获取用户订单
const fetchUserOrders = async () => {
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) return
    
    const response = await request.get(`/orders/user/${userId}`)
    if (response.code === 200) {
      userOrders.value = response.data
    }
  } catch (error) {
    console.error('获取用户订单失败:', error)
  }
}

// 地区选项（示例数据）
const regionOptions = ref([
  {
    value: 'guangdong',
    label: '广东省',
    children: [
      {
        value: 'shenzhen',
        label: '深圳市',
        children: [
          { value: 'nanshan', label: '南山区' },
          { value: 'futian', label: '福田区' }
        ]
      }
    ]
  }
])

// 处理创建订单
const handleCreateOrder = () => {
  createOrderDialogVisible.value = true
}

// 处理查看订单
const handleViewOrders = () => {
  fetchUserOrders()
  viewOrdersDialogVisible.value = true
}

// 提交订单
const submitOrder = async () => {
  // 验证必填字段
  if (!senderInfo.value.name || !senderInfo.value.phone || !senderInfo.value.address ||
      !receiverInfo.value.name || !receiverInfo.value.phone || !receiverInfo.value.address ||
      !itemInfo.value.type) {
    ElMessage.warning('请填写完整的订单信息')
    return
  }
  
  try {
    const userId = localStorage.getItem('userId')
    if (!userId) {
      ElMessage.error('用户未登录')
      return
    }
    
    const orderData = {
      userId: userId,
      senderName: senderInfo.value.name,
      senderPhone: senderInfo.value.phone,
      senderAddress: senderInfo.value.region.join('/') + senderInfo.value.address,
      receiverName: receiverInfo.value.name,
      receiverPhone: receiverInfo.value.phone,
      receiverAddress: receiverInfo.value.region.join('/') + receiverInfo.value.address,
      itemType: itemInfo.value.type,
      remark: itemInfo.value.remark || ''
    }
    
    const response = await request.post('/orders', orderData)
    if (response.code === 200) {
      ElMessage.success('订单创建成功')
      createOrderDialogVisible.value = false
      
      // 重置表单
      senderInfo.value = { name: '', phone: '', region: [], address: '' }
      receiverInfo.value = { name: '', phone: '', region: [], address: '' }
      itemInfo.value = { type: '', remark: '' }
    } else {
      ElMessage.error(response.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  }
}
</script>

<style scoped>
.el-menu {
  border-right: none;
}

.el-menu-item.is-active {
  color: #FF7E00 !important;
}

.el-menu-item:hover {
  color: #ff8533 !important;
}

.order-dialog :deep(.el-dialog) {
  border-radius: 1rem;
  overflow: hidden;
}

.order-dialog :deep(.el-dialog__header) {
  background: linear-gradient(to bottom right, #3376f2, #4a8bff);
  padding: 1.5rem;
  margin-right: 0;
}

.order-dialog :deep(.el-dialog__title) {
  color: white;
  font-size: 1.25rem;
  font-weight: bold;
}

.order-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
}

.order-dialog :deep(.el-form-item__label) {
  font-weight: 500;
}

.order-dialog :deep(.el-input__wrapper),
.order-dialog :deep(.el-select),
.order-dialog :deep(.el-cascader) {
  border-radius: 0.5rem;
}

.order-dialog :deep(.el-button) {
  border-radius: 0.5rem;
  padding: 0.75rem 1.5rem;
}

.order-dialog :deep(.el-button--primary) {
  background: linear-gradient(to right, #3376f2, #4a8bff);
  border: none;
}

.order-dialog :deep(.el-button--default) {
  border: 2px solid #3376f2;
  color: #3376f2;
}
</style>