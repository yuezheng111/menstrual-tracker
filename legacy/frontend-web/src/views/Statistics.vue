
<template>
    <div class="stats-page">
        <div v-if="overview" class="stats-content">
            <div class="stats-cards">
                <div v-for="(s, i) in statCards" :key="i" class="cute-card stat-card animate-fade-in-up" :style="{animationDelay:i*0.1+'s'}">
                    <div class="sc-value">{{ s.value }}</div>
                    <div class="sc-label">{{ s.label }}</div>
                </div>
            </div>

            <div class="cute-card" style="padding:20px;margin-top:16px">
                <div style="font-size:15px;font-weight:600;margin-bottom:12px">&#x1F52D; 预测信息</div>
                <div class="pred-row"><span class="pred-label">下次经期</span><span class="pred-val">{{ overview.nextPredictedStart || '-' }} ~ {{ overview.nextPredictedEnd || '-' }}</span></div>
                <div class="pred-row"><span class="pred-label">排卵日</span><span class="pred-val">{{ overview.ovulationStart || '-' }} ~ {{ overview.ovulationEnd || '-' }}</span></div>
                <div class="pred-row"><span class="pred-label">易孕期</span><span class="pred-val">{{ overview.fertileWindowStart || '-' }} ~ {{ overview.fertileWindowEnd || '-' }}</span></div>
                <div class="pred-row"><span class="pred-label">安全期</span><span class="pred-val">{{ overview.safePeriodStart || '-' }} ~ {{ overview.safePeriodEnd || '-' }}</span></div>
            </div>

            <div class="cute-card" style="padding:20px;margin-top:16px">
                <div style="font-size:15px;font-weight:600;margin-bottom:12px">&#x1F4C8; 近期周期</div>
                <div v-if="overview.recentCycles && overview.recentCycles.length > 0">
                    <div v-for="c in overview.recentCycles" :key="c.startDate" class="cycle-row">
                        <span class="cycle-date">{{ c.startDate }}</span>
                        <span class="cycle-dur">{{ c.periodLength || '-' }}天</span>
                        <span v-if="c.cycleLength" class="cycle-len">{{ c.cycleLength }}天周期</span>
                    </div>
                </div>
                <div v-else style="color:var(--text-muted);font-size:13px;text-align:center;padding:16px">暂无数据</div>
            </div>

            <div class="cute-card" style="padding:20px;margin-top:16px">
                <div style="font-size:15px;font-weight:600;margin-bottom:12px">&#x1F4AA; 常见症状</div>
                <div v-if="overview.topSymptoms && overview.topSymptoms.length > 0">
                    <div v-for="s in overview.topSymptoms" :key="s.name" class="symp-row">
                        <div style="flex:1">
                            <div style="font-size:14px;font-weight:500;color:var(--text-primary)">{{ s.name }}</div>
                            <div style="height:8px;border-radius:4px;background:#fce4ec;margin-top:6px;overflow:hidden">
                                <div :style="{width:s.percentage+'%',height:'100%',background:'linear-gradient(90deg,var(--pink-300),var(--pink-500))',borderRadius:'4px',transition:'width 0.8s'}"></div>
                            </div>
                        </div>
                        <span style="font-size:12px;color:var(--text-secondary);margin-left:12px">{{ s.count }}次</span>
                    </div>
                </div>
                <div v-else style="color:var(--text-muted);font-size:13px;text-align:center;padding:16px">暂无数据</div>
            </div>
        </div>
        <div v-else style="text-align:center;padding:60px;color:var(--text-muted)">
            <div style="font-size:48px;margin-bottom:12px">&#x1F4CA;</div>
            <div>暂无数据</div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '../api'

const overview = ref(null)
const statCards = computed(() => [
    { label: '总记录', value: overview.value?.totalRecords || 0 },
    { label: '总周期', value: overview.value?.totalCycles || 0 },
    { label: '平均周期', value: overview.value?.avgCycleLength ? overview.value.avgCycleLength.toFixed(1) + '天' : '-' },
    { label: '平均经期', value: overview.value?.avgPeriodLength ? overview.value.avgPeriodLength.toFixed(1) + '天' : '-' },
])

onMounted(async () => { try { overview.value = await axios.get('/statistics/overview') } catch (e) {} })
</script>

<style scoped>
.stats-cards { display:grid; grid-template-columns:1fr 1fr; gap:10px; }
.stat-card { padding:20px; text-align:center; }
.sc-value { font-size:24px; font-weight:700; color:var(--pink-500); }
.sc-label { font-size:12px; color:var(--text-secondary); margin-top:4px; }
.pred-row { display:flex; justify-content:space-between; padding:6px 0; border-bottom:1px solid #fce4ec; font-size:14px; }
.pred-label { color:var(--text-secondary); }
.pred-val { color:var(--text-primary); font-weight:500; }
.cycle-row { display:flex; align-items:center; gap:10px; padding:8px 0; border-bottom:1px solid #fce4ec; }
.cycle-date { font-size:14px; font-weight:500; color:var(--text-primary); }
.cycle-dur { font-size:12px; color:var(--pink-500); }
.cycle-len { font-size:12px; color:var(--text-secondary); }
.symp-row { display:flex; align-items:center; padding:8px 0; }
</style>
