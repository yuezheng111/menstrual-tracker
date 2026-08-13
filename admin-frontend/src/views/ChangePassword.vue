<template>
  <div class="change-password-page">
    <el-card class="change-card">
      <div class="change-title">
        <h2>修改密码</h2>
        <p>首次登录后必须修改初始密码</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="至少8位" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-button type="primary" class="change-btn" :loading="loading" @click="handleChange">
          确认修改
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { changePassword } from '../api/admin'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '新密码至少8位', trigger: 'blur' }
  ],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
}

async function handleChange() {
  await formRef.value.validate()
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  if (form.newPassword === form.oldPassword) {
    ElMessage.error('新密码不能与原密码相同')
    return
  }
  loading.value = true
  try {
    const res = await changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    localStorage.setItem('admin_token', res.data.token)
    localStorage.setItem('admin_user', JSON.stringify({ username: res.data.username, role: res.data.role }))
    ElMessage.success('密码修改成功')
    router.replace('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.change-password-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
}
.change-card {
  width: 420px;
}
.change-title {
  text-align: center;
  margin-bottom: 20px;
}
.change-title h2 {
  margin: 0 0 6px;
  color: #303133;
}
.change-title p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}
.change-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
