
<template>
    <div class="login-page">
        <div class="login-bg">
            <div class="heart" v-for="h in hearts" :key="h.id"
                 :style="{ left: h.x + '%', animationDelay: h.delay + 's', fontSize: h.size + 'px' }">
                {{ h.emoji }}
            </div>
        </div>
        <div class="login-content">
            <div class="logo-area animate-fade-in-up">
                <div class="logo-icon">🌸</div>
                <h1 class="app-name">小月历</h1>
                <p class="app-desc">温柔记录，智能预测</p>
            </div>
            <div class="cute-card login-card animate-fade-in-up" style="animation-delay:0.15s">
                <div class="tab-bar">
                    <span :class="['tab', { active: activeTab === 'login' }]" @click="activeTab = 'login'">登录</span>
                    <span :class="['tab', { active: activeTab === 'register' }]" @click="activeTab = 'register'">注册</span>
                </div>
                <div v-if="activeTab === 'login'">
                    <input v-model="loginForm.username" class="cute-input" placeholder="用户名" />
                    <input v-model="loginForm.password" type="password" class="cute-input" placeholder="密码" />
                    <button class="btn-pink btn-full" :disabled="loading" @click="handleLogin">
                        {{ loading ? '登录中...' : '登录' }}
                    </button>
                </div>
                <div v-else>
                    <input v-model="registerForm.username" class="cute-input" placeholder="用户名" />
                    <input v-model="registerForm.password" type="password" class="cute-input" placeholder="密码" />
                    <button class="btn-pink btn-full" :disabled="loading" @click="handleRegister">
                        {{ loading ? '注册中...' : '注册' }}
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../api'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '' })

const hearts = Array.from({ length: 8 }, (_, i) => ({
    id: i, x: Math.random() * 100,
    delay: Math.random() * 3,
    size: 18 + Math.random() * 16
}))

const notify = (msg) => {
    const el = document.createElement('div')
    el.className = 'toast-msg'; el.textContent = msg
    el.style.cssText = 'position:fixed;top:40%;left:50%;transform:translateX(-50%);background:rgba(0,0,0,0.75);color:white;padding:10px 20px;border-radius:20px;font-size:14px;z-index:9999;animation:fadeInUp 2s forwards'
    document.body.appendChild(el)
    setTimeout(() => el.remove(), 2000)
}

const handleLogin = async () => {
    loading.value = true
    try {
        const res = await axios.post('/auth/login', loginForm)
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.user.username)
        router.push('/dashboard')
    } catch (e) { notify('登录失败，请检查用户名和密码') }
    loading.value = false
}

const handleRegister = async () => {
    loading.value = true
    try {
        const res = await axios.post('/auth/register', registerForm)
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.user.username)
        router.push('/dashboard')
    } catch (e) { notify('注册失败，用户名可能已存在') }
    loading.value = false
}
</script>

<style scoped>
.login-page {
    min-height: 100vh;

    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    position: relative;
    overflow: hidden;
}
.login-bg { position: absolute; inset: 0; pointer-events: none; }
.heart {
    position: absolute; bottom: -20px;
    animation: heart-float 4s ease-in infinite;
    opacity: 0.5;
}
.login-content {
    width: 100%; max-width: 360px;
    display: flex; flex-direction: column; align-items: center;
    position: relative; z-index: 1;
}
.logo-area { text-align: center; margin-bottom: 28px; }
.logo-icon { font-size: 52px; animation: float 3s ease-in-out infinite; }
.app-name { font-size: 32px; font-weight: 800; color: #4a154b; margin: 8px 0 4px; letter-spacing: 2px; }
.app-desc { color: #8e5580; font-size: 14px; }
.login-card { width: 100%; padding: 24px; }
.tab-bar { display: flex; margin-bottom: 20px; border-radius: 12px; background: #fdf2f8; padding: 3px; }
.tab { flex: 1; text-align: center; padding: 10px; border-radius: 10px; cursor: pointer; font-weight: 600; font-size: 15px; color: #c89bb8; transition: all 0.3s; }
.tab.active { background: linear-gradient(135deg, #f06292, #d81b60); color: white; box-shadow: 0 2px 8px rgba(236,64,122,0.3); }
.cute-input {
    width: 100%; padding: 14px 16px; border: 2px solid #fce4ec; border-radius: 12px;
    font-size: 15px; outline: none; transition: all 0.3s; margin-bottom: 12px; background: #fff;
}
.cute-input:focus { border-color: #f06292; box-shadow: 0 0 0 3px rgba(240,98,146,0.15); }
.cute-input::placeholder { color: #c89bb8; }
.btn-full { width: 100%; margin-top: 8px; }

@keyframes heart-float {
    0% { transform: translateY(0) scale(1); opacity: 0.6; }
    100% { transform: translateY(-100vh) scale(0.4); opacity: 0; }
}
@keyframes fadeInUp {
    0% { opacity: 0; transform: translateX(-50%) translateY(10px); }
    15% { opacity: 1; transform: translateX(-50%) translateY(0); }
    85% { opacity: 1; transform: translateX(-50%) translateY(0); }
    100% { opacity: 0; transform: translateX(-50%) translateY(-10px); }
}
</style>
