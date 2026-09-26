import { defineConfig, loadEnv, type ProxyOptions } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxy: Record<string, ProxyOptions> = {
  '/api/graphql/': { target: env.BACKEND_TARGET || 'http://localhost:8088', changeOrigin: true, ws: false, rewrite: (path) => path.replace('/api/graphql', '') },
  };
  return { plugins: [react()], server: { port: 5173, strictPort: true, proxy } };
});
