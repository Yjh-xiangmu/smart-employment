<template>
  <div class="resume-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">收到的简历</span>
          <el-button type="primary" plain @click="fetchDeliveries" :icon="Refresh">刷新数据</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column label="应聘岗位" min-width="150">
          <template #default="scope">
            <div style="font-weight: 600; color: #303133;">{{ scope.row.job_name }}</div>
            <div style="font-size: 12px; color: #909399;">投递于: {{ scope.row.create_time }}</div>
          </template>
        </el-table-column>

        <el-table-column label="候选人信息" min-width="180">
          <template #default="scope">
            <div style="font-weight: 500; color: #409EFF;">
              <el-icon><User /></el-icon> {{ scope.row.student_name }}
            </div>
            <div style="font-size: 13px; color: #606266; margin-top: 4px;">
              {{ scope.row.major }} | {{ scope.row.grade }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" width="150">
          <template #default="scope">
            <div><el-icon><Iphone /></el-icon> {{ scope.row.phone || '未填' }}</div>
            <div style="font-size: 12px; margin-top: 2px;"><el-icon><Message /></el-icon> {{ scope.row.email || '未填' }}</div>
          </template>
        </el-table-column>

        <el-table-column label="附件简历" width="120" align="center">
          <template #default="scope">
            <el-button size="small" type="success" plain @click="previewResume(scope.row)">
              <el-icon><Document /></el-icon> 查看
            </el-button>
          </template>
        </el-table-column>

        <el-table-column label="处理状态" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="primary">已查看</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="warning" effect="dark">面试中</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="info">已淘汰</el-tag>
            <el-tag v-else-if="scope.row.status === 5" type="success" effect="dark">已录用</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <template v-if="scope.row.status === 0 || scope.row.status === 1">
              <el-button size="small" type="primary" @click="handleStatusChange(scope.row, 2)">邀约</el-button>
              <el-button size="small" type="danger" plain @click="handleStatusChange(scope.row, 3)">淘汰</el-button>
            </template>

            <template v-if="scope.row.status === 2">
              <el-button size="small" type="success" @click="handleStatusChange(scope.row, 5)">录用</el-button>
              <el-button size="small" type="danger" plain @click="handleStatusChange(scope.row, 3)">淘汰</el-button>
            </template>

            <span v-if="scope.row.status === 3" style="color: #909399; font-size: 13px;">流程结束 (淘汰)</span>
            <span v-if="scope.row.status === 5" style="color: #67c23a; font-size: 13px; font-weight: bold;">已发Offer</span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂时还没收到新的简历投递哦~" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh, User, Iphone, Message, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const enterpriseId = ref(null)

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.id) {
    enterpriseId.value = userInfo.id
    fetchDeliveries()
  }
})

const fetchDeliveries = () => {
  if (!enterpriseId.value) return
  loading.value = true
  request.get(`/enterprise/delivery/list/${enterpriseId.value}`).then(res => {
    tableData.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

const previewResume = (row) => {
  if (!row.student_id) return
  if (row.status === 0) {
    request.post('/enterprise/delivery/changeStatus', { id: row.delivery_id, status: 1 }).then(() => {
      fetchDeliveries()
    })
  }
  const fileUrl = `/api/student/profile/downloadResume/${row.student_id}`
  window.open(fileUrl, '_blank')
}

const handleStatusChange = (row, targetStatus) => {
  let actionText = ''
  let typeText = ''

  if (targetStatus === 2) { actionText = '邀约面试'; typeText = 'primary' }
  else if (targetStatus === 3) { actionText = '淘汰该候选人'; typeText = 'danger' }
  else if (targetStatus === 5) { actionText = '发放Offer并录用'; typeText = 'success' }

  ElMessageBox.confirm(
      `确定要对该候选人进行【${actionText}】操作吗？`,
      '处理确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: typeText }
  ).then(() => {
    request.post('/enterprise/delivery/changeStatus', { id: row.delivery_id, status: targetStatus }).then(res => {
      ElMessage.success(res.message)
      fetchDeliveries()
    })
  }).catch(() => {})
}
</script>

<style scoped>
.resume-container { height: 100%; }
.box-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; }
</style>