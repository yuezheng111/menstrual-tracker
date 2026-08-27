<template>
    <div class="profile-page">
        <div class="profile-card cute-card" style="padding:24px;text-align:center">
            <div style="font-size:60px;margin-bottom:8px;animation:float 3s ease-in-out infinite">👸</div>
            <div style="font-size:20px;font-weight:700;color:var(--text-primary)">{{ profile.username || '用户' }}</div>
        </div>

        <div style="text-align:center;margin-top:24px;padding-bottom:20px">
            <div style="font-size:12px;color:var(--text-muted);margin-bottom:8px">小月历 v1.0.0</div>
            <div style="font-size:12px;color:var(--text-muted);cursor:pointer" @click="logout">退出登录</div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../api'

const router = useRouter()
const profile = reactive({ username: '' })

onMounted(async () => { try { const d = await axios.get('/auth/profile'); Object.assign(profile, d) } catch (e) {} })

const logout = () => {
    localStorage.removeItem('token'); localStorage.removeItem('username')
    router.push('/login')
}
</script>

<style scoped>
</style>
