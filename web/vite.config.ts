import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 4173,
    strictPort: true,
    proxy: {
      "/api/catalog": {
        target: process.env.CATALOG_API_URL ?? "http://127.0.0.1:8081",
        rewrite: (path) => path.replace(/^\/api\/catalog/, "/api"),
      },
      "/api/booking": {
        target: process.env.BOOKING_API_URL ?? "http://127.0.0.1:8082",
        rewrite: (path) => path.replace(/^\/api\/booking/, "/api"),
      },
    },
  },
  build: {
    target: "es2022",
    sourcemap: false,
    cssCodeSplit: true,
  },
});
