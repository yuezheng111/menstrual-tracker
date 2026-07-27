
<template>
    <div class="records-page">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
            <div style="font-size:13px;color:var(--text-secondary)">共 {{ total }} 条记录</div>
        </div>
        <div v-if="loading" style="text-align:center;padding:40px;color:var(--text-muted)">加载中...</div>
        <div v-else-if="records.length === 0" style="text-align:center;padding:60px 20px;color:var(--text-muted)">
            <div style="font-size:48px;margin-bottom:12px">&#x1F4AD;</div>
            <div>还没有记录</div>
            <div style="font-size:13px;margin-top:4px">点击下方按钮开始记录吧</div>
        </div>
        <div v-else class="records-list">
            <div v-for="r in records" :key="r.id" class="record-card cute-card animate-fade-in-up" @click="editRecord(r)">
                <div class="record-header">
                    <div>
                        <span class="record-date">{{ r.startDate }}</span>
                        <span v-if="r.endDate" class="record-date-sep">~ {{ r.endDate }}</span>
                        <span class="record-cycle" v-if="r.cycleDay">第{{ r.cycleDay }}天</span>
                    </div>
                    <div style="display:flex;gap:6px">
                        <span class="badge" :class="'badge-' + (r.flow || '').toLowerCase()">{{ flowLabel(r.flow) }}</span>
                        <span class="badge" :class="'badge-' + (r.painLevel || '').toLowerCase()">{{ painLabel(r.painLevel) }}</span>
                    </div>
                </div>
                <div v-if="r.symptoms && r.symptoms.length" class="record-tags">
                    <span v-for="s in r.symptoms" :key="s" class="tag">{{ s }}</span>
                </div>
                <div class="record-actions">
                    <span style="color:var(--pink-400);font-size:12px" @click.stop="deleteRecord(r.id)">删除</span>
                </div>
            </div>
        </div>

        <button class="fab" @click="openDialog()">+</button>

        <div v-if="showDialog" class="dialog-overlay" @click.self="showDialog=false">
            <div class="dialog-card cute-card animate-fade-in-up" style="padding:24px;width:90%;max-width:360px;margin:auto">
                <div style="font-size:18px;font-weight:700;color:var(--text-primary);margin-bottom:16px;text-align:center">
                    {{ editingId ? '编辑记录' : '新增记录' }}
                </div>
                <label class="form-label">开始日期</label>
                <input type="date" v-model="form.startDate" class="cute-input" />
                <label class="form-label">结束日期</label>
                <input type="date" v-model="form.endDate" class="cute-input" />
                <label class="form-label">经量</label>
                <div class="chip-group">
                    <span v-for="o in flowOpts" :key="o.v" :class="['chip', { active: form.flow === o.v }]" @click="form.flow = o.v">{{ o.l }}</span>
                </div>
                <label class="form-label">痛经程度</label>
                <div class="chip-group">
                    <span v-for="o in painOpts" :key="o.v" :class="['chip', { active: form.painLevel === o.v }]" @click="form.painLevel = o.v">{{ o.l }}</span>
                </div>
                <label class="form-label">症状</label>
                <div class="chip-group">
                    <span v-for="s in symptomOptions" :key="s" :class="['chip', { active: hasTag(form.symptoms, s) }]" @click="toggleTag(form.symptoms, s)">{{ s }}</span>
                </div>
                <label class="form-label">情绪</label>
                <div class="chip-group">
                    <span v-for="m in moodOptions" :key="m" :class="['chip', { active: hasTag(form.moodTags, m) }]" @click="toggleTag(form.moodTags, m)">{{ m }}</span>
                </div>
                <label class="form-label">血块 <input type="checkbox" v-model="form.clots" style="margin-left:8px" /></label>
                <label class="form-label">备注</label>
                <textarea v-model="form.notes" class="cute-input" rows="2" style="resize:none"></textarea>
                <div style="display:flex;gap:10px;margin-top:16px">
                    <button class="btn-pink" style="flex:1;padding:12px" @click="showDialog=false">取消</button>
                    <button class="btn-pink" style="flex:2;padding:12px" :disabled="saving" @click="saveRecord">{{ saving ? '保存中...' : '保存' }}</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '../api'
import { ElMessageBox } from 'element-plus'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const showDialog = ref(false)
const editingId = ref(null)
const saving = ref(false)
const query = reactive({ page: 1, size: 50 })
const form = reactive({ startDate: '', endDate: '', flow: '', painLevel: '', symptoms: [], moodTags: [], clots: false, notes: '' })
const flowOpts = [{v:'LIGHT',l:'少'},{v:'MEDIUM',l:'中'},{v:'HEAVY',l:'多'}]
const painOpts = [{v:'NONE',l:'无'},{v:'MILD',l:'轻微'},{v:'MODERATE',l:'中等'},{v:'SEVERE',l:'严重'}]
const symptomOptions = ['头痛','疲劳','腹胀','腹痛','背痛','恶心','头晕','长痘','乳房胀痛','失眠']
const moodOptions = ['开心','难过','焦虑','易怒','平静','精力充沛','情绪化','疲惫','专注','压力大']
const flowLabel = (v) => ({LIGHT:'少',MEDIUM:'中',HEAVY:'多'}[v]||v||'-')
const painLabel = (v) => ({NONE:'无',MILD:'轻微',MODERATE:'中等',SEVERE:'严重'}[v]||v||'-')
const toggleTag = (arr, v) => { const i = arr.indexOf(v); i>=0 ? arr.splice(i,1) : arr.push(v) }


