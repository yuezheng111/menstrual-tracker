
<template>
    <div class="profile-page">
        <div class="profile-card cute-card" style="padding:24px;text-align:center">
            <div style="font-size:60px;margin-bottom:8px;animation:float 3s ease-in-out infinite">&#x1F478;</div>
            <div style="font-size:20px;font-weight:700;color:var(--text-primary)">{{ profile.username || '用户' }}</div>
            <div style="font-size:13px;color:var(--text-secondary);margin-top:4px">{{ profile.email || '未设置邮箱' }}</div>
        </div>

        <div class="cute-card" style="padding:20px;margin-top:16px">
            <div style="font-size:15px;font-weight:600;margin-bottom:16px">&#x2699;&#xFE0F; 个人设置</div>
            <label class="form-label">手机号</label>
            <input v-model="profile.phone" class="cute-input" placeholder="手机号" />
            <label class="form-label">邮箱</label>
            <input v-model="profile.email" class="cute-input" placeholder="邮箱" />
            <label class="form-label">出生日期</label>
            <input type="date" v-model="profile.birthDate" class="cute-input" />
            <label class="form-label">初潮年龄</label>
            <input type="number" v-model.number="profile.menarcheAge" class="cute-input" min="8" max="20" />
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-top:10px">
                <div><label class="form-label">平均周期</label><input type="number" v-model.number="profile.avgCycleDays" class="cute-input" min="20" max="45" /></div>
                <div><label class="form-label">平均经期</label><input type="number" v-model.number="profile.avgPeriodDays" class="cute-input" min="1" max="10" /></div>
            </div>
            <button class="btn-pink btn-full" style="margin-top:16px" :disabled="saving" @click="saveProfile">{{ saving ? '保存中...' : '保存设置' }}</button>
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
const profile = reactive({ username: '', phone: '', email: '', birthDate: '', menarcheAge: null, avgCycleDays: 28, avgPeriodDays: 5 })
const saving = ref(false)

const notify = (m) => {
    const e = document.createElement('div'); e.textContent = m
    e.style.cssText = 'position:fixed;top:40%;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.75);color:white;padding:10px 20px;border-radius:20px;font-size:14px;z-index:9999'
    document.body.appendChild(e); setTimeout(() => e.remove(), 2000)
}

onMounted(async () => { try { const d = await axios.get('/auth/profile'); Object.assign(profile, d) } catch (e) {} })

const saveProfile = async () => {
    saving.value = true
    try { await axios.put('/auth/profile', profile); notify('设置已保存') } catch (e) { notify('保存失败') }
    saving.value = false
}

const logout = () => {
    localStorage.removeItem('token'); localStorage.removeItem('username')
    router.push('/login')
}
</script>

<style scoped>
.form-label { font-size:13px; font-weight:500; color:var(--text-secondary); display:block; margin-bottom:4px; margin-top:10px; }
.cute-input { width:100%; padding:12px 14px; border:2px solid #fce4ec; border-radius:12px; font-size:14px; outline:none; transition:all 0.3s; margin-bottom:6px; background:#fff; }
.cute-input:focus { border-color:#f06292; }
.btn-full { width:100%; }
</style>
