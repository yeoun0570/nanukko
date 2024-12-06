<script setup>
import { useApi } from "@/composables/useApi";
import { useAuthStore } from "~/stores/authStore";

const api = useApi();
const router = useRouter();
const authStore = useAuthStore();
const { $showToast } = useNuxtApp();
const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["remove-success"]);

const removeWishlist = async () => {
  try {
    await api.post(`/my-store/wishlist/remove`, props.product, {
      params: {
        productId: props.product.productId,
      },
    });
    emit("remove-success");
  } catch (error) {
    console.error("찜목록에서 삭제 실패:", error);
    $showToast("삭제에 실패했습니다. 다시 시도해주세요.");
  }
};

const goToPayment = async () => {
  await navigateTo({
    path: "/payments",
    query: {
      productId: props.product.productId,
    },
  });
};

const handleChatClick = async () => {
  if (!authStore.isAuthenticated) {
    $showToast("로그인이 필요한 i서비스입니다. 로그인 화면으로 이동합니다.");
    setTimeout(() => {
      router.push("/auth/login");
    }, 1500);
    return;
  }

  try {
    await api.post("/chat/getChat", null, {
      params: {
        productId: props.product.productId,
      },
    });

    setTimeout(() => {
      router.push("/chat");
    }, 1500);
  } catch (err) {
    console.error("채팅방 생성 실패");
  }
};
</script>

<template>
  <div class="wishlist-card">
    <div class="card-content">
      <div class="image-and-info">
        <img
          :src="product.thumbnailImage"
          :alt="product.productName"
          class="product-image"
        />
        <div class="product-info">
          <h3 class="product-name">{{ product.productName }}</h3>
          <button class="wish-btn" @click="removeWishlist">
            ♥ <span>{{ product.wishCount }}</span>
          </button>
          <p class="price">{{ product.price.toLocaleString() }}원</p>
        </div>
      </div>
      <div class="button-group">
        <button class="chat-btn" @click="handleChatClick">채팅</button>
        <button @click="goToPayment" class="payment-btn">즉시 결제</button>
      </div>
    </div>
  </div>
</template>
