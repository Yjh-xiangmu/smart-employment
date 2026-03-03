<template>
  <div class="jobs-portal">
    <div class="search-section">
      <div class="main-search-wrapper">
        <el-input
            v-model="queryParams.keyword"
            placeholder="搜索职位名称或公司名称，例如：Java研发 / 腾讯"
            class="main-search-input"
            size="large"
            clearable
            @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button type="primary" :icon="Search" class="search-btn" @click="handleSearch">
              搜 索
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-box">
        <el-row :gutter="20" justify="center">
          <el-col :span="8">
            <div class="filter-item">
              <span class="filter-label">工作城市：</span>
              <el-select
                  v-model="queryParams.city"
                  placeholder="请选择或输入城市"
                  filterable
                  allow-create
                  clearable
                  @change="handleSearch"
                  style="width: 100%;"
              >
                <el-option label="全国" value="" />
                <el-option v-for="city in hotCities" :key="city" :label="city" :value="city" />
              </el-select>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="filter-item">
              <span class="filter-label">职位分类：</span>
              <el-select
                  v-model="queryParams.category"
                  placeholder="请选择或输入分类"
                  filterable
                  allow-create
                  clearable
                  @change="handleSearch"
                  style="width: 100%;"
              >
                <el-option label="全部" value="" />
                <el-option v-for="cat in hotCategories" :key="cat" :label="cat" :value="cat" />
              </el-select>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <div class="job-list-section" v-loading="loading">
      <el-empty v-if="jobList.length === 0" description="哎呀，没有找到符合条件的岗位~" />

      <el-row :gutter="20" v-else>
        <el-col :span="8" v-for="job in jobList" :key="job.id" style="margin-bottom: 20px;">
          <el-card class="job-card" shadow="hover" @click="viewJobDetail(job)">
            <div class="job-header">
              <span class="job-name">{{ job.job_name }}</span>
              <span class="job-salary">{{ job.salary }}</span>
            </div>
            <div class="job-tags">
              <el-tag size="small" type="info" class="custom-tag">{{ job.city }}</el-tag>
              <el-tag size="small" type="info" class="custom-tag">{{ job.experience }}</el-tag>
              <el-tag size="small" type="info" class="custom-tag">{{ job.education }}</el-tag>
            </div>
            <div class="company-info">
              <el-icon class="company-icon"><OfficeBuilding /></el-icon>
              <span class="company-name">{{ job.enterprise_name }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-drawer v-model="drawerVisible" size="600px" destroy-on-close :with-header="false">
      <div v-if="currentJob" class="drawer-container">
        <el-tabs v-model="activeTab" class="custom-tabs">

          <el-tab-pane label="职位详情" name="job">
            <div class="job-detail-content">
              <div class="detail-header-card">
                <h2 class="detail-title">{{ currentJob.job_name }}</h2>
                <span class="salary-text">{{ currentJob.salary }}</span>
                <div class="detail-tags-large">
                  <el-icon><Location /></el-icon> {{ currentJob.city }} &nbsp;|&nbsp;
                  <el-icon><Suitcase /></el-icon> {{ currentJob.experience }} &nbsp;|&nbsp;
                  <el-icon><Reading /></el-icon> {{ currentJob.education }}
                </div>
              </div>

              <el-divider content-position="left">职位描述与要求</el-divider>
              <div class="job-desc">
                <p style="white-space: pre-wrap; line-height: 1.8;">{{ currentJob.description }}</p>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="企业主页" name="company">
            <div class="company-detail-content" v-loading="companyLoading">
              <template v-if="companyData">
                <div class="company-header-card">
                  <el-avatar :size="60" style="background-color: #409EFF; font-size: 24px;">
                    {{ companyData.company.enterpriseName.charAt(0) }}
                  </el-avatar>
                  <div class="company-titles">
                    <h2>{{ companyData.company.enterpriseName }}</h2>
                    <p>法定代表人: {{ companyData.company.legalPerson }}</p>
                  </div>
                </div>

                <el-divider content-position="left">企业介绍与宣传</el-divider>
                <div class="company-desc">
                  <p v-if="companyData.company.description">{{ companyData.company.description }}</p>
                  <p v-else style="color: #999;">这家企业很低调，还没有填写宣传信息哦~</p>
                </div>

                <el-divider content-position="left">该企业的其他在招岗位 ({{ companyData.jobs.length }})</el-divider>
                <div class="company-jobs-list">
                  <div class="mini-job-card" v-for="cJob in companyData.jobs" :key="cJob.id" @click="switchToJob(cJob)">
                    <div class="mj-left">
                      <span class="mj-title">{{ cJob.jobName }}</span>
                      <span class="mj-tags">{{ cJob.city }} | {{ cJob.experience }} | {{ cJob.education }}</span>
                    </div>
                    <div class="mj-right">
                      <span class="mj-salary">{{ cJob.salary }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </el-tab-pane>

        </el-tabs>

        <div class="drawer-footer">
          <el-button type="primary" size="large" class="apply-btn" @click="handleApply">立即投递该岗位</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, OfficeBuilding, Location, Suitcase, Reading } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const jobList = ref([])

// 搜索和筛选参数
const queryParams = reactive({ keyword: '', city: '', category: '' })

// 预设的热门搜索选项 (用户也可以自由打字输入)
const hotCities = ref(['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉'])
const hotCategories = ref(['后端开发', '前端开发', '产品经理', 'UI设计', '市场运营', '人力资源', '算法工程师'])

// 抽屉与详情数据
const drawerVisible = ref(false)
const activeTab = ref('job') // 默认显示职位详情
const currentJob = ref(null)

const companyLoading = ref(false)
const companyData = ref(null) // 存放当前职位所在企业的详细信息及全部岗位

onMounted(() => {
  handleSearch()
})

const handleSearch = () => {
  loading.value = true
  request.get('/student/job/search', { params: queryParams }).then(res => {
    jobList.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

// 点击岗位卡片，打开抽屉
const viewJobDetail = (job) => {
  currentJob.value = job
  activeTab.value = 'job' // 每次点开默认在岗位tab
  drawerVisible.value = true
  fetchCompanyDetail(job.enterprise_id) // 后台悄悄拉取该企业的详情
}

// 获取企业主页及旗下所有岗位
const fetchCompanyDetail = (enterpriseId) => {
  companyLoading.value = true
  request.get(`/student/job/company/${enterpriseId}`).then(res => {
    companyData.value = res.data
  }).finally(() => {
    companyLoading.value = false
  })
}

// 在企业主页点击其他岗位时，直接切换过去
const switchToJob = (cJob) => {
  // 因为是从独立表查出来的，字段名是驼峰，我们需要将它转成跟列表一致的下划线格式或者直接渲染
  currentJob.value = {
    id: cJob.id,
    enterprise_id: cJob.enterpriseId,
    job_name: cJob.jobName,
    salary: cJob.salary,
    city: cJob.city,
    experience: cJob.experience,
    education: cJob.education,
    description: cJob.description,
    enterprise_name: companyData.value.company.enterpriseName
  }
  activeTab.value = 'job' // 自动切回职位详情 tab
}

// 真实的投递逻辑
const handleApply = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    return ElMessage.warning('登录状态异常，请重新登录！')
  }

  // 构造投递参数
  const payload = {
    studentId: userInfo.id,
    enterpriseId: currentJob.value.enterprise_id,
    jobId: currentJob.value.id
  }

  // 显示加载状态，防止连击
  const loadingInstance = ElMessage({
    message: '正在发送简历，请稍候...',
    type: 'info',
    duration: 0
  })

  request.post('/student/delivery/apply', payload).then(res => {
    loadingInstance.close()
    ElMessage.success(res.message)
    drawerVisible.value = false // 投递成功后关闭抽屉
  }).catch(() => {
    loadingInstance.close()
  })
}
</script>

<style scoped>
.jobs-portal { padding: 10px 0; }

/* 极简高级搜索区 */
.search-section { background: #fff; padding: 40px 30px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.04); margin-bottom: 24px; text-align: center; }
.main-search-wrapper { max-width: 700px; margin: 0 auto 30px; }
/* 深度修改搜索框，消除割裂感 */
:deep(.main-search-input .el-input-group__append) {
  background-color: #409EFF;
  color: white;
  border: 1px solid #409EFF;
  border-left: 0;
  box-shadow: none;
}
:deep(.main-search-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding-left: 15px;
}
:deep(.main-search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409EFF inset;
}
.search-btn { font-size: 16px; padding: 0 25px; letter-spacing: 2px; }

.filter-box { max-width: 800px; margin: 0 auto; }
.filter-item { display: flex; align-items: center; }
.filter-label { font-size: 14px; color: #606266; font-weight: bold; width: 80px; flex-shrink: 0; text-align: right; }

/* 岗位卡片 */
.job-card { border-radius: 8px; cursor: pointer; transition: all 0.3s; border: 1px solid #ebeef5; }
.job-card:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.08); border-color: #c6e2ff; }
.job-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.job-name { font-size: 16px; font-weight: 600; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 65%; }
.job-salary { font-size: 16px; font-weight: bold; color: #f56c6c; }
.job-tags { margin-bottom: 15px; }
.custom-tag { margin-right: 8px; background-color: #f8f9fa; border: none; color: #606266; }
.company-info { display: flex; align-items: center; border-top: 1px dashed #ebeef5; padding-top: 12px; }
.company-icon { color: #909399; margin-right: 6px; }
.company-name { font-size: 13px; color: #606266; }

/* 抽屉与 Tabs 样式 */
.drawer-container { height: 100vh; display: flex; flex-direction: column; }
.custom-tabs { flex: 1; padding: 20px 30px 0; overflow-y: auto; }
:deep(.el-tabs__item) { font-size: 16px; font-weight: 500; }
:deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #ebeef5; }

/* 标签一：职位详情 */
.detail-header-card { background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
.detail-title { font-size: 24px; color: #303133; margin: 0 0 10px 0; }
.salary-text { font-size: 20px; font-weight: bold; color: #f56c6c; display: block; margin-bottom: 15px; }
.detail-tags-large { color: #606266; font-size: 14px; display: flex; align-items: center; }
.job-desc { font-size: 14px; color: #333; text-align: justify; padding-bottom: 100px; }

/* 标签二：企业主页 */
.company-header-card { display: flex; align-items: center; gap: 20px; padding: 10px 0 20px; }
.company-titles h2 { margin: 0 0 8px 0; font-size: 20px; color: #333; }
.company-titles p { margin: 0; font-size: 14px; color: #666; }
.company-desc { font-size: 14px; color: #666; line-height: 1.6; text-align: justify; }

/* 企业旗下其他职位列表 */
.company-jobs-list { display: flex; flex-direction: column; gap: 12px; padding-bottom: 100px; }
.mini-job-card { display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #fafafa; border-radius: 6px; cursor: pointer; transition: background 0.2s; border: 1px solid #ebeef5; }
.mini-job-card:hover { background: #f0f7ff; border-color: #c6e2ff; }
.mj-left { display: flex; flex-direction: column; gap: 6px; }
.mj-title { font-size: 15px; font-weight: 600; color: #333; }
.mj-tags { font-size: 12px; color: #999; }
.mj-salary { font-size: 15px; font-weight: bold; color: #f56c6c; }

/* 底部固定投递按钮 */
.drawer-footer { position: fixed; bottom: 0; right: 0; width: 600px; background: rgba(255,255,255,0.9); backdrop-filter: blur(10px); padding: 20px 0; text-align: center; border-top: 1px solid #ebeef5; z-index: 10; }
.apply-btn { width: 80%; border-radius: 8px; letter-spacing: 2px; font-weight: bold; }
</style>