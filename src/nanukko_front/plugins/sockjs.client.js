import SockJS from 'sockjs-client'

export default defineNuxtPlugin(() => {//SockJS를 Nuxt 앱 전체에서 사용할 수 있게 해 줌
  return {
    provide: {
      SockJS: SockJS//$SockJS라는 이름으로 전역에서 접근 가능
    }
  }
})