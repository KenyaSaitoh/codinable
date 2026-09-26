import { defineConfig, loadEnv, type ProxyOptions } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxy: Record<string, ProxyOptions> = {
  '/api/amqp/': { target: env.BACKEND_TARGET || 'http://localhost:8093', changeOrigin: true, ws: false, rewrite: (path) => path.replace('/api/amqp', '') },
  '/api/amqp-consumer/': { target: env.CONSUMER_TARGET || 'http://localhost:8094', changeOrigin: true, rewrite: (path) => path.replace('/api/amqp-consumer', '') },
  };
  return { plugins: [react()], server: { port: 5173, strictPort: true, proxy } };
});
