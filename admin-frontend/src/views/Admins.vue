<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-button type="primary" @click="openGrant">
        <el-icon style="margin-right: 4px"><Plus /></el-icon>授予管理员
      </el-button>
    </div>

    <el-table :data="admins" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag type="danger">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'warning'">{{ row.enabled ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" plain :disabled="row.username === currentAdmin" @click="handleRevoke(row)">
            撤销管理员
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="grantVisible" title="授予管理员" width="420px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="grantUsername" placeholder="输入要提升的用户名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="grantVisible = false">取消</el-button>
        <el-button type="primary" :loading="granting" @click="confirmGrant">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdmins, grantAdmin, revokeAdmin, listUsers } from '../api/admin'

const admins = ref([])
const loading = ref(false)
const grantVisible = ref(false)
const granting = ref(false)
const grantUsername = ref('')
const currentAdmin = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('admin_user') || '{}').username
  } catch {
    return ''
  }
})

async function load() {
  loading.value = true
  try {
    const res = await listAdmins()
    admins.value = res.data || []
  } finally {
    loading.value = false
  }
}

function openGrant() {
  grantUsername.value = ''
  grantVisible.value = true
}

async function confirmGrant() {
  if (!grantUsername.value.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  granting.value = true
  try {
    // 先按用户名查用户
    const res = await listUsers({ page: 0, size: 100, keyword: grantUsername.value.trim() })
    const user = (res.data.content || []).find(u => u.username === grantUsername.value.trim())
    if (!user) {
      ElMessage.error('未找到该用户')
      return
    }
    await grantAdmin(user.id)
    ElMessage.success('已授予管理员角色')
    grantVisible.value = false
    load()
  } finally {
    granting.value = false
  }
}

async function handleRevoke(row) {
  await ElMessageBox.confirm(`确定撤销「${row.username}」的管理员权限吗？`, '提示', { type: 'warning' })
  await revokeAdmin(row.id)
  ElMessage.success('已撤销')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
