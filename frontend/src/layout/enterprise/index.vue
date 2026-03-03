<template>
  <el-container class="enterprise-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo-box">
        <el-icon class="logo-icon"><OfficeBuilding /></el-icon>
        <transition name="fade">
          <span v-show="!isCollapse" class="logo-text">企业招聘中心</span>
        </transition>
      </div>

      <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#2b2f3a"
          text-color="#bfcbd9"
          active-text-color="#ffffff"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
      >
        <el-menu-item index="/enterprise/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台概览</template>
        </el-menu-item>

        <el-menu-item index="/enterprise/jobs">
          <el-icon><Briefcase /></el-icon>
          <template #title>职位招聘管理</template>
        </el-menu-item>
        <el-menu-item index="/enterprise/resumes">
          <el-icon><DocumentChecked /></el-icon>
          <template #title>简历投递处理</template>
        </el-menu-item>

        <el-menu-item index="/enterprise/promotion">
          <el-icon><DataLine /></el-icon>
          <template #title>企业宣传管理</template>
        </el-menu-item>
        <el-menu-item index="/enterprise/profile">
          <el-icon><Setting /></el-icon>
          <template #title>企业信息维护</template>
        </el-menu-item>

        <el-menu-item index="/enterprise/messages">
          <el-icon><Bell /></el-icon>
          <template #title>系统消息中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>企业中心</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.name || '概览' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="3" class="msg-badge" type="danger">
            <el-icon class="bell-icon"><Bell /></el-icon>
          </el-badge>

          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
              <span class="username">{{ enterpriseName }}</span>
              <el-icon><CaretBottom /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  OfficeBuilding, Odometer, Briefcase, DocumentChecked,
  DataLine, Setting, Bell, Fold, Expand, CaretBottom
} from '@element-plus/icons-vue'

const isCollapse = ref(false)
const router = useRouter()
const enterpriseName = ref('企业用户')

// 页面加载时，从本地存储读取当前登录的企业信息
onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (userInfo.enterpriseName) {
    enterpriseName.value = userInfo.enterpriseName
  }
})

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.enterprise-layout { height: 100vh; overflow: hidden; }

/* 企业端侧边栏：深空灰 */
.aside {
  background-color: #2b2f3a;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1) 0s;
  box-shadow: 2px 0 8px 0 rgba(29,35,41,.05);
  z-index: 10;
}
.logo-box {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background-color: #2b2f3a;
  border-bottom: 1px solid #1f222a;
  overflow: hidden;
}
.logo-icon { font-size: 24px; color: #409EFF; }
.logo-text { margin-left: 10px; font-weight: 600; font-size: 16px; white-space: nowrap; letter-spacing: 1px; }
.el-menu-vertical { border-right: none; }
.el-menu-vertical:not(.el-menu--collapse) { width: 220px; }
.el-menu-item.is-active { background-color: #409EFF !important; color: white !important; }

.main-container { background-color: #f0f2f5; display: flex; flex-direction: column; }

/* 顶部导航 */
.header {
  background-color: #fff;
  height: 60px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 9;
}
.header-left { display: flex; align-items: center; }
.collapse-btn { font-size: 20px; cursor: pointer; margin-right: 20px; color: #666; transition: color 0.3s; }
.collapse-btn:hover { color: #409EFF; }

.header-right { display: flex; align-items: center; }
.msg-badge { margin-right: 25px; cursor: pointer; display: flex; align-items: center; }
.bell-icon { font-size: 20px; color: #666; transition: color 0.3s; }
.bell-icon:hover { color: #409EFF; }

.user-info { display: flex; align-items: center; cursor: pointer; color: #333; }
.username { margin: 0 8px; font-size: 14px; font-weight: 500; }

.main-content { padding: 24px; flex: 1; overflow-y: auto; }

.fade-transform-leave-active, .fade-transform-enter-active { transition: all 0.4s cubic-bezier(0.2, 0, 0, 1); }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }
</style>