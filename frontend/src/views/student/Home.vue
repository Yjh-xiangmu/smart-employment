<template>
  <div class="home-container">
    <el-carousel height="350px" class="hero-banner" motion-blur>
      <el-carousel-item v-for="(item, index) in banners" :key="index">
        <div class="banner-item" :style="{ background: item.color }">
          <div class="banner-text">
            <h1>{{ item.title }}</h1>
            <p>{{ item.subtitle }}</p>
            <el-button type="primary" size="large" class="banner-btn" @click="$router.push(item.link)">
              {{ item.btnText }}
            </el-button>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>

    <div class="info-section">
      <el-row :gutter="24">
        <el-col :span="12">
          <el-card class="news-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="header-title"><el-icon><Notification /></el-icon> 最新动态</span>
                <el-button link type="primary">更多</el-button>
              </div>
            </template>
            <ul class="news-list">
              <el-empty v-if="homeData.newsList.length === 0" description="暂无新闻" :image-size="60" />
              <li v-for="news in homeData.newsList" :key="news.id" @click="viewNewsDetail(news)">
                <span class="news-title">{{ news.title }}</span>
                <span class="news-time">{{ news.createTime.split(' ')[0] }}</span>
              </li>
            </ul>
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card class="news-card policy-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span class="header-title"><el-icon><Reading /></el-icon> 政策速递</span>
                <el-button link type="primary">更多</el-button>
              </div>
            </template>
            <ul class="news-list">
              <el-empty v-if="homeData.policyList.length === 0" description="暂无政策" :image-size="60" />
              <li v-for="policy in homeData.policyList" :key="policy.id" @click="viewNewsDetail(policy)">
                <span class="news-title">{{ policy.title }}</span>
                <span class="news-time">{{ policy.createTime.split(' ')[0] }}</span>
              </li>
            </ul>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="hot-jobs-section">
      <div class="section-title">
        <h2>名企热招</h2>
        <p>精选高薪好机会，助你斩获心仪 Offer</p>
      </div>

      <el-empty v-if="homeData.hotJobs.length === 0" description="暂时还没有企业发布岗位哦~" />

      <el-row :gutter="20">
        <el-col :span="6" v-for="job in homeData.hotJobs" :key="job.id" style="margin-bottom: 20px;">
          <el-card class="job-card" shadow="hover" @click="$router.push('/student/jobs')">
            <div class="job-header">
              <span class="job-name">{{ job.job_name }}</span>
              <span class="job-salary">{{ job.salary }}</span>
            </div>
            <div class="job-tags">
              <el-tag size="small" type="info" class="custom-tag">{{ job.city }}</el-tag>
              <el-tag size="small" type="info" class="custom-tag">{{ job.experience }}</el-tag>
            </div>
            <div class="company-info">
              <el-icon class="company-icon"><OfficeBuilding /></el-icon>
              <span class="company-name">{{ job.enterprise_name }}</span>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div class="view-more-box">
        <el-button type="primary" plain size="large" @click="$router.push('/student/jobs')" class="view-more-btn">
          查看全部优质岗位 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <el-dialog :title="currentNews?.type === 1 ? '新闻详情' : '政策详情'" v-model="newsDialogVisible" width="600px">
      <div v-if="currentNews" class="news-detail-container">
        <h2 class="detail-title">{{ currentNews.title }}</h2>
        <div class="detail-meta">{{ currentNews.createTime }} · 浏览量: {{ currentNews.viewCount || 0 }}</div>
        <el-divider />
        <div class="detail-content" style="white-space: pre-wrap; line-height: 1.8;">{{ currentNews.content }}</div>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Notification, Reading, OfficeBuilding, ArrowRight } from '@element-plus/icons-vue'
import request from '@/utils/request'

// 首页聚合数据
const homeData = reactive({
  newsList: [],
  policyList: [],
  hotJobs: []
})

