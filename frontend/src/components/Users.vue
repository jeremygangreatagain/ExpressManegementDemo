<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import request from '../utils/request'

// 用户数据
const users = ref([])
const total = ref(0)
const loading = ref(false)

// 搜索关键词
const searchQuery = ref('')

// 角色筛选
const roleFilter = ref('all')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 新建用户对话框
const dialogVisible = ref(false)
const newUser = ref({
  username: '',
  password: '',
  role: '工作人员',
  status: '启用'
})

// 删除用户相关
const deleteDialogVisible = ref(false)
const userToDelete = ref(null)

// 获取用户列表数据
const fetchUsers = async () => {
  loading.value = true
  try {
    // 检查用户角色是否为管理员
    const userRole = localStorage.getItem('userRole')
    if (userRole !== 'admin') {
      ElMessage.error('您没有权限访问用户管理页面')
      return
    }
    
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    // 添加搜索条件
    if (searchQuery.value) {
      params.username = searchQuery.value
    }
    
    // 添加角色筛选
    if (roleFilter.value !== 'all') {
      // 将中文角色名转换为英文，与后端对应
      const roleMap = {
        '管理员': 'admin',
        '工作人员': 'staff',
        '普通用户': 'customer'
      }
      params.role = roleMap[roleFilter.value]
    }
    
    console.log('发送用户列表请求，参数:', params)
    const res = await request.get('/users', { params })
    console.log('用户列表响应:', res)
    
    if (res.code === 200) {
      // 将后端返回的数据转换为前端需要的格式
      users.value = res.data.list.map(user => ({
        userId: user.userId,
        username: user.username,
        role: translateRole(user.role), // 将英文角色转为中文显示
        status: user.status === 1 ? '启用' : '禁用',
        lastLogin: user.lastLogin ? formatDateTime(user.lastLogin) : '-'
      }))
      total.value = res.data.total
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    if (error.response && error.response.status === 403) {
      ElMessage.error('您没有权限访问用户管理页面')
    } else {
      ElMessage.error('获取用户列表失败: ' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

// 将英文角色转为中文
const translateRole = (role) => {
  const roleMap = {
    'admin': '管理员',
    'staff': '工作人员',
    'customer': '普通用户'
  }
  return roleMap[role] || role
}

// 格式化日期时间
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 处理用户状态更新
const handleStatusChange = async (user, newStatus) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '更新中...'
    })
    
    const status = newStatus === '启用' ? 1 : 0
    const res = await request.put(`/users/${user.userId}/status`, { status })
    
    loading.close()
    
    if (res.code === 200) {
      ElMessage.success('用户状态更新成功')
      // 更新本地状态
      user.status = newStatus
    } else {
      ElMessage.error(res.message || '更新失败')
      // 恢复原状态
      user.status = user.status === '启用' ? '禁用' : '启用'
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
    // 恢复原状态
    user.status = user.status === '启用' ? '禁用' : '启用'
  }
}

// 处理新建用户
const handleCreateUser = async () => {
  if (!newUser.value.username || !newUser.value.password) {
    ElMessage.warning('请填写完整的用户信息')
    return
  }
  
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '创建中...'
    })
    
    // 将中文角色转为英文
    const roleMap = {
      '管理员': 'admin',
      '工作人员': 'staff',
      '普通用户': 'customer'
    }
    
    const userData = {
      username: newUser.value.username,
      password: newUser.value.password,
      role: roleMap[newUser.value.role],
      status: newUser.value.status === '启用' ? 1 : 0
    }
    
    const res = await request.post('/users', userData)
    
    loading.close()
    
    if (res.code === 200) {
      ElMessage.success('用户创建成功')
      dialogVisible.value = false
      newUser.value = {
        username: '',
        password: '',
        role: '工作人员',
        status: '启用'
      }
      // 重新加载用户列表
      fetchUsers()
    } else {
      ElMessage.error(res.message || '创建失败')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 处理删除用户
const handleDeleteUser = (user) => {
  userToDelete.value = user
  deleteDialogVisible.value = true
}

// 确认删除用户
const confirmDelete = async () => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '删除中...'
    })
    
    const res = await request.delete(`/users/${userToDelete.value.userId}`)
    
    loading.close()
    
    if (res.code === 200) {
      ElMessage.success('用户删除成功')
      // 重新加载用户列表
      fetchUsers()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
    
    deleteDialogVisible.value = false
    userToDelete.value = null
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

// 监听分页、搜索和筛选变化
const handlePageChange = () => {
  fetchUsers()
}

// 监听搜索和筛选变化的防抖处理
let searchTimer = null
const handleSearchChange = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    currentPage.value = 1 // 重置到第一页
    fetchUsers()
  }, 300)
}

