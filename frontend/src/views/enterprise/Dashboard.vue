<template>
  <div class="dashboard-container">
    <div class="welcome-section">
      <div class="welcome-text">
        <h2>早安，{{ enterpriseName }}！欢迎回到智慧就业招聘中心。</h2>
        <p>今天是获取优秀人才的好日子，看看您的招聘进度吧。</p>
      </div>
      <img src="https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48png.png" alt="welcome" class="welcome-img" />
    </div>

    <el-row :gutter="20" class="data-cards" v-loading="loading">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card blue-card">
          <div class="stat-icon"><el-icon><Briefcase /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">正在热招职位</div>
            <div class="stat-value">{{ stats.activeJobs }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card purple-card">
          <div class="stat-icon"><el-icon><DocumentCopy /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">收到简历总数</div>
            <div class="stat-value">{{ stats.totalResumes }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card orange-card">
          <div class="stat-icon"><el-icon><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">待处理简历</div>
            <div class="stat-value highlight">{{ stats.pendingResumes }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card green-card">
          <div class="stat-icon"><el-icon><Trophy /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">已发面试邀约</div>
            <div class="stat-value">{{ stats.inviteCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="quick-actions">
      <h3>常用快捷操作</h3>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="action-box" @click="$router.push('/enterprise/jobs')">
            <el-icon class="action-icon" style="color: #409EFF;"><Position /></el-icon>
            <span>发布新职位</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="action-box" @click="$router.push('/enterprise/resumes')">
            <el-badge :value="stats.pendingResumes" :hidden="stats.pendingResumes === 0" class="item">
              <el-icon class="action-icon" style="color: #E6A23C;"><Filter /></el-icon>
            </el-badge>
            <span>处理新简历</span>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="action-box" @click="$router.push('/enterprise/profile')">
            <el-icon class="action-icon" style="color: #67C23A;"><OfficeBuilding /></el-icon>
            <span>完善企业主页</span>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Briefcase, DocumentCopy, Bell, Trophy, Position, Filter, OfficeBuilding } from '@element-plus/icons-vue'
import request from '@/utils/request'

const enterpriseName = ref('')
const enterpriseId = ref(null)
const loading = ref(false)

const stats = reactive({
  activeJobs: 0,
  totalResumes: 0,
  pendingResumes: 0,
  inviteCount: 0
})

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.name) {
    enterpriseName.value = userInfo.name
  }
  if (userInfo.id) {
    enterpriseId.value = userInfo.id
    fetchStats()
  }
})

const fetchStats = () => {
  loading.value = true
  request.get(`/enterprise/dashboard/stats/${enterpriseId.value}`).then(res => {
    if (res.data) {
      stats.activeJobs = res.data.activeJobs || 0
      stats.totalResumes = res.data.totalResumes || 0
      stats.pendingResumes = res.data.pendingResumes || 0
      stats.inviteCount = res.data.inviteCount || 0
    }
  }).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.dashboard-container { padding: 10px; }

/* 欢迎区 */
.welcome-section { display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%); padding: 30px 40px; border-radius: 12px; margin-bottom: 30px; color: white; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
.welcome-text h2 { margin: 0 0 10px 0; font-size: 24px; font-weight: bold; text-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.welcome-text p { margin: 0; font-size: 15px; opacity: 0.9; }
.welcome-img { height: 80px; opacity: 0.9; }

/* 数据卡片 */
.data-cards { margin-bottom: 40px; }
.stat-card { border-radius: 12px; border: none; display: flex; align-items: center; padding: 10px; }
:deep(.stat-card .el-card__body) { display: flex; align-items: center; width: 100%; }
.stat-icon { width: 60px; height: 60px; border-radius: 12px; display: flex; justify-content: center; align-items: center; font-size: 28px; margin-right: 15px; }
.stat-info { flex: 1; }
.stat-title { font-size: 14px; color: #909399; margin-bottom: 8px; font-weight: 500; }
.stat-value { font-size: 28px; font-weight: bold; color: #303133; }
.highlight { color: #f56c6c; }

/* 不同卡片的颜色主题 */
.blue-card .stat-icon { background: #e6f1fc; color: #409EFF; }
.purple-card .stat-icon { background: #f3e8ff; color: #909399; }
.orange-card .stat-icon { background: #fdf6ec; color: #E6A23C; }
.green-card .stat-icon { background: #f0f9eb; color: #67C23A; }

/* 快捷操作 */
.quick-actions h3 { font-size: 18px; color: #303133; margin-bottom: 20px; font-weight: 600; }
.action-box { background: #fff; padding: 30px; border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: all 0.3s; box-shadow: 0 2px 12px rgba(0,0,0,0.04); border: 1px solid transparent; }
.action-box:hover { transform: translateY(-5px); border-color: #c6e2ff; box-shadow: 0 8px 20px rgba(64,158,255,0.1); }
.action-icon { font-size: 40px; margin-bottom: 15px; }
.action-box span { font-size: 15px; color: #606266; font-weight: 500; }
</style>