import { defineConfig, loadEnv, type ProxyOptions } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxy: Record<string, ProxyOptions> = {
  '/api/resilience/': { target: env.BACKEND_TARGET || 'http://localhost:8082', changeOrigin: true, ws: false, rewrite: (path) => path.replace('/api/resilience', '') },
  };
  return { plugins: [react()], server: { port: 5173, strictPort: true, proxy } };
});
