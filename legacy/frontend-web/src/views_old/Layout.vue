<template>
    <el-container style="height: 100vh">
        <el-aside width="220px" style="background: #304156">
            <div style="padding: 20px; color: white; text-align: center; font-size: 18px; font-weight: bold">经期记录助手</div>
            <el-menu :default-active="route.path" router background-color="#304156" text-color="#bfcbd9" active-text-color="#409EFF">
                <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>概览</span></el-menu-item>
                <el-menu-item index="/records"><el-icon><Calendar /></el-icon><span>记录</span></el-menu-item>
                <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><span>统计</span></el-menu-item>
                <el-menu-item index="/profile"><el-icon><User /></el-icon><span>个人</span></el-menu-item>
            </el-menu>
        </el-aside>
        <el-container>
            <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: flex-end">
                <el-dropdown @command="handleCommand">
                    <span style="cursor: pointer">{{ username }} <el-icon><ArrowDown /></el-icon></span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </el-header>
            <el-main style="background: #f0f2f5; padding: 20px"><router-view /></el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

const handleCommand = (cmd) => {
    if (cmd === 'logout') {
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        router.push('/login')
    } else if (cmd === 'profile') {
        router.push('/profile')
    }
}
</script>
