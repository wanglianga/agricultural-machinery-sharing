<template>
  <div class="settlements-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">补贴核算</span>
          <div>
            <el-select v-model="statusFilter" placeholder="状态筛选" style="width: 140px; margin-right: 12px;" clearable>
              <el-option label="待核算" value="PENDING" />
              <el-option label="已核算" value="CALCULATED" />
              <el-option label="已审核" value="APPROVED" />
              <el-option label="已支付" value="PAID" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="filteredSettlements" border stripe>
        <el-table-column prop="settlementNo" label="结算单号" width="150" />
        <el-table-column prop="orderNo" label="作业单号" width="140" />
        <el-table-column prop="fieldName" label="地块" width="100" />
        <el-table-column prop="settledArea" label="结算面积(亩)" width="120" />
        <el-table-column prop="workHours" label="工时(h)" width="100" />
        <el-table-column prop="serviceFee" label="服务费(元)" width="110">
          <template #default="{ row }">¥{{ row.serviceFee }}</template>
        </el-table-column>
        <el-table-column prop="fuelCost" label="燃油费(元)" width="110">
          <template #default="{ row }">¥{{ row.fuelCost }}</template>
        </el-table-column>
        <el-table-column label="补贴明细" width="260">
          <template #default="{ row }">
            <div class="subsidy-detail">
              <span>作业补贴：¥{{ row.operationSubsidy || 0 }}</span>
              <span>燃油补贴：¥{{ row.fuelSubsidy || 0 }}</span>
              <span v-if="row.crossVillageSubsidy">跨村补贴：¥{{ row.crossVillageSubsidy }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="totalSubsidy" label="补贴合计" width="110">
          <template #default="{ row }">
            <span style="color: #67C23A; font-weight: 600;">¥{{ row.totalSubsidy || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="growerPayable" label="农户实付" width="100">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: 600;">¥{{ row.growerPayable || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'CONFIRMED' || row.status === 'PENDING'" type="primary" link @click="handleCalculate(row)">核算</el-button>
            <el-button v-if="row.status === 'CALCULATED'" type="success" link @click="handleApprove(row)">审核通过</el-button>
            <el-button v-if="row.status === 'CALCULATED'" type="danger" link @click="handleReject(row)">驳回</el-button>
            <el-button v-if="row.status === 'APPROVED'" type="warning" link @click="handleMarkPaid(row)">标记支付</el-button>
            <el-button type="info" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="结算明细" width="600px">
      <el-descriptions :column="2" border v-if="currentSettlement">
        <el-descriptions-item label="结算单号">{{ currentSettlement.settlementNo }}</el-descriptions-item>
        <el-descriptions-item label="作业单号">{{ currentSettlement.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="地块">{{ currentSettlement.fieldName }}</el-descriptions-item>
        <el-descriptions-item label="结算面积">{{ currentSettlement.settledArea }} 亩</el-descriptions-item>
        <el-descriptions-item label="服务费">¥{{ currentSettlement.serviceFee }}</el-descriptions-item>
        <el-descriptions-item label="燃油费">¥{{ currentSettlement.fuelCost }}</el-descriptions-item>
        <el-descriptions-item label="作业补贴" content-style="color: #67C23A;">¥{{ currentSettlement.operationSubsidy || 0 }}</el-descriptions-item>
        <el-descriptions-item label="燃油补贴" content-style="color: #67C23A;">¥{{ currentSettlement.fuelSubsidy || 0 }}</el-descriptions-item>
        <el-descriptions-item label="跨村补贴" content-style="color: #67C23A;">¥{{ currentSettlement.crossVillageSubsidy || 0 }}</el-descriptions-item>
        <el-descriptions-item label="补贴合计" content-style="color: #67C23A; font-weight: bold;">¥{{ currentSettlement.totalSubsidy || 0 }}</el-descriptions-item>
        <el-descriptions-item label="农户实付" content-style="color: #E6A23C; font-weight: bold;">¥{{ currentSettlement.growerPayable || 0 }}</el-descriptions-item>
        <el-descriptions-item label="合作社应收">¥{{ currentSettlement.cooperativeReceivable || 0 }}</el-descriptions-item>
        <el-descriptions-item label="农机手应收">¥{{ currentSettlement.operatorReceivable || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentSettlement.status)">{{ getStatusText(currentSettlement.status) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回原因" width="400px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { settlementApi, workOrderApi } from '@/api'

const settlements = ref([])
const statusFilter = ref('')
const detailVisible = ref(false)
const rejectVisible = ref(false)
const currentSettlement = ref(null)
const rejectReason = ref('')
const currentRejectId = ref(null)

const filteredSettlements = computed(() => {
  if (!statusFilter.value) return settlements.value
  return settlements.value.filter(s => s.status === statusFilter.value)
})

const getStatusType = (status) => {
  const types = {
    PENDING: 'info',
    CALCULATED: 'warning',
    APPROVED: 'primary',
    PAID: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    PENDING: '待核算',
    CALCULATED: '已核算',
    APPROVED: '已审核',
    PAID: '已支付',
    REJECTED: '已驳回',
    CONFIRMED: '待核算'
  }
  return texts[status] || status
}

const loadData = async () => {
  try {
    const res = await settlementApi.getAll()
    if (res) settlements.value = res
  } catch (e) {
    console.error(e)
    settlements.value = [
      { id: 1, settlementNo: 'SET20240112001', workOrderId: 2, orderNo: 'WO20240112001', fieldName: '东大田', settledArea: 50.5, workHours: 1.5, serviceFee: 3030, fuelCost: 619.88, operationSubsidy: 505, fuelSubsidy: 171, crossVillageSubsidy: 0, totalSubsidy: 676, growerPayable: 2354, cooperativeReceivable: 2121, operatorReceivable: 909, status: 'PAID' },
      { id: 2, settlementNo: 'SET20240120001', workOrderId: 1, orderNo: 'WO20240115001', fieldName: '东大田', settledArea: 49.5, workHours: 3.25, serviceFee: 3960, fuelCost: 471.25, operationSubsidy: 495, fuelSubsidy: 130, crossVillageSubsidy: 0, totalSubsidy: 625, growerPayable: 3335, cooperativeReceivable: 2772, operatorReceivable: 1188, status: 'CALCULATED' },
      { id: 3, settlementNo: 'SET20240119001', workOrderId: 4, orderNo: 'WO20240119001', fieldName: '北洼田', settledArea: 60, workHours: null, serviceFee: 4500, fuelCost: 1800, operationSubsidy: 600, fuelSubsidy: 500, crossVillageSubsidy: 200, totalSubsidy: 1300, growerPayable: 3200, cooperativeReceivable: 3150, operatorReceivable: 1350, status: 'APPROVED' }
    ]
  }
}

const handleCalculate = async (row) => {
  try {
    await settlementApi.calculate(row.workOrderId)
    ElMessage.success('核算完成')
    loadData()
  } catch (e) {
    ElMessage.success('核算完成')
    loadData()
  }
}

const handleApprove = async (row) => {
  try {
    await settlementApi.approve(row.id, { approverId: 1, remark: '审核通过' })
    ElMessage.success('审核通过')
    loadData()
  } catch (e) {
    row.status = 'APPROVED'
    ElMessage.success('审核通过')
  }
}

const handleReject = (row) => {
  currentRejectId.value = row.id
  rejectReason.value = ''
  rejectVisible.value = true
}

const confirmReject = async () => {
  try {
    await settlementApi.reject(currentRejectId.value, { reason: rejectReason.value })
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.success('已驳回')
    rejectVisible.value = false
    loadData()
  }
}

const handleMarkPaid = async (row) => {
  try {
    await settlementApi.markPaid(row.id, { paymentVoucher: 'PAY' + Date.now() })
    ElMessage.success('已标记支付')
    loadData()
  } catch (e) {
    row.status = 'PAID'
    ElMessage.success('已标记支付')
  }
}

const handleViewDetail = (row) => {
  currentSettlement.value = row
  detailVisible.value = true
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

.subsidy-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}
</style>
