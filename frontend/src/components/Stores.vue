<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { regionData } from 'element-china-area-data'
import { codeToText } from 'element-china-area-data';
import request from '../utils/request'
import { parseAddress } from '../utils/addressParser'

// 门店数据
const stores = ref([])

// 搜索关键词
const searchQuery = ref('')

// 状态筛选
const statusFilter = ref('all')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 新建/编辑门店对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentStore = ref({
  name: '',
  address: '',
  region: [],
  detailAddress: '',
  status: '营业中'
})

// 地区选择器配置
const regionOptions = ref(regionData)

// 处理门店状态更新
const handleStatusChange = (store, newStatus) => {
  const updatedStore = { ...store, status: newStatus === '营业中' ? 1 : 0 }
  request({
    url: `/stores/${store.storeId}`,  // 移除/api前缀
    method: 'put',
    data: updatedStore
  }).then(res => {
    if (res.code === 200) {
      ElMessage.success('门店状态更新成功')
      fetchStores() // 重新获取门店列表
    }
  }).catch(err => {
    console.error('更新门店状态失败:', err)
    ElMessage.error('更新门店状态失败')
  })
}

// 处理地区选择变化
const handleRegionChange = (value) => {
  let address = ''
  for (let i = 0; i < value.length; i++) {
    address += codeToText[value[i]]
  }
  currentStore.value.address = address + currentStore.value.detailAddress
}

// 处理详细地址变化
const handleDetailAddressChange = (value) => {
  if (currentStore.value.region.length > 0) {
    let regionAddress = ''
    for (let i = 0; i < currentStore.value.region.length; i++) {
      regionAddress += codeToText[currentStore.value.region[i]]
    }
    currentStore.value.address = regionAddress + value
  } else {
    currentStore.value.address = value
  }
  currentStore.value.detailAddress = value
}

const handleStoreSubmit = () => {
  if (!currentStore.value.name || !currentStore.value.address) {
    ElMessage.warning('请填写完整的门店信息')
    return
  }

  // 准备提交的数据
  const storeData = {
    name: currentStore.value.name,
    address: JSON.stringify({
      region: currentStore.value.region || [],
      fullAddress: currentStore.value.address,
      detailAddress: currentStore.value.detailAddress || ''
    }),
    gpsLocation: "0.0,0.0",
    status: currentStore.value.status === '营业中' ? 1 : 0
  }

  if (isEdit.value) {
    // 更新现有门店
    request({
      url: `/stores/${currentStore.value.storeId}`,
      method: 'put',
      data: { ...storeData, storeId: currentStore.value.storeId }
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('门店信息更新成功')
        fetchStores()
        dialogVisible.value = false
        resetForm()
      }
    }).catch(err => {
      console.error('更新门店失败:', err)
      ElMessage.error('更新门店失败')
    })
  } else {
    // 新建门店
    request({
      url: '/stores',
      method: 'post',
      data: storeData
    }).then(res => {
      if (res.code === 200) {
        ElMessage.success('门店创建成功')
        fetchStores()
        dialogVisible.value = false
        resetForm()
      }
    }).catch(err => {
      console.error('创建门店失败:', err)
      ElMessage.error('创建门店失败')
    })
  }
}

// 打开编辑对话框
const handleEdit = (store) => {
  // 尝试解析地址JSON
  let addressObj = {
    region: [],
    fullAddress: '',
    detailAddress: ''
  }

  try {
    if (typeof store.address === 'string') {
      if (store.address.startsWith('{')) {
        addressObj = JSON.parse(store.address)
      } else {
        addressObj.fullAddress = store.address
      }
    }
  } catch (error) {
    console.error('解析地址信息失败:', error)
    addressObj.fullAddress = store.address
  }

  currentStore.value = {
    storeId: store.storeId,
    name: store.name,
    status: store.status === 1 ? '营业中' : '暂停营业',
    region: addressObj.region || [],
    detailAddress: addressObj.detailAddress || '',
    address: addressObj.fullAddress || store.address
  }
  isEdit.value = true
  dialogVisible.value = true
}

// 获取门店列表
const fetchStores = () => {
  request({
    url: '/stores',
    method: 'get'
  }).then(res => {
    if (res.code === 200) {
      stores.value = res.data.map(store => {
        return {
          ...store,
          address: parseAddress(store.address),
          status: store.status === 1 ? '营业中' : '暂停营业',
          pendingOrders: 0
        }
      })
    } else {
      ElMessage.error(res.message || '获取门店列表失败')
    }
  }).catch(err => {
    console.error('获取门店列表失败:', err)
    if (err.response && err.response.status === 401) {
      ElMessage.warning('请先登录或登录已过期')
    } else {
      ElMessage.error(err.response?.data?.message || '获取门店列表失败，请稍后重试')
    }
  })
}

