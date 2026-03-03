<template>
  <div class="audit-container">
    <el-card class="data-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="title">待审核企业列表</span>
          <el-button type="primary" plain @click="fetchData" :icon="Refresh">刷新数据</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
        <el-table-column prop="account" label="统一社会信用代码/账号" min-width="200" />
        <el-table-column prop="legalPerson" label="法人代表" width="120" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default>
            <el-tag type="warning" effect="light">待审核</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleAudit(scope.row)">进行审核</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="当前没有需要审核的企业" />
        </template>
      </el-table>
    </el-card>

    <el-drawer v-model="drawerVisible" title="企业资质审查" size="500px">
      <div v-loading="detailLoading" class="drawer-content">
        <el-descriptions title="企业基本信息" :column="1" border>
          <el-descriptions-item label="企业名称">{{ currentDetail.enterpriseName }}</el-descriptions-item>
          <el-descriptions-item label="法人代表">{{ currentDetail.legalPerson }}</el-descriptions-item>
        </el-descriptions>

        <div class="license-preview-box">
          <h4>营业执照扫描件</h4>
          <el-image
              v-if="currentDetail.licenseImage"
              :src="currentDetail.licenseImage"
              :preview-src-list="[currentDetail.licenseImage]"
              fit="contain"
              class="license-img"
          />
          <span v-else class="no-img">暂无图片数据</span>
        </div>
      </div>

      <template #footer>
        <div class="drawer-footer">
          <el-button type="danger" @click="submitAudit(2)">驳 回 申 请</el-button>
          <el-button type="success" @click="submitAudit(1)">通 过 入 驻</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])

const drawerVisible = ref(false)
const detailLoading = ref(false)
const currentDetail = ref({})

// 获取待审核列表
const fetchData = () => {
  loading.value = true
  request.get('/admin/enterprise/pending').then(res => {
    tableData.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

// 打开审查抽屉，拉取图片详情
const handleAudit = (row) => {
  drawerVisible.value = true
  detailLoading.value = true
  request.get(`/admin/enterprise/detail/${row.id}`).then(res => {
    currentDetail.value = res.data
  }).finally(() => {
    detailLoading.value = false
  })
}

// 提交审核结果
const submitAudit = (status) => {
  const actionText = status === 1 ? '通过' : '驳回'
  ElMessageBox.confirm(`确定要 ${actionText} 该企业的入驻申请吗？`, '操作确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: status === 1 ? 'success' : 'warning'
  }).then(() => {
    request.post('/admin/enterprise/audit', {
      id: currentDetail.value.id,
      status: status
    }).then(res => {
      ElMessage.success(res.message)
      drawerVisible.value = false
      fetchData() // 刷新表格
    })
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.audit-container { height: 100%; }
.data-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,21,41,.08); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.title { font-size: 16px; font-weight: 600; color: #333; }

.drawer-content { padding-right: 10px; }
.license-preview-box { margin-top: 30px; }
.license-preview-box h4 { margin-bottom: 15px; color: #333; font-weight: 500; border-left: 4px solid #1890ff; padding-left: 8px; }
.license-img { width: 100%; max-height: 400px; border-radius: 4px; border: 1px solid #ebeef5; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1); }
.no-img { color: #999; font-size: 14px; }
.drawer-footer { display: flex; justify-content: space-between; padding: 0 20px 20px; }
</style>