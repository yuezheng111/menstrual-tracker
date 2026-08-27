<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名/昵称"
        clearable
        style="width: 300px"
        @keyup.enter="handleSearch"
        @clear="handleSearch"
      >
        <template #append>
          <el-button @click="handleSearch"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
    </div>

    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120">
        <template #default="{ row }">{{ row.nickname || '-' }}</template>
      </el-table-column>
      <el-table-column prop="recordCount" label="记录数" width="90" />
      <el-table-column prop="role" label="角色" width="90">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.enabled ? 'success' : 'warning'">{{ row.enabled ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.enabled ? 'warning' : 'success'" @click="toggleStatus(row)">
            {{ row.enabled ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="primary" plain @click="openReset(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 16px; justify-content: flex-end"
      layout="total, prev, pager, next, sizes"
      :total="total"
      :page-sizes="[10, 20, 50]"
      v-model:current-page="page"
      v-model:page-size="size"
      @current-change="load"
      @size-change="handleSizeChange"
    />

    <el-dialog v-model="resetVisible" title="重置密码" width="420px">
      <el-form :model="resetForm" label-width="90px">
        <el-form-item label="用户名">
          <span>{{ resetForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="8-64 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetting" @click="confirmReset">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, setUserStatus, resetUserPassword } from '../api/admin'

const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)

const resetVisible = ref(false)
const resetting = ref(false)
const resetForm = reactive({ id: null, username: '', newPassword: '' })

function formatTime(t) {
  return t ? String(t).replace('T', ' ').substring(0, 19) : '-'
}

async function load() {
  loading.value = true
  try {
    const res = await listUsers({ page: page.value - 1, size: size.value, keyword: keyword.value || undefined })
    users.value = res.data.content || []
    total.value = res.data.totalElements || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  load()
}

function handleSizeChange() {
  page.value = 1
  load()
}

async function toggleStatus(row) {
  const action = row.enabled ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await setUserStatus(row.id, !row.enabled)
  ElMessage.success(`${action}成功`)
  load()
}

function openReset(row) {
  resetForm.id = row.id
  resetForm.username = row.username
  resetForm.newPassword = ''
  resetVisible.value = true
}

async function confirmReset() {
  if (!resetForm.newPassword || resetForm.newPassword.length < 8) {
    ElMessage.warning('新密码至少 8 位')
    return
  }
  resetting.value = true
  try {
    await resetUserPassword(resetForm.id, resetForm.newPassword)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } finally {
    resetting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
