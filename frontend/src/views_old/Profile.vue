<template>
    <el-row :gutter="20">
        <el-col :span="8">
            <el-card shadow="hover" style="text-align: center">
                <el-avatar :size="100" icon="UserFilled" style="margin: 20px auto" />
                <h3>{{ profile.username || '用户' }}</h3>
                <p style="color: #999">{{ profile.email || '未设置邮箱' }}</p>
            </el-card>
        </el-col>
        <el-col :span="16">
            <el-card shadow="hover">
                <template #header><span>个人设置</span></template>
                <el-form :model="profile" label-width="140px">
                    <el-form-item label="用户名"><el-input v-model="profile.username" disabled /></el-form-item>
                    <el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item>
                    <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
                    <el-form-item label="出生日期"><el-date-picker v-model="profile.birthDate" type="date" /></el-form-item>
                    <el-form-item label="初潮年龄"><el-input-number v-model="profile.menarcheAge" :min="8" :max="20" /></el-form-item>
                    <el-form-item label="平均周期天数"><el-input-number v-model="profile.avgCycleDays" :min="20" :max="45" /></el-form-item>
                    <el-form-item label="平均经期天数"><el-input-number v-model="profile.avgPeriodDays" :min="1" :max="10" /></el-form-item>
                    <el-form-item>
                        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
                    </el-form-item>
                </el-form>
            </el-card>
        </el-col>
    </el-row>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from '../api'
import { ElMessage } from 'element-plus'

const profile = reactive({ username: '', phone: '', email: '', birthDate: '', menarcheAge: null, avgCycleDays: 28, avgPeriodDays: 5 })
const saving = ref(false)

onMounted(async () => {
    try { const data = await axios.get('/auth/profile'); Object.assign(profile, data) } catch (e) {}
})

const saveProfile = async () => {
    saving.value = true
    try {
        await axios.put('/auth/profile', profile)
        ElMessage.success('个人信息已更新')
    } catch (e) {}
    saving.value = false
}
</script>
