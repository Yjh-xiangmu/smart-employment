<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span style="font-size: 18px; font-weight: bold;">企业信息维护</span>
          <el-button type="primary" @click="submitForm">保存修改</el-button>
        </div>
      </template>

      <el-form
          :model="form"
          :rules="rules"
          ref="profileFormRef"
          label-width="120px"
          style="max-width: 800px; margin-top: 20px;"
      >
        <el-form-item label="企业账号">
          <el-input v-model="form.account" disabled placeholder="账号不可修改" />
        </el-form-item>

        <el-form-item label="企业名称" prop="enterpriseName">
          <el-input v-model="form.enterpriseName" placeholder="请输入企业名称" />
        </el-form-item>

        <el-form-item label="企业法人" prop="legalPerson">
          <el-input v-model="form.legalPerson" placeholder="请输入法人姓名" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="form.contactPerson" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-select v-model="form.industry" placeholder="请选择所属行业" style="width: 100%;">
                <el-option label="互联网/IT/软件" value="互联网/IT/软件" />
                <el-option label="金融/银行/投资" value="金融/银行/投资" />
                <el-option label="教育/培训/科研" value="教育/培训/科研" />
                <el-option label="制造/工业/机械" value="制造/工业/机械" />
                <el-option label="医疗/医药/健康" value="医疗/医药/健康" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业规模" prop="scale">
              <el-select v-model="form.scale" placeholder="请选择企业规模" style="width: 100%;">
                <el-option label="0-50人" value="0-50人" />
                <el-option label="50-150人" value="50-150人" />
                <el-option label="150-500人" value="150-500人" />
                <el-option label="500-2000人" value="500-2000人" />
                <el-option label="2000人以上" value="2000人以上" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="企业详细地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细办公地址，方便学生了解" />
        </el-form-item>

        <el-form-item label="企业简介" prop="description">
          <el-input
              type="textarea"
              v-model="form.description"
              :rows="6"
              placeholder="请输入详细的企业介绍，吸引更多优秀学生投递..."
          />
        </el-form-item>

      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request' // 确保你的 axios 封装文件在这个路径

const profileFormRef = ref(null)

const form = ref({
  id: null,
  account: '',
  enterpriseName: '',
  legalPerson: '',
  contactPerson: '',
  contactPhone: '',
  industry: '',
  scale: '',
  address: '',
  description: ''
})

const rules = {
  enterpriseName: [{ required: true, message: '企业名称不能为空', trigger: 'blur' }],
  legalPerson: [{ required: true, message: '法人姓名不能为空', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '联系人不能为空', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }]
}

const getEnterpriseId = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return userInfo.id
}

const loadProfile = async () => {
  const id = getEnterpriseId()
  if (!id) {
    ElMessage.error('未获取到企业登录信息，请重新登录')
    return
  }
  try {
    const res = await request.get(`/enterprise/profile/${id}`)
    if (res.code === 200 && res.data) {
      // 将后端返回的数据合并到表单中
      form.value = Object.assign({}, form.value, res.data)
    }
  } catch (error) {
    console.error(error)
  }
}

// 2. 具体的保存逻辑
const submitForm = () => {
  profileFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 3. 这里向你刚才写的 EnterpriseController 的 update 接口发送请求
        const res = await request.put('/enterprise/update', form.value)

        if (res.code === 200) {
          ElMessage.success('企业信息保存成功！')
          // 更新本地存储（用于侧边栏或顶部的名称展示）
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          userInfo.enterpriseName = form.value.enterpriseName
          localStorage.setItem('userInfo', JSON.stringify(userInfo))
        } else {
          ElMessage.error(res.msg || '保存失败')
        }
      } catch (error) {
        console.error(error)
      }
    } else {
      ElMessage.warning('请完善必填信息！')
    }
  })
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>