const hasTag = (arr, val) => Array.isArray(arr) && arr.indexOf(val) !== -1
const notify = (m) => {
    const e = document.createElement('div'); e.textContent = m
    e.style.cssText = 'position:fixed;top:40%;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.75);color:white;padding:10px 20px;border-radius:20px;font-size:14px;z-index:9999'
    document.body.appendChild(e); setTimeout(() => e.remove(), 2000)
}

const loadRecords = async () => {
    loading.value = true
    try { const res = await axios.get('/records', { params: query }); records.value = res.content; total.value = res.totalElements } catch (e) {}
    loading.value = false
}

const openDialog = () => {
    editingId.value = null; form.startDate = ''; form.endDate = ''; form.flow = ''; form.painLevel = ''; form.symptoms = []; form.moodTags = []; form.clots = false; form.notes = ''; showDialog.value = true
}

const editRecord = (r) => {
    editingId.value = r.id; form.startDate = (r.startDate||'').substring(0,10); form.endDate = (r.endDate||'').substring(0,10); form.flow = r.flow; form.painLevel = r.painLevel; form.symptoms = (r.symptoms||[]).map(s => String(s)); form.moodTags = (r.moodTags||[]).map(m => String(m)); form.clots = r.clots || false; form.notes = r.notes; showDialog.value = true
}

const saveRecord = async () => {
    saving.value = true
    try {
        editingId.value ? await axios.put('/records/' + editingId.value, form) : await axios.post('/records', form)
        notify(editingId.value ? '记录已更新' : '记录已创建'); showDialog.value = false; loadRecords()
    } catch (e) { notify('保存失败') }
    saving.value = false
}

const deleteRecord = (id) => {
    ElMessageBox.confirm('确定删除这条记录吗？', '确认', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
        .then(async () => { await axios.delete('/records/' + id); notify('已删除'); loadRecords() })
        .catch(() => {})
}

onMounted(loadRecords)
</script>

<style scoped>
.record-card { padding:16px; margin-bottom:12px; cursor:pointer; }
.record-header { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:8px; }
.record-date { font-size:15px; font-weight:600; color:var(--text-primary); }
.record-date-sep { font-size:13px; color:var(--text-secondary); margin-left:4px; }
.record-cycle { font-size:12px; color:var(--pink-400); background:#fce4ec; border-radius:8px; padding:2px 8px; margin-left:8px; }
.badge { font-size:11px; padding:2px 8px; border-radius:8px; font-weight:500; }
.badge-light { background:#e8f5e9; color:#2e7d32; }
.badge-medium { background:#fff3e0; color:#e65100; }
.badge-heavy { background:#fce4ec; color:#c62828; }
.badge-none { background:#f3e5f5; color:#6a1b9a; }
.badge-mild { background:#e3f2fd; color:#1565c0; }
.badge-moderate { background:#fff3e0; color:#e65100; }
.badge-severe { background:#fce4ec; color:#c62828; }
.record-tags { margin-bottom:8px; }
.tag { display:inline-block; font-size:11px; padding:2px 8px; border-radius:8px; background:#fdf2f8; color:var(--pink-500); margin:2px; }
.record-actions { display:flex; justify-content:flex-end; }
.fab { position:fixed; bottom:90px; left:50%; transform:translateX(-50%); width:56px; height:56px; border-radius:50%; background:linear-gradient(135deg,#f06292,#d81b60); color:white; font-size:28px; border:none; box-shadow:0 4px 16px rgba(216,27,96,0.4); cursor:pointer; z-index:99; transition:all 0.3s; display:flex; align-items:center; justify-content:center; margin-left:160px; }
.fab:active { transform:translateX(-50%) scale(0.92); }
.dialog-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.4); display:flex; align-items:center; justify-content:center; z-index:200; backdrop-filter:blur(4px); }
.form-label { font-size:13px; font-weight:500; color:var(--text-secondary); display:block; margin-bottom:4px; margin-top:10px; }
.chip-group { display:flex; flex-wrap:wrap; gap:8px; margin-bottom:4px; }
.chip { padding:6px 14px; border-radius:16px; font-size:13px; background:#fdf2f8; color:var(--text-secondary); cursor:pointer; transition:all 0.2s; border:1px solid transparent; }
.chip.active { background:var(--pink-400); color:white; border-color:var(--pink-500); }
.cute-input { width:100%; padding:12px 14px; border:2px solid #fce4ec; border-radius:12px; font-size:14px; outline:none; transition:all 0.3s; margin-bottom:6px; background:#fff; }
.cute-input:focus { border-color:#f06292; }
</style>


