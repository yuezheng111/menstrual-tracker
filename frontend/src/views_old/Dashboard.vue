<template>
    <div>
        <el-row :gutter="20">
            <el-col :span="6" v-for="item in stats" :key="item.label">
                <el-card shadow="hover" style="margin-bottom: 20px">
                    <div style="text-align: center">
                        <div style="font-size: 32px; font-weight: bold; color: #409EFF">{{ item.value }}</div>
                        <div style="color: #999; margin-top: 5px">{{ item.label }}</div>
                    </div>
                </el-card>
            </el-col>
        </el-row>
        <el-row :gutter="20">
            <el-col :span="16">
                <el-card shadow="hover">
                    <template #header><span>周期预测</span></template>
                    <div v-if="prediction" style="padding: 10px 0">
                        <el-descriptions :column="2" border>
                            <el-descriptions-item label="下次经期">{{ prediction.nextPeriodStart }} ~ {{ prediction.nextPeriodEnd }}</el-descriptions-item>
                            <el-descriptions-item label="周期长度">{{ prediction.predictedCycleLength }} 天</el-descriptions-item>
                            <el-descriptions-item label="排卵窗口">{{ prediction.ovulationStart }} ~ {{ prediction.ovulationEnd }}</el-descriptions-item>
                            <el-descriptions-item label="易受孕期">{{ prediction.fertileWindowStart }} ~ {{ prediction.fertileWindowEnd }}</el-descriptions-item>
                            <el-descriptions-item label="安全期">{{ prediction.safePeriodStart }} ~ {{ prediction.safePeriodEnd }}</el-descriptions-item>
                            <el-descriptions-item label="可信度">{{ prediction.confidence }}</el-descriptions-item>
                        </el-descriptions>
                    </div>
                    <el-empty v-else description="暂无数据，开始记录经期后即可获得预测。" />
                </el-card>
            </el-col>
            <el-col :span="8">
                <el-card shadow="hover">
                    <template #header><span>即将到来的提醒</span></template>
                    <div v-if="reminders.length > 0">
                        <div v-for="r in reminders" :key="r.date" style="padding: 10px 0; border-bottom: 1px solid #eee">
                            <div><strong>{{ r.title }}</strong></div>
                            <div style="color: #666; font-size: 13px">{{ r.description }}</div>
                            <div style="color: #999; font-size: 12px">{{ r.date }} ({{ r.daysUntil }} 天后)</div>
                        </div>
                    </div>
                    <el-empty v-else description="暂无提醒" />
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from '../api'

const stats = ref([
    { label: '总记录', value: '-' },
    { label: '平均周期', value: '-' },
    { label: '平均经期', value: '-' },
    { label: '下次经期', value: '-' }
])
const prediction = ref(null)
const reminders = ref([])

onMounted(async () => {
    try {
        const overview = await axios.get('/statistics/overview')
        stats.value = [
            { label: '总记录', value: overview.totalRecords || 0 },
            { label: '平均周期', value: overview.avgCycleLength ? overview.avgCycleLength.toFixed(1) + '天' : '-' },
            { label: '平均经期', value: overview.avgPeriodLength ? overview.avgPeriodLength.toFixed(1) + '天' : '-' },
            { label: '下次经期', value: overview.nextPredictedStart || '-' }
        ]
        prediction.value = {
            nextPeriodStart: overview.nextPredictedStart || '-',
            nextPeriodEnd: overview.nextPredictedEnd || '-',
            predictedCycleLength: overview.avgCycleLength ? Math.round(overview.avgCycleLength) : '-',
            predictedPeriodLength: overview.avgPeriodLength ? Math.round(overview.avgPeriodLength) : '-',
            ovulationStart: overview.ovulationStart || '-',
            ovulationEnd: overview.ovulationEnd || '-',
            fertileWindowStart: overview.fertileWindowStart || '-',
            fertileWindowEnd: overview.fertileWindowEnd || '-',
            safePeriodStart: overview.safePeriodStart || '-',
            safePeriodEnd: overview.safePeriodEnd || '-',
            confidence: '中'
        }
    } catch (e) {}
    try { reminders.value = await axios.get('/predictions/reminders?advanceDays=7') } catch (e) {}
})
</script>
