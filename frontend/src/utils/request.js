import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
    baseURL: '/api', // 对应 vite.config.js 里的代理配置
    timeout: 5000
})

// 响应拦截器：统一处理后端返回的 Result
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (res.code === 200) {
            return res;
        } else {
            ElMessage.error(res.message || '系统错误');
            return Promise.reject(res.message);
        }
    },
    error => {
        ElMessage.error('网络请求失败，请检查后端是否启动');
        return Promise.reject(error);
    }
)

export default request;