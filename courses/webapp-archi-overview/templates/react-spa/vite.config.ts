import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    // アプリが出力から URL を見つけてプレビュータブを開く。
    // 127.0.0.1 に固定しておくと、環境によって表示が変わらない。
    host: '127.0.0.1',
    port: 5173,
  },
});
