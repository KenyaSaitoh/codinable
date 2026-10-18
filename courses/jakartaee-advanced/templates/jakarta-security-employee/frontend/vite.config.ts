import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 画面（5173）から見て同じオリジンの /api を、8080 の Payara Server の
// /jakarta-security-employee/api へ転送する（Webブラウザから見ると画面と API は同じオリジンなので CORS は使わない）
// セッション Cookie（SECURITY_SESSION）は Path=/jakarta-security-employee で返るので、/ に書き換えて画面へ渡す
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: (path) => '/jakarta-security-employee' + path,
        cookiePathRewrite: { '/jakarta-security-employee': '/' },
      },
    },
  },
});