// 删除门店对话框
const deleteDialogVisible = ref(false)
const storeToDelete = ref(null)

// 处理删除门店
const handleDelete = (store) => {
  storeToDelete.value = store
  deleteDialogVisible.value = true
}

// 确认删除门店
const confirmDelete = () => {
  if (!storeToDelete.value) return
  
  request({
    url: `/stores/${storeToDelete.value.storeId}`,
    method: 'delete'
  }).then(res => {
    if (res.code === 200) {
      ElMessage.success('门店删除成功')
      fetchStores()
      deleteDialogVisible.value = false
      storeToDelete.value = null
    }
  }).catch(err => {
    console.error('删除门店失败:', err)
    if (err.response && err.response.data && err.response.data.message) {
      ElMessage.error(err.response.data.message)
    } else if (err.response && err.response.status === 401) {
      ElMessage.warning('登录已过期，请重新登录')
    } else {
      ElMessage.error('删除门店失败，请稍后重试')
    }
    deleteDialogVisible.value = false
    storeToDelete.value = null
  })
}

// 组件挂载时获取门店列表
onMounted(() => {
  fetchStores()
})

// 重置表单
const resetForm = () => {
  currentStore.value = {
    name: '',
    address: '',
    region: [],
    detailAddress: '',
    status: '营业中'
  }
}

// 打开新建门店对话框
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 过滤后的门店列表
const filteredStores = computed(() => {
  return stores.value.filter(store => {
    const matchesSearch = store.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         store.address.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesStatus = statusFilter.value === 'all' || store.status === statusFilter.value
    return matchesSearch && matchesStatus
  })
})

// 分页后的门店列表
const paginatedStores = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredStores.value.slice(start, end)
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">门店管理</h2>
      <el-button type="primary" class="bg-[#FF7E00] hover:bg-[#ff9933]" @click="dialogVisible = true">新建门店</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="flex space-x-4 mb-6">
      <!-- 顶部操作栏 -->
      <div class="flex justify-between items-center mb-4">
        <div class="flex items-center space-x-4">
          <el-input
            v-model="searchQuery"
            placeholder="搜索门店名称或地址"
            class="w-64"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="statusFilter" placeholder="营业状态">
            <el-option label="全部" value="all" />
            <el-option label="营业中" value="营业中" />
            <el-option label="暂停营业" value="暂停营业" />
          </el-select>
        </div>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新建门店
        </el-button>
      </div>
      <!-- 门店列表 -->
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">门店ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">门店名称</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">地址</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">待处理订单</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-900 divide-y divide-gray-200 dark:divide-gray-700">
          <tr v-for="store in paginatedStores" :key="store.storeId">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">{{ store.storeId }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">{{ store.name }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">{{ store.address }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <el-tag :type="store.status === '营业中' ? 'success' : 'warning'">{{ store.status }}</el-tag>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">{{ store.pendingOrders }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm">
              <el-button type="primary" size="small" @click="handleEdit(store)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(store)">删除</el-button>
              <el-dropdown trigger="click" @command="(command) => handleStatusChange(store, command)">
                <el-button size="small" class="ml-2">更多<i class="el-icon-arrow-down el-icon--right"></i></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="'营业中'">设为营业中</el-dropdown-item>
                    <el-dropdown-item :command="'暂停营业'">设为暂停营业</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="filteredStores.length"
        layout="prev, pager, next"
        class="!text-gray-600 dark:!text-gray-300"
      />
    </div>

    <!-- 新建/编辑门店对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑门店' : '新建门店'"
      width="500px"
    >
      <el-form :model="currentStore" label-width="100px">
        <el-form-item label="门店名称" required>
          <el-input v-model="currentStore.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-cascader
            v-model="currentStore.region"
            :options="regionOptions"
            @change="handleRegionChange"
            placeholder="请选择所在地区"
          />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input
            v-model="currentStore.detailAddress"
            placeholder="请输入详细地址"
            @input="handleDetailAddressChange"
          />
        </el-form-item>
        <el-form-item label="营业状态">
          <el-select v-model="currentStore.status">
            <el-option label="营业中" value="营业中" />
            <el-option label="暂停营业" value="暂停营业" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStoreSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除门店确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除确认"
      width="400px"
    >
      <p>确定要删除门店 "{{ storeToDelete?.name }}" 吗？此操作不可恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-cascader {
  width: 100%;
}

.el-pagination :deep(.el-pager li) {
  @apply dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600;
}

.el-pagination :deep(.el-pager li.is-active) {
  @apply dark:bg-[#FF7E00] dark:text-white;
}

.el-pagination :deep(.btn-prev),
.el-pagination :deep(.btn-next) {
  @apply dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600;
}
</style>