// 静态轮播图配置
const banners = ref([
  { title: '2026 届春季校园招聘', subtitle: '海量名企岗位，一键极速投递', btnText: '浏览岗位', link: '/student/jobs', color: 'linear-gradient(135deg, #1890ff 0%, #36cfc9 100%)' },
  { title: '求职干货与面经分享', subtitle: '来看看学长学姐是怎么拿到大厂 Offer 的', btnText: '进入论坛', link: '/student/forum', color: 'linear-gradient(135deg, #722ed1 0%, #eb2f96 100%)' }
])

// 新闻详情弹窗
const newsDialogVisible = ref(false)
const currentNews = ref(null)

onMounted(() => {
  fetchHomeData()
})

const fetchHomeData = () => {
  request.get('/student/home/data').then(res => {
    if (res.data) {
      homeData.newsList = res.data.newsList || []
      homeData.policyList = res.data.policyList || []
      homeData.hotJobs = res.data.hotJobs || []
    }
  })
}

const viewNewsDetail = (news) => {
  currentNews.value = news
  newsDialogVisible.value = true
}
</script>

<style scoped>
.home-container { padding-bottom: 40px; }

/* 轮播图 */
.hero-banner { border-radius: 12px; overflow: hidden; margin-bottom: 30px; box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.banner-item { height: 100%; display: flex; align-items: center; padding: 0 80px; color: white; }
.banner-text h1 { font-size: 36px; margin: 0 0 15px 0; font-weight: 700; letter-spacing: 2px; }
.banner-text p { font-size: 18px; margin: 0 0 30px 0; opacity: 0.9; }
.banner-btn { border-radius: 20px; font-weight: bold; padding: 0 30px; border: none; background: rgba(255,255,255,0.2); color: white; backdrop-filter: blur(4px); }
.banner-btn:hover { background: rgba(255,255,255,0.4); }

/* 资讯与政策 */
.info-section { margin-bottom: 40px; }
.news-card { border-radius: 8px; border: none; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 18px; font-weight: 600; color: #333; display: flex; align-items: center; gap: 8px; }
.news-list { list-style: none; padding: 0; margin: 0; }
.news-list li { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px dashed #ebeef5; cursor: pointer; transition: color 0.2s; }
.news-list li:last-child { border-bottom: none; }
.news-list li:hover { color: #409EFF; }
.news-title { font-size: 14px; width: 75%; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.news-time { font-size: 13px; color: #999; }

/* 热门职位 */
.section-title { text-align: center; margin-bottom: 30px; }
.section-title h2 { font-size: 28px; color: #303133; margin: 0 0 10px 0; }
.section-title p { color: #909399; margin: 0; }

.job-card { border-radius: 8px; cursor: pointer; transition: all 0.3s; border: 1px solid #ebeef5; }
.job-card:hover { transform: translateY(-4px); box-shadow: 0 8px 20px rgba(0,0,0,0.08); border-color: #c6e2ff; }
.job-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.job-name { font-size: 15px; font-weight: 600; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 60%; }
.job-salary { font-size: 15px; font-weight: bold; color: #f56c6c; }
.job-tags { margin-bottom: 15px; }
.custom-tag { margin-right: 8px; background-color: #f8f9fa; border: none; color: #606266; }
.company-info { display: flex; align-items: center; border-top: 1px dashed #ebeef5; padding-top: 12px; }
.company-icon { color: #909399; margin-right: 6px; }
.company-name { font-size: 12px; color: #606266; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.view-more-box { text-align: center; margin-top: 20px; }
.view-more-btn { border-radius: 20px; letter-spacing: 1px; }

/* 新闻详情 */
.detail-title { text-align: center; font-size: 20px; color: #333; margin-top: 0; }
.detail-meta { text-align: center; font-size: 12px; color: #999; margin-bottom: 15px; }
.detail-content { font-size: 15px; color: #333; text-align: justify; }
</style>