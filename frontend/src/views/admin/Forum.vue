<template>
  <div class="admin-forum-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">论坛社区内容审核</span>
          <div class="header-actions">
            <el-input
                v-model="keyword"
                placeholder="搜索帖子标题或正文违规词..."
                class="search-input"
                clearable
                @keyup.enter="fetchPosts"
            >
              <template #append>
                <el-button :icon="Search" @click="fetchPosts" />
              </template>
            </el-input>
            <el-button type="primary" plain @click="fetchPosts" :icon="Refresh" style="margin-left: 15px;">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="title" label="帖子标题" min-width="200">
          <template #default="scope">
            <span style="font-weight: 500; color: #303133;">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="author_name" label="发帖人" width="120">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.author_name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="view_count" label="浏览量" width="100" align="center" />
        <el-table-column prop="create_time" label="发布时间" width="180" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="viewPost(scope.row)">
              审查内容
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
              违规删除
            </el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="当前社区风平浪静，没有任何帖子~" />
        </template>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" title="帖子内容审查" size="600px">
      <div v-if="currentPost" class="review-content">
        <h2 class="review-title">{{ currentPost.title }}</h2>
        <div class="review-meta">
          <span>发帖人：{{ currentPost.author_name }}</span>
          <span>时间：{{ currentPost.create_time }}</span>
        </div>
        <el-divider />
        <div class="review-body">
          <p style="white-space: pre-wrap; line-height: 1.8;">{{ currentPost.content }}</p>
        </div>
      </div>
      <template #footer>
        <div class="drawer-footer">
          <el-button type="danger" @click="handleDelete(currentPost)">确认违规并删除</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const keyword = ref('')

const drawerVisible = ref(false)
const currentPost = ref(null)

onMounted(() => {
  fetchPosts()
})

// 拉取帖子列表
const fetchPosts = () => {
  loading.value = true
  request.get('/admin/forum/list', { params: { keyword: keyword.value } }).then(res => {
    tableData.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

// 审查帖子正文
const viewPost = (row) => {
  currentPost.value = row
  drawerVisible.value = true
}

// 删除帖子
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要永久删除帖子【${row.title}】及其所有评论吗？此操作不可恢复！`,
      '高危操作确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error',
      }
  ).then(() => {
    request.post(`/admin/forum/delete/${row.id}`).then(res => {
      ElMessage.success(res.message)
      drawerVisible.value = false
      fetchPosts() // 刷新列表
    })
  }).catch(() => {})
}
</script>

<style scoped>
.admin-forum-container { height: 100%; }
.box-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; }
.header-actions { display: flex; align-items: center; }
.search-input { width: 300px; }

.review-content { padding: 0 10px; }
.review-title { font-size: 20px; color: #303133; margin-top: 0; margin-bottom: 15px; }
.review-meta { font-size: 13px; color: #909399; display: flex; gap: 20px; }
.review-body { font-size: 15px; color: #333; text-align: justify; }
.drawer-footer { text-align: center; padding: 20px 0; }
</style>