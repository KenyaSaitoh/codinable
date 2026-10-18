import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 画面（5173）から見て同じオリジンの /graphql を、8080 の Payara Server の
// /jakarta-graphql-employee/graphql へ転送する
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/graphql': {
        target: 'http://localhost:8080',
        rewrite: (path) => '/jakarta-graphql-employee' + path,
      },
    },
  },
});
