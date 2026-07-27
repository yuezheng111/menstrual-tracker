import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
    baseURL: '/api',
    timeout: 30000
})

service.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = 'Bearer ' + token
    }
    return config
})

service.interceptors.response.use(response => {
    const res = response.data
    if (res.code !== 0) {
        ElMessage.error(res.message || 'Error')
        if (res.code === 401) {
            localStorage.removeItem('token')
            window.location.href = '/#/login'
        }
        return Promise.reject(new Error(res.message))
    }
    return res.data
}, error => {
    ElMessage.error(error.message)
    return Promise.reject(error)
})

export default service
