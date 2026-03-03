<template>
  <div class="forum-container">
    <div class="forum-layout">

      <div class="main-content">
        <div class="action-bar">
          <el-input
              v-model="keyword"
              placeholder="搜索感兴趣的面经、求职干货 ..."
              class="search-input"
              clearable
              @keyup.enter="fetchPosts"
          >
            <template #append>
              <el-button type="primary" :icon="Search" @click="fetchPosts" />
            </template>
          </el-input>

          <el-button type="primary" :icon="Edit" class="post-btn" @click="dialogVisible = true">我要发帖</el-button>
        </div>

        <div class="post-list" v-loading="loading">
          <el-empty v-if="postList.length === 0" description="社区好冷清或没有搜到结果，快来发布吧！" />

          <el-card
              v-for="post in postList"
              :key="post.id"
              class="post-card"
              shadow="hover"
              @click="viewPost(post)"
          >
            <div class="post-header">
              <h3 class="post-title">{{ post.title }}</h3>
            </div>
            <p class="post-summary">{{ post.content.substring(0, 80) }}{{ post.content.length > 80 ? '...' : '' }}</p>

            <div class="post-footer">
              <div class="author-info">
                <el-avatar :size="24" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
                <span class="author-name">{{ post.author_name }}</span>
                <span class="post-time">{{ post.create_time }}</span>
              </div>
              <div class="post-stats">
                <el-icon><View /></el-icon> <span>{{ post.view_count }}</span>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <div class="side-bar">
        <el-card class="notice-card" shadow="never">
          <template #header>
            <div class="card-header"><el-icon><Notification /></el-icon> 社区公约</div>
          </template>
          <div class="notice-content">
            <p>1. 欢迎分享面经、笔试真题、求职经验。</p>
            <p>2. 请勿发布虚假、低俗、引战等违规内容。</p>
            <p>3. 尊重他人，友善交流，共建纯净的求职圈。</p>
            <p style="color: #f56c6c; font-weight: bold; margin-top: 15px;">* 违规帖子将被随时删除。</p>
          </div>
        </el-card>
      </div>

    </div>

    <el-dialog title="发布新帖子" v-model="dialogVisible" width="650px" destroy-on-close>
      <el-form :model="postForm" :rules="rules" ref="formRef">
        <el-form-item prop="title">
          <el-input v-model="postForm.title" placeholder="请输入吸引人的标题..." maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item prop="content" style="margin-top: 20px;">
          <el-input
              v-model="postForm.content"
              type="textarea"
              :rows="12"
              placeholder="请详细描述你的求职经验或想要讨论的问题..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPost" :loading="submitLoading">发 布</el-button>
        </span>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" size="700px" :with-header="false">
      <div v-if="currentPost" class="drawer-container">

        <div class="post-detail-content">
          <h1 class="detail-title">{{ currentPost.title }}</h1>
          <div class="detail-meta">
            <el-avatar :size="32" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
            <div class="meta-info">
              <span class="meta-author">{{ currentPost.author_name }}</span>
              <span class="meta-time">{{ currentPost.create_time }} · <el-icon><View /></el-icon> {{ currentPost.view_count + 1 }} 次浏览</span>
            </div>
          </div>
          <el-divider />
          <div class="detail-body">
            <p style="white-space: pre-wrap; line-height: 1.8;">{{ currentPost.content }}</p>
          </div>
        </div>

        <div class="comment-section">
          <div class="comment-header">共 {{ commentList.length }} 条评论</div>

          <div class="comment-list" v-loading="commentLoading">
            <el-empty v-if="commentList.length === 0" description="还没有人评论，快来抢沙发吧！" :image-size="80" />
            <div v-else class="comment-item" v-for="(comment, index) in commentList" :key="comment.id">
              <el-avatar :size="36" src="https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png" />
              <div class="comment-right">
                <div class="comment-meta">
                  <span class="comment-author">{{ comment.commenter_name }}</span>
                  <span class="comment-time">{{ comment.create_time }}</span>
                </div>
                <div class="comment-content">{{ comment.content }}</div>
              </div>
            </div>
          </div>

          <div class="comment-input-box">
            <el-input
                v-model="newComment"
                placeholder="友善发言，说说你的看法..."
                @keyup.enter="submitComment"
            >
              <template #append>
                <el-button type="primary" @click="submitComment" :loading="submitCommentLoading">发送</el-button>
              </template>
            </el-input>
          </div>
        </div>

      </div>
    </el-drawer>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Edit, View, Notification } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const keyword = ref('')
const postList = ref([])

// 发帖表单相关
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const postForm = reactive({ title: '', content: '' })
const rules = reactive({
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  content: [{ required: true, message: '正文内容不能为空', trigger: 'blur' }]
})

// 详情抽屉与评论相关
const drawerVisible = ref(false)
const currentPost = ref(null)
const commentList = ref([])
const commentLoading = ref(false)
const newComment = ref('')
const submitCommentLoading = ref(false)

onMounted(() => {
  fetchPosts()
})

// 拉取帖子列表 (带真实的搜索词 params)
const fetchPosts = () => {
  loading.value = true
  request.get('/forum/list', { params: { keyword: keyword.value } }).then(res => {
    postList.value = res.data || []
  }).finally(() => {
    loading.value = false
  })
}

