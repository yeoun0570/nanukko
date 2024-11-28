<!-- pages/index.vue -->
<template>
  <div>
    <h1 v-if="isAuthenticted">{{ userId }}님 환영합니다.</h1>
    <h1 v-else>로그인이 필요합니다.</h1>
  </div>
</template>

<script setup>
import { useAuth } from '~/composables/auth/useAuth';

// useAuth()가 반환하는 객체에서 필요한 값들을 구조 분해 할당
const { isAuthenticted, userId, nickname } = useAuth();
</script>