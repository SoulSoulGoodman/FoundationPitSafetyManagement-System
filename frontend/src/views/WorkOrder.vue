<template>
  <div class="workorder">
    <div class="page-header">
      <h2>工单调度</h2>
      <el-button type="primary" @click="handleCreateWorkOrder">
        <el-icon><Plus /></el-icon>
        创建工单
      </el-button>
    </div>

    <div class="filter-bar">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="工单状态" clearable>
            <el-option label="待派单" value="pending" />
            <el-option label="已派单" value="assigned" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
            <el-option label="已验收" value="accepted" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.priority" placeholder="优先级" clearable>
            <el-option label="紧急" value="urgent" />
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="table-container">
      <el-table
        :data="tableData"
        border
        stripe
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="workOrderId" label="工单编号" width="120" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="faultType" label="故障类型" width="120" />
        <el-table-column prop="reporter" label="报修人" width="100" />
        <el-table-column prop="reportTime" label="报修时间" width="150" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="scope">
            <el-tag
              :type="getPriorityType(scope.row.priority)"
              size="small"
            >
              {{ getPriorityText(scope.row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assignee" label="维修人员" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending'"
              type="success"
              size="small"
              @click="handleAssign(scope.row)"
            >
              派单
            </el-button>
            <el-button
              v-if="scope.row.status === 'completed'"
              type="warning"
              size="small"
              @click="handleAccept(scope.row)"
            >
              验收
            </el-button>
            <el-button
              v-if="scope.row.status === 'pending' || scope.row.status === 'assigned'"
              type="danger"
              size="small"
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 工单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="500px">
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentOrder.workOrderId }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ currentOrder.deviceName }}</el-descriptions-item>
        <el-descriptions-item label="故障类型">{{ currentOrder.faultType }}</el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(currentOrder.priority)" size="small">{{ getPriorityText(currentOrder.priority) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修人">{{ currentOrder.reporter }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ currentOrder.reportTime }}</el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ currentOrder.assignee || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)" size="small">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 创建工单弹窗 -->
    <el-dialog v-model="createVisible" title="创建工单" width="500px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="createForm.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="createForm.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="数据异常" value="数据异常" />
            <el-option label="设备离线" value="设备离线" />
            <el-option label="需要校准" value="需要校准" />
            <el-option label="电源故障" value="电源故障" />
            <el-option label="传感器异常" value="传感器异常" />
            <el-option label="机械损坏" value="机械损坏" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="createForm.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="紧急" value="urgent" />
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="报修人" prop="reporter">
          <el-input v-model="createForm.reporter" placeholder="请输入报修人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const detailVisible = ref(false)
const createVisible = ref(false)
const currentOrder = ref(null)
const createFormRef = ref()

const searchForm = reactive({
  status: '',
  priority: ''
})

const createForm = reactive({
  deviceName: '',
  faultType: '',
  priority: 'medium',
  reporter: ''
})

const createRules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  reporter: [{ required: true, message: '请输入报修人', trigger: 'blur' }]
}

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 假数据
const mockData = [
  {
    workOrderId: 'WO001',
    deviceName: '测斜仪-01号',
    faultType: '数据异常',
    reporter: '张工',
    reportTime: '2024-05-06 09:30',
    priority: 'urgent',
    status: 'pending',
    assignee: ''
  },
  {
    workOrderId: 'WO002',
    deviceName: '水位计-03号',
    faultType: '设备离线',
    reporter: '李工',
    reportTime: '2024-05-06 08:15',
    priority: 'high',
    status: 'assigned',
    assignee: '王师傅'
  },
  {
    workOrderId: 'WO003',
    deviceName: '土压力计-02号',
    faultType: '需要校准',
    reporter: '赵工',
    reportTime: '2024-05-05 16:45',
    priority: 'medium',
    status: 'in_progress',
    assignee: '刘师傅'
  },
  {
    workOrderId: 'WO004',
    deviceName: '测斜仪-02号',
    faultType: '电源故障',
    reporter: '陈工',
    reportTime: '2024-05-05 14:20',
    priority: 'low',
    status: 'completed',
    assignee: '孙师傅'
  },
  {
    workOrderId: 'WO005',
    deviceName: '水位计-01号',
    faultType: '传感器异常',
    reporter: '周工',
    reportTime: '2024-05-05 10:30',
    priority: 'medium',
    status: 'accepted',
    assignee: '钱师傅'
  }
]