// 发布新帖子
const submitPost = () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) return ElMessage.warning('请先登录！')

  formRef.value.validate((valid) => {
    if (valid) {
      submitLoading.value = true
      const payload = { ...postForm, studentId: userInfo.id }

      request.post('/forum/add', payload).then(res => {
        ElMessage.success(res.message)
        dialogVisible.value = false
        postForm.title = ''
        postForm.content = ''
        fetchPosts() // 刷新列表
      }).finally(() => {
        submitLoading.value = false
      })
    }
  })
}

// 浏览帖子，并拉取评论
const viewPost = (post) => {
  currentPost.value = post
  drawerVisible.value = true
  // 浏览量+1
  request.post(`/forum/view/${post.id}`).then(() => fetchPosts())
  // 拉取评论列表
  fetchComments(post.id)
}

// 拉取评论列表
const fetchComments = (postId) => {
  commentLoading.value = true
  request.get(`/forum/comment/list/${postId}`).then(res => {
    commentList.value = res.data || []
  }).finally(() => {
    commentLoading.value = false
  })
}

// 提交评论
const submitComment = () => {
  if (!newComment.value.trim()) {
    return ElMessage.warning('评论内容不能为空哦！')
  }

  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) return ElMessage.warning('请先登录！')

  submitCommentLoading.value = true
  const payload = {
    postId: currentPost.value.id,
    studentId: userInfo.id,
    content: newComment.value.trim()
  }

  request.post('/forum/comment/add', payload).then(res => {
    ElMessage.success(res.message)
    newComment.value = '' // 清空输入框
    fetchComments(currentPost.value.id) // 刷新评论列表，看到自己的盖楼
  }).finally(() => {
    submitCommentLoading.value = false
  })
}
</script>

<style scoped>
.forum-container { padding: 10px 0; }
.forum-layout { display: flex; gap: 20px; align-items: flex-start; }

.main-content { flex: 1; }
.action-bar { display: flex; justify-content: space-between; margin-bottom: 20px; background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
.search-input { width: 400px; }
.post-btn { letter-spacing: 1px; }

.post-list { display: flex; flex-direction: column; gap: 15px; }
.post-card { border-radius: 8px; cursor: pointer; transition: all 0.2s; border: none; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.post-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.post-title { margin: 0 0 10px 0; font-size: 18px; color: #303133; }
.post-summary { margin: 0 0 15px 0; font-size: 14px; color: #606266; line-height: 1.5; }
.post-footer { display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #ebeef5; padding-top: 12px; }
.author-info { display: flex; align-items: center; gap: 8px; }
.author-name { font-size: 13px; font-weight: 500; color: #333; }
.post-time { font-size: 12px; color: #999; }
.post-stats { display: flex; align-items: center; gap: 5px; font-size: 13px; color: #999; }

.side-bar { width: 300px; flex-shrink: 0; position: sticky; top: 80px; }
.notice-card { border-radius: 8px; border: none; box-shadow: 0 1px 4px rgba(0,0,0,0.05); }
.card-header { display: flex; align-items: center; gap: 8px; font-weight: bold; color: #303133; }
.notice-content p { margin: 10px 0; font-size: 13px; color: #606266; line-height: 1.6; }

/* 抽屉总体布局：让输入框固定在底部 */
.drawer-container { display: flex; flex-direction: column; height: 100vh; }

.post-detail-content { padding: 20px 30px; }
.detail-title { font-size: 24px; color: #303133; margin-top: 0; margin-bottom: 20px; }
.detail-meta { display: flex; align-items: center; gap: 15px; margin-bottom: 15px; }
.meta-info { display: flex; flex-direction: column; gap: 5px; }
.meta-author { font-size: 15px; font-weight: bold; color: #333; }
.meta-time { font-size: 13px; color: #909399; }
.detail-body { font-size: 15px; color: #333; text-align: justify; }

/* 评论区样式 */
.comment-section { flex: 1; display: flex; flex-direction: column; background-color: #f5f7fa; padding-top: 20px; border-top: 1px solid #ebeef5; }
.comment-header { padding: 0 30px 15px; font-weight: bold; color: #333; font-size: 16px; }
.comment-list { flex: 1; overflow-y: auto; padding: 0 30px 100px; }
.comment-item { display: flex; gap: 15px; margin-bottom: 20px; }
.comment-right { flex: 1; border-bottom: 1px dashed #ebeef5; padding-bottom: 15px; }
.comment-meta { display: flex; justify-content: space-between; margin-bottom: 8px; }
.comment-author { font-size: 14px; font-weight: bold; color: #409EFF; }
.comment-time { font-size: 12px; color: #999; }
.comment-content { font-size: 14px; color: #333; line-height: 1.5; white-space: pre-wrap; }

/* 底部固定评论框 */
.comment-input-box { position: fixed; bottom: 0; right: 0; width: 700px; padding: 15px 30px; background: #fff; box-shadow: 0 -2px 10px rgba(0,0,0,0.05); }
:deep(.comment-input-box .el-input-group__append) { background-color: #409EFF; color: white; border-color: #409EFF; cursor: pointer; transition: all 0.3s;}
:deep(.comment-input-box .el-input-group__append:hover) { background-color: #66b1ff; }
</style>