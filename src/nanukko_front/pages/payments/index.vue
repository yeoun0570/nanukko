<script setup>
import { provide, ref, onMounted } from "vue";
import axios from "axios";
import PaymentProductSection from "~/components/payments/PaymentProductSection.vue";
import DeliverySection from "~/components/payments/DeliverySection.vue";
import PaymentAmountSection from "~/components/payments/PaymentAmountSection.vue";
import PaymentButton from "~/components/payments/PaymentButton.vue";

//추후에 상세페이지에서 라우팅 받으면 받아야 될 값
const route = useRoute();
// const { productId } = route.query;

const orderData = ref(null);
const loading = ref(false);
const error = ref(null);
const tossPayments = ref(null);
const orderId = ref(null);
const productId = 1;

provide("orderData", orderData);

const loadOrderPage = async () => {
  try {
    const response = await axios.get(
      `http://localhost:8080/api/payments/page/${productId}`
    );
    orderData.value = response.data;
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
});

const startPayment = async () => {
  try {
    loading.value = true;
    orderId.value = `ORDER_${new Date().getTime()}`;

    console.log("orderId:", orderId.value);

    const paymentInfo = {
      productId: productId,
      buyerId: "buyer1",
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
    console.error("결제 시작 실패:", error);
    alert("결제가 취소되었습니다.");
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

<style scoped>
@import url("../../assets/payments/paymentTest.css");
</style>
