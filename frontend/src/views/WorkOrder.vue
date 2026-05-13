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
            <el-option label="待派单" :value="0" />
            <el-option label="待接单" :value="1" />
            <el-option label="维修中" :value="2" />
            <el-option label="待验收" :value="3" />
            <el-option label="已完成" :value="4" />
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
      <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNo" label="工单编号" width="140" />
        <el-table-column prop="deviceCode" label="设备编码" width="120" />
        <el-table-column prop="faultDesc" label="故障描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repairerName" label="维修人员" width="100" />
        <el-table-column prop="createTime" label="报修时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.status === 0" type="success" size="small" @click="handleAssign(scope.row)">派单</el-button>
            <el-button v-if="scope.row.status === 1" type="warning" size="small" @click="handleStart(scope.row)">签到</el-button>
            <el-button v-if="scope.row.status === 2" type="primary" size="small" @click="handleComplete(scope.row)">完成</el-button>
            <el-button v-if="scope.row.status === 3" type="success" size="small" @click="handleAccept(scope.row)">验收</el-button>
            <el-button v-if="[0, 1].includes(scope.row.status)" type="danger" size="small" @click="handleCancel(scope.row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="工单详情" width="500px">
      <el-descriptions v-if="currentOrder" :column="2" border>
        <el-descriptions-item label="工单编号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="设备编码">{{ currentOrder.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="故障描述">{{ currentOrder.faultDesc }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)" size="small">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="维修人员">{{ currentOrder.repairerName || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="报修时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="维修日志" :span="2">{{ currentOrder.repairLog || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="createVisible" title="创建工单" width="500px" :close-on-click-modal="false">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="设备ID" prop="deviceId">
          <el-input-number v-model="createForm.deviceId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDesc">
          <el-input v-model="createForm.faultDesc" type="textarea" :rows="3" placeholder="请输入故障描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit">确定创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeVisible" title="填写维修日志" width="500px" :close-on-click-modal="false">
      <el-form :model="completeForm" ref="completeFormRef" label-width="100px">
        <el-form-item label="维修日志" prop="repairLog">
          <el-input v-model="completeForm.repairLog" type="textarea" :rows="4" placeholder="请填写维修日志及耗材" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompleteSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getWorkOrderList,
  getWorkOrderDetail,
  createWorkOrder,
  assignWorkOrder,
  startWorkOrder,
  completeWorkOrder,
  acceptWorkOrder,
  cancelWorkOrder
} from '@/api/workorder'

const loading = ref(false)
const detailVisible = ref(false)
const createVisible = ref(false)
const completeVisible = ref(false)
const currentOrder = ref(null)
const createFormRef = ref()
const completeFormRef = ref()

const searchForm = reactive({ status: null })
const createForm = reactive({ deviceId: 1, faultDesc: '' })
const completeForm = reactive({ repairLog: '' })

const createRules = {
  deviceId: [{ required: true, message: '请输入设备ID', trigger: 'blur' }],
  faultDesc: [{ required: true, message: '请输入故障描述', trigger: 'blur' }]
}

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const tableData = ref([])

const getStatusType = (status) => {
  const map = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'orange', 4: 'success' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待派单', 1: '待接单', 2: '维修中', 3: '待验收', 4: '已完成' }
  return map[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getWorkOrderList({
      status: searchForm.status || undefined,
      page: pagination.currentPage,
      pageSize: pagination.pageSize
    })
    if (data && data.list) {
      tableData.value = data.list
      pagination.total = data.total || 0
    } else {
      tableData.value = data || []
      pagination.total = tableData.value.length
    }
  } catch (error) {
    console.error('加载工单列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
  ElMessage.success('查询完成')
}

const handleReset = () => {
  searchForm.status = null
  loadData()
}

const handleCreateWorkOrder = () => {
  createForm.deviceId = 1
  createForm.faultDesc = ''
  createVisible.value = true
}

const handleCreateSubmit = async () => {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await createWorkOrder({ ...createForm })
    ElMessage.success('工单创建成功')
    createVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleView = async (row) => {
  try {
    currentOrder.value = await getWorkOrderDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    currentOrder.value = row
    detailVisible.value = true
  }
}

const handleAssign = async (row) => {
  try {
    await ElMessageBox.confirm(`确定派发工单 ${row.orderNo} 吗？`,'派单确认',{
      confirmButtonText: '确定派单', cancelButtonText: '取消', type: 'warning'
    })
    await assignWorkOrder(row.id)
    ElMessage.success('派单成功')
    loadData()
  } catch (_) {}
}

const handleStart = async (row) => {
  try {
    await ElMessageBox.confirm(`确定到场签到并开始维修工单 ${row.orderNo} 吗？`,'签到确认',{
      confirmButtonText: '确定签到', cancelButtonText: '取消', type: 'warning'
    })
    await startWorkOrder(row.id)
    ElMessage.success('已签到，维修中')
    loadData()
  } catch (_) {}
}

const handleComplete = (row) => {
  currentOrder.value = row
  completeForm.repairLog = ''
  completeVisible.value = true
}

const handleCompleteSubmit = async () => {
  try {
    await completeWorkOrder(currentOrder.value.id, completeForm.repairLog)
    ElMessage.success('维修完成，等待验收')
    completeVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

const handleAccept = async (row) => {
  try {
    await ElMessageBox.confirm(`确定验收工单 ${row.orderNo} 吗？`,'验收确认',{
      confirmButtonText: '确定验收', cancelButtonText: '取消', type: 'warning'
    })
    await acceptWorkOrder(row.id)
    ElMessage.success('验收完成')
    loadData()
  } catch (_) {}
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(`确定取消工单 ${row.orderNo} 吗？`,'取消确认',{
      confirmButtonText: '确定取消', cancelButtonText: '返回', type: 'warning'
    })
    await cancelWorkOrder(row.id)
    ElMessage.success('工单已取消')
    loadData()
  } catch (_) {}
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