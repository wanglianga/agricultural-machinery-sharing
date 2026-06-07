<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in stats" :key="stat.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <el-icon :size="28"><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>作业状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>月度作业量趋势</span>
            </div>
          </template>
          <div ref="trendChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最近作业动态</span>
            </div>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :timestamp="activity.time"
              :type="activity.type"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import * as echarts from 'echarts'
import { Location, Tools, Calendar, Money } from '@element-plus/icons-vue'

const statusChartRef = ref(null)
const trendChartRef = ref(null)

const stats = reactive([
  { title: '地块总数', value: 128, icon: 'Location', color: '#409EFF' },
  { title: '机具总数', value: 56, icon: 'Tools', color: '#67C23A' },
  { title: '本月预约', value: 89, icon: 'Calendar', color: '#E6A23C' },
  { title: '补贴总额', value: '¥12.5万', icon: 'Money', color: '#F56C6C' }
])

const activities = reactive([
  { content: '种植大户张三提交了新的地块信息（东大田 50亩）', time: '2024-01-15 14:30', type: 'primary' },
  { content: '农机手李四完成了小麦收割作业，面积45亩', time: '2024-01-15 12:00', type: 'success' },
  { content: '合作社为预约APT20240115001安排了东方红拖拉机', time: '2024-01-15 10:30', type: 'warning' },
  { content: '农服中心审核通过了油票异常处理申请', time: '2024-01-15 09:00', type: 'info' },
  { content: '大雨天气，系统自动延期了3个预约', time: '2024-01-14 18:00', type: 'danger' }
])

const initStatusChart = () => {
  const chart = echarts.init(statusChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [{
      name: '作业状态',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 16, fontWeight: 'bold' }
      },
      labelLine: { show: false },
      data: [
        { value: 15, name: '待排程', itemStyle: { color: '#909399' } },
        { value: 25, name: '已排程', itemStyle: { color: '#409EFF' } },
        { value: 8, name: '作业中', itemStyle: { color: '#E6A23C' } },
        { value: 35, name: '已完成', itemStyle: { color: '#67C23A' } },
        { value: 6, name: '面积争议', itemStyle: { color: '#F56C6C' } }
      ]
    }]
  })
}

const initTrendChart = () => {
  const chart = echarts.init(trendChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['作业面积(亩)', '作业次数'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: [{ type: 'value', name: '亩' }, { type: 'value', name: '次' }],
    series: [
      {
        name: '作业面积(亩)',
        type: 'line',
        smooth: true,
        data: [1200, 1900, 1500, 2800, 3200, 2800],
        itemStyle: { color: '#67C23A' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
          ])
        }
      },
      {
        name: '作业次数',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: [45, 68, 52, 89, 102, 88],
        itemStyle: { color: '#409EFF' }
      }
    ]
  })
}

onMounted(() => {
  initStatusChart()
  initTrendChart()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
</style>
