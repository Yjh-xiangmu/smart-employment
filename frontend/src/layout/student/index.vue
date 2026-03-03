<template>
  <div class="student-layout">
    <el-header class="nav-header">
      <div class="nav-container">
        <div class="logo-box" @click="router.push('/student/home')">
          <el-icon class="logo-icon"><Promotion /></el-icon>
          <span class="logo-text">智慧就业平台</span>
        </div>

        <el-menu :default-active="$route.path" class="top-menu" mode="horizontal" :ellipsis="false" router>
          <el-menu-item index="/student/home">首页</el-menu-item>
          <el-menu-item index="/student/jobs">岗位浏览</el-menu-item>
          <el-menu-item index="/student/forum">求职论坛</el-menu-item>
        </el-menu>

        <div class="user-actions">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="msg-badge" type="danger">
            <el-icon class="bell-icon" @click="router.push('/student/messages')"><Bell /></el-icon>
          </el-badge>

          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="36" src="https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png" />
              <span class="username">{{ studentName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown">
                <el-dropdown-item @click="router.push('/student/profile')">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/student/applications')">
                  <el-icon><Document /></el-icon> 我的投递
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <el-main class="main-content">
      <div class="page-container">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </el-main>

    <el-footer class="footer">
      <div class="footer-content">
        <p>© 2026 智慧就业招聘信息管理系统 - Designed by Jalen</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted , onUnmounted} from 'vue'
import { useRouter } from 'vue-router'
import { Promotion, Bell, CaretBottom, User, Document, SwitchButton } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const studentName = ref('同学')
const unreadCount = ref(0) // 新增：未读消息数量状态

onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.name) {
    studentName.value = userInfo.name
  }
  // 如果登录了，拉取未读消息数量
  if (userInfo.id) {
    fetchUnreadCount(userInfo.id)
    // 【新增】竖起耳朵监听广播
    window.addEventListener('update-unread-count', () => {
      fetchUnreadCount(userInfo.id)})
  }
})
onUnmounted(() => {
  window.removeEventListener('update-unread-count', fetchUnreadCount)
})
// 拉取未读消息数
const fetchUnreadCount = (studentId) => {
  request.get(`/student/message/list/${studentId}`).then(res => {
    unreadCount.value = res.data.unreadCount || 0
  })
}

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
/* 保持极简高级样式不变 */
.student-layout { min-height: 100vh; display: flex; flex-direction: column; background-color: #f5f7fa; }
.nav-header { position: sticky; top: 0; z-index: 100; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-bottom: 1px solid #ebeef5; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04); padding: 0; height: 64px; }
.nav-container { max-width: 1200px; margin: 0 auto; height: 100%; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; }
.logo-box { display: flex; align-items: center; cursor: pointer; }
.logo-icon { font-size: 28px; color: #409EFF; }
.logo-text { margin-left: 10px; font-weight: 600; font-size: 18px; color: #303133; }
.top-menu { flex: 1; margin-left: 40px; border-bottom: none; background: transparent; }
.el-menu--horizontal > .el-menu-item { height: 64px; line-height: 64px; font-size: 16px; color: #606266; border-bottom: 2px solid transparent; transition: all 0.3s; }
.el-menu--horizontal > .el-menu-item:hover { color: #409EFF; background: transparent; }
.el-menu--horizontal > .el-menu-item.is-active { color: #409EFF !important; border-bottom: 2px solid #409EFF !important; font-weight: 500; }
.user-actions { display: flex; align-items: center; }
.msg-badge { margin-right: 30px; cursor: pointer; }
.bell-icon { font-size: 22px; color: #606266; transition: color 0.3s; }
.bell-icon:hover { color: #409EFF; }
.user-info { display: flex; align-items: center; cursor: pointer; color: #303133; }
.username { margin: 0 8px; font-size: 14px; font-weight: 500; }
.main-content { flex: 1; padding: 30px 20px; }
.page-container { max-width: 1200px; margin: 0 auto; min-height: calc(100vh - 200px); }
.footer { background-color: #fff; border-top: 1px solid #ebeef5; padding: 30px 0; height: auto; }
.footer-content { max-width: 1200px; margin: 0 auto; text-align: center; color: #606266; font-size: 14px; }
.fade-slide-leave-active, .fade-slide-enter-active { transition: all 0.4s cubic-bezier(0.2, 0, 0, 1); }
.fade-slide-enter-from { opacity: 0; transform: translateY(15px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-15px); }
</style>