<script setup>
import DeliveryRegistrationModal from "~/components/delivery/DeliveryRegistrationModal.vue";
import { useApi } from '@/composables/useApi';

const { $showToast } = useNuxtApp();
const api = useApi();
const router = useRouter();
const showDeliveryModal = ref(false);

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["product-updated"]);

const goToModify = () => {
  router.push({
    path: "/my-store/sale-products/modify?productId",
    query: {
      productId: props.product.productId,
    },
    state: {
      productInfo: props.product,
    },
  });
};

const removeProduct = async () => {
  console.log("productId: ", props.product.productId);
  try {
    await api.post(
      `/my-store/sale-products/remove`,
      null,
      {
        params: {
          productId: props.product.productId,
        },
      }
    );
    $showToast("상품 삭제에 성공했습니다.");
    emit("product-updated");
  } catch (error) {
    console.log("상품 삭제에 실패했습니다: ", error);
    console.error("에러 응답:", error.response?.data); // 에러 응답 확인용 로그
    if (error.response?.data?.message) {
      $showToast(error.response.data.message);
    } else {
      $showToast("상품 삭제에 실패했습니다.");
    }
  }
};

const handleDeliverySuccess = () => {
  showDeliveryModal.value = false;
  emit("product-updated");
};

const goToProduct = (productId) => {
  router.push(`/products/${productId}`);
};
</script>

<template>
  <div class="product-card">
    <div class="product-image" @click="goToProduct(props.product.productId)">
      <img :src="product.thumbnailImage" :alt="product.productName" />
    </div>
    <div class="product-info">
      <h3 class="product-name">{{ product.productName }}</h3>
      <div class="price-status-container">
        <p class="price">{{ product.price.toLocaleString() }}원</p>
        <p class="status" :class="product.status.toLowerCase()">
          {{
            product.status === "SELLING"
              ? "판매중"
              : product.status === "RESERVED"
                ? "예약중"
                : "판매완료"
          }}
        </p>
      </div>
      <div class="button-container">
        <button v-if="product.status === 'RESERVED' && !product.hasDelivery" @click="showDeliveryModal = true"
          class="delivery-btn">
          운송장 등록
        </button>
        <button @click="goToModify" class="modify-btn">수정</button>
        <button @click="removeProduct" class="remove-btn">삭제</button>
      </div>
    </div>
  </div>

  <!-- 운송장 등록 모달 -->
  <DeliveryRegistrationModal v-if="showDeliveryModal" :product-id="product.productId" @close="showDeliveryModal = false"
    @success="handleDeliverySuccess" />
</template>
