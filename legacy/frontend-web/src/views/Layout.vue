
<template>
    <div style="max-width:420px;margin:0 auto;min-height:100vh;background:#fdf2f8">
        <div class="header-bar">
            <span class="header-title"><span style="-webkit-text-fill-color:initial;color:#f06292;margin-right:4px">🌸</span>小月历</span>
            <span style="font-size:14px;color:#8e5580;cursor:pointer" @click="router.push('/profile')">{{ username }}</span>
        </div>
        <div style="padding:0 16px 80px">
            <router-view v-slot="{ Component }">
                <transition name="page" mode="out-in">
                    <component :is="Component" />
                </transition>
            </router-view>
        </div>
        <div class="bottom-nav">
            <div v-for="item in navItems" :key="item.path"
                 :class="['nav-item', { active: route.path === item.path }]"
                 @click="router.push(item.path)">
                <span class="icon" v-html="item.icon"></span>
                <span>{{ item.label }}</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')

const navItems = [
    { path: '/dashboard', label: '概览', icon: '&#x1F3AF;' },
    { path: '/records', label: '记录', icon: '&#x1F4C5;' },
    { path: '/statistics', label: '统计', icon: '&#x1F4CA;' },
    { path: '/profile', label: '我的', icon: '&#x1F469;' },
]
</script>
