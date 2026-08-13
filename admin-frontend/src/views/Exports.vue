<template>
  <el-card shadow="never">
    <el-alert
      title="以下导出接口需要管理员权限，导出的文件为 CSV 格式（UTF-8 编码）。"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    />
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="export-card">
          <div class="export-title">
            <el-icon :size="28" color="#409EFF"><User /></el-icon>
            <span>用户数据导出</span>
          </div>
          <p>导出全部注册用户信息（ID、用户名、角色、状态、注册时间）。</p>
          <el-button type="primary" :loading="exportingUsers" @click="handleExport('users')">
            下载 users.csv
          </el-button>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="export-card">
          <div class="export-title">
            <el-icon :size="28" color="#67C23A"><Document /></el-icon>
            <span>经期记录导出</span>
          </div>
          <p>导出全部经期记录（日期、经量、痛经、症状、情绪、备注等）。</p>
          <el-button type="success" :loading="exportingRecords" @click="handleExport('records')">
            下载 records.csv
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { exportUsers, exportRecords } from '../api/admin'

const exportingUsers = ref(false)
const exportingRecords = ref(false)

async function handleExport(type) {
  const exporting = type === 'users' ? exportingUsers : exportingRecords
  const api = type === 'users' ? exportUsers : exportRecords
  exporting.value = true
  try {
    const res = await api()
    const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${type}.csv`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败，请检查权限')
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.export-card {
  height: 220px;
}
.export-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.export-card p {
  color: #909399;
  font-size: 13px;
  margin-bottom: 16px;
  min-height: 40px;
}
</style>
