// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: "2024-04-03",
  devtools: { enabled: true },
  // 컴포넌트 자동 import 설정
  components: {
    dirs: ["~/components", "~/components/payments"],
  },
  runtimeConfig: {
    apiSecret: '',
    public: {
      kakaoMapApiKey: process.env.NUXT_PUBLIC_KAKAO_MAP_API_KEY
    }
  },
  // 토스 페이먼츠 스크립트
  app: {
    head: {
      script: [
        {
          src: `//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NUXT_PUBLIC_KAKAO_MAP_API_KEY}&libraries=services&autoload=true`,
          type: 'text/javascript',
        },
        {
          src: "https://js.tosspayments.com/v1",
          defer: true,
        },
      ],
    },
  },
});
