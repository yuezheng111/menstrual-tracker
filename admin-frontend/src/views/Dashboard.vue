<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" :style="{ background: card.bg, color: card.color }">
              <el-icon :size="24"><component :is="card.icon" /></el-icon>
            </div>
            <div>
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>近 30 天用户增长</template>
          <div ref="growthRef" style="height: 340px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>症状统计 TOP10</template>
          <div ref="symptomRef" style="height: 340px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview } from '../api/admin'

const growthRef = ref()
const symptomRef = ref()
let growthChart = null
let symptomChart = null

const cards = reactive([
  { label: '总用户数', value: 0, icon: 'User', color: '#409EFF', bg: 'rgba(64,158,255,.12)' },
  { label: '本月新增', value: 0, icon: 'UserFilled', color: '#67C23A', bg: 'rgba(103,194,58,.12)' },
  { label: '总记录数', value: 0, icon: 'Document', color: '#E6A23C', bg: 'rgba(230,162,60,.12)' },
  { label: '今日记录', value: 0, icon: 'EditPen', color: '#F56C6C', bg: 'rgba(245,108,108,.12)' },
  { label: '7日活跃用户', value: 0, icon: 'Aim', color: '#909399', bg: 'rgba(144,147,153,.12)' }
])

onMounted(async () => {
  const res = await getOverview()
  const data = res.data
  cards[0].value = data.totalUsers ?? 0
  cards[1].value = data.newUsersThisMonth ?? 0
  cards[2].value = data.totalRecords ?? 0
  cards[3].value = data.todayRecords ?? 0
  cards[4].value = data.activeUsers7d ?? 0
  await nextTick()
  renderGrowth(data.userGrowth || [])
  renderSymptom(data.topSymptoms || [])
})

function renderGrowth(points) {
  if (!growthRef.value) return
  growthChart = echarts.init(growthRef.value)
  growthChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: points.map(p => p.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line',
      smooth: true,
      areaStyle: { opacity: .15 },
      data: points.map(p => p.count),
      itemStyle: { color: '#409EFF' }
    }]
  })
}

function renderSymptom(items) {
  if (!symptomRef.value) return
  symptomChart = echarts.init(symptomRef.value)
  const data = items.slice().reverse()
  symptomChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 90, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: data.map(i => i.name) },
    series: [{
      type: 'bar',
      barWidth: 14,
      itemStyle: { color: '#ff9a9e', borderRadius: [0, 4, 4, 0] },
      data: data.map(i => i.count)
    }]
  })
}

window.addEventListener('resize', () => {
  growthChart && growthChart.resize()
  symptomChart && symptomChart.resize()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
}
.stat-label {
  font-size: 13px;
  color: #909399;
}
</style>
