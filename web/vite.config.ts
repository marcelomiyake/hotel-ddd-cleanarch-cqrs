import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

function apiProxy() {
  return {
    "/api/catalog": {
      target: process.env.CATALOG_API_URL ?? "http://127.0.0.1:8081",
      rewrite: (path: string) => path.replace(/^\/api\/catalog/, "/api"),
    },
    "/api/booking": {
      target: process.env.BOOKING_API_URL ?? "http://127.0.0.1:8082",
      rewrite: (path: string) => path.replace(/^\/api\/booking/, "/api"),
    },
  };
}

export default defineConfig({
  plugins: [react()],
  server: {
    port: 4173,
    strictPort: true,
    proxy: apiProxy(),
  },
  preview: { proxy: apiProxy() },
  build: {
    target: "es2022",
    sourcemap: false,
    cssCodeSplit: true,
  },
});
