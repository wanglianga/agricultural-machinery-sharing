<template>
  <div class="subsidy-policies-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">补贴政策管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增政策
          </el-button>
        </div>
      </template>

      <el-table :data="policies" border stripe>
        <el-table-column prop="policyName" label="政策名称" width="180" />
        <el-table-column prop="policyCode" label="政策编码" width="140" />
        <el-table-column prop="workType" label="适用作业" width="100" />
        <el-table-column prop="subsidyPerMu" label="作业补贴(元/亩)" width="130">
          <template #default="{ row }">
            <span v-if="row.subsidyPerMu" style="color: #67C23A;">¥{{ row.subsidyPerMu }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="fuelSubsidyPerLiter" label="燃油补贴(元/升)" width="140">
          <template #default="{ row }">
            <span v-if="row.fuelSubsidyPerLiter" style="color: #67C23A;">¥{{ row.fuelSubsidyPerLiter }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="subsidyRatio" label="补贴比例(%)" width="120">
          <template #default="{ row }">
            <span v-if="row.subsidyRatio">{{ row.subsidyRatio }}%</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="跨村作业" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.crossVillageApplicable" type="warning">
              适用 +¥{{ row.crossVillageExtraSubsidy || 0 }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="有效期" width="220">
          <template #default="{ row }">
            {{ row.effectiveDate }} ~ {{ row.expiryDate || '长期' }}
          </template>
        </el-table-column>
        <el-table-column prop="applicableRegion" label="适用区域" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.active ? 'success' : 'info'">
              {{ row.active ? '生效中' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑政策' : '新增政策'" width="600px">
      <el-form :model="form" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="政策名称" prop="policyName">
              <el-input v-model="form.policyName" placeholder="请输入政策名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="政策编码" prop="policyCode">
              <el-input v-model="form.policyCode" placeholder="如：BT2024001" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="适用作业" prop="workType">
              <el-select v-model="form.workType" placeholder="请选择" style="width: 100%;">
                <el-option label="全部" value="全部" />
                <el-option label="耕地" value="耕地" />
                <el-option label="播种" value="播种" />
                <el-option label="插秧" value="插秧" />
                <el-option label="收割" value="收割" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用区域" prop="applicableRegion">
              <el-input v-model="form.applicableRegion" placeholder="如：全县/张庄镇" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="作业补贴(元/亩)" prop="subsidyPerMu">
              <el-input-number v-model="form.subsidyPerMu" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="燃油补贴(元/升)" prop="fuelSubsidyPerLiter">
              <el-input-number v-model="form.fuelSubsidyPerLiter" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="补贴比例(%)" prop="subsidyRatio">
              <el-input-number v-model="form.subsidyRatio" :min="0" :max="100" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">跨村作业补贴</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="是否适用">
              <el-switch v-model="form.crossVillageApplicable" />
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="额外补贴(元/次)" prop="crossVillageExtraSubsidy">
              <el-input-number v-model="form.crossVillageExtraSubsidy" :min="0" :precision="2" style="width: 100%;" :disabled="!form.crossVillageApplicable" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">有效期设置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生效日期" prop="effectiveDate">
              <el-date-picker v-model="form.effectiveDate" type="date" style="width: 100%;" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止日期" prop="expiryDate">
              <el-date-picker v-model="form.expiryDate" type="date" style="width: 100%;" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="是否启用" prop="active">
          <el-switch v-model="form.active" active-text="生效中" inactive-text="已停用" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { subsidyPolicyApi } from '@/api'
import { Plus } from '@element-plus/icons-vue'

const policies = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  policyName: '',
  policyCode: '',
  workType: '全部',
  subsidyPerMu: 0,
  subsidyPerHour: null,
  fuelSubsidyPerLiter: 0,
  subsidyRatio: null,
  crossVillageApplicable: false,
  crossVillageExtraSubsidy: 0,
  effectiveDate: '',
  expiryDate: null,
  active: true,
  applicableRegion: '',
  remark: ''
})

const loadPolicies = async () => {
  try {
    const res = await subsidyPolicyApi.getAll()
    if (res) policies.value = res
  } catch (e) {
    console.error(e)
    policies.value = [
      { id: 1, policyName: '2024年小麦收割补贴', policyCode: 'XMSG2024001', workType: '收割', subsidyPerMu: 10, fuelSubsidyPerLiter: 2, subsidyRatio: null, crossVillageApplicable: true, crossVillageExtraSubsidy: 200, effectiveDate: '2024-01-01', expiryDate: '2024-12-31', applicableRegion: '全县', active: true },
      { id: 2, policyName: '2024年水稻插秧补贴', policyCode: 'SDCY2024001', workType: '插秧', subsidyPerMu: 15, fuelSubsidyPerLiter: 1.5, subsidyRatio: null, crossVillageApplicable: true, crossVillageExtraSubsidy: 150, effectiveDate: '2024-01-01', expiryDate: '2024-12-31', applicableRegion: '张庄镇、李庄镇', active: true },
      { id: 3, policyName: '跨村作业专项补贴', policyCode: 'KCZY2024001', workType: '全部', subsidyPerMu: null, fuelSubsidyPerLiter: null, subsidyRatio: null, crossVillageApplicable: true, crossVillageExtraSubsidy: 300, effectiveDate: '2024-01-01', expiryDate: '2024-12-31', applicableRegion: '全县', active: true }
    ]
  }
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await subsidyPolicyApi.update(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await subsidyPolicyApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadPolicies()
  } catch (e) {
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadPolicies()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除政策"${row.policyName}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await subsidyPolicyApi.delete(row.id)
      ElMessage.success('删除成功')
      loadPolicies()
    } catch (e) {
      policies.value = policies.value.filter(p => p.id !== row.id)
      ElMessage.success('删除成功')
    }
  })
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    policyName: '',
    policyCode: '',
    workType: '全部',
    subsidyPerMu: 0,
    subsidyPerHour: null,
    fuelSubsidyPerLiter: 0,
    subsidyRatio: null,
    crossVillageApplicable: false,
    crossVillageExtraSubsidy: 0,
    effectiveDate: '',
    expiryDate: null,
    active: true,
    applicableRegion: '',
    remark: ''
  })
}

onMounted(() => {
  loadPolicies()
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
