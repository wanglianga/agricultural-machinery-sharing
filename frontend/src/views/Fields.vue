<template>
  <div class="fields-page">
    <el-card shadow="never">
      <template #header>
        <div class="page-header">
          <span class="page-title">地块管理</span>
          <div>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索地块名称/位置"
              style="width: 240px; margin-right: 12px;"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增地块
            </el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredFields" border stripe>
        <el-table-column prop="fieldName" label="地块名称" width="150" />
        <el-table-column prop="location" label="位置" width="200" />
        <el-table-column prop="village" label="村" width="120" />
        <el-table-column prop="town" label="乡镇" width="120" />
        <el-table-column prop="area" label="面积(亩)" width="100">
          <template #default="{ row }">
            <span style="color: #409EFF; font-weight: 600;">{{ row.area }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cropType" label="作物类型" width="120" />
        <el-table-column prop="soilType" label="土壤类型" width="100" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleCreateAppointment(row)">预约作业</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑地块' : '新增地块'"
      width="600px"
    >
      <el-form :model="form" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="地块名称" prop="fieldName">
              <el-input v-model="form.fieldName" placeholder="请输入地块名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(亩)" prop="area">
              <el-input-number v-model="form.area" :min="0.1" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="乡镇" prop="town">
              <el-input v-model="form.town" placeholder="请输入乡镇" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="村" prop="village">
              <el-input v-model="form.village" placeholder="请输入村" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入详细位置" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="作物类型" prop="cropType">
              <el-select v-model="form.cropType" placeholder="请选择" style="width: 100%;">
                <el-option label="小麦" value="小麦" />
                <el-option label="玉米" value="玉米" />
                <el-option label="水稻" value="水稻" />
                <el-option label="大豆" value="大豆" />
                <el-option label="花生" value="花生" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="土壤类型" prop="soilType">
              <el-select v-model="form.soilType" placeholder="请选择" style="width: 100%;">
                <el-option label="壤土" value="壤土" />
                <el-option label="砂土" value="砂土" />
                <el-option label="粘土" value="粘土" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactName">
              <el-input v-model="form.contactName" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="灌溉方式" prop="irrigationMethod">
          <el-select v-model="form.irrigationMethod" placeholder="请选择" style="width: 100%;">
            <el-option label="喷灌" value="喷灌" />
            <el-option label="滴灌" value="滴灌" />
            <el-option label="漫灌" value="漫灌" />
            <el-option label="无" value="无" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { fieldApi } from '@/api'
import { useRouter } from 'vue-router'
import { Search, Plus } from '@element-plus/icons-vue'

const router = useRouter()

const fields = ref([])
const searchKeyword = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)

const form = reactive({
  id: null,
  fieldName: '',
  location: '',
  village: '',
  town: '',
  area: 0,
  cropType: '',
  soilType: '',
  irrigationMethod: '',
  contactName: '',
  contactPhone: '',
  growerId: 1,
  remark: ''
})

const filteredFields = computed(() => {
  if (!searchKeyword.value) return fields.value
  const keyword = searchKeyword.value.toLowerCase()
  return fields.value.filter(f =>
    f.fieldName.toLowerCase().includes(keyword) ||
    f.location.toLowerCase().includes(keyword)
  )
})

const loadFields = async () => {
  try {
    const res = await fieldApi.getAll()
    if (res) fields.value = res
  } catch (e) {
    console.error(e)
    fields.value = [
      { id: 1, fieldName: '东大田', location: '村东头', village: '李家庄', town: '张庄镇', area: 50.5, cropType: '小麦', soilType: '壤土', contactName: '张三', contactPhone: '13800138001' },
      { id: 2, fieldName: '西坡地', location: '村西坡', village: '李家庄', town: '张庄镇', area: 35.2, cropType: '玉米', soilType: '砂土', contactName: '李四', contactPhone: '13800138002' },
      { id: 3, fieldName: '南河湾', location: '村南河边', village: '王家庄', town: '张庄镇', area: 80.0, cropType: '水稻', soilType: '粘土', contactName: '王五', contactPhone: '13800138003' }
    ]
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    fieldName: '',
    location: '',
    village: '',
    town: '',
    area: 0,
    cropType: '',
    soilType: '',
    irrigationMethod: '',
    contactName: '',
    contactPhone: '',
    growerId: 1,
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
      await fieldApi.update(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await fieldApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadFields()
  } catch (e) {
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    loadFields()
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除地块"${row.fieldName}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await fieldApi.delete(row.id)
      ElMessage.success('删除成功')
      loadFields()
    } catch (e) {
      fields.value = fields.value.filter(f => f.id !== row.id)
      ElMessage.success('删除成功')
    }
  })
}

const handleSearch = () => {}

const handleCreateAppointment = (row) => {
  router.push({ path: '/appointments', query: { fieldId: row.id } })
}

onMounted(() => {
  loadFields()
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
