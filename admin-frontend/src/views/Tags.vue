<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-radio-group v-model="typeFilter" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="SYMPTOM">症状</el-radio-button>
        <el-radio-button value="MOOD">情绪</el-radio-button>
      </el-radio-group>
    </div>

    <el-table :data="tags" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="type" label="类型" width="120">
        <template #default="{ row }">
          <el-tag :type="row.type === 'SYMPTOM' ? 'danger' : 'primary'">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="颜色" width="100">
        <template #default="{ row }">
          <span class="color-dot" :style="{ background: row.color || '#409EFF' }"></span>
        </template>
      </el-table-column>
      <el-table-column prop="icon" label="图标" width="100">
        <template #default="{ row }">{{ row.icon || '-' }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listTags, deleteTag } from '../api/admin'

const tags = ref([])
const typeFilter = ref('')
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await listTags(typeFilter.value || undefined)
    tags.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteTag(row.id)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.color-dot {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  vertical-align: middle;
}
</style>
