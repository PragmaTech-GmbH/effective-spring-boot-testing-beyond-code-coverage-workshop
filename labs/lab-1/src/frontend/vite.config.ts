import { defineConfig } from 'vite';
import { resolve } from 'path';
import tailwindcss from '@tailwindcss/vite';

// Emits the built SPA directly into Spring Boot's classpath static resources,
// so the Spring Boot fat jar serves it at /app/** without any extra controller.
export default defineConfig({
  base: '/app/',
  plugins: [tailwindcss()],
  build: {
    outDir: resolve(__dirname, '../../target/classes/static/app'),
    emptyOutDir: true,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
