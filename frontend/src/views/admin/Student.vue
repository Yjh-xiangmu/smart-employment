<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span style="font-weight: bold; font-size: 16px;">学生账号管理</span>
      </template>

      <el-table :data="tableData" border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="account" label="登录账号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" align="center" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="createTime" label="注册时间" width="180" align="center">
          <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
        </el-table-column>

        <el-table-column label="账号状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.status === 1"
                size="small"
                type="danger"
                plain
                @click="toggleStatus(scope.row, 0)"
            >封禁账号</el-button>
            <el-button
                v-else
                size="small"
                type="success"
                @click="toggleStatus(scope.row, 1)"
            >解除封禁</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)

const formatTime = (time) => time ? time.replace('T', ' ') : ''

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/manage/student/list')
    if (res.code === 200) tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const toggleStatus = (row, targetStatus) => {
  const action = targetStatus === 1 ? '解除封禁' : '封禁'
  ElMessageBox.confirm(`确定要${action}学生【${row.name}】的账号吗？`, '提示', { type: 'warning' }).then(async () => {
    const res = await request.post('/admin/manage/student/changeStatus', { id: row.id, status: targetStatus })
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadData()
    }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>