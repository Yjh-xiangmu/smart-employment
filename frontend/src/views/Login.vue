<template>
  <div class="login-container">
    <div class="login-box">
      <div class="box-header">
        <h2 class="title">{{ mode === 'login' ? '欢迎回来' : (mode === 'register' ? '创建账号' : '找回密码') }}</h2>
        <p class="subtitle">{{ mode === 'login' ? '登录智慧就业管理系统' : (mode === 'register' ? '注册新用户' : '通过学号和姓名重置密码') }}</p>
      </div>

      <el-form :model="formData" :rules="rules" ref="formRef" size="large">

        <el-form-item v-if="mode !== 'forgot'">
          <el-radio-group v-model="formData.role" class="role-group">
            <el-radio label="student">学生</el-radio>
            <el-radio label="enterprise">企业</el-radio>
            <el-radio label="admin" v-if="mode === 'login'">管理员</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item prop="account">
          <el-input v-model="formData.account" :placeholder="accountPlaceholder" prefix-icon="User" />
        </el-form-item>

        <transition name="fade-slide">
          <el-form-item prop="name" v-if="(mode === 'register' || mode === 'forgot') && formData.role === 'student'">
            <el-input v-model="formData.name" placeholder="请输入真实姓名" prefix-icon="Postcard" />
          </el-form-item>
        </transition>

        <transition name="fade-slide">
          <div v-if="mode === 'register' && formData.role === 'enterprise'">
            <el-form-item prop="enterpriseName">
              <el-input v-model="formData.enterpriseName" placeholder="请输入企业名称" prefix-icon="OfficeBuilding" />
            </el-form-item>
            <el-form-item prop="legalPerson">
              <el-input v-model="formData.legalPerson" placeholder="请输入法人代表姓名" prefix-icon="Avatar" />
            </el-form-item>
            <el-form-item prop="licenseFile" class="upload-item">
              <el-upload class="license-uploader" action="#" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange" accept="image/*">
                <img v-if="imageUrl" :src="imageUrl" class="uploaded-img" />
                <el-icon v-else class="uploader-icon"><Plus /></el-icon>
                <div class="el-upload__text" v-if="!imageUrl">点击上传营业执照</div>
              </el-upload>
            </el-form-item>
          </div>
        </transition>

        <el-form-item prop="password">
          <el-input v-model="formData.password" type="password" :placeholder="mode === 'forgot' ? '请输入新密码' : '请输入密码'" show-password prefix-icon="Lock" @keyup.enter="handleSubmit" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit">
            {{ mode === 'login' ? '登 录' : (mode === 'register' ? '注 册' : '重 置 密 码') }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="box-footer">
        <template v-if="mode === 'login'">
          <span class="action-text" @click="changeMode('register')" v-if="formData.role !== 'admin'">没有账号？点击注册</span>
          <span class="split-line" v-if="formData.role === 'student'"> | </span>
          <span class="action-text" @click="changeMode('forgot')" v-if="formData.role === 'student'">忘记密码？</span>
        </template>
        <template v-else>
          <span class="action-text" @click="changeMode('login')">返回登录</span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { User, Lock, Postcard, OfficeBuilding, Avatar, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const mode = ref('login') // login, register, forgot
const loading = ref(false)
const formRef = ref(null)
const imageUrl = ref('')
const licenseFile = ref(null)

const formData = reactive({ role: 'student', account: '', password: '', name: '', enterpriseName: '', legalPerson: '' })

const accountPlaceholder = computed(() => {
  if (formData.role === 'student') return '请输入学号'
  if (formData.role === 'enterprise') return '请输入社会信用代码/企业账号'
  return '请输入管理员账号'
})

const rules = reactive({
  account: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
})

const changeMode = (newMode) => {
  mode.value = newMode
  if (newMode === 'forgot') formData.role = 'student' // 找回密码默认锁定为学生
  formRef.value.resetFields()
  imageUrl.value = ''
  licenseFile.value = null
}

const handleFileChange = (uploadFile) => {
  const file = uploadFile.raw
  if (!file.type.startsWith('image/')) return ElMessage.error('只能上传图片文件！')
  licenseFile.value = file
  imageUrl.value = URL.createObjectURL(file)
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      let url = ''

      // 1. 找回密码流程
      if (mode.value === 'forgot') {
        if (!formData.name) { loading.value = false; return ElMessage.warning('请输入真实姓名验证身份') }
        request.post('/student/forgotPassword', {
          studentNo: formData.account, name: formData.name, password: formData.password
        }).then(res => {
          ElMessage.success(res.message)
          changeMode('login')
        }).finally(() => { loading.value = false })
        return
      }

      // 2. 企业注册流程 (FormData)
      if (mode.value === 'register' && formData.role === 'enterprise') {
        url = '/enterprise/register'
        const formPayload = new FormData()
        formPayload.append('account', formData.account)
        formPayload.append('password', formData.password)
        formPayload.append('enterpriseName', formData.enterpriseName)
        formPayload.append('legalPerson', formData.legalPerson)
        formPayload.append('licenseImage', licenseFile.value)
        request.post(url, formPayload, { headers: { 'Content-Type': 'multipart/form-data' } }).then(res => {
          ElMessage.success(res.message)
          changeMode('login')
        }).finally(() => { loading.value = false })
        return
      }

      // 3. 常规登录/注册流程
      let payload = {}
      if (formData.role === 'student') {
        url = mode.value === 'login' ? '/student/login' : '/student/register'
        payload = { studentNo: formData.account, password: formData.password, name: formData.name }
      } else if (formData.role === 'enterprise') {
        url = '/enterprise/login'; payload = { account: formData.account, password: formData.password }
      } else if (formData.role === 'admin') {
        url = '/admin/login'; payload = { username: formData.account, password: formData.password }
      }

      request.post(url, payload).then(res => {
        ElMessage.success(res.message)
        if (mode.value === 'login') {
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          localStorage.setItem('userRole', formData.role)
          if (formData.role === 'student') router.push('/student/home')
          else if (formData.role === 'admin') router.push('/admin/dashboard')
          else if (formData.role === 'enterprise') router.push('/enterprise/dashboard')
        } else {
          changeMode('login')
        }
      }).finally(() => { loading.value = false })
    }
  })
}
</script>

<style scoped>
/* 样式保留，仅增加 split-line 样式 */
.login-container { height: 100vh; display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); overflow: hidden;}
.login-box { width: 400px; padding: 40px 40px; background: rgba(255, 255, 255, 0.75); backdrop-filter: blur(12px); border-radius: 16px; border: 1px solid rgba(255, 255, 255, 0.3); box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08); animation: slideUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;}
@keyframes slideUp { to { opacity: 1; transform: translateY(0); } }
.box-header { text-align: center; margin-bottom: 25px; }
.title { margin: 0; font-size: 28px; color: #2c3e50; font-weight: 600; }
.subtitle { margin-top: 10px; font-size: 14px; color: #7f8c8d; }
.role-group { display: flex; justify-content: center; width: 100%; margin-bottom: 10px; }
.submit-btn { width: 100%; border-radius: 8px; font-size: 16px; letter-spacing: 2px; transition: all 0.3s ease; }
.submit-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3); }
.box-footer { text-align: center; margin-top: 15px; height: 20px; }
.action-text { font-size: 14px; color: #409EFF; cursor: pointer; }
.action-text:hover { color: #66b1ff; text-decoration: underline; }
.split-line { color: #ccc; margin: 0 10px; }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.4s ease; }
.fade-slide-enter-from, .fade-slide-leave-to { opacity: 0; transform: translateY(-10px); }
.upload-item { display: flex; justify-content: center; }
.license-uploader { border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 100%; text-align: center; background-color: #fafafa; transition: border-color 0.3s; }
.license-uploader:hover { border-color: #409EFF; }
.uploader-icon { font-size: 28px; color: #8c939d; margin-top: 20px;}
.el-upload__text { font-size: 12px; color: #666; margin-bottom: 20px; }
.uploaded-img { width: 100%; height: 120px; object-fit: cover; display: block; }
</style>