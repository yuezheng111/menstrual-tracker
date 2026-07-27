<template>
    <div>
        <el-card shadow="hover">
            <template #header>
                <div style="display: flex; justify-content: space-between; align-items: center">
                    <span>经期记录</span>
                    <el-button type="primary" @click="openDialog()">+ 新增记录</el-button>
                </div>
            </template>
            <el-table :data="records" stripe v-loading="loading" style="width: 100%">
                <el-table-column prop="startDate" label="开始日期" width="140" />
                <el-table-column prop="endDate" label="结束日期" width="140" />
                <el-table-column prop="cycleDay" label="周期第几天" width="110" />
                <el-table-column prop="flow" label="经量" width="100">
                    <template #default="scope">
                        <el-tag :type="flowTagType(scope.row.flow)" size="small">{{ flowLabel(scope.row.flow) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="painLevel" label="痛经" width="100">
                    <template #default="scope">
                        <el-tag :type="painTagType(scope.row.painLevel)" size="small">{{ painLabel(scope.row.painLevel) }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="symptoms" label="症状" min-width="200">
                    <template #default="scope">
                        <el-tag v-for="s in (scope.row.symptoms || [])" :key="s" size="small" style="margin: 2px">{{ s }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                    <template #default="scope">
                        <el-button size="small" @click="editRecord(scope.row)">编辑</el-button>
                        <el-button size="small" type="danger" @click="deleteRecord(scope.row.id)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-pagination
                v-model:page="query.page"
                :page-size="query.size"
                :total="total"
                layout="prev, pager, next"
                style="margin-top: 20px; justify-content: center"
                @current-change="loadRecords"
            />
        </el-card>

        <el-dialog v-model="showDialog" :title="editingId ? '编辑记录' : '新增记录'" width="500px">
            <el-form :model="form" label-width="100px">
                <el-form-item label="开始日期" required>
                    <el-date-picker v-model="form.startDate" type="date" placeholder="选择日期" style="width: 100%" />
                </el-form-item>
                <el-form-item label="结束日期">
                    <el-date-picker v-model="form.endDate" type="date" placeholder="选择日期" style="width: 100%" />
                </el-form-item>
                <el-form-item label="经量">
                    <el-select v-model="form.flow" style="width: 100%">
                        <el-option label="少" value="LIGHT" />
                        <el-option label="中" value="MEDIUM" />
                        <el-option label="多" value="HEAVY" />
                    </el-select>
                </el-form-item>
                <el-form-item label="痛经程度">
                    <el-select v-model="form.painLevel" style="width: 100%">
                        <el-option label="无" value="NONE" />
                        <el-option label="轻微" value="MILD" />
                        <el-option label="中等" value="MODERATE" />
                        <el-option label="严重" value="SEVERE" />
                    </el-select>
                </el-form-item>
                <el-form-item label="症状">
                    <el-select v-model="form.symptoms" multiple filterable allow-create default-first-option style="width: 100%">
                        <el-option v-for="s in symptomOptions" :key="s" :label="s" :value="s" />
                    </el-select>
                </el-form-item>
                <el-form-item label="情绪">
                    <el-select v-model="form.moodTags" multiple filterable allow-create default-first-option style="width: 100%">
                        <el-option v-for="m in moodOptions" :key="m" :label="m" :value="m" />
                    </el-select>
                </el-form-item>
                <el-form-item label="血块">
                    <el-switch v-model="form.clots" />
                </el-form-item>
                <el-form-item label="备注">
                    <el-input v-model="form.notes" type="textarea" :rows="2" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="showDialog = false">取消</el-button>
                <el-button type="primary" :loading="saving" @click="saveRecord">保存</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref([])
const total = ref(0)
const loading = ref(false)
const showDialog = ref(false)
const editingId = ref(null)
const saving = ref(false)

const query = reactive({ page: 1, size: 20 })
const form = reactive({ startDate: '', endDate: '', flow: '', painLevel: '', symptoms: [], moodTags: [], clots: false, notes: '' })

const symptomOptions = ['头痛', '笮劳', '腹胀', '腹痛', '背痛', '恶心', '头晕', '长痘', '乳房胀痛', '失眠']
const moodOptions = ['开心', '难过', '焦虑', '易怒', '平静', '精力充沛', '情绪化', '笮惫', '专注', '压力大']

const flowLabel = (v) => ({ LIGHT: '少', MEDIUM: '中', HEAVY: '多' }[v] || v)
const painLabel = (v) => ({ NONE: '无', MILD: '轻微', MODERATE: '中等', SEVERE: '严重' }[v] || v)
const flowTagType = (flow) => ({ LIGHT: 'success', MEDIUM: 'warning', HEAVY: 'danger' }[flow] || 'info')
const painTagType = (pain) => ({ NONE: 'success', MILD: 'info', MODERATE: 'warning', SEVERE: 'danger' }[pain] || 'info')

const loadRecords = async () => {
    loading.value = true
    try {
        const res = await axios.get('/records', { params: query })
        records.value = res.content
        total.value = res.totalElements
    } catch (e) {}
    loading.value = false
}

const openDialog = () => {
    editingId.value = null
    form.startDate = ''; form.endDate = ''; form.flow = ''
    form.painLevel = ''; form.symptoms = []; form.moodTags = []
    form.clots = false; form.notes = ''
    showDialog.value = true
}

const editRecord = (row) => {
    editingId.value = row.id
    form.startDate = row.startDate; form.endDate = row.endDate
    form.flow = row.flow; form.painLevel = row.painLevel
    form.symptoms = row.symptoms || []; form.moodTags = row.moodTags || []
    form.clots = row.clots || false; form.notes = row.notes
    showDialog.value = true
}

const saveRecord = async () => {
    saving.value = true
    try {
        if (editingId.value) {
            await axios.put('/records/' + editingId.value, form)
            ElMessage.success('记录已更新')
        } else {
            await axios.post('/records', form)
            ElMessage.success('记录已创建')
        }
        showDialog.value = false
        loadRecords()
    } catch (e) {}
    saving.value = false
}

const deleteRecord = (id) => {
    ElMessageBox.confirm('确定删除这条记录吗？', '确认', { type: 'warning' }).then(async () => {
        await axios.delete('/records/' + id)
        ElMessage.success('记录已删除')
        loadRecords()
    }).catch(() => {})
}

onMounted(loadRecords)
</script>
