<script setup>
import PaymentProductSection from "~/components/payments/PaymentProductSection.vue";
import DeliverySection from "~/components/payments/DeliverySection.vue";
import PaymentAmountSection from "~/components/payments/PaymentAmountSection.vue";
import PaymentButton from "~/components/payments/PaymentButton.vue";
import { useApi } from '@/composables/useApi';
import { useAuth } from "~/composables/auth/useAuth";

const api = useApi();
const auth = useAuth();
const { $showToast } = useNuxtApp();

//추후에 상세페이지에서 라우팅 받으면 받아야 될 값
const route = useRoute();
const productId = route.query.productId; //추가

const orderData = ref(null);
const loading = ref(false);
const error = ref(null);
const tossPayments = ref(null);
const orderId = ref(null);
// const productId = 11;

const loadOrderPage = async () => {
  try {
    const response = await api.get(
      `/payments/page/${productId}`
    );
    orderData.value = response;
    console.log(orderData.value.status);
    if (response.status !== "SELLING") {
      $showToast("판매중인 상품이 아닙니다.");
      // 이전 페이지로 돌아가기
      navigateTo('/');
      return;
    }
  } catch (error) {
    console.log("주문 페이지 로딩 실패: ", error);
  }
};

onMounted(() => {
  // @ts-ignore
  tossPayments.value = window.TossPayments(
    "test_ck_nRQoOaPz8L9JLpKXkEGN3y47BMw6"
  );

  loadOrderPage();
  console.log("구매자: ", auth.userId.value);
});

const startPayment = async () => {
  try {
    loading.value = true;

    console.log("구매자 검증: ", orderData.value, ", ", auth.userId.value);
    if (orderData.value.sellerId === auth.userId.value) {
      $showToast("자신의 상품은 구매할 수 없습니다.");
      return;
    }

    orderId.value = `ORDER_${new Date().getTime()}`;

    console.log("orderId:", orderId.value);

    const paymentInfo = {
      productId: productId,
      buyerId: auth.userId.value, // 추후에 로그인한 사용자로 변경
      amount: orderData.value.totalAmount,
      productName: orderData.value.productName,
    };

    //데이터 확인용 로그
    console.log("Payment Info:", paymentInfo);

    // successUrl에 필요한 정보만 포함
    const successUrl = new URL(`${window.location.origin}/payments/success`);

    await tossPayments.value.requestPayment("카드", {
      amount: orderData.value.totalAmount,
      orderId: orderId.value,
      orderName: orderData.value.productName,
      customerName: orderData.value.nickname,
      successUrl: `${successUrl}?productId=${productId}`, // productId만 추가
      failUrl: `${window.location.origin}/payments/fail`,
    });
  } catch (error) {
    console.log("결제 실패: ", error);
    console.error("에러 응답:", error.response?.data); // 에러 응답 확인용 로그

    if (error.response?.data) {
      // 백엔드의 예외 메시지를 그대로 표시
      if (error.response?.data?.message) {
        $showToast(error.response.data.message);
      } else {
        $showToast("결제 처리 중 오류가 발생했습니다.");
      }
    }
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="order-page">
    <h1>결제하기</h1>
    <br />
    <hr color="black" />
    <br />
    <div v-if="error" class="error">{{ error }}</div>
    <div v-else-if="orderData" class="order-content">
      <PaymentProductSection :productData="orderData" />
      <br />
      <hr />
      <br />
      <DeliverySection :deliveryData="orderData" />
      <br />
      <hr />
      <br />
      <PaymentAmountSection :product-data="orderData" />
      <br />
      <hr />
      <br />
      <PaymentButton :loading="loading" @startPayment="startPayment" />
    </div>
  </div>
</template>

<style>
@import url("../../assets/payments/payment.css");
</style>
