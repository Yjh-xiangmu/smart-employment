import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 3000, // 前端运行端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 代理到我们的 Spring Boot 后端
        changeOrigin: true
      }
    }
  }
})