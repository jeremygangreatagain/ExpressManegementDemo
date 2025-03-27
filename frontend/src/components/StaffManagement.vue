<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

// 门店数据
const stores = ref([])

// 员工数据
const staffs = ref([])

// 搜索和筛选
const searchQuery = ref('')
const selectedStore = ref('all')
const statusFilter = ref('all')

// 新建员工对话框
const dialogVisible = ref(false)
const newStaff = ref({
  name: '',
  storeId: '',
  position: '',
  phone: '',
  status: '在职',
  joinDate: '' // 前端使用joinDate表示入职日期，后端会转换为createdAt
})

// 编辑员工对话框
const editDialogVisible = ref(false)
const currentStaff = ref(null)

// 过滤后的员工列表
const filteredStaffs = computed(() => {
  return staffs.value.filter(staff => {
    const matchesSearch = staff.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         staff.phone.includes(searchQuery.value)
    const matchesStore = selectedStore.value === 'all' || staff.storeId === parseInt(selectedStore.value)
    const matchesStatus = statusFilter.value === 'all' || staff.status === statusFilter.value
    return matchesSearch && matchesStore && matchesStatus
  })
})

// 按门店分组的员工列表
const groupedStaffs = computed(() => {
  const grouped = {}
  // 将员工分配到对应的门店
  filteredStaffs.value.forEach(staff => {
    if (!grouped[staff.storeId]) {
      grouped[staff.storeId] = []
    }
    grouped[staff.storeId].push(staff)
  })
  return grouped
})

// 获取门店列表
const fetchStores = () => {
  request({
    url: '/stores',
    method: 'get'
  }).then(res => {
    if (res.code === 200) {
      stores.value = res.data
    } else {
      ElMessage.error(res.message || '获取门店列表失败')
    }
  }).catch(err => {
    console.error('获取门店列表失败:', err)
    ElMessage.error('获取门店列表失败')
  })
}

// 获取员工列表
const fetchStaffs = async () => {
  try {
    const response = await request.get('/staff')
    if (response.code === 200) {
      // 处理员工数据，将数字状态转换为文本状态
      const statusTextMap = {
        0: '离职',
        1: '在职',
        2: '休假'
      }
      
      staffs.value = (response.data.staffs || []).map(staff => ({
        ...staff,
        id: staff.staffId, // 确保id字段存在，用于前端列表渲染
        status: statusTextMap[staff.status] || '在职', // 将数字状态转换为文本状态
        storeId: staff.storeId // 确保storeId是数字类型
      }))
    } else {
      ElMessage.error(response.message || '获取员工列表失败')
    }
  } catch (error) {
    console.error('获取员工列表失败:', error)
    ElMessage.error('获取员工列表失败')
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchStores()
  fetchStaffs()
})

// 处理新建员工
const handleCreateStaff = async () => {
  if (!newStaff.value.name || !newStaff.value.storeId || !newStaff.value.position || !newStaff.value.phone || !newStaff.value.joinDate) {
    ElMessage.warning('请填写完整的员工信息')
    return
  }

  try {
    // 将状态转换为数字
    const statusMap = {
      '在职': 1,
      '休假': 2,
      '离职': 3
    }
    
    // 构建员工数据
    const staffData = {
      name: newStaff.value.name,
      storeId: parseInt(newStaff.value.storeId),
      position: newStaff.value.position,
      phone: newStaff.value.phone,
      status: statusMap[newStaff.value.status] || 1,
      joinDate: newStaff.value.joinDate
    }
    
    const response = await request.post('/staff', staffData)
    if (response.code === 200) {
      // 重新获取员工列表
      await fetchStaffs()
      
      dialogVisible.value = false
      newStaff.value = {
        name: '',
        storeId: '',
        position: '',
        phone: '',
        status: '在职',
        joinDate: ''
      }
      ElMessage.success('员工添加成功')
    } else {
      ElMessage.error(response.message || '添加员工失败')
    }
  } catch (error) {
    console.error('添加员工失败:', error)
    ElMessage.error('添加员工失败')
  }
}

