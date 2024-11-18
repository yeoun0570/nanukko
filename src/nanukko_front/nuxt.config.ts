// nuxt.config.ts
export default defineNuxtConfig({
  devtools: { enabled: true },

  // 컴포넌트 자동 import 설정
  components: {
    dirs: ["~/components", "~/components/payments"],
  },

  // 토스 페이먼츠 스크립트
  app: {
    head: {
      script: [
        {
          src: "https://js.tosspayments.com/v1",
          defer: true,
        },
      ],
    },
  },

  // SSR 설정
  ssr: true,

  // 모듈 설정
  modules: [
    // 필요한 모듈들 추가
  ],

  // Vite 설정
  vite: {
    define: {
      'process.env.DEBUG': false,
      global: 'globalThis'
    },
    optimizeDeps: {
      include: ['sockjs-client']
    },
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        }
      }
    }
  },

  // 런타임 설정
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080'
    }
  },

  // 프록시 설정 (Nitro)
  nitro: {
    devProxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },

  // 빌드 설정
  build: {
    transpile: ['@stomp/stompjs']
  },

  // 플러그인
  plugins: [
    { src: '~/plugins/socket', mode: 'client' }
  ],

  compatibilityDate: '2024-11-17'
})