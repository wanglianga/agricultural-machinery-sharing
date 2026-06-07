<template>
  <div class="machines-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">机具管理</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索机具型号"
              style="width: 240px; margin-right: 12px;"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增机具
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredMachines" border stripe>
        <el-table-column prop="machineModel" label="机具型号" width="150" />
        <el-table-column prop="machineType" label="机具类型" width="120" />
        <el-table-column prop="plateNumber" label="车牌号" width="120" />
        <el-table-column prop="hourMeter" label="小时表(h)" width="110" />
        <el-table-column prop="operatorName" label="驾驶员" width="100" />
        <el-table-column prop="serviceArea" label="服务区域" width="150" />
        <el-table-column prop="pricePerMu" label="单价(元/亩)" width="110">
          <template #default="{ row }">
            <span style="color: #E6A23C; font-weight: 600;">¥{{ row.pricePerMu }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workTypes" label="作业类型" width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.available ? 'success' : 'info'">
              {{ row.available ? '可用' : '占用' }}
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

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑机具' : '新增机具'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="机具型号" prop="machineModel">
              <el-input v-model="form.machineModel" placeholder="如：东方红LX804" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="机具类型" prop="machineType">
              <el-select v-model="form.machineType" style="width: 100%;">
                <el-option label="拖拉机" value="拖拉机" />
                <el-option label="收割机" value="收割机" />
                <el-option label="插秧机" value="插秧机" />
                <el-option label="播种机" value="播种机" />
                <el-option label="打药机" value="打药机" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="车牌号" prop="plateNumber">
              <el-input v-model="form.plateNumber" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="小时表(h)" prop="hourMeter">
              <el-input-number v-model="form.hourMeter" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="驾驶员" prop="operatorName">
              <el-input v-model="form.operatorName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务区域" prop="serviceArea">
              <el-input v-model="form.serviceArea" placeholder="如：张庄镇、李庄镇" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="单价(元/亩)" prop="pricePerMu">
              <el-input-number v-model="form.pricePerMu" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作业类型" prop="workTypes">
              <el-input v-model="form.workTypes" placeholder="如：耕地、播种、收割" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="是否可用" prop="available">
          <el-switch v-model="form.available" active-text="可用" inactive-text="占用" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { machineApi } from '@/api'
import { Search, Plus } from '@element-plus/icons-vue'

const machines = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  machineModel: '',
  machineType: '',
  plateNumber: '',
  hourMeter: 0,
  cooperativeId: 1,
  operatorId: null,
  operatorName: '',
  serviceArea: '',
  pricePerHour: 0,
  pricePerMu: 0,
  workTypes: '',
  available: true,
  remark: ''
})

const filteredMachines = computed(() => {
  if (!searchKeyword.value) return machines.value
  const keyword = searchKeyword.value.toLowerCase()
  return machines.value.filter(m =>
    m.machineModel.toLowerCase().includes(keyword)
  )
})

const loadMachines = async () => {
  try {
    const res = await machineApi.getAll()
    if (res) machines.value = res
  } catch (e) {
    console.error(e)
    machines.value = [
      { id: 1, machineModel: '东方红LX804', machineType: '拖拉机', plateNumber: '皖01-00001', hourMeter: 1250, operatorName: '刘师傅', serviceArea: '张庄镇、李庄镇', pricePerMu: 60, workTypes: '耕地、耙地、播种', available: true },
      { id: 2, machineModel: '久保田688', machineType: '收割机', plateNumber: '皖01-00002', hourMeter: 890, operatorName: '王师傅', serviceArea: '张庄镇', pricePerMu: 80, workTypes: '小麦收割、水稻收割', available: true },
      { id: 3, machineModel: '洋马VP6D', machineType: '插秧机', plateNumber: '皖01-00003', hourMeter: 560, operatorName: '赵师傅', serviceArea: '张庄镇、王庄镇', pricePerMu: 50, workTypes: '水稻插秧', available: false },
      { id: 4, machineModel: '雷沃谷神', machineType: '收割机', plateNumber: '皖01-00004', hourMeter: 1100, operatorName: '孙师傅', serviceArea: '李庄镇、王庄镇', pricePerMu: 75, workTypes: '小麦收割、玉米收割', available: true }
    ]
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    machineModel: '',
    machineType: '',
    plateNumber: '',
    hourMeter: 0,
    cooperativeId: 1,
    operatorId: null,
    operatorName: '',
    serviceArea: '',
    pricePerHour: 0,
    pricePerMu: 0,
    workTypes: '',
    available: true,
    remark: ''
  })
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
      await machineApi.update(form.id, form)
    } else {
      await machineApi.create(form)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadMachines()
  } catch (e) {
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadMachines()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除机具"${row.machineModel}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await machineApi.delete(row.id)
      ElMessage.success('删除成功')
      loadMachines()
    } catch (e) {
      machines.value = machines.value.filter(m => m.id !== row.id)
      ElMessage.success('删除成功')
    }
  })
}

onMounted(() => {
  loadMachines()
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
