import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 画面（5173）から見て同じオリジンの /api を、8080 の Payara Server の
// /jakarta-security-employee-google/api へ転送する
// セッション Cookie（GOOGLE_SESSION）は web.xml で Path=/ にしてあるので、localhost の 5173 への要求にも付き、そのまま転送される
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: (path) => '/jakarta-security-employee-google' + path,
      },
    },
  },
});
