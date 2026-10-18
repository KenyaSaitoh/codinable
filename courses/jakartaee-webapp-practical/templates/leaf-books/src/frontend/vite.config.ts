import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      // すべてのAPIリクエストを leaf-books-api に転送（コンテキストルートはフォルダ名）
      // leaf-books-api が唯一のバックエンド（書籍・在庫・注文・認証をすべて担当）
      '/api': {
        target: 'http://localhost:8080/leaf-books-api',
        changeOrigin: true,
      }
    }
  }
})

