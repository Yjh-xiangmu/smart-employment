<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">
            系统消息中心
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="item" style="margin-left: 10px;" />
          </span>
          <el-button type="success" plain @click="readAll" :disabled="unreadCount === 0">一键全部已读</el-button>
        </div>
      </template>

      <el-table :data="messageList" style="width: 100%" v-loading="loading">
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isRead === 1 ? 'info' : 'danger'">
              {{ scope.row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="通知标题" width="220" />
        <el-table-column prop="content" label="通知内容" />
        <el-table-column prop="createTime" label="时间" width="180" align="center">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <el-button
                v-if="scope.row.isRead === 0"
                size="small"
                type="primary"
                plain
                @click="readMsg(scope.row.id)"
            >标为已读</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const messageList = ref([])
const unreadCount = ref(0)

const getEnterpriseId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.id
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ') : ''
}

// 加载列表
const loadData = async () => {
  const entId = getEnterpriseId()
  if (!entId) return
  loading.value = true
  try {
    const res = await request.get(`/enterprise/message/list/${entId}`)
    if (res.code === 200) {
      messageList.value = res.data.list
      unreadCount.value = res.data.unreadCount
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 单个已读
const readMsg = async (id) => {
  try {
    const res = await request.post(`/enterprise/message/read/${id}`)
    if (res.code === 200) {
      ElMessage.success('已标记为已读')
      loadData()
    }
  } catch (error) {
    console.error(error)
  }
}

// 全部已读
const readAll = async () => {
  const entId = getEnterpriseId()
  try {
    const res = await request.post(`/enterprise/message/readAll/${entId}`)
    if (res.code === 200) {
      ElMessage.success('全部已读成功')
      loadData()
    }
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>