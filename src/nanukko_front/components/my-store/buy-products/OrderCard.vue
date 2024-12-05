<script setup>
import { useApi } from "@/composables/useApi";

const api = useApi();
const hasReview = ref(false);
const { $showToast } = useNuxtApp();
const props = defineProps({
  order: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["order-updated"]);

// 해당 주문에 대한 리뷰 존재 여부 확인
const checkReviewExists = async () => {
  try {
    const response = await api.get(`/review/check`, {
      params: {
        orderId: props.order.orderId,
      },
    });
    hasReview.value = response.exists;
  } catch (error) {
    console.error("리뷰 존재 여부 확인 중 에러: ", error);
    hasReview.value = false;
  }
};

// 구매 확정 처리
const confirmPurchase = async () => {
  if (!props?.order.orderId) return;

  try {
    console.log("구매확정 요청 시작 - orderId:", props.order.orderId);

    const response = await api.post(`/payments/${props.order.orderId}/confirm`);

    console.log("구매확정 응답:", response);

    // status만 확인하여 ESCROW_RELEASED면 성공으로 처리
    if (response.status === "ESCROW_RELEASED") {
      $showToast("구매가 확정되었습니다.");
      emit("order-updated");
    } else {
      throw new Error(`잘못된 상태: ${response.status}`);
    }
  } catch (error) {
    console.error("구매 확정 중 오류:", error);
    // 실제 에러인 경우에만 사용자에게 알림
    if (error.response?.status === 400 || error.response?.status === 500) {
      $showToast(
        error.response?.data?.message || "구매 확정 중 오류가 발생했습니다."
      );
    }
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

    const response = await api.post(`/payments/${props.order.orderId}/cancel`);
    console.log("응답 데이터: ", response);

    if (response.status === "CANCELED") {
      // 실제 취소가 성공했을 때만
      console.log("결제 취소 완료:", response);
      $showToast("결제가 취소되었습니다. 전체 금액이 환불됩니다.");
      emit("order-updated");
      // 취소 후 메인 페이지나 상품 페이지로 이동
      //navigateTo("/"); // 또는 상품 상세 페이지로 이동
    }
  } catch (error) {
    console.error("결제 취소 중 오류:", error);

    // 서버에서 전달된 에러 메시지 표시
    if (error.response?.data?.message) {
      $showToast(error.response.data.message);
    } else {
      $showToast("결제 취소 중 오류가 발생했습니다.");
    }
  }
};

const goToWriteReview = async () => {
  console.log("Order data:", props.order); // 데이터 확인
  await navigateTo({
    path: "/my-store/buy-products/write-review",
    query: {
      orderId: props.order.orderId,
      productName: props.order.productName,
      thumbnailImage: props.order.thumbnailImage,
    },
  });
};

onMounted(() => {
  if (props.order.status === "ESCROW_RELEASED") {
    checkReviewExists();
  }
});
</script>

<template>
  <div class="order-card">
    <div class="order-image">
      <img :src="order.thumbnailImage" :alt="order.productName" />
    </div>
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
                : order.status === "IN_DELIVERY"
                  ? "배송중"
                  : order.status === "DELIVERED"
                    ? "배송완료"
                    : "없는상품"
          }}
        </p>
      </div>
      <div class="button-container">
        <button v-if="order.status === 'DELIVERED'" @click="confirmPurchase" class="confirm-btn">
          구매 확정
        </button>
        <button v-if="order.status === 'ESCROW_HOLDING'" @click="cancelOrder" class="cancel-btn">
          결제 취소
        </button>
        <button v-if="order.status === 'ESCROW_RELEASED'" @click="goToWriteReview" :class="{
          'review-btn': !hasReview,
          'review-completed-btn': hasReview,
        }" :disabled="hasReview">
          {{ hasReview ? "후기 작성 완료" : "후기 작성" }}
        </button>
      </div>
    </div>
  </div>
</template>
