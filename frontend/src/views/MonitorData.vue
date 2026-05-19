<template>
  <div class="monitor-data">
    <div class="page-header">
      <h2>监测数据</h2>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="全站仪数据" name="totalStation"></el-tab-pane>
      <el-tab-pane label="伺服轴力数据" name="axialForce"></el-tab-pane>
      <el-tab-pane label="钢支撑温度" name="steelTemperature"></el-tab-pane>
    </el-tabs>

    <div class="filter-bar">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="currentSensor" placeholder="传感器" @change="loadData">
            <el-option v-for="s in sensors" :key="s" :label="s" :value="s" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="onTimeChange"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="pagination.pageSize" placeholder="每页条数" @change="loadData">
            <el-option label="50条" :value="50" />
            <el-option label="100条" :value="100" />
            <el-option label="200条" :value="200" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-button type="warning" @click="handlePredict" :loading="predicting" :disabled="!currentSensor">
            🔮 智能预测
          </el-button>
        </el-col>
      </el-row>
    </div>

    <div class="chart-container">
      <div ref="chartRef" style="height: 350px;"></div>
    </div>

    <div class="table-container">
      <el-table :data="tableData" border stripe v-loading="loading" max-height="400">
        <el-table-column prop="collectTime" label="采集时间" width="180" />
        <el-table-column v-if="activeTab === 'totalStation'" prop="deltaX" label="ΔX(mm)" width="100" />
        <el-table-column v-if="activeTab === 'totalStation'" prop="deltaY" label="ΔY(mm)" width="100" />
        <el-table-column v-if="activeTab === 'totalStation'" prop="deltaH" label="ΔH(mm)" width="100" />
        <el-table-column v-if="activeTab === 'totalStation'" prop="totalX" label="∑X(mm)" width="100" />
        <el-table-column v-if="activeTab === 'totalStation'" prop="totalY" label="∑Y(mm)" width="100" />
        <el-table-column v-if="activeTab === 'totalStation'" prop="totalH" label="∑H(mm)" width="100" />
        <el-table-column v-if="activeTab === 'axialForce'" label="轴力(kN)" width="120">
          <template #default="scope">
            {{ safeGetField(scope.row, 'wForce') }}
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'axialForce'" label="行程(mm)" width="120">
          <template #default="scope">
            {{ safeGetField(scope.row, 'fPosition') }}
          </template>
        </el-table-column>
        <el-table-column v-if="activeTab === 'steelTemperature'" prop="temperature" label="温度(℃)" width="120" />
        <el-table-column v-if="activeTab === 'steelTemperature'" prop="measureVal" label="原始测量值" width="120" />
        <el-table-column prop="temperature" label="温度(℃)" width="100" v-if="activeTab !== 'steelTemperature'" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog
      v-model="showPredict"
      title="🔮 AI 智能预测"
      width="700px"
      :close-on-click-modal="false"
    >
      <div class="predict-info">
        <el-tag>{{ currentSensor }}</el-tag>
        <span style="margin: 0 8px; color: #909399">{{ predictTimeRange }}</span>
      </div>
      <div v-if="predicting" class="predict-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>AI 正在分析数据趋势，请稍候...</span>
      </div>
      <div v-else-if="predictResult" class="predict-result"
           v-html="formatMarkdown(predictResult)" />
      <template #footer>
        <el-button @click="showPredict = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getTotalStation,
  getAxialForce,
  getSteelTemperature,
  getSensors
} from '@/api/monitor'
import { predictAuto } from '@/api/ai'

const chartRef = ref(null)
let chartInstance = null
const loading = ref(false)
const activeTab = ref('totalStation')
const currentSensor = ref('')
const timeRange = ref(null)
const defaultStartTime = '2025-01-01 00:00:00'
const defaultEndTime = '2026-12-31 23:59:59'

const sensors = ref([])
const allSensors = ref([])
const tableData = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 100,
  total: 0
})

const showPredict = ref(false)
const predicting = ref(false)
const predictResult = ref('')

const predictTimeRange = computed(() => {
  if (timeRange.value && timeRange.value.length === 2) {
    return `${timeRange.value[0]} ~ ${timeRange.value[1]}`
  }
  return `${defaultStartTime} ~ ${defaultEndTime}`
})

const getApiMethod = () => {
  const apiMap = {
    totalStation: getTotalStation,
    axialForce: getAxialForce,
    steelTemperature: getSteelTemperature
  }
  return apiMap[activeTab.value]
}

const getYField = () => {
  const fieldMap = {
    totalStation: 'totalX',
    axialForce: 'wForce',
    steelTemperature: 'temperature'
  }
  return fieldMap[activeTab.value]
}

const safeGetField = (item, field) => {
  return item[field] !== undefined ? item[field] : item[field.toLowerCase()]
}

