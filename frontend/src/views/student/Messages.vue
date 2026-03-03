<template>
  <div class="message-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">系统消息通知</span>
          <el-button v-if="unreadCount > 0" type="primary" plain @click="handleReadAll">全部标为已读</el-button>
        </div>
      </template>

      <div class="msg-content" v-loading="loading">
        <el-empty v-if="msgList.length === 0" description="暂无任何消息通知" />

        <el-timeline v-else>
          <el-timeline-item
              v-for="msg in msgList"
              :key="msg.id"
              :timestamp="msg.createTime"
              :type="msg.isRead === 0 ? 'primary' : 'info'"
              :hollow="msg.isRead !== 0"
              placement="top"
          >
            <el-card :class="{ 'unread-card': msg.isRead === 0 }" shadow="hover" @click="readMessage(msg)">
              <div class="msg-title">
                <el-badge is-dot :hidden="msg.isRead === 1" class="dot-badge">
                  <span>{{ msg.title }}</span>
                </el-badge>
              </div>
              <div class="msg-desc">{{ msg.content }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const msgList = ref([])
const unreadCount = ref(0)
const studentId = ref(null)

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.id) {
    studentId.value = userInfo.id
    fetchMessages()
  }
})

const fetchMessages = () => {
  if (!studentId.value) return
  loading.value = true
  request.get(`/student/message/list/${studentId.value}`).then(res => {
    msgList.value = res.data.list || []
    unreadCount.value = res.data.unreadCount || 0
  }).finally(() => {
    loading.value = false
  })
}

// 点击卡片标记为已读
const readMessage = (msg) => {
  if (msg.isRead === 1) return // 已读的就不调接口了
  request.post(`/student/message/read/${msg.id}`).then(() => {
    msg.isRead = 1
    unreadCount.value-- // 本地未读数-1
    // 【新增】发广播通知外层 Layout 更新小铃铛
    window.dispatchEvent(new CustomEvent('update-unread-count'))
  })
}

// 一键已读
const handleReadAll = () => {
  request.post(`/student/message/readAll/${studentId.value}`).then(() => {
    fetchMessages() // 重新拉取列表
    // 【新增】发广播通知外层 Layout 更新小铃铛
    window.dispatchEvent(new CustomEvent('update-unread-count'))
  })
}
</script>

<style scoped>
.message-container { padding: 10px; max-width: 800px; margin: 0 auto; }
.box-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); min-height: 500px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; border-left: 4px solid #409EFF; padding-left: 10px; }
.msg-content { padding-top: 20px; }

/* 消息卡片样式 */
.el-card { cursor: pointer; transition: all 0.3s; border: 1px solid #ebeef5; }
.unread-card { border-color: #a0cfff; background-color: #f4f9ff; }
.msg-title { font-size: 16px; font-weight: bold; color: #303133; margin-bottom: 10px; }
.msg-desc { font-size: 14px; color: #606266; line-height: 1.6; }
:deep(.dot-badge .el-badge__content.is-fixed) { transform: translateY(-50%) translateX(100%); }
</style>