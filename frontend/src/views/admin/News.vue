<template>
  <div class="admin-news-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">新闻与政策管理</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">发布新内容</el-button>
        </div>
      </template>

      <div class="filter-section">
        <el-input v-model="queryParams.keyword" placeholder="搜索文章标题..." class="search-input" clearable @keyup.enter="fetchNews" />
        <el-select v-model="queryParams.type" placeholder="全部分类" clearable @change="fetchNews" style="width: 150px; margin-left: 15px;">
          <el-option label="新闻资讯" :value="1" />
          <el-option label="就业政策" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchNews" style="margin-left: 15px;">查询</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%; margin-top: 20px;">
        <el-table-column prop="title" label="文章标题" min-width="250">
          <template #default="scope">
            <span style="font-weight: 500; color: #303133;">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="分类" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.type === 1 ? 'primary' : 'success'">
              {{ scope.row.type === 1 ? '新闻资讯' : '就业政策' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="阅读量" width="100" align="center" />
        <el-table-column prop="createTime" label="发布时间" width="180" />

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无内容，快去发布吧~" /></template>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" destroy-on-close top="5vh">
      <el-form :model="newsForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="newsForm.title" placeholder="请输入标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="文章分类" prop="type">
          <el-radio-group v-model="newsForm.type">
            <el-radio :label="1">新闻资讯</el-radio>
            <el-radio :label="2">就业政策</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文内容" prop="content">
          <el-input
              v-model="newsForm.content"
              type="textarea"
              :rows="15"
              placeholder="请输入正文内容 (后续可接入 WangEditor 等富文本编辑器)..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitNews" :loading="submitLoading">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const queryParams = reactive({ keyword: '', type: '' })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const dialogTitle = ref('发布新内容')
const formRef = ref(null)

const newsForm = reactive({ id: null, title: '', type: 1, content: '' })
const rules = reactive({
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
})

onMounted(() => {
  fetchNews()
})

const fetchNews = () => {
  loading.value = true
  request.get('/admin/news/list', { params: queryParams }).then(res => {
    tableData.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

const openAddDialog = () => {
  dialogTitle.value = '发布新内容'
  Object.assign(newsForm, { id: null, title: '', type: 1, content: '' })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  dialogTitle.value = '编辑内容'
  Object.assign(newsForm, row)
  dialogVisible.value = true
}

const submitNews = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      request.post('/admin/news/save', newsForm).then(res => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        fetchNews()
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除《${row.title}》吗？`, '删除确认', { type: 'warning' }).then(() => {
    request.post(`/admin/news/delete/${row.id}`).then(res => {
      ElMessage.success(res.message)
      fetchNews()
    })
  }).catch(() => {})
}
</script>

<style scoped>
.admin-news-container { height: 100%; }
.box-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; }
.filter-section { display: flex; align-items: center; }
.search-input { width: 300px; }
</style>