<script setup>
import axios from 'axios';

const props = defineProps({
  order: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["order-updated"]);

// 구매 확정 처리
const confirmPurchase = async () => {
  console.log(props.order);
  console.log(props.order.orderId);
  if (!props?.order.orderId) return;

  try {
    console.log("구매확정 요청 시작 - orderId:", props.order.orderId);

    const response = await axios.post(
      `http://localhost:8080/api/payments/${props.order.orderId}/confirm`
    );

    console.log("구매확정 응답:", response.data);

    if (response.data.status === "ESCROW_RELEASED") {
      alert("구매가 확정되었습니다.");
      emit("order-updated");
    } else {
      throw new Error(`잘못된 상태: ${response.data.status}`);
    }
  } catch (error) {
    console.error("구매 확정 중 오류:", error);
    error.value =
      error.response?.data?.message || "구매 확정 중 오류가 발생했습니다.";
    alert(error.value);
  }
};

// 결제 취소 처리
const cancelOrder = async () => {
  if (!props.order?.orderId) return;

  try {
    // 취소 전 확인
    if (!confirm("결제를 취소하시겠습니까?")) {
      return;
    }

    console.log("결제 취소 시작 - orderId:", props.order.orderId);

    const response = await axios.post(
      `http://localhost:8080/api/payments/${props.order.orderId}/cancel`
    );

    console.log("결제 취소 완료:", response.data);
    alert("결제가 취소되었습니다. 전체 금액이 환불됩니다.");
    emit("order-updated");

    // 취소 후 메인 페이지나 상품 페이지로 이동
    // navigateTo("/"); // 또는 상품 상세 페이지로 이동
  } catch (error) {
    console.error("결제 취소 중 오류:", error);
    error.value =
      error.response?.data?.message || "결제 취소 중 오류가 발생했습니다.";
  }
};
</script>

<template>
  <div class="order-card">
    <img
      :src="order.thumbnailImage"
      :alt="order.productName"
      class="order-image"
    />
    <div class="order-info">
      <h3 class="product-name">{{ order.productName }}</h3>
      <div class="price-status-container">
        <p class="price">{{ order.price.toLocaleString() }}원</p>
        <p class="status" :class="order.status.toLowerCase()">
          {{
            order.status === "ESCROW_HOLDING"
              ? "구매중"
              : order.status === "ESCROW_RELEASED"
                ? "구매완료"
                : "없는상품"
          }}
        </p>
      </div>
      <div class="button-container">
        <button
          v-if="order.status === 'ESCROW_HOLDING'"
          @click="confirmPurchase"
          class="confirm-btn"
        >
          구매 확정
        </button>
        <button
          v-if="order.status === 'ESCROW_HOLDING'"
          @click="cancelOrder"
          class="cancel-btn"
        >
          결제 취소
        </button>
      </div>
    </div>
  </div>
</template>
