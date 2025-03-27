<script setup>


import { ref, computed } from 'vue'

// 静态工作日志数据
const logs = ref([
  {
    id: 1,
    orderId: '1741870252988',
    type: '订单创建',
    operator: 'testUser1',
    time: '2025-03-13T20:50:53',
    details: '创建了新订单，收件人: testUser2，物品类型: 玻璃'
  },
  {
    id: 2,
    orderId: '1741870252988',
    type: '状态变更',
    operator: 'system',
    time: '2025-03-13T20:51:00',
    details: '订单状态从「待处理」变更为「配送中」'
  },
  {
    id: 3,
    orderId: '1741106393032',
    type: '订单创建',
    operator: 'user2',
    time: '2025-03-05T00:39:53',
    details: '创建了新订单，收件人: user3，物品类型: 电器'
  },
  {
    id: 4,
    orderId: '1741106393032',
    type: '状态变更',
    operator: 'system',
    time: '2025-03-05T00:40:00',
    details: '订单状态从「待处理」变更为「配送中」'
  },
  {
    id: 5,
    orderId: '1741093892059',
    type: '订单创建',
    operator: 'user1',
    time: '2025-03-04T21:11:32',
    details: '创建了新订单，收件人: user2，物品类型: 电器'
  },
  {
    id: 6,
    orderId: '1741093892059',
    type: '状态变更',
    operator: 'system',
    time: '2025-03-04T21:12:00',
    details: '订单状态从「待处理」变更为「待处理」'
  }
])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 日期范围
const dateRange = ref([])

// 搜索关键词
const searchQuery = ref('')

// 日志类型筛选
const typeFilter = ref('all')

// 日志详情对话框
const detailsDialogVisible = ref(false)
const currentLog = ref(null)

// 查看日志详情
const viewLogDetails = (log) => {
  currentLog.value = log
  detailsDialogVisible.value = true
}

// 导出CSV
const exportCSV = () => {
  if (filteredLogs.value.length === 0) {
    ElMessage.warning('没有可导出的数据')
    return
  }

  // 构建CSV内容
  const headers = ['订单编号', '操作类型', '操作人', '操作时间', '详情']
  const rows = filteredLogs.value.map(log => [
    log.orderId,
    log.type,
    log.operator,
    log.time,
    log.details
  ])

  // 添加UTF-8 BOM头
  const BOM = '\uFEFF'
  const csvContent = BOM + [
    headers.map(h => `"${h}"`).join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ].join('\n')

  // 创建并下载文件
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  const now = new Date()
  const timestamp = now.toISOString().replace(/[:.]/g, '-').slice(0, 19)
  link.download = `工作日志_${timestamp}.csv`
  link.click()
}

// 过滤后的日志列表
const filteredLogs = computed(() => {
  return logs.value.filter(log => {
    const matchesSearch = log.orderId.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         log.operator.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesType = typeFilter.value === 'all' || log.type === typeFilter.value
    const matchesDate = !dateRange.value || dateRange.value.length === 0 ||
                       (new Date(log.time) >= new Date(dateRange.value[0]) &&
                        new Date(log.time) <= new Date(dateRange.value[1]))
    return matchesSearch && matchesType && matchesDate
  })
})

// 分页后的日志列表
const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredLogs.value.slice(start, end)
})
</script>

<template>
  <div class="space-y-6">
    <div class="flex justify-between items-center">
      <h2 class="text-2xl font-semibold text-gray-800 dark:text-white">工作日志</h2>
      <el-button type="primary" class="bg-[#FF7E00] hover:bg-[#ff9933]" @click="exportCSV">导出日志</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="flex space-x-4 mb-6">
      <el-input
        v-model="searchQuery"
        placeholder="搜索订单编号或操作人"
        class="w-64"
      >
        <template #prefix>
          <el-icon><i class="el-icon-search"></i></el-icon>
        </template>
      </el-input>

      <el-select v-model="typeFilter" placeholder="日志类型" class="w-32">
        <el-option label="全部" value="all" />
        <el-option label="状态变更" value="状态变更" />
        <el-option label="订单创建" value="订单创建" />
      </el-select>

      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        class="w-72"
      />
    </div>

    <!-- 时间轴 -->
    <div class="relative pl-8 space-y-8">
      <!-- 垂直时间线 -->
      <div class="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200 dark:bg-gray-700"></div>

      <!-- 日志条目 -->
      <div
        v-for="log in paginatedLogs"
        :key="log.id"
        class="relative bg-white dark:bg-gray-900 p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer"
        @click="viewLogDetails(log)"
      >
        <!-- 时间点 -->
        <div class="absolute -left-[27px] w-[14px] h-[14px] rounded-full bg-[#FF7E00] border-4 border-white dark:border-gray-900"></div>

        <!-- 日志内容 -->
        <div class="grid grid-cols-[1fr,auto] gap-4">
          <div class="space-y-2">
            <div class="flex items-center space-x-2">
              <span class="text-sm font-medium text-gray-500">订单编号：{{ log.orderId }}</span>
              <el-tag size="small" :type="log.type === '订单创建' ? 'success' : 'warning'">{{ log.type }}</el-tag>
            </div>
            <p class="text-gray-700 dark:text-gray-300">{{ log.details }}</p>
            <div class="text-sm text-gray-500">
              操作人：{{ log.operator }}
            </div>
          </div>
          <div class="text-sm text-gray-500">
            {{ new Date(log.time).toLocaleString() }}
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="flex justify-center mt-6">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredLogs.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
      />
    </div>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="日志详情"
      width="500px"
    >
      <div v-if="currentLog" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <div class="text-gray-500">订单编号</div>
            <div>{{ currentLog.orderId }}</div>
          </div>
          <div>
            <div class="text-gray-500">操作类型</div>
            <div>
              <el-tag :type="currentLog.type === '订单创建' ? 'success' : 'warning'">{{ currentLog.type }}</el-tag>
            </div>
          </div>
          <div>
            <div class="text-gray-500">操作人</div>
            <div>{{ currentLog.operator }}</div>
          </div>
          <div>
            <div class="text-gray-500">操作时间</div>
            <div>{{ new Date(currentLog.time).toLocaleString() }}</div>
          </div>
        </div>
        <div>
          <div class="text-gray-500">详情</div>
          <div class="mt-1">{{ currentLog.details }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.el-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.el-button {
  border-radius: 8px;
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