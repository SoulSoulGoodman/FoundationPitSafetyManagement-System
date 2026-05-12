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
          <el-input
            v-model="searchForm.deviceName"
            placeholder="设备名称"
            clearable
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.status" placeholder="设备状态" clearable>
            <el-option label="正常运行" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="维护中" value="maintenance" />
            <el-option label="已报废" value="scrapped" />
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
        <el-table-column prop="deviceId" label="设备编号" width="120" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="120" />
        <el-table-column prop="location" label="安装位置" width="180" />
        <el-table-column prop="installDate" label="安装日期" width="120" />
        <el-table-column prop="status" label="设备状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              size="small"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastMaintenance" label="上次维护" width="120" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              :type="scope.row.status === 'normal' ? 'warning' : 'success'"
              size="small"
              @click="handleStatusChange(scope.row)"
            >
              {{ scope.row.status === 'normal' ? '设为故障' : '设为正常' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑设备对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="formData.deviceName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="formData.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="测斜仪" value="测斜仪" />
            <el-option label="水位计" value="水位计" />
            <el-option label="土压力计" value="土压力计" />
            <el-option label="钢筋应力计" value="钢筋应力计" />
            <el-option label="全站仪" value="全站仪" />
            <el-option label="伺服轴力计" value="伺服轴力计" />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入安装位置" />
        </el-form-item>
        <el-form-item label="安装日期" prop="installDate">
          <el-date-picker
            v-model="formData.installDate"
            type="date"
            placeholder="请选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="设备状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常运行" value="normal" />
            <el-option label="故障" value="fault" />
            <el-option label="维护中" value="maintenance" />
            <el-option label="已报废" value="scrapped" />
          </el-select>
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

const loading = ref(false)
const dialogVisible = ref(false)
const formRef = ref()
const isEdit = ref(false)
const editingRow = ref(null)

const formData = reactive({
  deviceName: '',
  deviceType: '',
  location: '',
  installDate: '',
  status: 'normal'
})

const formRules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  location: [{ required: true, message: '请输入安装位置', trigger: 'blur' }],
  installDate: [{ required: true, message: '请选择安装日期', trigger: 'change' }]
}

const searchForm = reactive({
  deviceName: '',
  status: ''
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 假数据
const mockData = [
  {
    deviceId: 'DEV001',
    deviceName: '测斜仪-01号',
    deviceType: '测斜仪',
    location: 'A区-北侧基坑',
    installDate: '2024-01-15',
    status: 'normal',
    lastMaintenance: '2024-03-10'
  },
  {
    deviceId: 'DEV002',
    deviceName: '水位计-02号',
    deviceType: '水位计',
    location: 'B区-南侧基坑',
    installDate: '2024-01-20',
    status: 'fault',
    lastMaintenance: '2024-02-28'
  },
  {
    deviceId: 'DEV003',
    deviceName: '土压力计-01号',
    deviceType: '土压力计',
    location: 'A区-东侧基坑',
    installDate: '2024-02-01',
    status: 'maintenance',
    lastMaintenance: '2024-04-05'
  },
  {
    deviceId: 'DEV004',
    deviceName: '测斜仪-03号',
    deviceType: '测斜仪',
    location: 'C区-西侧基坑',
    installDate: '2024-02-10',
    status: 'normal',
    lastMaintenance: '2024-03-15'
  },
  {
    deviceId: 'DEV005',
    deviceName: '水位计-05号',
    deviceType: '水位计',
    location: 'B区-中央基坑',
    installDate: '2024-02-15',
    status: 'scrapped',
    lastMaintenance: '2024-01-20'
  },
  {
    deviceId: 'DEV006',
    deviceName: '土压力计-02号',
    deviceType: '土压力计',
    location: 'C区-北侧基坑',
    installDate: '2024-03-01',
    status: 'normal',
    lastMaintenance: '2024-04-01'
  }
]

const tableData = ref([])

const getStatusType = (status) => {
  const statusMap = {
    normal: 'success',
    fault: 'danger',
    maintenance: 'warning',
    scrapped: 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    normal: '正常运行',
    fault: '故障',
    maintenance: '维护中',
    scrapped: '已报废'
  }
  return statusMap[status] || '未知'
}

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    // 根据筛选条件过滤数据
    let filteredData = [...mockData]
    
    if (searchForm.deviceName) {
      filteredData = filteredData.filter(item => 
        item.deviceName.toLowerCase().includes(searchForm.deviceName.toLowerCase())
      )
    }
    
    if (searchForm.status) {
      filteredData = filteredData.filter(item => item.status === searchForm.status)
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
  searchForm.deviceName = ''
  searchForm.status = ''
  loadData()
}

const dialogTitle = computed(() => isEdit.value ? '编辑设备' : '新增设备')

const handleAdd = () => {
  isEdit.value = false
  editingRow.value = null
  formData.deviceName = ''
  formData.deviceType = ''
  formData.location = ''
  formData.installDate = ''
  formData.status = 'normal'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  editingRow.value = row
  formData.deviceName = row.deviceName
  formData.deviceType = row.deviceType
  formData.location = row.location
  formData.installDate = row.installDate
  formData.status = row.status
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      if (isEdit.value && editingRow.value) {
        Object.assign(editingRow.value, { ...formData })
        ElMessage.success('设备信息更新成功')
      } else {
        const newDevice = {
          deviceId: 'DEV' + String(tableData.value.length + 1).padStart(3, '0'),
          lastMaintenance: '-',
          ...formData
        }
        tableData.value.push(newDevice)
        pagination.total = tableData.value.length
        ElMessage.success('新设备添加成功')
      }
      dialogVisible.value = false
    }
  })
}

const handleStatusChange = (row) => {
  const newStatus = row.status === 'normal' ? '故障' : '正常'
  ElMessageBox.confirm(
    `确定要将设备 ${row.deviceName} 状态设为 ${newStatus} 吗？`,
    '状态变更确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    row.status = row.status === 'normal' ? 'fault' : 'normal'
    ElMessage.success('状态修改成功')
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除设备 ${row.deviceName} 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = tableData.value.findIndex(item => item.deviceId === row.deviceId)
    if (index > -1) {
      tableData.value.splice(index, 1)
      pagination.total = tableData.value.length
      ElMessage.success('删除成功')
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
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

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
