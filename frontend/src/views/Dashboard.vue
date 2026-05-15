<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>首页大盘</h2>
    </div>

    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon normal">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ stats.total || 0 }}</h3>
                <p>设备总数</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon success">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ stats.normal || 0 }}</h3>
                <p>正常运行</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon warning">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ stats.warning || 0 }}</h3>
                <p>需要维护</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stats-card">
            <div class="stats-content">
              <div class="stats-icon danger">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ stats.error || 0 }}</h3>
                <p>设备故障</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>设备状态分布</span>
              </div>
            </template>
            <div ref="pieChartRef" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>近7天告警趋势</span>
              </div>
            </template>
            <div ref="lineChartRef" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="recent-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近告警</span>
              </div>
            </template>
            <div class="alert-list">
              <el-empty v-if="!recentAlerts.length" description="暂无告警" />
              <div class="alert-item" v-for="alert in recentAlerts" :key="alert.id">
                <div class="alert-info">
                  <span class="alert-title">{{ alert.deviceName }}</span>
                  <span class="alert-time">{{ alert.faultDesc }}</span>
                </div>
                <el-tag :type="getAlertType(alert.status)" size="small">
                  {{ getStatusText(alert.status) }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>待处理工单</span>
              </div>
            </template>
            <div class="workorder-list">
              <el-empty v-if="!pendingOrders.length" description="暂无待处理工单" />
              <div class="workorder-item" v-for="order in pendingOrders" :key="order.id">
                <div class="workorder-info">
                  <span class="workorder-title">{{ order.orderNo }}</span>
                  <span class="workorder-time">{{ order.faultDesc }}</span>
                </div>
                <el-tag :type="getOrderTagType(order.status)" size="small">
                  {{ getOrderStatusText(order.status) }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Monitor, CircleCheck, Warning, CircleClose } from '@element-plus/icons-vue'
import { getStats, getAlertTrend, getRecentAlerts, getPendingOrders } from '@/api/dashboard'

const pieChartRef = ref(null)
const lineChartRef = ref(null)

const stats = reactive({
  total: 0,
  normal: 0,
  warning: 0,
  error: 0,
  scrap: 0
})

const recentAlerts = ref([])
const pendingOrders = ref([])

const getAlertType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '正常', 2: '异常预警', 3: '故障待修', 4: '已报废' }
  return map[status] || '未知'
}

const getOrderTagType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'success', 4: 'success' }
  return map[status] || 'info'
}

const getOrderStatusText = (status) => {
  const map = { 0: '待派单', 1: '待接单', 2: '维修中', 3: '待验收', 4: '已完成' }
  return map[status] || '未知'
}

const initPieChart = (data) => {
  if (!pieChartRef.value) return
  const chart = echarts.init(pieChartRef.value)
  const chartData = [
    { value: data.normal || 0, name: '正常运行' },
    { value: data.warning || 0, name: '需要维护' },
    { value: data.error || 0, name: '设备故障' },
    { value: data.scrap || 0, name: '已报废' }
  ]
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    color: ['#67C23A', '#E6A23C', '#F56C6C', '#909399'],
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: { show: false },
      data: chartData
    }]
  })
}

const initLineChart = (data) => {
  if (!lineChartRef.value) return
  const chart = echarts.init(lineChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: data.dates || [],
      boundaryGap: false
    },
    yAxis: { type: 'value' },
    series: [{
      data: data.counts || [],
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const loadData = async () => {
  try {
    const statsData = await getStats()
    const transformedStats = { total: 0, normal: 0, warning: 0, error: 0, scrap: 0 }
    if (Array.isArray(statsData)) {
      statsData.forEach(item => {
        const status = item.status
        const count = item.count || 0
        transformedStats.total += count
        if (status === 1) transformedStats.normal = count
        if (status === 2) transformedStats.warning = count
        if (status === 3) transformedStats.error = count
        if (status === 4) transformedStats.scrap = count
      })
    }
    stats.total = transformedStats.total
    stats.normal = transformedStats.normal
    stats.warning = transformedStats.warning
    stats.error = transformedStats.error
    stats.scrap = transformedStats.scrap

    await nextTick()
    initPieChart(transformedStats)

    const trendData = await getAlertTrend()
    const transformedTrend = { dates: [], counts: [] }
    if (Array.isArray(trendData)) {
      transformedTrend.dates = trendData.map(item => item.date)
      transformedTrend.counts = trendData.map(item => item.count)
    }
    await nextTick()
    initLineChart(transformedTrend)

    recentAlerts.value = await getRecentAlerts()
    pendingOrders.value = await getPendingOrders()
  } catch (error) {
    console.error('加载Dashboard数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.stats-card:hover {
  transform: translateY(-2px);
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stats-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stats-icon.normal {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.success {
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
}

.stats-icon.warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.danger {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-info h3 {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

.stats-info p {
  margin: 5px 0 0 0;
  color: #7f8c8d;
  font-size: 14px;
}

.charts-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.alert-list, .workorder-list {
  max-height: 300px;
  overflow-y: auto;
}

.alert-item, .workorder-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-item:last-child, .workorder-item:last-child {
  border-bottom: none;
}

.alert-info, .workorder-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.alert-title, .workorder-title {
  font-weight: 500;
  color: #2c3e50;
}

.alert-time, .workorder-time {
  font-size: 12px;
  color: #7f8c8d;
}
</style>