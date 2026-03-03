<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo-box">
        <el-icon class="logo-icon"><Platform /></el-icon>
        <transition name="fade">
          <span v-show="!isCollapse" class="logo-text">总控管理系统</span>
        </transition>
      </div>

      <el-menu
          :default-active="$route.path"
          class="el-menu-vertical"
          background-color="#001529"
          text-color="#a6adb4"
          active-text-color="#fff"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Monitor /></el-icon>
          <template #title>企业资质审核</template>
        </el-menu-item>
        <el-menu-item index="/admin/students">
          <el-icon><User /></el-icon>
          <template #title>学生账号管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/jobs">
          <el-icon><Briefcase /></el-icon>
          <template #title>职位招聘管理</template>
        </el-menu-item>

        <el-menu-item index="/admin/resumes">
          <el-icon><Document /></el-icon>
          <template #title>学生简历管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/applications">
          <el-icon><Promotion /></el-icon>
          <template #title>简历投递管理</template>
        </el-menu-item>

        <el-menu-item index="/admin/news">
          <el-icon><Notification /></el-icon>
          <template #title>新闻资讯管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/forum">
          <el-icon><ChatSquare /></el-icon>
          <template #title>论坛社区管理</template>
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
            <el-breadcrumb-item>管理员控制台</el-breadcrumb-item>
            <el-breadcrumb-item>{{ $route.name || '概览' }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
              <span class="username">超级管理员</span>
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
// 引入所有需要的 Element Plus 图标
import {
  Platform, Monitor, User, Briefcase,
  Document, Promotion, Notification, ChatSquare,
  Fold, Expand, CaretBottom
} from '@element-plus/icons-vue'

const isCollapse = ref(false)
const router = useRouter()

const handleLogout = () => {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { height: 100vh; overflow: hidden; }
/* 侧边栏：极简深蓝色 */
.aside {
  background-color: #001529;
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
  background-color: #002140;
  overflow: hidden;
}
.logo-icon { font-size: 24px; color: #1890ff; }
.logo-text { margin-left: 10px; font-weight: 600; font-size: 16px; white-space: nowrap; }
.el-menu-vertical { border-right: none; }
.el-menu-vertical:not(.el-menu--collapse) { width: 220px; }
/* 激活态的高级感 */
.el-menu-item.is-active { background-color: #1890ff !important; color: white !important; }

.main-container { background-color: #f0f2f5; display: flex; flex-direction: column; }
/* 顶部导航：纯白带轻微阴影 */
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
.collapse-btn:hover { color: #1890ff; }
.user-info { display: flex; align-items: center; cursor: pointer; color: #333; }
.username { margin: 0 8px; font-size: 14px; }

/* 内容区留白 */
.main-content { padding: 24px; flex: 1; overflow-y: auto; }

/* 路由切换过渡动画 */
.fade-transform-leave-active, .fade-transform-enter-active { transition: all 0.4s cubic-bezier(0.2, 0, 0, 1); }
.fade-transform-enter-from { opacity: 0; transform: translateX(-20px); }
.fade-transform-leave-to { opacity: 0; transform: translateX(20px); }
</style>