<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">企业宣传与简章管理</span>
          <el-button type="primary" @click="handleAdd">发布新宣传</el-button>
        </div>
      </template>

      <el-table :data="tableData" border style="width: 100%; margin-top: 15px;">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="title" label="宣传标题" />
        <el-table-column prop="viewCount" label="浏览量" width="100" align="center" />
        <el-table-column prop="createTime" label="发布时间" width="180" align="center" />
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="60%">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="宣传标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题（如：2026届秋季校园招聘简章）" />
        </el-form-item>
        <el-form-item label="宣传内容" prop="content">
          <el-input
              type="textarea"
              v-model="form.content"
              :rows="12"
              placeholder="请输入详细的宣传介绍、招聘要求、福利待遇等..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('发布新宣传')
const formRef = ref(null)

const form = ref({
  id: null,
  title: '',
  content: '',
  enterpriseId: null
})

const rules = {
  title: [{ required: true, message: '请输入宣传标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入宣传内容', trigger: 'blur' }]
}

const getEnterpriseId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.id
}

const loadData = async () => {
  const entId = getEnterpriseId()
  if (!entId) return
  try {
    const res = await request.get(`/enterprise/promotion/list?enterpriseId=${entId}`)
    if (res.code === 200) {
      tableData.value = res.data
    }
  } catch (error) {
    console.error(error)
  }
}

const handleAdd = () => {
  dialogTitle.value = '发布新宣传'
  form.value = { id: null, title: '', content: '', enterpriseId: getEnterpriseId() }
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑宣传简章'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条宣传简章吗?', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/enterprise/promotion/delete/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadData()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      console.error(error)
    }
  }).catch(() => {})
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const isEdit = form.value.id != null
        const url = isEdit ? '/enterprise/promotion/update' : '/enterprise/promotion/add'
        const method = isEdit ? 'put' : 'post'

        const res = await request[method](url, form.value)
        if (res.code === 200) {
          ElMessage.success(isEdit ? '修改成功' : '发布成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.msg || '操作失败')
        }
      } catch (error) {
        console.error(error)
      }
    }
  })
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