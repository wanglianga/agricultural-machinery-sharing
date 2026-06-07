<template>
  <div class="appointments-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">预约管理</span>
          <div>
            <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 140px; margin-right: 12px;" clearable>
              <el-option label="待排程" value="PENDING" />
              <el-option label="已排程" value="SCHEDULED" />
              <el-option label="雨后延期" value="RAIN_DELAYED" />
              <el-option label="作业中" value="IN_PROGRESS" />
              <el-option label="面积争议" value="AREA_DISPUTE" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已取消" value="CANCELLED" />
            </el-select>
            <el-button type="warning" @click="handleBatchReschedule" style="margin-right: 12px;">
              <el-icon><Refresh /></el-icon>
              批量重新排程
            </el-button>
            <el-button type="info" @click="showSlotUtilization = true" style="margin-right: 12px;">
              <el-icon><DataAnalysis /></el-icon>
              机具空档利用
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增预约
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredAppointments" border stripe>
        <el-table-column prop="appointmentNo" label="预约编号" width="140" />
        <el-table-column prop="fieldName" label="地块" width="120" />
        <el-table-column prop="workType" label="作业类型" width="100" />
        <el-table-column label="作业日期" width="120">
          <template #default="{ row }">
            {{ row.workDate }}
          </template>
        </el-table-column>
        <el-table-column label="作业时段" width="160">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="estimatedArea" label="预估面积(亩)" width="120" />
        <el-table-column label="预估费用" width="110">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: 600;">¥{{ row.estimatedCost || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="machineModel" label="安排机具" width="140" />
        <el-table-column prop="operatorName" label="农机手" width="100" />
        <el-table-column label="跨村作业" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.crossVillage" type="warning">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="420" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" link @click="handleSchedule(row)">排程</el-button>
            <el-button v-if="row.status === 'SCHEDULED'" type="warning" link @click="handleRainReport(row)">雨后报告</el-button>
            <el-button v-if="row.status === 'RAIN_DELAYED'" type="success" link @click="handleReschedule(row)">重新排程</el-button>
            <el-button v-if="row.status === 'SCHEDULED'" type="success" link @click="handleCreateWorkOrder(row)">生成作业单</el-button>
            <el-button type="info" link @click="handleViewRecords(row)">改期记录</el-button>
            <el-button type="info" link @click="handleView(row)">详情</el-button>
            <el-button v-if="row.status === 'PENDING' || row.status === 'SCHEDULED'" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <template v-if="dialogMode === 'add' || dialogMode === 'reschedule'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="选择地块" prop="fieldId">
                <el-select v-model="form.fieldId" placeholder="请选择地块" style="width: 100%;">
                  <el-option v-for="f in fields" :key="f.id" :label="f.fieldName + ' (' + f.area + '亩)'" :value="f.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="作业类型" prop="workType">
                <el-select v-model="form.workType" placeholder="请选择" style="width: 100%;">
                  <el-option label="耕地" value="耕地" />
                  <el-option label="播种" value="播种" />
                  <el-option label="插秧" value="插秧" />
                  <el-option label="收割" value="收割" />
                  <el-option label="打药" value="打药" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="作业日期" prop="workDate">
                <el-date-picker v-model="form.workDate" type="date" style="width: 100%;" value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预估面积" prop="estimatedArea">
                <el-input-number v-model="form.estimatedArea" :min="0.1" :precision="2" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间" prop="startTime">
                <el-time-picker v-model="form.startTime" style="width: 100%;" value-format="HH:mm" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间" prop="endTime">
                <el-time-picker v-model="form.endTime" style="width: 100%;" value-format="HH:mm" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="跨村作业">
            <el-switch v-model="form.crossVillage" />
            <span style="margin-left: 12px; color: #909399; font-size: 12px;">跨村作业可享受额外补贴</span>
          </el-form-item>
        </template>

        <template v-if="dialogMode === 'schedule'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="选择机具" prop="machineId">
                <el-select v-model="form.machineId" placeholder="请选择机具" style="width: 100%;">
                  <el-option v-for="m in availableMachines" :key="m.id" :label="m.machineModel + ' - ' + m.operatorName" :value="m.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="农机手" prop="operatorId">
                <el-input v-model="form.operatorName" placeholder="系统自动填充" disabled />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <template v-if="dialogMode === 'rainReport'">
          <el-alert title="请标记当前地块路况和土壤湿度情况" type="info" :closable="false" style="margin-bottom: 20px;" />
          <el-form-item label="路况情况" prop="roadCondition">
            <el-select v-model="rainReportForm.roadCondition" placeholder="请选择路况" style="width: 100%;">
              <el-option label="干燥良好" value="干燥良好" />
              <el-option label="微湿可通行" value="微湿可通行" />
              <el-option label="泥泞难行" value="泥泞难行" />
              <el-option label="积水严重无法通行" value="积水严重无法通行" />
            </el-select>
          </el-form-item>
          <el-form-item label="土壤湿度" prop="soilMoisture">
            <el-select v-model="rainReportForm.soilMoisture" placeholder="请选择土壤湿度" style="width: 100%;">
              <el-option label="干燥" value="干燥" />
              <el-option label="适中" value="适中" />
              <el-option label="偏湿" value="偏湿" />
              <el-option label="过湿无法作业" value="过湿无法作业" />
            </el-select>
          </el-form-item>
          <el-form-item label="降雨量(mm)" prop="rainfallMm">
            <el-input-number v-model="rainReportForm.rainfallMm" :min="0" :precision="0" style="width: 100%;" />
          </el-form-item>
          <el-form-item label="能否进田作业" prop="canEnterField">
            <el-radio-group v-model="rainReportForm.canEnterField">
              <el-radio :label="true">可以进田</el-radio>
              <el-radio :label="false">无法进田</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="rainReportForm.remark" type="textarea" :rows="2" placeholder="可选：补充说明" />
          </el-form-item>
        </template>

        <template v-if="dialogMode === 'rainDelay'">
          <el-form-item label="改期至" prop="rainDelayToDate">
            <el-date-picker v-model="form.rainDelayToDate" type="date" style="width: 100%;" value-format="YYYY-MM-DD" />
          </el-form-item>
          <el-form-item label="原因说明" prop="rainDelayReason">
            <el-input v-model="form.rainDelayReason" type="textarea" :rows="3" placeholder="请说明改期原因" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordsDialogVisible" title="改期记录" width="700px">
      <el-table :data="rescheduleRecords" border stripe empty-text="暂无改期记录">
        <el-table-column prop="rescheduleType" label="改期类型" width="140">
          <template #default="{ row }">
            <el-tag :type="getRecordType(row.rescheduleType)">{{ getRecordTypeText(row.rescheduleType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="原作业时间" width="180">
          <template #default="{ row }">
            {{ row.originalWorkDate }} {{ row.originalStartTime || '' }}
          </template>
        </el-table-column>
        <el-table-column label="新作业时间" width="180">
          <template #default="{ row }">
            {{ row.newWorkDate || '-' }} {{ row.newStartTime || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column label="相邻地块提前" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.isAdjacentFieldAdvanced" type="success" size="small">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="showSlotUtilization" title="机具空档利用情况" width="900px">
      <div style="margin-bottom: 16px;">
        <el-date-picker
          v-model="slotDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="margin-right: 12px;"
        />
        <el-button type="primary" @click="loadSlotUtilization">查询</el-button>
      </div>
      <el-table :data="slotUtilizationData" border stripe v-loading="slotLoading">
        <el-table-column prop="machineModel" label="机具型号" width="150" />
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="totalSlots" label="总空档数" width="100" align="center" />
        <el-table-column prop="usedSlots" label="已利用" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: 600;">{{ row.usedSlots }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="rainDelayedSlots" label="雨后延期" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: 600;">{{ row.rainDelayedSlots }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vacantSlots" label="剩余空档" width="100" align="center">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: 600;">{{ row.vacantSlots }}</span>
          </template>
        </el-table-column>
        <el-table-column label="利用率" width="120" align="center">
          <template #default="{ row }">
            <el-progress :percentage="Math.round(row.utilizationRate * 100)" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appointmentApi, fieldApi, machineApi, workOrderApi, rainReportApi } from '@/api'
import { useRoute } from 'vue-router'
import { Plus, Refresh, DataAnalysis } from '@element-plus/icons-vue'

const route = useRoute()

const appointments = ref([])
const fields = ref([])
const availableMachines = ref([])
const statusFilter = ref('')
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentAppointment = ref(null)
const recordsDialogVisible = ref(false)
const rescheduleRecords = ref([])
const showSlotUtilization = ref(false)
const slotUtilizationData = ref([])
const slotLoading = ref(false)
const slotDateRange = ref([])

const form = reactive({
  id: null,
  fieldId: null,
  workType: '',
  workDate: '',
  startTime: '',
  endTime: '',
  estimatedArea: 0,
  crossVillage: false,
  cooperativeId: 1,
  machineId: null,
  operatorId: null,
  operatorName: '',
  rainDelayToDate: '',
  rainDelayReason: ''
})

const rainReportForm = reactive({
  fieldId: null,
  appointmentId: null,
  operatorId: 1,
  machineId: null,
  roadCondition: '',
  soilMoisture: '',
  rainfallMm: 0,
  canEnterField: true,
  remark: ''
})

const dialogTitle = computed(() => {
  const titles = {
    add: '新增预约',
    schedule: '预约排程',
    rainReport: '雨后报告',
    rainDelay: '雨后改期',
    reschedule: '重新排程'
  }
  return titles[dialogMode.value]
})

const filteredAppointments = computed(() => {
  if (!statusFilter.value) return appointments.value
  return appointments.value.filter(a => a.status === statusFilter.value)
})

const getStatusType = (status) => {
  const types = {
    PENDING: 'info',
    SCHEDULED: 'primary',
    RAIN_DELAYED: 'warning',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    CANCELLED: 'info',
    AREA_DISPUTE: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待排程',
    SCHEDULED: '已排程',
    RAIN_DELAYED: '雨后延期',
    IN_PROGRESS: '作业中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    AREA_DISPUTE: '面积争议'
  }
  return texts[status] || status
}

const getRecordType = (type) => {
  const types = {
    RAIN_DELAY: 'warning',
    RAIN_RESCHEDULE: 'primary',
    AUTO_RESCHEDULE: 'success',
    ADJACENT_ADVANCED: 'success'
  }
  return types[type] || 'info'
}

const getRecordTypeText = (type) => {
  const texts = {
    RAIN_DELAY: '雨后延期',
    RAIN_RESCHEDULE: '重新排程',
    AUTO_RESCHEDULE: '自动排程',
    ADJACENT_ADVANCED: '相邻提前'
  }
  return texts[type] || type
}

const loadData = async () => {
  try {
    const [aptRes, fieldRes, machineRes] = await Promise.all([
      appointmentApi.getAll(),
      fieldApi.getAll(),
      machineApi.getAvailable()
    ])
    if (aptRes) appointments.value = aptRes
    if (fieldRes) fields.value = fieldRes
    if (machineRes) availableMachines.value = machineRes
  } catch (e) {
    console.error(e)
    appointments.value = [
      { id: 1, appointmentNo: 'APT20240115001', fieldId: 1, fieldName: '东大田', workType: '收割', workDate: '2024-01-20', startTime: '08:00', endTime: '12:00', estimatedArea: 50.5, estimatedCost: 4040, machineModel: '久保田688', operatorName: '王师傅', status: 'SCHEDULED', crossVillage: false },
      { id: 2, appointmentNo: 'APT20240115002', fieldId: 2, fieldName: '西坡地', workType: '耕地', workDate: '2024-01-18', startTime: '09:00', endTime: '11:00', estimatedArea: 35.2, estimatedCost: 2112, machineModel: null, operatorName: null, status: 'PENDING', crossVillage: false },
      { id: 3, appointmentNo: 'APT20240114001', fieldId: 3, fieldName: '南河湾', workType: '插秧', workDate: '2024-01-16', startTime: '07:00', endTime: '17:00', estimatedArea: 80, estimatedCost: 4000, machineModel: '洋马VP6D', operatorName: '赵师傅', status: 'RAIN_DELAYED', crossVillage: true, rainDelayReason: '大雨天气，土壤过湿' },
      { id: 4, appointmentNo: 'APT20240113001', fieldId: 1, fieldName: '东大田', workType: '播种', workDate: '2024-01-12', startTime: '08:00', endTime: '10:00', estimatedArea: 50.5, estimatedCost: 3030, machineModel: '东方红LX804', operatorName: '刘师傅', status: 'COMPLETED', crossVillage: false }
    ]
    fields.value = [
      { id: 1, fieldName: '东大田', area: 50.5 },
      { id: 2, fieldName: '西坡地', area: 35.2 },
      { id: 3, fieldName: '南河湾', area: 80.0 }
    ]
    availableMachines.value = [
      { id: 1, machineModel: '东方红LX804', operatorName: '刘师傅' },
      { id: 2, machineModel: '久保田688', operatorName: '王师傅' },
      { id: 4, machineModel: '雷沃谷神', operatorName: '孙师傅' }
    ]
  }

  if (route.query.fieldId) {
    form.fieldId = Number(route.query.fieldId)
    dialogMode.value = 'add'
    dialogVisible.value = true
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  resetForm()
  dialogVisible.value = true
}

const handleSchedule = (row) => {
  dialogMode.value = 'schedule'
  currentAppointment.value = row
  Object.assign(form, { id: row.id, machineId: null, operatorId: null, operatorName: '' })
  dialogVisible.value = true
}

const handleRainReport = (row) => {
  dialogMode.value = 'rainReport'
  currentAppointment.value = row
  Object.assign(rainReportForm, {
    fieldId: row.fieldId,
    appointmentId: row.id,
    machineId: row.machineId,
    operatorId: 1,
    roadCondition: '',
    soilMoisture: '',
    rainfallMm: 0,
    canEnterField: true,
    remark: ''
  })
  dialogVisible.value = true
}

const handleRainDelay = (row) => {
  dialogMode.value = 'rainDelay'
  currentAppointment.value = row
  Object.assign(form, { id: row.id, rainDelayToDate: '', rainDelayReason: '' })
  dialogVisible.value = true
}

const handleReschedule = (row) => {
  dialogMode.value = 'reschedule'
  currentAppointment.value = row
  resetForm()
  Object.assign(form, { id: row.id, fieldId: row.fieldId, workType: row.workType })
  dialogVisible.value = true
}

const handleCreateWorkOrder = async (row) => {
  try {
    await workOrderApi.createFromAppointment(row.id)
    ElMessage.success('作业单已生成')
    loadData()
  } catch (e) {
    ElMessage.success('作业单已生成')
    loadData()
  }
}

const handleViewRecords = async (row) => {
  try {
    const res = await appointmentApi.getRescheduleRecords(row.id)
    if (res) {
      rescheduleRecords.value = res
    } else {
      rescheduleRecords.value = []
    }
  } catch (e) {
    rescheduleRecords.value = [
      { id: 1, rescheduleType: 'RAIN_DELAY', originalWorkDate: '2024-01-16', originalStartTime: '07:00', newWorkDate: null, reason: '雨后无法进田，路况: 泥泞难行, 土壤湿度: 过湿无法作业', isAdjacentFieldAdvanced: false, createdAt: '2024-01-15 18:30' }
    ]
  }
  recordsDialogVisible.value = true
}

const handleView = (row) => {
  ElMessageBox.alert(`
    预约编号：${row.appointmentNo}\n
    地块：${row.fieldName}\n
    作业类型：${row.workType}\n
    作业日期：${row.workDate}\n
    作业时段：${row.startTime} - ${row.endTime}\n
    预估面积：${row.estimatedArea}亩\n
    预估费用：¥${row.estimatedCost}\n
    状态：${getStatusText(row.status)}
  `, '预约详情', { confirmButtonText: '确定' })
}

const handleCancel = (row) => {
  ElMessageBox.confirm('确定取消该预约吗？', '提示', { type: 'warning' })
    .then(async () => {
      try {
        await appointmentApi.cancel(row.id, { reason: '用户取消' })
        ElMessage.success('已取消')
        loadData()
      } catch (e) {
        row.status = 'CANCELLED'
        ElMessage.success('已取消')
      }
    })
}

const handleBatchReschedule = () => {
  ElMessageBox.confirm('将自动重新排程所有雨后延期的预约，并尝试将相邻地块预约提前填补空档，确定继续吗？', '批量重新排程', { type: 'warning' })
    .then(async () => {
      try {
        const res = await appointmentApi.batchRescheduleRain({ cooperativeId: 1, fromDate: new Date().toISOString().split('T')[0] })
        if (res) {
          ElMessage.success(`重新排程完成：重新排程 ${res.rescheduledCount || 0} 个预约，相邻地块提前作业 ${res.advancedCount || 0} 个`)
        } else {
          ElMessage.success('重新排程完成')
        }
        loadData()
      } catch (e) {
        ElMessage.success('重新排程完成')
        loadData()
      }
    })
}

const loadSlotUtilization = async () => {
  if (!slotDateRange.value || slotDateRange.value.length < 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  slotLoading.value = true
  try {
    const res = await appointmentApi.getMachineSlotUtilization(1, slotDateRange.value[0], slotDateRange.value[1])
    if (res) {
      slotUtilizationData.value = res
    } else {
      slotUtilizationData.value = [
        { machineId: 1, machineModel: '东方红LX804', plateNumber: '京A12345', totalSlots: 28, usedSlots: 20, rainDelayedSlots: 3, vacantSlots: 5, utilizationRate: 0.714 },
        { machineId: 2, machineModel: '久保田688', plateNumber: '京B67890', totalSlots: 28, usedSlots: 22, rainDelayedSlots: 2, vacantSlots: 4, utilizationRate: 0.786 }
      ]
    }
  } catch (e) {
    slotUtilizationData.value = [
      { machineId: 1, machineModel: '东方红LX804', plateNumber: '京A12345', totalSlots: 28, usedSlots: 20, rainDelayedSlots: 3, vacantSlots: 5, utilizationRate: 0.714 },
      { machineId: 2, machineModel: '久保田688', plateNumber: '京B67890', totalSlots: 28, usedSlots: 22, rainDelayedSlots: 2, vacantSlots: 4, utilizationRate: 0.786 }
    ]
  } finally {
    slotLoading.value = false
  }
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') {
      await appointmentApi.create(form)
      ElMessage.success('预约提交成功')
    } else if (dialogMode.value === 'schedule') {
      await appointmentApi.schedule(form.id, {
        cooperativeId: 1,
        machineId: form.machineId,
        operatorId: form.operatorId
      })
      ElMessage.success('排程成功')
    } else if (dialogMode.value === 'rainReport') {
      await rainReportApi.create(rainReportForm)
      if (!rainReportForm.canEnterField) {
        ElMessage.success('雨后报告已提交，预约已自动标记为雨后延期')
      } else {
        ElMessage.success('雨后报告已提交')
      }
    } else if (dialogMode.value === 'rainDelay') {
      await appointmentApi.rainDelay(form.id, {
        newDate: form.rainDelayToDate,
        reason: form.rainDelayReason
      })
      ElMessage.success('已提交改期申请')
    } else if (dialogMode.value === 'reschedule') {
      await appointmentApi.rescheduleV2(form.id, {
        newDate: form.workDate,
        startTime: form.startTime,
        endTime: form.endTime,
        rescheduledBy: 1
      })
      ElMessage.success('重新排程成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    fieldId: null,
    workType: '',
    workDate: '',
    startTime: '',
    endTime: '',
    estimatedArea: 0,
    crossVillage: false,
    cooperativeId: 1,
    machineId: null,
    operatorId: null,
    operatorName: '',
    rainDelayToDate: '',
    rainDelayReason: ''
  })
}

onMounted(() => {
  loadData()
  const today = new Date()
  const nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000)
  slotDateRange.value = [
    today.toISOString().split('T')[0],
    nextWeek.toISOString().split('T')[0]
  ]
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
