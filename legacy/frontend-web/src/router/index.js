import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
    { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
    {
        path: '/',
        component: () => import('../views/Layout.vue'),
        redirect: '/dashboard',
        children: [
            { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
            { path: 'records', name: 'Records', component: () => import('../views/Records.vue') },
            { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue') },
            { path: 'profile', name: 'Profile', component: () => import('../views/Profile.vue') }
        ]
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.name !== 'Login' && !token) {
        next({ name: 'Login' })
    } else if (to.name === 'Login' && token) {
        next({ name: 'Dashboard' })
    } else {
        next()
    }
})

export default router
