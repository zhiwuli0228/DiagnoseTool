import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const proxyEnabled = env.VITE_DEV_PROXY_ENABLED !== 'false';
  const proxyTarget = env.VITE_BACKEND_PROXY_TARGET || 'http://localhost:8080';

  return {
    plugins: [react()],
    server: {
      proxy: proxyEnabled ? {
        // 本地调试时把前端 /api 请求转发到后端，生产环境由网关或部署平台负责。
        '/api': {
          target: proxyTarget,
          changeOrigin: true,
          secure: false
        }
      } : undefined
    },
    test: {
      environment: 'jsdom',
      setupFiles: './src/test/setup.js'
    }
  };
});
