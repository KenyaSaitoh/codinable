import { defineConfig, loadEnv, type ProxyOptions } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxy: Record<string, ProxyOptions> = {
  '/api/websocket/': { target: env.BACKEND_TARGET || 'http://localhost:8096', changeOrigin: true, ws: true, rewrite: (path) => path.replace('/api/websocket', '') },
  };
  return { plugins: [react()], server: { port: 5173, strictPort: true, proxy } };
});
