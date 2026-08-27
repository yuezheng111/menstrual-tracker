
<template>
    <div class="dashboard">
        <div class="cycle-card cute-card animate-fade-in-up" style="padding:24px;margin-bottom:16px;position:relative;overflow:hidden">
            <div style="position:absolute;top:-20px;right:-10px;font-size:80px;opacity:0.08;pointer-events:none">&#x1F338;</div>
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
                <div>
                    <div style="font-size:13px;color:var(--text-secondary)">当前周期状态</div>
                    <div style="font-size:24px;font-weight:700;color:var(--text-primary);margin-top:4px">{{ cycleStatus }}</div>
                </div>
                <div style="font-size:40px;animation:float 3s ease-in-out infinite">&#x1F338;</div>
            </div>
            <div style="font-size:36px;font-weight:800;color:var(--pink-500)" class="gradient-text">
                {{ stats[0].value }}
            </div>
            <div style="font-size:14px;color:var(--text-secondary);margin-top:2px">{{ stats[0].label }}</div>
        </div>

        <div class="stats-grid">
            <div v-for="(s, i) in stats.slice(1)" :key="i"
                 class="cute-card stat-item animate-fade-in-up"
                 :style="{ animationDelay: (0.1 * i) + 's' }">
                <div class="stat-value">{{ s.value }}</div>
                <div class="stat-label">{{ s.label }}</div>
            </div>
        </div>

        <div v-if="prediction" class="cute-card" style="padding:20px;margin-top:16px">
            <div style="font-size:15px;font-weight:600;color:var(--text-primary);margin-bottom:12px">&#x1F514; 预测</div>
            <div class="pred-row"><span class="pred-label">下次经期</span><span class="pred-val">{{ prediction.nextPeriodStart }} ~ {{ prediction.nextPeriodEnd }}</span></div>
            <div class="pred-row"><span class="pred-label">排卵窗口</span><span class="pred-val">{{ prediction.ovulationStart }} ~ {{ prediction.ovulationEnd }}</span></div>
            <div class="pred-row"><span class="pred-label">易孕期</span><span class="pred-val">{{ prediction.fertileWindowStart }} ~ {{ prediction.fertileWindowEnd }}</span></div>
            <div class="pred-row"><span class="pred-label">安全期</span><span class="pred-val">{{ prediction.safePeriodStart }} ~ {{ prediction.safePeriodEnd }}</span></div>
        </div>

        <div class="cute-card" style="padding:20px;margin-top:16px">
            <div style="font-size:15px;font-weight:600;color:var(--text-primary);margin-bottom:12px">&#x23F0; 提醒</div>
            <div v-if="reminders.length > 0">
                <div v-for="r in reminders" :key="r.date" class="reminder-item">
                    <div class="reminder-dot"></div>
                    <div><div style="font-size:14px;font-weight:500">{{ r.title }}</div><div style="font-size:12px;color:var(--text-secondary)">{{ r.description }}</div></div>
                </div>
            </div>
            <div v-else style="color:var(--text-muted);font-size:13px;text-align:center;padding:16px">暂无提醒</div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from '../api'

const stats = ref([{ label: '总记录', value: '-' }, { label: '平均周期', value: '-' }, { label: '平均经期', value: '-' }, { label: '下次经期', value: '-' }])
const prediction = ref(null)
const reminders = ref([])

const cycleStatus = computed(() => {
    if (!prediction.value) return '暂无数据'
    const today = new Date()
    const next = new Date(prediction.value.nextPeriodStart)
    const diff = Math.round((next - today) / (1000*60*60*24))
    if (diff < 0) return '经期中 &#x1F49D;'
    if (diff <= 3) return '即将到来 &#x23F3;'
    return '安全期 &#x1F33F;'
})

onMounted(async () => {
    try {
        const o = await axios.get('/statistics/overview')
        stats.value = [
            { label: '总记录', value: o.totalRecords || 0 },
            { label: '平均周期', value: o.avgCycleLength ? o.avgCycleLength.toFixed(1) + '天' : '-' },
            { label: '平均经期', value: o.avgPeriodLength ? o.avgPeriodLength.toFixed(1) + '天' : '-' },
            { label: '下次经期', value: o.nextPredictedStart || '-' }
        ]
        if (o.nextPredictedStart) {
            prediction.value = {
                nextPeriodStart: o.nextPredictedStart, nextPeriodEnd: o.nextPredictedEnd || '-',
                ovulationStart: o.ovulationStart || '-', ovulationEnd: o.ovulationEnd || '-',
                fertileWindowStart: o.fertileWindowStart || '-', fertileWindowEnd: o.fertileWindowEnd || '-',
                safePeriodStart: o.safePeriodStart || '-', safePeriodEnd: o.safePeriodEnd || '-'
            }
        }
    } catch (e) {}
    try { reminders.value = await axios.get('/predictions/reminders?advanceDays=7') } catch (e) {}
})
</script>

<style scoped>
.stats-grid { display:grid; grid-template-columns:1fr 1fr 1fr; gap:10px; }
.stat-item { padding:16px; text-align:center; }
.stat-value { font-size:20px; font-weight:700; color:var(--pink-500); margin-bottom:4px; }
.stat-label { font-size:12px; color:var(--text-secondary); }
.pred-row { display:flex; justify-content:space-between; padding:6px 0; border-bottom:1px solid #fce4ec; font-size:14px; }
.pred-label { color:var(--text-secondary); }
.pred-val { color:var(--text-primary); font-weight:500; }
.reminder-item { display:flex; align-items:center; gap:12px; padding:8px 0; border-bottom:1px solid #fce4ec; }
.reminder-item:last-child { border-bottom:none; }
.reminder-dot { width:8px; height:8px; border-radius:50%; background:var(--pink-400); flex-shrink:0; }
</style>
