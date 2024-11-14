<script setup>
import axios from "axios";

const route = useRoute();
const loading = ref(true);
const error = ref(null);

onMounted(async () => {
  try {
    // URL 쿼리 파라미터에서 결제 정보 가져오기
    const { orderId, paymentKey, amount, productId } = route.query;
    console.log("Received payment data:", {
      orderId,
      paymentKey,
      amount,
      productId,
    }); // 데이터 확인용

        //백엔드의 OrderConfirmDTO 형식과 동일하게 데이터 구성해서 전송
        const confirmData = {
      paymentKey,
      orderId,
      amount: parseInt(amount),
      productId: parseInt(productId),
    };

    // 결제 승인 요청
    await axios.post("http://localhost:8080/api/payments/confirm", {
      confirmData
    });

    if (confirmResponse.data && confirmResponse.data.status === "DONE") {
      // 에스크로 전환은 백엔드에서 자동으로 처리됨
      loading.value = false;
    } else {
      throw new Error("결제 승인이 실패했습니다.");
    }
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
      <h2>마이페이지 > 구매내역 에서 확인하세요</h2>
      <NuxtLink to="/" class="home-button">홈으로</NuxtLink>
    </div>
  </div>
</template>

<style scoped>
@import url("../../assets/payments/PaymentSuccess.css");
</style>