const tableData = ref([])

const getPriorityType = (priority) => {
  const priorityMap = {
    urgent: 'danger',
    high: 'warning',
    medium: 'primary',
    low: 'info'
  }
  return priorityMap[priority] || 'info'
}

const getPriorityText = (priority) => {
  const priorityMap = {
    urgent: '紧急',
    high: '高',
    medium: '中',
    low: '低'
  }
  return priorityMap[priority] || '未知'
}

const getStatusType = (status) => {
  const statusMap = {
    pending: 'info',
    assigned: 'primary',
    in_progress: 'warning',
    completed: 'success',
    accepted: 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待派单',
    assigned: '已派单',
    in_progress: '进行中',
    completed: '已完成',
    accepted: '已验收'
  }
  return statusMap[status] || '未知'
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    // 根据筛选条件过滤数据
    let filteredData = [...mockData]
    
    if (searchForm.status) {
      filteredData = filteredData.filter(item => item.status === searchForm.status)
    }
    
    if (searchForm.priority) {
      filteredData = filteredData.filter(item => item.priority === searchForm.priority)
    }
    
    tableData.value = filteredData
    pagination.total = filteredData.length
    loading.value = false
  }, 500)
}

const handleSearch = () => {
  loadData()
  ElMessage.success('查询完成')
}

const handleReset = () => {
  searchForm.status = ''
  searchForm.priority = ''
  loadData()
}

const handleCreateWorkOrder = () => {
  createForm.deviceName = ''
  createForm.faultType = ''
  createForm.priority = 'medium'
  createForm.reporter = ''
  createVisible.value = true
}

const handleCreateSubmit = () => {
  createFormRef.value.validate((valid) => {
    if (valid) {
      const newOrder = {
        workOrderId: 'WO' + String(tableData.value.length + 1).padStart(3, '0'),
        reportTime: new Date().toLocaleString('zh-CN', { hour12: false }),
        status: 'pending',
        assignee: '',
        ...createForm
      }
      tableData.value.unshift(newOrder)
      pagination.total = tableData.value.length
      createVisible.value = false
      ElMessage.success('工单创建成功，等待派单')
    }
  })
}

const handleView = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const handleAssign = (row) => {
  ElMessageBox.confirm(
    `确定要派发工单 ${row.workOrderId} 吗？派单后维修人员将收到通知。`,
    '派单确认',
    {
      confirmButtonText: '确定派单',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.status = 'assigned'
    row.assignee = '待分配'
    ElMessage.success(`工单 ${row.workOrderId} 已成功派单`)
  }).catch(() => {
    ElMessage.info('已取消派单')
  })
}

const handleAccept = (row) => {
  ElMessageBox.confirm(
    `确定验收工单 ${row.workOrderId} 吗？验收后工单将归档为已完成。`,
    '验收确认',
    {
      confirmButtonText: '确定验收',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.status = 'accepted'
    ElMessage.success(`工单 ${row.workOrderId} 已验收完成`)
  }).catch(() => {
    ElMessage.info('已取消验收')
  })
}

const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消工单 ${row.workOrderId} 吗？取消后不可恢复。`,
    '取消确认',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '返回',
      type: 'warning'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.workOrderId === row.workOrderId)
    if (index > -1) {
      tableData.value.splice(index, 1)
      pagination.total = tableData.value.length
      ElMessage.success(`工单 ${row.workOrderId} 已取消`)
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.workorder {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #2c3e50;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.table-container {
  margin-top: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
