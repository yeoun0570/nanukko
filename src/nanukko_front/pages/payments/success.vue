<script setup>
import { useApi } from '@/composables/useApi';
import { useAuth } from '~/composables/auth/useAuth';

const api = useApi();
const route = useRoute();
const loading = ref(true);
const error = ref(null);
const auth = useAuth();

onMounted(async () => {
  try {
    const { orderId, paymentKey, amount, productId } = route.query;

    if (!localStorage.getItem(`payment_${orderId}`)) {
      await api.post(`/payments/confirm`, {
        paymentKey,
        orderId,
        amount: parseInt(amount),
        productId: parseInt(productId),
        buyerId: auth.userId.value,
      });

      localStorage.setItem(`payment_${orderId}`, "true");
    }

    loading.value = false;
  } catch (err) {
    console.error("결제 처리 중 오류:", err);
    error.value = "결제 처리 중 오류가 발생했습니다.";
    loading.value = false;
  }
});
</script>

<template>
  <div class="payment-success">
    <div v-if="loading" class="loading">결제를 처리중입니다...</div>

    <div v-else-if="error" class="error">
      <h2>오류가 발생했습니다</h2>
      <p>{{ error }}</p>
    </div>

    <div v-else class="success">
      <h1>결제가 완료되었습니다.</h1>
      <p>마이페이지 > 구매내역 에서 확인하세요</p>
      <NuxtLink to="/" class="home-button">홈으로</NuxtLink>
    </div>
  </div>
</template>

<style scoped>
@import url("../../assets/payments/PaymentSuccess.css");
</style>
