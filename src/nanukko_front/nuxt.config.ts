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

  // stomp 설정
  build: {
    transpile: ['@stomp/stompjs']
    //@stomp/stompjs는 ES6+ 문법을 사용하는 모듈(설정 안 해주면 브라우저 호환성 문제 발생할 수 있음)
    //해당 모듈을 브라우저가 이해할 수 있는 코드로 변환(트랜스파일)하라고 Nuxt에게 알려주는 것
  },
  plugins: [
    { src: '~/plugins/sockjs.client.js', mode: 'client' }
    //mode: 'client'는 이 플러그인이 브라우저에서만 실행되어야 함을 의미
    //SockJS는 브라우저의 WebSocket을 사용하므로 서버사이드에서는 실행되면 안됨
  ],


});
