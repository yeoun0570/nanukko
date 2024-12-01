<script setup>
import Header from "~/components/layout/header.vue";
import Footer from "~/components/layout/footer.vue";
import navigator from "~/components/layout/navigator.vue";
import banner from "~/components/mainpage/banner.vue";
import Main from "~/components/products/main/main.vue";


import { useAuth } from '~/composables/auth/useAuth';
import TestChatButton from '~/components/chat/TestChatButton.vue';


// useAuth()가 반환하는 객체에서 필요한 값들을 구조 분해 할당
const { isAuthenticated, userId, nickname } = useAuth();
</script>

<template>
  <div>
    <h1 v-if="isAuthenticated">{{ userId }}님 환영합니다.</h1>
    <h1 v-else>로그인이 필요합니다.</h1>
  </div>

  <div class="home-container">
    <h1>채팅버튼</h1>

    <!-- 테스트 채팅 버튼 추가 -->
    <TestChatButton />
  </div>



  <banner />

  <Main />

</template>

<style>
.home-container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
}
</style>