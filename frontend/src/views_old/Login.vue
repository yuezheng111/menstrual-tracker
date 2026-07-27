<template>
    <div style="height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
        <el-card style="width: 400px; padding: 20px">
            <h2 style="text-align: center; margin-bottom: 30px; color: #409EFF">经期记录助手</h2>
            <el-tabs v-model="activeTab" style="margin-bottom: 20px">
                <el-tab-pane label="登录" name="login">
                    <el-form :model="loginForm" label-width="0">
                        <el-form-item><el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" /></el-form-item>
                        <el-form-item><el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password /></el-form-item>
                        <el-form-item><el-button type="primary" :loading="loading" style="width: 100%" @click="handleLogin">登录</el-button></el-form-item>
                    </el-form>
                </el-tab-pane>
                <el-tab-pane label="注册" name="register">
                    <el-form :model="registerForm" label-width="0">
                        <el-form-item><el-input v-model="registerForm.username" placeholder="用户名" prefix-icon="User" /></el-form-item>
                        <el-form-item><el-input v-model="registerForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password /></el-form-item>
                        <el-form-item><el-input v-model="registerForm.email" placeholder="邮箱（选填）" prefix-icon="Message" /></el-form-item>
                        <el-form-item><el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister">注册</el-button></el-form-item>
                    </el-form>
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import axios from '../api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', email: '' })

const handleLogin = async () => {
    loading.value = true
    try {
        const res = await axios.post('/auth/login', loginForm)
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.user.username)
        ElMessage.success('登录成功')
        router.push('/dashboard')
    } catch (e) {}
    loading.value = false
}

const handleRegister = async () => {
    loading.value = true
    try {
        const res = await axios.post('/auth/register', registerForm)
        localStorage.setItem('token', res.token)
        localStorage.setItem('username', res.user.username)
        ElMessage.success('注册成功')
        router.push('/dashboard')
    } catch (e) {}
    loading.value = false
}
</script>
