<template>
  <div class="device-list">
    <div class="page-header">
      <h2>设备管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增设备
      </el-button>
    </div>

    <div class="filter-bar">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.keyword" placeholder="设备名称/编码" clearable />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="设备状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="异常预警" :value="2" />
            <el-option label="故障待修" :value="3" />
            <el-option label="已报废" :value="4" />
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
        <el-table-column prop="deviceCode" label="设备编码" width="120" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="140" />
        <el-table-column prop="ipGrade" label="防护等级" width="100" />
        <el-table-column prop="maintenanceCycle" label="维保周期(天)" width="120" />
        <el-table-column prop="status" label="设备状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入库时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleStatusChange(scope.row)">
              {{ scope.row.status === 1 ? '设为故障' : '设为正常' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="110px">
        <el-form-item label="设备编码" prop="deviceCode">
          <el-input v-model="formData.deviceCode" placeholder="请输入设备编码" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="formData.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="formData.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="全站仪" value="全站仪" />
            <el-option label="伺服轴力计" value="伺服轴力计" />
            <el-option label="温度传感器" value="温度传感器" />
            <el-option label="测斜仪" value="测斜仪" />
            <el-option label="水位计" value="水位计" />
            <el-option label="土压力计" value="土压力计" />
            <el-option label="传感器" value="传感器" />
          </el-select>
        </el-form-item>
        <el-form-item label="防护等级" prop="ipGrade">
          <el-input v-model="formData.ipGrade" placeholder="如 IP68" />
        </el-form-item>
        <el-form-item label="维保周期(天)" prop="maintenanceCycle">
          <el-input-number v-model="formData.maintenanceCycle" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { getDeviceList, addDevice, updateDevice, deleteDevice, updateDeviceStatus } from '@/api/device'

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editingRow = ref(null)

const formData = reactive({
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  ipGrade: '',
  maintenanceCycle: 30
})

const formRules = {
  deviceCode: [{ required: true, message: '请输入设备编码', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }]
}

const searchForm = reactive({
  keyword: '',
  status: null
})

const tableData = ref([])

const getStatusType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 1: '正常', 2: '异常预警', 3: '故障待修', 4: '已报废' }
  return map[status] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDeviceList({
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined
    })
    tableData.value = data || []
  } catch (error) {
    console.error('加载设备列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadData()
  ElMessage.success('查询完成')
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  loadData()
}

const dialogTitle = computed(() => isEdit.value ? '编辑设备' : '新增设备')

const handleAdd = () => {
  isEdit.value = false
  editingRow.value = null
  formData.deviceCode = ''
  formData.deviceName = ''
  formData.deviceType = ''
  formData.ipGrade = ''
  formData.maintenanceCycle = 30
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingRow.value = row
  formData.deviceCode = row.deviceCode
  formData.deviceName = row.deviceName
  formData.deviceType = row.deviceType
  formData.ipGrade = row.ipGrade || ''
  formData.maintenanceCycle = row.maintenanceCycle || 30
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      if (isEdit.value && editingRow.value) {
        await updateDevice({ id: editingRow.value.id, ...formData })
        ElMessage.success('设备信息更新成功')
      } else {
        await addDevice({ ...formData })
        ElMessage.success('新设备添加成功')
      }
      dialogVisible.value = false
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

const handleStatusChange = async (row) => {
  const newStatus = row.status === 1 ? 3 : 1
  const statusText = row.status === 1 ? '故障待修' : '正常'
  try {
    await ElMessageBox.confirm(
      `确定将设备 ${row.deviceName} 状态设为 ${statusText} 吗？`,
      '状态变更确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await updateDeviceStatus(row.id, newStatus)
    row.status = newStatus
    ElMessage.success('状态修改成功')
  } catch (_) {
    if (_.toString() !== 'cancel') {
      ElMessage.error('状态修改失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备 ${row.deviceName} 吗？`,
      '删除确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteDevice(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (_) {
    if (_.toString() !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.device-list {
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
</style>