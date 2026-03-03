<template>
  <div class="applications-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">我的投递记录</span>
          <el-button type="primary" plain @click="fetchDeliveries" :icon="Refresh">刷新进度</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="job_name" label="投递岗位" min-width="150">
          <template #default="scope">
            <span style="font-weight: bold; color: #303133;">{{ scope.row.job_name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enterprise_name" label="企业名称" min-width="180">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 5px; color: #909399;"><OfficeBuilding /></el-icon>
              <span>{{ scope.row.enterprise_name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="city" label="城市" width="100" />
        <el-table-column prop="salary" label="薪资" width="120">
          <template #default="scope">
            <span style="color: #f56c6c;">{{ scope.row.salary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="投递时间" width="180" />

        <el-table-column label="当前状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 0" type="warning">待处理</el-tag>
            <el-tag v-else-if="scope.row.status === 1" type="primary">已查看</el-tag>
            <el-tag v-else-if="scope.row.status === 2" type="success">邀约面试</el-tag>
            <el-tag v-else-if="scope.row.status === 3" type="danger">暂不合适</el-tag>
            <el-tag v-else-if="scope.row.status === 4" type="info">已取消</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.status === 0"
                size="small"
                type="danger"
                plain
                @click="handleCancel(scope.row)">
              取消投递
            </el-button>
            <span v-else style="color: #c0c4cc; font-size: 13px;">不可操作</span>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="您还没有投递过任何简历哦，快去寻觅心仪的岗位吧~">
            <el-button type="primary" @click="$router.push('/student/jobs')">去浏览岗位</el-button>
          </el-empty>
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { OfficeBuilding, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const studentId = ref(null)

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.id) {
    studentId.value = userInfo.id
    fetchDeliveries()
  }
})

// 拉取我的投递记录
const fetchDeliveries = () => {
  if (!studentId.value) return
  loading.value = true
  request.get(`/student/delivery/list/${studentId.value}`).then(res => {
    tableData.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

// 取消投递
const handleCancel = (row) => {
  ElMessageBox.confirm(
      `确定要取消投递【${row.enterprise_name}】的【${row.job_name}】岗位吗？`,
      '取消确认',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '暂不取消',
        type: 'warning',
      }
  ).then(() => {
    request.post(`/student/delivery/cancel/${row.delivery_id}`).then(res => {
      ElMessage.success(res.message)
      fetchDeliveries() // 刷新列表
    })
  }).catch(() => {})
}
</script>

<style scoped>
.applications-container { padding: 10px; }
.box-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); min-height: 500px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; border-left: 4px solid #409EFF; padding-left: 10px; }
</style>