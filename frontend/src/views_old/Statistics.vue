<template>
    <el-card shadow="hover">
        <template #header><span>统计概览</span></template>
        <div v-if="overview">
            <el-row :gutter="20" style="margin-bottom: 20px">
                <el-col :span="6" v-for="item in statCards" :key="item.label">
                    <el-statistic :title="item.label" :value="item.value" />
                </el-col>
            </el-row>
            <el-divider />
            <el-descriptions :column="2" border title="预测信息">
                <el-descriptions-item label="下次经期">{{ overview.nextPredictedStart || '-' }} ~ {{ overview.nextPredictedEnd || '-' }}</el-descriptions-item>
                <el-descriptions-item label="排卵日">{{ overview.ovulationStart || '-' }} ~ {{ overview.ovulationEnd || '-' }}</el-descriptions-item>
                <el-descriptions-item label="易受孕期">{{ overview.fertileWindowStart || '-' }} ~ {{ overview.fertileWindowEnd || '-' }}</el-descriptions-item>
                <el-descriptions-item label="安全期">{{ overview.safePeriodStart || '-' }} ~ {{ overview.safePeriodEnd || '-' }}</el-descriptions-item>
            </el-descriptions>
            <el-divider />
            <h3>近期周期</h3>
            <el-table :data="overview.recentCycles || []" stripe style="width: 100%">
                <el-table-column prop="startDate" label="开始" width="140" />
                <el-table-column prop="endDate" label="结束" width="140" />
                <el-table-column prop="cycleLength" label="周期(天)" width="100" />
                <el-table-column prop="periodLength" label="经期(天)" width="100" />
                <el-table-column prop="flow" label="经量" width="100" />
                <el-table-column prop="painLevel" label="痛经" width="100" />
            </el-table>
            <el-divider />
            <h3>常见症状</h3>
            <el-table :data="overview.topSymptoms || []" stripe style="width: 100%">
                <el-table-column prop="name" label="症状" />
                <el-table-column prop="count" label="次数" />
                <el-table-column prop="percentage" label="占比">
                    <template #default="scope">
                        <el-progress :percentage="scope.row.percentage" :show-text="true" />
                    </template>
                </el-table-column>
            </el-table>
        </div>
        <el-empty v-else description="暂无数据" />
    </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '../api'

const overview = ref(null)
const statCards = computed(() => [
    { label: '总记录', value: overview.value?.totalRecords || 0 },
    { label: '总周期数', value: overview.value?.totalCycles || 0 },
    { label: '平均周期', value: overview.value?.avgCycleLength ? overview.value.avgCycleLength.toFixed(1) + '天' : '-' },
    { label: '平均经期', value: overview.value?.avgPeriodLength ? overview.value.avgPeriodLength.toFixed(1) + '天' : '-' },
])

onMounted(async () => {
    try { overview.value = await axios.get('/statistics/overview') } catch (e) {}
})
</script>
