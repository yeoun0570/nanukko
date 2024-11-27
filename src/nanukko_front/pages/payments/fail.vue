<script setup>
const route = useRoute();
const loading = ref(true);
const error = ref(null);

onMounted(() => {
  const { code, message } = route.query;
  if (code || message) {
    error.value = `결제 실패: ${message || "알 수 없는 오류가 발생했습니다."}`;
  }
  loading.value = false;
});

//위의 주석 처리된 부분을 Nuxt의 navigateTo 사용해서 변형
const goBack = () => {
  navigateTo(`/payments?productId=${route.query.productId}`);
};
</script>

<template>
  <div class="payment-fail">
    <div v-if="loading" class="loading">처리중입니다...</div>

    <div v-else-if="error" class="error">
      <h2>오류가 발생했습니다</h2>
      <p>{{ error }}</p>
      <button @click="goBack" class="back-button">이전으로</button>
    </div>

    <div v-else class="fail">
      <h1>결제가 취소되었습니다.</h1>
      <p>이전으로 돌아가 재결제 해주세요.</p>
      <button @click="goBack" class="back-button">이전으로</button>
    </div>
  </div>
</template>

<style scoped>
@import url("../../assets/payments/PaymentFail.css");
</style>
