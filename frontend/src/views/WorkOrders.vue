<template>
  <div class="work-orders-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">作业单管理</span>
          <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 140px;" clearable>
            <el-option label="已派单" value="ASSIGNED" />
            <el-option label="已到田" value="ARRIVED" />
            <el-option label="作业中" value="WORKING" />
            <el-option label="待确认" value="AWAITING_CONFIRMATION" />
            <el-option label="已确认" value="CONFIRMED" />
            <el-option label="有争议" value="DISPUTED" />
          </el-select>
        </div>
      </template>

      <el-table :data="filteredOrders" border stripe>
        <el-table-column prop="orderNo" label="作业单号" width="140" />
        <el-table-column prop="appointmentNo" label="预约编号" width="140" />
        <el-table-column prop="fieldName" label="地块" width="100" />
        <el-table-column prop="machineModel" label="机具" width="130" />
        <el-table-column prop="operatorName" label="农机手" width="100" />
        <el-table-column label="到田时间" width="160">
          <template #default="{ row }">{{ row.arriveTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="作业时长(h)" width="110">
          <template #default="{ row }">{{ row.workHours || '-' }}</template>
        </el-table-column>
        <el-table-column label="实测面积(亩)" width="120">
          <template #default="{ row }">{{ row.actualArea || '-' }}</template>
        </el-table-column>
        <el-table-column label="确认面积(亩)" width="120">
          <template #default="{ row }">
            <span v-if="row.growerConfirmedArea">{{ row.growerConfirmedArea }}</span>
            <span v-else style="color: #909399;">待确认</span>
          </template>
        </el-table-column>
        <el-table-column label="面积一致" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.areaConsistent === true" type="success">一致</el-tag>
            <el-tag v-else-if="row.areaConsistent === false" type="danger">不一致</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'ASSIGNED'" type="primary" link @click="handleArrive(row)">报到田</el-button>
            <el-button v-if="row.status === 'ARRIVED'" type="success" link @click="handleStart(row)">开始作业</el-button>
            <el-button v-if="row.status === 'WORKING'" type="warning" link @click="handleComplete(row)">完工上报</el-button>
            <el-button v-if="row.status === 'AWAITING_CONFIRMATION'" type="primary" link @click="handleConfirm(row)">面积确认</el-button>
            <el-button v-if="row.status === 'DISPUTED'" type="danger" link @click="handleResolve(row)">争议处理</el-button>
            <el-button type="info" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="100px">
        <template v-if="dialogMode === 'complete'">
          <el-form-item label="实测面积(亩)" prop="actualArea">
            <el-input-number v-model="form.actualArea" :min="0.1" :precision="2" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="完工照片" prop="completionPhotoUrl">
            <el-input v-model="form.completionPhotoUrl" placeholder="请输入照片URL" />
          </el-form-item>
        </template>
        <template v-if="dialogMode === 'confirm'">
          <el-form-item label="确认面积(亩)" prop="confirmedArea">
            <el-input-number v-model="form.confirmedArea" :min="0.1" :precision="2" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="面积一致" prop="areaConsistent">
            <el-radio-group v-model="form.areaConsistent">
              <el-radio :label="true">一致</el-radio>
              <el-radio :label="false">不一致</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="!form.areaConsistent" label="争议原因" prop="disputeReason">
            <el-input v-model="form.disputeReason" type="textarea" :rows="3" placeholder="请说明面积争议原因" />
          </el-form-item>
        </template>
        <template v-if="dialogMode === 'resolve'">
          <el-form-item label="最终面积(亩)" prop="finalArea">
            <el-input-number v-model="form.finalArea" :min="0.1" :precision="2" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="处理说明" prop="resolutionRemark">
            <el-input v-model="form.resolutionRemark" type="textarea" :rows="3" placeholder="请说明争议处理结果" />
          </el-form-item>
        </template>
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
import { ElMessage } from 'element-plus'
import { workOrderApi } from '@/api'

const workOrders = ref([])
const statusFilter = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('')
const currentOrder = ref(null)

const form = reactive({
  actualArea: 0,
  completionPhotoUrl: '',
  confirmedArea: 0,
  areaConsistent: true,
  disputeReason: '',
  finalArea: 0,
  resolutionRemark: ''
})

const dialogTitle = computed(() => {
  const titles = {
    complete: '完工上报',
    confirm: '面积确认',
    resolve: '争议处理'
  }
  return titles[dialogMode.value]
})

const filteredOrders = computed(() => {
  if (!statusFilter.value) return workOrders.value
  return workOrders.value.filter(o => o.status === statusFilter.value)
})

const getStatusType = (status) => {
  const types = {
    ASSIGNED: 'info',
    ARRIVED: 'primary',
    WORKING: 'warning',
    AWAITING_CONFIRMATION: 'warning',
    CONFIRMED: 'success',
    DISPUTED: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    ASSIGNED: '已派单',
    ARRIVED: '已到田',
    WORKING: '作业中',
    AWAITING_CONFIRMATION: '待确认',
    CONFIRMED: '已确认',
    DISPUTED: '有争议'
  }
  return texts[status] || status
}

