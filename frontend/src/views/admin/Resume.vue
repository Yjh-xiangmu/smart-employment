<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <span style="font-weight: bold; font-size: 16px;">学生简历资源库</span>
      </template>

      <el-table :data="tableData" border v-loading="loading" style="width: 100%">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120" align="center" />
        <el-table-column label="联系方式" width="200">
          <template #default="scope">
            <div><el-icon><Iphone /></el-icon> {{ scope.row.phone || '未填' }}</div>
            <div style="font-size: 12px; color: #666; margin-top: 4px;">
              <el-icon><Message /></el-icon> {{ scope.row.email || '未填' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="profile.major" label="专业" width="150" />
        <el-table-column prop="profile.grade" label="年级" width="100" align="center" />

        <el-table-column label="附件简历" min-width="200">
          <template #default="scope">
            <span v-if="scope.row.profile.resumeName" style="color: #67c23a; font-weight: 500;">
              <el-icon><DocumentChecked /></el-icon> {{ scope.row.profile.resumeName }}
            </span>
            <span v-else style="color: #f56c6c;">未上传附件简历</span>
          </template>
        </el-table-column>

        <el-table-column prop="profile.createTime" label="更新时间" width="180" align="center">
          <template #default="scope">{{ formatTime(scope.row.profile.createTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
                v-if="scope.row.profile.resumeName"
                size="small"
                type="primary"
                plain
                @click="previewResume(scope.row.profile.studentId)"
            >预览简历</el-button>
            <span v-else style="color: #999; font-size: 13px;">无附件可看</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Iphone, Message, DocumentChecked } from '@element-plus/icons-vue'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)

const formatTime = (time) => time ? time.replace('T', ' ') : ''

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/manage/resume/list')
    if (res.code === 200) tableData.value = res.data
  } finally {
    loading.value = false
  }
}

// 管理员复用学生端的下载简历接口进行预览
const previewResume = (studentId) => {
  const fileUrl = `/api/student/profile/downloadResume/${studentId}`
  window.open(fileUrl, '_blank')
}

onMounted(() => loadData())
</script>