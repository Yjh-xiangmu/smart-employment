<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span style="font-weight: bold; font-size: 16px;">平台投递大盘监控</span>
      </template>

      <el-table :data="tableData" border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="delivery.createTime" label="投递时间" width="180" align="center">
          <template #default="scope">{{ formatTime(scope.row.delivery.createTime) }}</template>
        </el-table-column>

        <el-table-column prop="studentName" label="求职学生" width="120" align="center">
          <template #default="scope">
            <span style="font-weight: bold;">{{ scope.row.studentName }}</span>
          </template>
        </el-table-column>

        <el-table-column label="流转方向" min-width="300">
          <template #default="scope">
            投递给：<span style="color: #409EFF; font-weight: bold;">{{ scope.row.enterpriseName }}</span>
            的 <span style="color: #E6A23C; font-weight: bold;">{{ scope.row.jobName }}</span> 岗位
          </template>
        </el-table-column>

        <el-table-column label="当前进展状态" width="150" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.delivery.status === 0" type="warning">企业待处理</el-tag>
            <el-tag v-else-if="scope.row.delivery.status === 1" type="primary">企业已查看</el-tag>
            <el-tag v-else-if="scope.row.delivery.status === 2" type="warning" effect="dark">面试邀约中</el-tag>
            <el-tag v-else-if="scope.row.delivery.status === 3" type="danger">已淘汰</el-tag>
            <el-tag v-else-if="scope.row.delivery.status === 4" type="info">学生已取消</el-tag>
            <el-tag v-else-if="scope.row.delivery.status === 5" type="success" effect="dark">发Offer录用</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)

const formatTime = (time) => time ? time.replace('T', ' ') : ''

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/manage/delivery/list')
    if (res.code === 200) tableData.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>