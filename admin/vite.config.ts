import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// 后端地址：运维接口统一走 /ops/**，与移动端/业务后台 /v1/** /admin/** 区分。
// 开发期通过 dev proxy 转发到本地后端（默认 8080）。
const BACKEND_TARGET = process.env.VITE_BACKEND_TARGET || 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/ops': { target: BACKEND_TARGET, changeOrigin: true },
      '/admin': { target: BACKEND_TARGET, changeOrigin: true }
    }
  },
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1500
  }
})
