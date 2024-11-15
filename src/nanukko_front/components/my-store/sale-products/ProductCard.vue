<script setup>
import axios from "axios";

const router = useRouter();

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
  userId: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["product-updated"]);

const goToModify = () => {
  console.log('goToModify - productId: ', props.product.productId);
  console.log('goToModify - userId: ', props.userId);
  console.log('goToModify - product: ', props.product);

  router.push({
    path: '/my-store/sale-products/modify?productId',
    query: {
      userId: props.userId,
      productId: props.product.productId,
    },
    state: {
      productInfo: props.product,
    },
  });
};

const removeProduct = async () => {
  try {
    await axios.post(
      "http://localhost:8080/api/my-store/sale-products/remove",
      props.product,
      {
        params: {
          userId: props.userId,
          productId: props.product.productId,
        },
      }
    );
    alert("상품 삭제에 성공했습니다.");
    emit("product-updated");
  } catch (error) {
    console.log("상품 삭제에 실패했습니다: ", error);
  }
};
</script>

<template>
  <div class="product-card">
    <img :src="product.thumbnailImage" class="product-image" />
    <h3>{{ product.productName }}</h3>
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
    <button @click="goToModify" class="modify-btn">수정하기</button>
    <button @click="removeProduct" class="remove-btn">삭제하기</button>
  </div>
</template>
