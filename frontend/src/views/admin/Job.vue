<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span style="font-weight: bold; font-size: 16px;">全局职位监管</span>
      </template>

      <el-table :data="tableData" border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="job.jobName" label="职位名称" min-width="150" />
        <el-table-column prop="enterpriseName" label="发布企业" min-width="180">
          <template #default="scope">
            <span style="font-weight: bold; color: #409EFF;">{{ scope.row.enterpriseName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="job.city" label="城市" width="100" align="center" />
        <el-table-column prop="job.salary" label="薪资待遇" width="120" align="center" />
        <el-table-column prop="job.createTime" label="发布时间" width="180" align="center">
          <template #default="scope">{{ formatTime(scope.row.job.createTime) }}</template>
        </el-table-column>

        <el-table-column label="当前状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.job.status === 1 ? 'success' : 'danger'">
              {{ scope.row.job.status === 1 ? '招聘中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="监管操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.job.status === 1"
                size="small"
                type="danger"
                @click="toggleStatus(scope.row.job.id, 0)"
            >违规下架</el-button>
            <el-button
                v-else
                size="small"
                type="success"
                plain
                @click="toggleStatus(scope.row.job.id, 1)"
            >恢复上架</el-button>
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
    const res = await request.get('/admin/manage/job/list')
    if (res.code === 200) tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const toggleStatus = (jobId, targetStatus) => {
  const action = targetStatus === 1 ? '恢复上架' : '强制下架'
  ElMessageBox.confirm(`确定要${action}该职位吗？`, '违规操作', { type: 'warning' }).then(async () => {
    const res = await request.post('/admin/manage/job/changeStatus', { id: jobId, status: targetStatus })
    if (res.code === 200) {
      ElMessage.success(res.message)
      loadData()
    }
  }).catch(() => {})
}

onMounted(() => loadData())
</script>