// 初始化加载数据
onMounted(() => {
  fetchUsers()
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">用户管理</h2>
      <el-button type="primary" class="bg-[#FF7E00] hover:bg-[#ff9933]" @click="dialogVisible = true">新建用户</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="flex space-x-4 mb-6">
      <el-input
        v-model="searchQuery"
        placeholder="搜索用户名"
        class="w-64"
        @input="handleSearchChange"
      >
        <template #prefix>
          <el-icon><i class="el-icon-search"></i></el-icon>
        </template>
      </el-input>

      <el-select v-model="roleFilter" placeholder="角色" class="w-32" @change="handleSearchChange">
        <el-option label="全部" value="all" />
        <el-option label="管理员" value="管理员" />
        <el-option label="工作人员" value="工作人员" />
        <el-option label="普通用户" value="普通用户" />
      </el-select>
    </div>

    <!-- 用户列表 -->
    <div class="overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
        <thead class="bg-gray-50 dark:bg-gray-800">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">用户名</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">角色</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">状态</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">最后登录</th>
            <th class="px-6 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">操作</th>
          </tr>
        </thead>
        <tbody class="bg-white dark:bg-gray-700 divide-y divide-gray-200 dark:divide-gray-600">
          <tr v-for="user in users" :key="user.userId">
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ user.username }}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ user.role }}</td>
            <td class="px-6 py-4 whitespace-nowrap">
              <div class="flex items-center">
                <el-switch
                  v-model="user.status"
                  :active-value="'启用'"
                  :inactive-value="'禁用'"
                  @change="(val) => handleStatusChange(user, val)"
                />
                <span :class="{
                  'ml-2 px-2 py-1 text-xs font-medium rounded-full': true,
                  'bg-green-100 text-green-800 dark:bg-green-800 dark:text-green-100': user.status === '启用',
                  'bg-red-100 text-red-800 dark:bg-red-800 dark:text-red-100': user.status === '禁用'
                }">
                  {{ user.status }}
                </span>
              </div>
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{{ user.lastLogin }}</td>
            <td class="px-6 py-4 whitespace-nowrap flex justify-center">
              <el-button type="danger" size="small" class="hover:bg-red-600" @click="handleDeleteUser(user)">
                删除
              </el-button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div class="mt-6 flex justify-center">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        class="!text-gray-600 dark:!text-gray-300"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新建用户对话框 -->
    <el-dialog v-model="dialogVisible" title="新建用户" width="500px">
      <el-form :model="newUser" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="newUser.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="newUser.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="newUser.role" placeholder="请选择角色">
            <el-option label="管理员" value="管理员" />
            <el-option label="工作人员" value="工作人员" />
            <el-option label="普通用户" value="普通用户" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleCreateUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px">
      <p class="text-base text-gray-700 dark:text-gray-200">确定要删除用户 "<span class="font-medium text-gray-900 dark:text-white">{{ userToDelete?.username }}</span>" 吗？<span class="text-gray-700 dark:text-gray-200">此操作不可恢复。</span></p>
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