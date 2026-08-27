<template>
    <el-row :gutter="20">
        <el-col :span="8">
            <el-card shadow="hover" style="text-align: center">
                <el-avatar :size="100" icon="UserFilled" style="margin: 20px auto" />
                <h3>{{ profile.username || '用户' }}</h3>
            </el-card>
        </el-col>
    </el-row>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import axios from '../api'

const profile = reactive({ username: '' })

onMounted(async () => {
    try { const data = await axios.get('/auth/profile'); Object.assign(profile, data) } catch (e) {}
})
</script>
