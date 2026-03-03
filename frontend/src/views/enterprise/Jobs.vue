<template>
  <div class="job-container">
    <el-card class="box-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">职位招聘管理</span>
          <el-button type="primary" @click="openAddDialog" :icon="Plus">发布新职位</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="jobName" label="职位名称" min-width="150" />
        <el-table-column prop="category" label="职位分类" width="130" />
        <el-table-column prop="city" label="工作城市" width="100" />
        <el-table-column prop="salary" label="薪资范围" width="120">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold;">{{ scope.row.salary }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '招聘中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button
                size="small"
                :type="scope.row.status === 1 ? 'danger' : 'success'"
                link
                @click="handleStatusChange(scope.row)">
              {{ scope.row.status === 1 ? '下架' : '重新上架' }}
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂未发布任何职位" />
        </template>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" destroy-on-close>
      <el-form :model="jobForm" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="职位名称" prop="jobName">
          <el-input v-model="jobForm.jobName" placeholder="例如：Java架构师" />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位分类" prop="category">
              <el-select
                  v-model="jobForm.category"
                  placeholder="请选择或直接输入"
                  filterable
                  allow-create
                  default-first-option
                  style="width: 100%;">
                <el-option
                    v-for="item in categoryOptions"
                    :key="item"
                    :label="item"
                    :value="item"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作城市" prop="city">
              <el-input v-model="jobForm.city" placeholder="例如：深圳" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历要求" prop="education">
              <el-select v-model="jobForm.education" placeholder="请选择" style="width: 100%;">
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士及以上" value="硕士及以上" />
                <el-option label="不限" value="不限" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经验要求" prop="experience">
              <el-select v-model="jobForm.experience" placeholder="请选择" style="width: 100%;">
                <el-option label="应届生" value="应届生" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
                <el-option label="5年以上" value="5年以上" />
                <el-option label="不限" value="不限" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="薪资范围" prop="salary">
          <el-input v-model="jobForm.salary" placeholder="例如：15k-25k" />
        </el-form-item>

        <el-form-item label="职位描述" prop="description">
          <el-input
              v-model="jobForm.description"
              type="textarea"
              :rows="5"
              placeholder="请详细描述该岗位的工作职责与任职要求..."
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitJob" :loading="submitLoading">确 定</el-button>
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

const enterpriseId = ref(null)
const loading = ref(false)
const tableData = ref([])

// 分类下拉框数据 (包含基础分类和后端查出的历史分类)
const categoryOptions = ref(['后端开发', '前端开发', '产品经理', 'UI设计', '市场运营', '人力资源'])

// 弹窗相关
const dialogVisible = ref(false)
const submitLoading = ref(false)
const dialogTitle = ref('发布新职位')
const formRef = ref(null)

const jobForm = reactive({
  id: null,
  jobName: '', category: '', city: '', salary: '',
  education: '', experience: '', description: ''
})

const rules = reactive({
  jobName: [{ required: true, message: '请输入职位名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择或输入职位分类', trigger: 'change' }],
  city: [{ required: true, message: '请输入工作城市', trigger: 'blur' }],
  salary: [{ required: true, message: '请输入薪资范围', trigger: 'blur' }],
  description: [{ required: true, message: '请输入职位描述', trigger: 'blur' }]
})

// 初始化拉取数据
onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.id) {
    enterpriseId.value = userInfo.id
    fetchJobs()
    fetchCategories() // 拉取历史分类
  }
})

// 拉取企业使用过的历史分类
const fetchCategories = () => {
  request.get(`/enterprise/job/categories/${enterpriseId.value}`).then(res => {
    if (res.data && res.data.length > 0) {
      // 合并并去重
      categoryOptions.value = Array.from(new Set([...categoryOptions.value, ...res.data]))
    }
  })
}

// 拉取职位列表
const fetchJobs = () => {
  loading.value = true
  request.get(`/enterprise/job/list/${enterpriseId.value}`).then(res => {
    tableData.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

// 打开新增弹窗
const openAddDialog = () => {
  dialogTitle.value = '发布新职位'
  Object.assign(jobForm, { id: null, jobName: '', category: '', city: '', salary: '', education: '', experience: '', description: '' })
  dialogVisible.value = true
}

// 打开编辑弹窗
const openEditDialog = (row) => {
  dialogTitle.value = '编辑职位信息'
  Object.assign(jobForm, row) // 回显数据
  dialogVisible.value = true
}

// 提交表单 (新增或更新)
const submitJob = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      const payload = { ...jobForm, enterpriseId: enterpriseId.value }
      const url = jobForm.id ? '/enterprise/job/update' : '/enterprise/job/add'

      request.post(url, payload).then(res => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        fetchJobs() // 刷新表格
        fetchCategories() // 重新拉取一次分类，以防有刚创建的新分类
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

// 上下架切换
const handleStatusChange = (row) => {
  const targetStatus = row.status === 1 ? 0 : 1
  const actionText = targetStatus === 1 ? '重新上架' : '下架'

  ElMessageBox.confirm(`确定要 ${actionText} 该职位吗？`, '系统提示', {
    type: 'warning'
  }).then(() => {
    request.post('/enterprise/job/changeStatus', { id: row.id, status: targetStatus }).then(res => {
      ElMessage.success(res.message)
      fetchJobs()
    })
  }).catch(() => {})
}
</script>

<style scoped>
.job-container { height: 100%; }
.box-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; }
</style>