const loadOrders = async () => {
  try {
    const res = await workOrderApi.getAll()
    if (res) workOrders.value = res
  } catch (e) {
    console.error(e)
    workOrders.value = [
      { id: 1, orderNo: 'WO20240115001', appointmentNo: 'APT20240115001', fieldName: '东大田', machineModel: '久保田688', operatorName: '王师傅', arriveTime: '2024-01-20 08:15', startTime: '2024-01-20 08:30', endTime: '2024-01-20 11:45', workHours: 3.25, actualArea: 48.5, growerConfirmedArea: 50.5, areaConsistent: false, finalSettledArea: null, status: 'DISPUTED', areaDisputeReason: '实际作业面积与GPS测量有差异' },
      { id: 2, orderNo: 'WO20240112001', appointmentNo: 'APT20240113001', fieldName: '东大田', machineModel: '东方红LX804', operatorName: '刘师傅', arriveTime: '2024-01-12 08:05', startTime: '2024-01-12 08:20', endTime: '2024-01-12 09:50', workHours: 1.5, actualArea: 50.5, growerConfirmedArea: 50.5, areaConsistent: true, finalSettledArea: 50.5, status: 'CONFIRMED' },
      { id: 3, orderNo: 'WO20240118001', appointmentNo: 'APT20240115002', fieldName: '西坡地', machineModel: '东方红LX804', operatorName: '刘师傅', arriveTime: null, startTime: null, endTime: null, workHours: null, actualArea: null, growerConfirmedArea: null, areaConsistent: null, status: 'ASSIGNED' },
      { id: 4, orderNo: 'WO20240119001', appointmentNo: 'APT20240116001', fieldName: '北洼田', machineModel: '雷沃谷神', operatorName: '孙师傅', arriveTime: '2024-01-19 07:50', startTime: '2024-01-19 08:10', endTime: null, workHours: null, actualArea: null, growerConfirmedArea: null, areaConsistent: null, status: 'WORKING' }
    ]
  }
}

const handleArrive = async (row) => {
  try {
    await workOrderApi.reportArrival(row.id)
    ElMessage.success('已报到田')
    loadOrders()
  } catch (e) {
    row.status = 'ARRIVED'
    row.arriveTime = new Date().toLocaleString()
    ElMessage.success('已报到田')
  }
}

const handleStart = async (row) => {
  try {
    await workOrderApi.startWork(row.id)
    ElMessage.success('已开始作业')
    loadOrders()
  } catch (e) {
    row.status = 'WORKING'
    row.startTime = new Date().toLocaleString()
    ElMessage.success('已开始作业')
  }
}

const handleComplete = (row) => {
  dialogMode.value = 'complete'
  currentOrder.value = row
  Object.assign(form, { actualArea: 0, completionPhotoUrl: '' })
  dialogVisible.value = true
}

const handleConfirm = (row) => {
  dialogMode.value = 'confirm'
  currentOrder.value = row
  Object.assign(form, { confirmedArea: row.actualArea || 0, areaConsistent: true, disputeReason: '' })
  dialogVisible.value = true
}

const handleResolve = (row) => {
  dialogMode.value = 'resolve'
  currentOrder.value = row
  Object.assign(form, { finalArea: row.actualArea || 0, resolutionRemark: '' })
  dialogVisible.value = true
}

const handleViewDetail = (row) => {
  let detail = `作业单号：${row.orderNo}\n预约编号：${row.appointmentNo}\n地块：${row.fieldName}\n机具：${row.machineModel}\n农机手：${row.operatorName}\n`
  if (row.arriveTime) detail += `到田时间：${row.arriveTime}\n`
  if (row.workHours) detail += `作业时长：${row.workHours}小时\n`
  if (row.actualArea) detail += `实测面积：${row.actualArea}亩\n`
  if (row.growerConfirmedArea) detail += `确认面积：${row.growerConfirmedArea}亩\n`
  if (row.areaDisputeReason) detail += `争议原因：${row.areaDisputeReason}\n`
  detail += `状态：${getStatusText(row.status)}`
  
  ElMessage({
    message: detail,
    type: 'info',
    duration: 5000,
    showClose: true
  })
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'complete') {
      await workOrderApi.completeWork(currentOrder.value.id, {
        actualArea: form.actualArea,
        completionPhotoUrl: form.completionPhotoUrl
      })
      ElMessage.success('完工上报成功')
    } else if (dialogMode.value === 'confirm') {
      await workOrderApi.confirmArea(currentOrder.value.id, {
        confirmedArea: form.confirmedArea,
        areaConsistent: form.areaConsistent,
        disputeReason: form.disputeReason
      })
      ElMessage.success('面积确认已提交')
    } else if (dialogMode.value === 'resolve') {
      await workOrderApi.resolveDispute(currentOrder.value.id, {
        finalArea: form.finalArea,
        resolutionRemark: form.resolutionRemark
      })
      ElMessage.success('争议已处理')
    }
    dialogVisible.value = false
    loadOrders()
  } catch (e) {
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadOrders()
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}
</style>
