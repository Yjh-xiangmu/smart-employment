import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        redirect: '/login' // 根目录默认跳到登录页
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue')
    },
    // ================= 学生端路由 =================
    {
        path: '/student',
        component: () => import('@/layout/student/index.vue'),
        children: [
            { path: 'home', component: () => import('@/views/student/Home.vue') },
            { path: 'profile', component: () => import('@/views/student/Profile.vue') },
            { path: 'jobs', component: () => import('@/views/student/Jobs.vue') },
            { path: 'applications', component: () => import('@/views/student/Applications.vue') },
            { path: 'messages', component: () => import('@/views/student/Messages.vue') },
            { path: 'forum', component: () => import('@/views/student/Forum.vue') },
            // 后续学生的 个人中心、岗位浏览、投递记录 都在这里加
        ]
    },
    // ================= 企业端路由 =================
    {
        path: '/enterprise',
        component: () => import('@/layout/enterprise/index.vue'),
        children: [
            { path: 'dashboard', component: () => import('@/views/enterprise/Dashboard.vue') },
            { path: 'jobs', component: () => import('@/views/enterprise/Jobs.vue') },
            { path: 'resumes', component: () => import('@/views/enterprise/Resumes.vue') },
            { path: 'profile', component: () => import('@/views/enterprise/Profile.vue') },
            { path: 'promotion', component: () => import('@/views/enterprise/Promotion.vue') },
            { path: 'messages', component: () => import('@/views/enterprise/Messages.vue') }
            // 后续企业的 职位发布、简历管理 都在这里加
        ]
    },
    // ================= 管理员路由 =================
    {
        path: '/admin',
        component: () => import('@/layout/admin/index.vue'),
        children: [
            { path: 'dashboard', component: () => import('@/views/admin/Dashboard.vue') },
            { path: 'forum', component: () => import('@/views/admin/Forum.vue') },
            { path: 'news', component: () => import('@/views/admin/News.vue') },
            { path: 'students', component: () => import('@/views/admin/Student.vue') },
            { path: 'jobs', component: () => import('@/views/admin/Job.vue') },
            { path: 'resumes', component: () => import('@/views/admin/Resume.vue') },
            { path: 'applications', component: () => import('@/views/admin/Delivery.vue') }
            // 后续管理员的 企业审核、新闻发布 都在这里加
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router