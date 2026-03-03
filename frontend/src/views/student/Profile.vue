<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <el-col :span="16">
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人档案维护</span>
              <el-button type="primary" plain @click="saveProfile">保存信息</el-button>
            </div>
          </template>

          <el-form :model="profileForm" label-width="80px" class="profile-form">
            <el-divider content-position="left">基本信息</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="专业">
                  <el-input v-model="profileForm.major" placeholder="例如：计算机科学与技术" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="年级">
                  <el-input v-model="profileForm.grade" placeholder="例如：2022级" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联系电话">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="电子邮箱">
                  <el-input v-model="profileForm.email" placeholder="请输入常用邮箱" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left" style="margin-top: 30px;">求职意向</el-divider>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="期望岗位">
                  <el-input v-model="profileForm.expectedPosition" placeholder="例如：Java开发工程师" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期望城市">
                  <el-input v-model="profileForm.expectedCity" placeholder="例如：深圳 / 广州" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="期望薪资">
                  <el-select v-model="profileForm.expectedSalary" placeholder="请选择薪资范围" style="width: 100%;">
                    <el-option label="5k-8k" value="5k-8k" />
                    <el-option label="8k-12k" value="8k-12k" />
                    <el-option label="12k-15k" value="12k-15k" />
                    <el-option label="15k以上" value="15k以上" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="box-card resume-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>附件简历</span>
            </div>
          </template>

          <div class="resume-content">
            <el-icon class="doc-icon" :class="{ 'has-file': profileForm.resumeName }">
              <Document />
            </el-icon>

            <div class="file-status">
              <p v-if="profileForm.resumeName" class="file-name">{{ profileForm.resumeName }}</p>
              <p v-else class="no-file">暂未上传简历 (支持 PDF / Word)</p>
            </div>

            <div class="action-group">
              <el-upload
                  class="upload-demo"
                  action="#"
                  :show-file-list="false"
                  :auto-upload="false"
                  :on-change="handleResumeUpload"
                  accept=".pdf,.doc,.docx"
              >
                <el-button type="primary" class="upload-btn" :plain="!!profileForm.resumeName">
                  {{ profileForm.resumeName ? '重新上传' : '点击上传' }}
                </el-button>
              </el-upload>

              <el-button
                  v-if="profileForm.resumeName"
                  type="success"
                  class="view-btn"
                  @click="previewResume"
              >
                查看简历
              </el-button>
            </div>

            <p class="upload-tip">建议上传 PDF 格式，文件大小不超过 5MB</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import request from '@/utils/request'

const studentId = ref(null)
const profileForm = ref({
  major: '', grade: '', phone: '', email: '',
  expectedPosition: '', expectedCity: '', expectedSalary: '',
  resumeName: ''
})

// 初始化获取数据
onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.id) {
    studentId.value = userInfo.id
    fetchProfile()
  }
})

// 获取档案
const fetchProfile = () => {
  request.get(`/student/profile/${studentId.value}`).then(res => {
    if (res.data) {
      profileForm.value = Object.assign(profileForm.value, res.data)
    }
  })
}

// 保存表单数据
const saveProfile = () => {
  const payload = { ...profileForm.value, studentId: studentId.value }
  request.post('/student/profile/save', payload).then(res => {
    ElMessage.success(res.message)
  })
}

// 处理简历上传 (转换为 FormData)
const handleResumeUpload = (file) => {
  const isPdfOrWord = file.raw.type === 'application/pdf' || file.raw.type.includes('word') || file.name.endsWith('.doc') || file.name.endsWith('.docx')
  if (!isPdfOrWord) {
    return ElMessage.error('只能上传 PDF 或 Word 格式的文件！')
  }

  const formData = new FormData()
  formData.append('studentId', studentId.value)
  formData.append('file', file.raw)

  request.post('/student/profile/uploadResume', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    ElMessage.success(res.message)
    profileForm.value.resumeName = res.data // 更新显示的文件名
  })
}

// 预览/下载简历
const previewResume = () => {
  if (!studentId.value) return;
  // 利用浏览器的能力打开后端的下载/预览接口
  const fileUrl = `/api/student/profile/downloadResume/${studentId.value}`;
  window.open(fileUrl, '_blank'); // 在新标签页打开
}
</script>

<style scoped>
.profile-container { padding: 10px; }
.box-card { border-radius: 8px; border: none; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-weight: 600; color: #303133; }
.profile-form { margin-top: 10px; }

/* 简历上传区域专属样式 */
.resume-card { text-align: center; }
.resume-content { padding: 30px 10px; display: flex; flex-direction: column; align-items: center; }
.doc-icon { font-size: 60px; color: #dcdfe6; margin-bottom: 20px; transition: color 0.3s; }
.doc-icon.has-file { color: #409EFF; }
.file-status { margin-bottom: 25px; }
.file-name { margin: 0; font-size: 15px; color: #303133; font-weight: 500; word-break: break-all; }
.no-file { margin: 0; font-size: 14px; color: #909399; }

/* 操作按钮组样式 */
.action-group { display: flex; gap: 15px; justify-content: center; align-items: center; margin-bottom: 5px; }
.upload-btn { border-radius: 20px; }
.view-btn { border-radius: 20px; }

.upload-tip { margin-top: 20px; font-size: 12px; color: #a8abb2; }
</style>