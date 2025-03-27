<template>
  <div class="space-y-6">
    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
      <div class="bg-white dark:bg-gray-700 rounded-lg shadow p-6">
        <h3 class="text-gray-900 dark:text-white text-sm font-medium">今日订单</h3>
        <p class="text-2xl font-semibold text-[#FF7E00] dark:text-[#ff9933] mt-2">{{ orderData.todayOrders }}</p>
      </div>
      <div class="bg-white dark:bg-gray-700 rounded-lg shadow p-6">
        <h3 class="text-gray-900 dark:text-white text-sm font-medium">待处理</h3>
        <p class="text-2xl font-semibold text-[#FF7E00] dark:text-[#ff9933] mt-2">{{ orderData.pendingOrders }}</p>
      </div>
      <div class="bg-white dark:bg-gray-700 rounded-lg shadow p-6">
        <h3 class="text-gray-900 dark:text-white text-sm font-medium">配送中</h3>
        <p class="text-2xl font-semibold text-[#FF7E00] dark:text-[#ff9933] mt-2">{{ orderData.inDeliveryOrders }}</p>
      </div>
      <div class="bg-white dark:bg-gray-700 rounded-lg shadow p-6">
        <h3 class="text-gray-900 dark:text-white text-sm font-medium">已完成</h3>
        <p class="text-2xl font-semibold text-[#FF7E00] dark:text-[#ff9933] mt-2">{{ orderData.completedOrders }}</p>
      </div>
    </div>

    <!-- 图表展示区域 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white dark:bg-gray-700 rounded-lg shadow p-6">
        <div id="status-chart" style="width: 100%; height: 400px;"></div>
      </div>
      <div class="bg-white dark:bg-gray-700 rounded-lg shadow p-6">
        <div id="trend-chart" style="width: 100%; height: 400px;"></div>
      </div>
    </div>

    <!-- 最近订单列表 -->
    <div class="bg-white dark:bg-gray-700 rounded-lg shadow">
      <div class="p-6">
        <h3 class="text-lg font-medium text-gray-900 dark:text-white">最近订单</h3>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-600">
          <thead class="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">订单号</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">客户</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">状态</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider">时间</th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-700 divide-y divide-gray-200 dark:divide-gray-600">
            <tr v-for="order in recentOrders" :key="order.id">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ order.id }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{{ order.customer }}</td>
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
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'

// 订单数据
const orderData = ref({
  todayOrders: 0,
  pendingOrders: 0,
  inDeliveryOrders: 0,
  completedOrders: 0
})

// 最近订单列表数据
const recentOrders = ref([])

// 图表实例引用
let statusChart = null
let trendChart = null

// 获取订单统计数据
const fetchOrderStats = async () => {
  try {
    const response = await request.get('/orders/stats')
    if (response.code === 200) {
      // 处理后端返回的数据
      orderData.value = {
        todayOrders: response.data.todayOrders || 0,
        pendingOrders: response.data.status0Count || 0,
        inDeliveryOrders: response.data.status1Count || 0,
        completedOrders: response.data.status2Count || 0,
        // 添加趋势数据，如果后端没有提供，则使用空数组
        trend: response.data.trend || [0, 0, 0, 0, 0, 0, 0]
      }
      updateCharts()
    }
  } catch (error) {
    console.error('获取订单统计数据失败:', error)
  }
}

// 获取最近订单列表
const fetchRecentOrders = async () => {
  try {
    const response = await request.get('/orders/recent')
    if (response.code === 200) {
      // 处理订单状态显示
      recentOrders.value = response.data.map(order => {
        // 解析寄件人和收件人信息
        const senderInfo = JSON.parse(order.senderInfo || '{}')
        const receiverInfo = JSON.parse(order.receiverInfo || '{}')
        
        // 将数字状态转换为文本状态
        let statusText = '';
        switch(order.status) {
          case 0: statusText = '待处理'; break;
          case 1: statusText = '配送中'; break;
          case 2: statusText = '已完成'; break;
          default: statusText = '未知状态';
        }
        
        return {
          id: order.orderId || order.id || '', // 优先使用orderId，如果不存在则使用id
          status: statusText,
          customer: `${senderInfo.name || '未知'}(${senderInfo.phone || '无'}) → ${receiverInfo.name || '未知'}(${receiverInfo.phone || '无'})`,
          time: order.createdAt || new Date().toLocaleString()
        };
      })
    }
  } catch (error) {
    console.error('获取最近订单列表失败:', error)
  }
}

// 更新图表配置
const updateChartTheme = () => {
  const isDark = document.documentElement.classList.contains('dark')
  const textColor = isDark ? '#E5E7EB' : '#1F2937'

  if (statusChart) {
    statusChart.setOption({
      title: {
        textStyle: {
          color: textColor
        }
      },
      legend: {
        textStyle: {
          color: textColor
        }
      }
    })
  }

  if (trendChart) {
    trendChart.setOption({
      title: {
        textStyle: {
          color: textColor
        }
      },
      xAxis: {
        axisLabel: {
          color: textColor
        }
      },
      yAxis: {
        axisLabel: {
          color: textColor
        }
      }
    })
  }
}

// 初始化图表
onMounted(() => {
  initCharts()
  fetchOrderStats()
  fetchRecentOrders()

  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    statusChart?.resize()
    trendChart?.resize()
  })

  // 监听主题变化
  const observer = new MutationObserver(() => {
    updateChartTheme()
  })

  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class']
  })
})

// 组件卸载时清理图表实例
onUnmounted(() => {
  if (statusChart) {
    statusChart.dispose()
    statusChart = null
  }
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})

// 更新图表数据
const updateCharts = () => {
  if (statusChart) {
    statusChart.setOption({
      series: [{
        data: [
          { value: orderData.value.pendingOrders, name: '待处理' },
          { value: orderData.value.inDeliveryOrders, name: '配送中' },
          { value: orderData.value.completedOrders, name: '已完成' }
        ]
      }]
    })
  }

  if (trendChart) {
    // 这里应该使用后端返回的趋势数据
    trendChart.setOption({
      series: [{
        data: orderData.value.trend || []
      }]
    })
  }
}

// 提取图表初始化逻辑到单独的函数
const initCharts = () => {
  // 订单状态分布饼图
  statusChart = echarts.init(document.getElementById('status-chart'))
  statusChart.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '订单状态分布',
      left: 'center',
      textStyle: {
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: [],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  })

  // 订单趋势折线图
  trendChart = echarts.init(document.getElementById('trend-chart'))
  trendChart.setOption({
    backgroundColor: 'transparent',
    title: {
      text: '近7天订单趋势',
      left: 'center',
      textStyle: {
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        data: [],
        itemStyle: {
          color: '#FF7E00'
        },
        lineStyle: {
          color: '#FF7E00'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0,
              color: 'rgba(255, 126, 0, 0.3)'
            }, {
              offset: 1,
              color: 'rgba(255, 126, 0, 0)'
            }]
          }
        }
      }
    ]
  })
}
</script>