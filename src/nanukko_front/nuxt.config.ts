// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2024-04-03",
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
});