// 处理编辑员工
const handleEditStaff = (staff) => {
  currentStaff.value = { ...staff }
  editDialogVisible.value = true
}

// 确认编辑员工
const confirmEdit = async () => {
  try {
    // 将状态转换为数字
    const statusMap = {
      '在职': 1,
      '休假': 2,
      '离职': 0
    }
    
    // 构建员工数据
    const staffData = {
      name: currentStaff.value.name,
      storeId: parseInt(currentStaff.value.storeId),
      position: currentStaff.value.position,
      phone: currentStaff.value.phone,
      status: statusMap[currentStaff.value.status] || 1,
      joinDate: currentStaff.value.joinDate
    }
    
    const response = await request.put(`/staff/${currentStaff.value.staffId}`, staffData)
    if (response.code === 200) {
      // 重新获取员工列表
      await fetchStaffs()
      ElMessage.success('员工信息更新成功')
    } else {
      ElMessage.error(response.message || '更新员工信息失败')
    }
  } catch (error) {
    console.error('更新员工信息失败:', error)
    ElMessage.error('更新员工信息失败')
  }
  
  editDialogVisible.value = false
  currentStaff.value = null
}

// 处理员工状态更新
const handleStatusChange = async (staff, newStatus) => {
  try {
    // 将状态转换为数字
    const statusMap = {
      '在职': 1,
      '休假': 2,
      '离职': 0
    }
    
    // 构建员工数据
    const staffData = {
      name: staff.name,
      storeId: parseInt(staff.storeId),
      position: staff.position,
      phone: staff.phone,
      status: statusMap[newStatus] || 1,
      joinDate: staff.joinDate
    }
    
    const response = await request.put(`/staff/${staff.staffId}`, staffData)
    if (response.code === 200) {
      staff.status = newStatus
      ElMessage.success('员工状态更新成功')
    } else {
      ElMessage.error(response.message || '更新员工状态失败')
      // 恢复原状态
      await fetchStaffs()
    }
  } catch (error) {
    console.error('更新员工状态失败:', error)
    ElMessage.error('更新员工状态失败')
    // 恢复原状态
    await fetchStaffs()
  }
}
// 删除确认对话框
const deleteDialogVisible = ref(false)
const staffToDelete = ref(null)

// 处理删除员工
const handleDeleteStaff = (staff) => {
  staffToDelete.value = staff
  deleteDialogVisible.value = true
}