const getYLabel = () => {
  const labelMap = {
    totalStation: '累计X位移 ∑X(mm)',
    axialForce: '轴力 WForce(kN)',
    steelTemperature: '温度(℃)'
  }
  return labelMap[activeTab.value]
}

const filterSensorsByType = (sensorList, type) => {
  if (!sensorList || !Array.isArray(sensorList)) return []
  return sensorList.filter(s => {
    if (type === 'totalStation') {
      return s.startsWith('FRHY')
    } else if (type === 'axialForce') {
      return s.startsWith('SP') || s.startsWith('4P') || s.startsWith('HSD')
    } else if (type === 'steelTemperature') {
      return s.startsWith('650')
    }
    return true
  })
}

const initChart = (data) => {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  const yField = getYField()
  const times = data.map(item => item.collectTime).reverse()
  const values = data.map(item => safeGetField(item, yField)).reverse()

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 20, bottom: 40 },
    xAxis: {
      type: 'category',
      data: times,
      boundaryGap: false,
      axisLabel: { rotate: 45, fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      name: getYLabel()
    },
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { type: 'slider', start: 0, end: 100, height: 20, bottom: 0 }
    ],
    series: [{
      data: values,
      type: 'line',
      smooth: true,
      showSymbol: false,
      areaStyle: { opacity: 0.1 },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const loadData = async () => {
  if (!currentSensor.value) return
  loading.value = true
  try {
    const params = {
      sensorCode: currentSensor.value,
      startTime: (timeRange.value && timeRange.value.length === 2) ? timeRange.value[0] : defaultStartTime,
      endTime: (timeRange.value && timeRange.value.length === 2) ? timeRange.value[1] : defaultEndTime,
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    }
    const apiMethod = getApiMethod()
    const data = await apiMethod(params)

    if (data && data.list) {
      tableData.value = data.list
      pagination.total = data.total || 0
    } else if (Array.isArray(data)) {
      tableData.value = data
      pagination.total = data.length
    }

    if (tableData.value.length > 0) {
      await nextTick()
      initChart(tableData.value)
    }
  } catch (error) {
    console.error('加载监测数据失败:', error)
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  currentSensor.value = ''
  tableData.value = []
  pagination.currentPage = 1
  sensors.value = filterSensorsByType(allSensors.value, activeTab.value)
  if (sensors.value.length > 0) {
    currentSensor.value = sensors.value[0]
    loadData()
  }
}

const onTimeChange = () => {
  pagination.currentPage = 1
  loadData()
}

const loadSensorList = async () => {
  try {
    const data = await getSensors()
    allSensors.value = data || []
    sensors.value = filterSensorsByType(allSensors.value, activeTab.value)
    if (sensors.value.length > 0) {
      currentSensor.value = sensors.value[0]
      loadData()
    }
  } catch (error) {
    console.error('加载传感器列表失败:', error)
  }
}

const handlePredict = async () => {
  if (!currentSensor.value) return
  showPredict.value = true
  predicting.value = true
  predictResult.value = ''
  try {
    const startTime = (timeRange.value && timeRange.value.length === 2) ? timeRange.value[0] : defaultStartTime
    const endTime = (timeRange.value && timeRange.value.length === 2) ? timeRange.value[1] : defaultEndTime
    const result = await predictAuto({
      sensorCode: currentSensor.value,
      startTime,
      endTime
    })
    predictResult.value = result || 'AI 未返回预测结果'
  } catch (e) {
    predictResult.value = '预测请求失败，请检查后端服务和 API Key 配置。'
  } finally {
    predicting.value = false
  }
}

const formatMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/### (.*)/g, '<h4>$1</h4>')
    .replace(/## (.*)/g, '<h3>$1</h3>')
    .replace(/# (.*)/g, '<h2>$1</h2>')
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
    .replace(/\*(.*?)\*/g, '<i>$1</i>')
    .replace(/- (.*)/g, '<li>$1</li>')
    .replace(/\n\n/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
}

onMounted(() => {
  loadSensorList()
})
</script>

<style scoped>
.monitor-data {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.page-header h2 {
  margin: 0 0 16px 0;
  color: #2c3e50;
}

.filter-bar {
  margin-bottom: 16px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.chart-container {
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.table-container {
  margin-top: 16px;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

.predict-info {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.predict-loading {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

.predict-loading .el-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.predict-result {
  max-height: 500px;
  overflow-y: auto;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
  line-height: 1.8;
  font-size: 14px;
}

.predict-result h3 {
  margin: 16px 0 8px;
  color: #303133;
}

.predict-result h4 {
  margin: 12px 0 6px;
  color: #409EFF;
}

.predict-result li {
  margin: 4px 0 4px 20px;
}
</style>