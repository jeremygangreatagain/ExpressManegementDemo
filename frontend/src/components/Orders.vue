<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

// 获取当前用户角色
const userRole = localStorage.getItem('userRole')
const isAdminOrStaff = computed(() => userRole === 'admin' || userRole === 'staff')

// 订单数据
const orders = ref([])

// 搜索关键词
const searchQuery = ref('')

// 筛选状态
const statusFilter = ref('all')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 状态映射
const statusMap = {
  0: '待处理',
  1: '配送中',
  2: '已完成'
}

const statusReverseMap = {
  '待处理': 0,
  '配送中': 1,
  '已完成': 2
}

// 新建订单对话框
const dialogVisible = ref(false)
const newOrder = ref({
  senderName: '',
  senderPhone: '',
  senderAddress: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  itemType: '电器',
  status: 0
})

// 获取订单列表
const fetchOrders = async () => {
  try {
    const response = await request.get('/orders/list')
    if (response.code === 200) {
      // 处理订单数据，解析JSON字符串
      orders.value = response.data.orders.map(order => {
        const senderInfo = JSON.parse(order.senderInfo || '{}')
        const receiverInfo = JSON.parse(order.receiverInfo || '{}')
        return {
          id: order.orderId || '',  // 使用orderId字段作为订单号
          status: order.status || 0,  // 确保状态不为空
          time: order.createdAt || '',
          itemType: order.itemType || '',
          senderName: senderInfo.name || '',
          senderPhone: senderInfo.phone || '',
          senderAddress: senderInfo.address || '',
          receiverName: receiverInfo.name || '',
          receiverPhone: receiverInfo.phone || '',
          receiverAddress: receiverInfo.address || ''
        }
      })
    } else {
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  }
}

// 页面加载时获取订单列表
onMounted(() => {
  fetchOrders()
})

// 处理订单状态更新
const handleStatusChange = async (order, newStatusText) => {
  try {
    // 检查用户权限
    if (!isAdminOrStaff.value) {
      ElMessage.error('您没有权限执行此操作')
      return
    }

    if (!order || !order.id) {
      ElMessage.error('订单ID不存在，无法更新状态')
      return
    }
    
    // 将状态文本转换为数值
    const newStatus = statusReverseMap[newStatusText]
    console.log('更新订单状态:', order.id, newStatusText, newStatus)
    
    const response = await request.put(`/orders/${order.id}/status`, { status: newStatus })
    if (response.code === 200) {
      order.status = newStatus
      ElMessage.success('订单状态更新成功')
      // 刷新订单列表
      fetchOrders()
    } else {
      ElMessage.error(response.message || '更新订单状态失败')
    }
  } catch (error) {
    console.error('更新订单状态失败:', error)
    if (error.response && error.response.status === 401) {
      ElMessage.error('未授权或登录已过期，请重新登录')
    } else {
      ElMessage.error('更新订单状态失败')
    }
  }
}

// 处理新建订单
const handleCreateOrder = async () => {
  // 验证必填字段
  if (!newOrder.value.senderName || !newOrder.value.senderPhone || !newOrder.value.senderAddress ||
      !newOrder.value.receiverName || !newOrder.value.receiverPhone || !newOrder.value.receiverAddress) {
    ElMessage.warning('请填写完整的订单信息')
    return
  }

  try {
    // 构建符合后端API要求的订单数据结构
    const orderData = {
      senderInfo: JSON.stringify({
        name: newOrder.value.senderName,
        phone: newOrder.value.senderPhone,
        address: newOrder.value.senderAddress
      }),
      receiverInfo: JSON.stringify({
        name: newOrder.value.receiverName,
        phone: newOrder.value.receiverPhone,
        address: newOrder.value.receiverAddress
      }),
      itemType: newOrder.value.itemType,
      status: newOrder.value.status
    }

    const response = await request.post('/orders', orderData)
    if (response.code === 200) {
      // 重新获取订单列表
      fetchOrders()
      
      dialogVisible.value = false
      newOrder.value = {
        senderName: '',
        senderPhone: '',
        senderAddress: '',
        receiverName: '',
        receiverPhone: '',
        receiverAddress: '',
        itemType: '电器',
        status: 0
      }
      ElMessage.success('订单创建成功')
    } else {
      ElMessage.error(response.message || '创建订单失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  }
}

// 根据搜索条件和状态筛选订单
const filteredOrders = computed(() => {
  return orders.value.filter(order => {
    const matchesSearch = searchQuery.value === '' || 
      (order.id && String(order.id).includes(searchQuery.value)) ||
      (order.senderName && order.senderName.includes(searchQuery.value)) ||
      (order.senderPhone && order.senderPhone.includes(searchQuery.value)) ||
      (order.receiverName && order.receiverName.includes(searchQuery.value)) ||
      (order.receiverPhone && order.receiverPhone.includes(searchQuery.value)) ||
      (order.senderAddress && order.senderAddress.includes(searchQuery.value)) ||
      (order.receiverAddress && order.receiverAddress.includes(searchQuery.value))

    // 状态筛选：当选择'all'时显示所有订单，否则将文本状态转换为数字后再比较
    const matchesStatus = statusFilter.value === 'all' || order.status === statusReverseMap[statusFilter.value]

    return matchesSearch && matchesStatus
  })
})

// 分页后的订单数据
const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredOrders.value.slice(start, end)
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">订单管理</h2>
      <el-button type="primary" class="bg-[#FF7E00] hover:bg-[#ff9933]" @click="dialogVisible = true">新建订单</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="flex space-x-4 mb-6">
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单号、寄件人、收件人、地址或电话"
        class="w-64"
      >
        <template #prefix>
          <el-icon><i class="el-icon-search"></i></el-icon>
        </template>
      </el-input>

      <el-select v-model="statusFilter" placeholder="订单状态" class="w-32">
        <el-option label="全部" value="all" />
        <el-option label="待处理" value="待处理" />
        <el-option label="配送中" value="配送中" />
        <el-option label="已完成" value="已完成" />
      </el-select>
    </div>

    <!-- 订单列表 -->
    <div class="bg-white dark:bg-gray-700 rounded-lg shadow overflow-hidden overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-32">订单号</th>
            <th v-if="isAdminOrStaff" class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-20">寄件人</th>
            <th v-if="isAdminOrStaff" class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-28">寄件人电话</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-40">寄件地址</th>
            <th v-if="isAdminOrStaff" class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-20">收件人</th>
            <th v-if="isAdminOrStaff" class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-28">收件人电话</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-40">收件地址</th>
            <th v-if="isAdminOrStaff" class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-20">物品类型</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-20">状态</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-32">时间</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider w-20">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-700 divide-y divide-gray-200 dark:divide-gray-600">
          <tr v-for="order in paginatedOrders" :key="order.id">
            <td class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.id }}</td>
            <td v-if="isAdminOrStaff" class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.senderName }}</td>
            <td v-if="isAdminOrStaff" class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.senderPhone }}</td>
            <td class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.senderAddress }}</td>
            <td v-if="isAdminOrStaff" class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.receiverName }}</td>
            <td v-if="isAdminOrStaff" class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.receiverPhone }}</td>
            <td class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.receiverAddress }}</td>
            <td v-if="isAdminOrStaff" class="px-3 py-2 text-sm text-gray-900 dark:text-white">{{ order.itemType }}</td>
            <td class="px-3 py-2">
              <span :class="{
                  'px-2 py-1 text-xs font-medium rounded-full': true,
                  'bg-yellow-100 text-yellow-800 dark:bg-yellow-800 dark:text-yellow-100': order.status === 0,
                  'bg-blue-100 text-blue-800 dark:bg-blue-800 dark:text-blue-100': order.status === 1,
                  'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100': order.status === 2
                }">
                  {{ statusMap[order.status] }}
                </span>
            </td>
            <td class="px-3 py-2 text-sm text-gray-500 dark:text-gray-400">{{ order.time }}</td>
            <td class="px-3 py-2 text-sm text-gray-500">
              <el-dropdown @command="(command) => handleStatusChange(order, command)">
                <el-button type="text" class="text-[#FF7E00] hover:text-[#ff9933]">
                  更改状态<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="待处理">待处理</el-dropdown-item>
                    <el-dropdown-item command="配送中">配送中</el-dropdown-item>
                    <el-dropdown-item command="已完成">已完成</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 新建订单对话框 -->
    <el-dialog v-model="dialogVisible" title="新建订单" width="500px">
      <el-form :model="newOrder" label-width="80px">
        <el-form-item label="寄件人">
          <el-input v-model="newOrder.senderName" placeholder="请输入寄件人姓名"></el-input>
        </el-form-item>
        <el-form-item label="寄件电话">
          <el-input v-model="newOrder.senderPhone" placeholder="请输入寄件人电话"></el-input>
        </el-form-item>
        <el-form-item label="寄件地址">
          <el-input v-model="newOrder.senderAddress" placeholder="请输入寄件地址"></el-input>
        </el-form-item>
        <el-form-item label="收件人">
          <el-input v-model="newOrder.receiverName" placeholder="请输入收件人姓名"></el-input>
        </el-form-item>
        <el-form-item label="收件电话">
          <el-input v-model="newOrder.receiverPhone" placeholder="请输入收件人电话"></el-input>
        </el-form-item>
        <el-form-item label="收件地址">
          <el-input v-model="newOrder.receiverAddress" placeholder="请输入收件地址"></el-input>
        </el-form-item>
        <el-form-item label="物品类型">
          <el-select v-model="newOrder.itemType" placeholder="请选择物品类型" class="w-full">
            <el-option label="电器" value="电器" />
            <el-option label="玻璃" value="玻璃" />
            <el-option label="陶瓷" value="陶瓷" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateOrder">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  @apply dark:bg-gray-700 dark:border-gray-600;
}

.el-input :deep(.el-input__inner) {
  @apply dark:text-gray-200;
}

.el-button {
  border-radius: 8px;
}

.el-dialog :deep(.el-dialog__header) {
  @apply dark:bg-gray-800;
}

.el-dialog :deep(.el-dialog__header .el-dialog__title) {
  @apply dark:text-gray-200;
}

.el-dialog :deep(.el-dialog__body) {
  @apply dark:bg-gray-800;
}

.el-dialog :deep(.el-dialog__footer) {
  @apply dark:bg-gray-800;
}

.el-form-item :deep(.el-form-item__label) {
  @apply dark:text-gray-300;
}

.el-input :deep(.el-input__wrapper) {
  @apply dark:bg-gray-700 dark:border-gray-600;
}

.el-input :deep(.el-input__inner) {
  @apply dark:text-gray-200;
}

.el-select :deep(.el-input__wrapper) {
  @apply dark:bg-gray-700 dark:border-gray-600;
}

.el-select :deep(.el-select__input) {
  @apply dark:text-gray-200;
}

.el-pagination {
  --el-pagination-button-bg-color: #FF7E00;
  --el-pagination-hover-color: #ff8533;
}
</style>