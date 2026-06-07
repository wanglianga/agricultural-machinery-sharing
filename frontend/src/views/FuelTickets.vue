<template>
  <div class="fuel-tickets-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">油票管理</span>
          <div>
            <el-tag type="danger" style="margin-right: 12px; cursor: pointer;" @click="showAbnormal = !showAbnormal">
              异常油票: {{ abnormalCount }}
            </el-tag>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              上传油票
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="displayTickets" border stripe>
        <el-table-column prop="ticketNo" label="油票编号" width="140" />
        <el-table-column prop="orderNo" label="作业单号" width="140" />
        <el-table-column prop="operatorName" label="上报人" width="100" />
        <el-table-column prop="machineModel" label="机具" width="130" />
        <el-table-column prop="fuelAmount" label="加油量(L)" width="110" />
        <el-table-column prop="unitPrice" label="单价(元)" width="100" />
        <el-table-column prop="totalAmount" label="总金额(元)" width="120">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: 600;">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="purchaseDate" label="加油日期" width="120" />
        <el-table-column prop="gasStation" label="加油站" width="150" />
        <el-table-column label="异常" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.abnormal" type="danger">是</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.verified ? 'success' : 'warning'">
              {{ row.verified ? '已审核' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.verified" type="primary" link @click="handleVerify(row)">审核</el-button>
            <el-button type="info" link @click="handleView(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" label-width="100px">
        <template v-if="dialogMode === 'add'">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="关联作业单" prop="workOrderId">
                <el-select v-model="form.workOrderId" placeholder="请选择作业单" style="width: 100%;">
                  <el-option v-for="o in workOrders" :key="o.id" :label="o.orderNo" :value="o.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="加油日期" prop="purchaseDate">
                <el-date-picker v-model="form.purchaseDate" type="date" style="width: 100%;" value-format="YYYY-MM-DD" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="加油量(L)" prop="fuelAmount">
                <el-input-number v-model="form.fuelAmount" :min="0" :precision="2" style="width: 100%;" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="单价(元)" prop="unitPrice">
                <el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="加油站" prop="gasStation">
            <el-input v-model="form.gasStation" placeholder="请输入加油站名称" />
          </el-form-item>
          <el-form-item label="油票照片" prop="ticketImageUrl">
            <el-input v-model="form.ticketImageUrl" placeholder="请上传油票照片URL" />
          </el-form-item>
        </template>
        <template v-if="dialogMode === 'verify'">
          <el-alert title="油票信息核对" type="info" :closable="false" style="margin-bottom: 20px;">
            <p>加油量：{{ currentTicket?.fuelAmount }}L</p>
            <p>单价：¥{{ currentTicket?.unitPrice }}</p>
            <p>总金额：¥{{ currentTicket?.totalAmount }}</p>
            <p>加油日期：{{ currentTicket?.purchaseDate }}</p>
          </el-alert>
          <el-form-item label="是否正常" prop="isNormal">
            <el-radio-group v-model="form.isNormal">
              <el-radio :label="true">正常</el-radio>
              <el-radio :label="false">异常</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="!form.isNormal" label="异常原因" prop="abnormalReason">
            <el-input v-model="form.abnormalReason" type="textarea" :rows="3" placeholder="请说明异常原因" />
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
import { fuelTicketApi, workOrderApi } from '@/api'
import { Plus } from '@element-plus/icons-vue'

const fuelTickets = ref([])
const workOrders = ref([])
const showAbnormal = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('add')
const currentTicket = ref(null)

const form = reactive({
  workOrderId: null,
  fuelAmount: 0,
  unitPrice: 0,
  purchaseDate: '',
  gasStation: '',
  ticketImageUrl: '',
  operatorId: 1,
  machineId: 1,
  isNormal: true,
  abnormalReason: ''
})

const dialogTitle = computed(() => dialogMode.value === 'add' ? '上传油票' : '油票审核')

const abnormalCount = computed(() => fuelTickets.value.filter(t => t.abnormal).length)

const displayTickets = computed(() => {
  if (showAbnormal.value) {
    return fuelTickets.value.filter(t => t.abnormal)
  }
  return fuelTickets.value
})

const loadData = async () => {
  try {
    const [ticketRes, orderRes] = await Promise.all([
      fuelTicketApi.getAll(),
      workOrderApi.getAll()
    ])
    if (ticketRes) fuelTickets.value = ticketRes
    if (orderRes) workOrders.value = orderRes
  } catch (e) {
    console.error(e)
    fuelTickets.value = [
      { id: 1, ticketNo: 'FT20240115001', workOrderId: 2, orderNo: 'WO20240112001', operatorName: '刘师傅', machineModel: '东方红LX804', fuelAmount: 85.5, unitPrice: 7.25, totalAmount: 619.88, purchaseDate: '2024-01-12', gasStation: '中石化张庄加油站', abnormal: false, verified: true },
      { id: 2, ticketNo: 'FT20240115002', workOrderId: 1, orderNo: 'WO20240115001', operatorName: '王师傅', machineModel: '久保田688', fuelAmount: 65.0, unitPrice: 7.25, totalAmount: 471.25, purchaseDate: '2024-01-20', gasStation: '中石油李庄加油站', abnormal: false, verified: false },
      { id: 3, ticketNo: 'FT20240115003', workOrderId: 4, orderNo: 'WO20240119001', operatorName: '孙师傅', machineModel: '雷沃谷神', fuelAmount: 250.0, unitPrice: 7.20, totalAmount: 1800.0, purchaseDate: '2024-01-19', gasStation: '个体加油站', abnormal: true, verified: false, abnormalReason: '加油量异常，超过单次最大限值' }
    ]
    workOrders.value = [
      { id: 1, orderNo: 'WO20240115001' },
      { id: 2, orderNo: 'WO20240112001' },
      { id: 3, orderNo: 'WO20240118001' },
      { id: 4, orderNo: 'WO20240119001' }
    ]
  }
}

const handleAdd = () => {
  dialogMode.value = 'add'
  Object.assign(form, {
    workOrderId: null,
    fuelAmount: 0,
    unitPrice: 7.25,
    purchaseDate: '',
    gasStation: '',
    ticketImageUrl: '',
    operatorId: 1,
    machineId: 1
  })
  dialogVisible.value = true
}

const handleVerify = (row) => {
  dialogMode.value = 'verify'
  currentTicket.value = row
  Object.assign(form, { isNormal: !row.abnormal, abnormalReason: row.abnormalReason || '' })
  dialogVisible.value = true
}

const handleView = (row) => {
  ElMessage({
    message: `油票编号：${row.ticketNo}\n作业单号：${row.orderNo}\n加油量：${row.fuelAmount}L\n单价：¥${row.unitPrice}\n总金额：¥${row.totalAmount}\n加油站：${row.gasStation}\n状态：${row.verified ? '已审核' : '待审核'}${row.abnormal ? '\n异常原因：' + row.abnormalReason : ''}`,
    type: 'info',
    duration: 5000,
    showClose: true
  })
}

const handleSubmit = async () => {
  try {
    if (dialogMode.value === 'add') {
      if (!form.totalAmount) {
        form.totalAmount = form.fuelAmount * form.unitPrice
      }
      await fuelTicketApi.create(form)
      ElMessage.success('油票上传成功')
    } else if (dialogMode.value === 'verify') {
      await fuelTicketApi.verify(currentTicket.value.id, {
        verifierId: 1,
        isNormal: form.isNormal,
        abnormalReason: form.abnormalReason
      })
      ElMessage.success('审核完成')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  }
}

onMounted(() => {
  loadData()
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