// 确认删除员工
const confirmDelete = async () => {
  try {
    const response = await request.delete(`/staff/${staffToDelete.value.staffId}`)
    if (response.code === 200) {
      // 重新获取员工列表
      await fetchStaffs()
      ElMessage.success('员工删除成功')
    } else {
      ElMessage.error(response.message || '删除员工失败')
    }
  } catch (error) {
    console.error('删除员工失败:', error)
    ElMessage.error('删除员工失败')
  }
  
  deleteDialogVisible.value = false
  staffToDelete.value = null
}
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">员工管理</h2>
      <el-button type="primary" class="bg-[#FF7E00] hover:bg-[#ff9933]" @click="dialogVisible = true">新增员工</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="flex space-x-4 mb-6">
      <el-input
        v-model="searchQuery"
        placeholder="搜索员工姓名或手机号"
        class="w-64"
      >
        <template #prefix>
          <el-icon><i class="el-icon-search"></i></el-icon>
        </template>
      </el-input>

      <el-select v-model="selectedStore" placeholder="选择门店" class="w-40">
        <el-option label="全部门店" value="all" />
        <el-option
          v-for="store in stores"
          :key="store.storeId"
          :label="store.name"
          :value="store.storeId.toString()"
        />
      </el-select>

      <el-select v-model="statusFilter" placeholder="状态" class="w-32">
        <el-option label="全部" value="all" />
        <el-option label="在职" value="在职" />
        <el-option label="休假" value="休假" />
        <el-option label="离职" value="离职" />
      </el-select>
    </div>

    <!-- 员工列表（按门店分组） -->
    <div v-for="store in stores" :key="store.storeId" class="mb-8">
      <div>
        <h3 class="text-lg font-medium text-gray-700 dark:text-gray-300 mb-4">{{ store.name }}</h3>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
            <thead class="bg-gray-50 dark:bg-gray-800">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">姓名</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">职位</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">手机号</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">状态</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">入职日期</th>
                <th class="px-6 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody class="bg-white dark:bg-gray-700 divide-y divide-gray-200 dark:divide-gray-600">
              <tr v-for="staff in groupedStaffs[store.storeId]" :key="staff.id">
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ staff.name }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ staff.position }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ staff.phone }}</td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="flex items-center">
                    <el-select
                      v-model="staff.status"
                      size="small"
                      @change="(val) => handleStatusChange(staff, val)"
                    >
                      <el-option label="在职" value="在职" />
                      <el-option label="休假" value="休假" />
                      <el-option label="离职" value="离职" />
                    </el-select>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ staff.joinDate }}</td>
                <td class="px-6 py-4 whitespace-nowrap text-center">
                  <el-button type="primary" size="small" @click="handleEditStaff(staff)">编辑</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteStaff(staff)">删除</el-button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新增员工对话框 -->
    <el-dialog v-model="dialogVisible" title="新增员工" width="500px">
      <el-form :model="newStaff" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="newStaff.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="所属门店">
          <el-select v-model="newStaff.storeId" placeholder="请选择门店">
            <el-option
              v-for="store in stores"
              :key="store.storeId"
              :label="store.name"
              :value="store.storeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位">
          <el-select v-model="newStaff.position" placeholder="请选择职位">
            <el-option label="店长" value="店长" />
            <el-option label="快递员" value="快递员" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="newStaff.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker
            v-model="newStaff.joinDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateStaff">确定</el-button>
        </span>
      </template>
    </el-dialog>
    <el-dialog v-model="editDialogVisible" title="编辑员工信息" width="500px">
      <el-form v-if="currentStaff" :model="currentStaff" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="currentStaff.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="所属门店">
          <el-select v-model="currentStaff.storeId" placeholder="请选择门店">
            <el-option
              v-for="store in stores"
              :key="store.storeId"
              :label="store.name"
              :value="store.storeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职位">
          <el-select v-model="currentStaff.position" placeholder="请选择职位">
            <el-option label="店长" value="店长" />
            <el-option label="快递员" value="快递员" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="currentStaff.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="入职日期">
          <el-date-picker
            v-model="currentStaff.joinDate"
            type="date"
            placeholder="选择日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmEdit">确定</el-button>
        </span>
      </template>
    </el-dialog>
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <p class="text-gray-600 dark:text-gray-300">确定要删除该员工吗？此操作不可恢复。</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
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
  @apply dark:bg-gray-800 dark:text-white;
  border-bottom: 1px solid;
  @apply dark:border-gray-700;
}

.el-dialog :deep(.el-dialog__body) {
  @apply dark:bg-gray-800;
}

.el-dialog :deep(.el-dialog__title) {
  @apply dark:text-white text-gray-900;
}

.el-dialog :deep(.el-dialog__footer) {
  @apply dark:bg-gray-800;
  border-top: 1px solid;
  @apply dark:border-gray-700;
}

.el-form-item :deep(.el-form-item__label) {
  @apply dark:text-gray-300;
}

.el-select :deep(.el-input__wrapper) {
  @apply dark:bg-gray-700 dark:border-gray-600;
}

.el-select :deep(.el-select__input) {
  @apply dark:text-gray-200;
}

.el-date-picker :deep(.el-input__wrapper) {
  @apply dark:bg-gray-700 dark:border-gray-600;
}

.el-date-picker :deep(.el-input__inner) {
  @apply dark:text-gray-200;
}
</style>