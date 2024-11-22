<script setup>
import axios from "axios";
import StarRating from "~/components/my-store/buy-products/StarRating.vue";
import { useApi } from "~/composables/useApi";

definePageMeta({
  layout: 'mystore'
});


const { baseURL } = useApi();

const route = useRoute();

const reviewInfo = ref({
  authorId: route.query.userId,
  orderId: route.query.orderId,
  productName: route.query.productName,
  thumbnailImage: route.query.thumbnailImage,
  review: "",
  rate: 0,
});

onMounted(() => {
  if (!reviewInfo.value.orderId) {
    alert("잘못된 접근입니다.");
    navigateTo("/my-store/buy-products");
  }
});

const writeReview = async () => {
  try {
    if (!reviewInfo.value.review.trim()) {
      alert("리뷰 내용을 입력해주세요.");
      return;
    }
    if (reviewInfo.value.rate === 0) {
      alert("별점을 선택해주세요.");
      return;
    }

    const reviewData = {
      orderId: reviewInfo.value.orderId,
      authorId: reviewInfo.value.authorId,
      review: reviewInfo.value.review,
      rate: reviewInfo.value.rate,
    };

    await axios.post(`${baseURL}/review/write`, reviewData);
    alert("리뷰가 작성되었습니다.");
    navigateTo("/my-store/buy-products");
  } catch (error) {
    console.error("리뷰 작성 실패: ", error);
    alert(error.response?.data?.message || "리뷰 작성에 실패했습니다.");
  }
};

const back = () => {
  navigateTo("/my-store/buy-products");
};
</script>

<template>
  <div class="review-form">
    <h2>상품 후기 작성</h2>

    <!-- 상품 정보 표시 -->
    <div class="product-info">
      <img
        :src="reviewInfo.thumbnailImage"
        :alt="reviewInfo.productName"
        class="product-image"
      />
      <h3>{{ reviewInfo.productName }}</h3>
    </div>

    <!-- 별점 입력 -->
    <div class="rating-input">
      <label>평점을 선택해주세요</label>
      <StarRating v-model="reviewInfo.rate" />
    </div>

    <!-- 리뷰 내용 입력 -->
    <div class="review-input">
      <label>리뷰 내용</label>
      <textarea
        v-model="reviewInfo.review"
        placeholder="상품은 만족하셨나요? 솔직한 후기를 남겨주세요."
        rows="5"
        required
      ></textarea>
    </div>

    <div class="button-group">
      <button @click="writeReview" class="submit-btn">작성하기</button>
      <button @click="back" class="cancel-btn">취소</button>
    </div>
  </div>
</template>

<style>
@import url("../../../assets/my-store/WriteReview.css");
</style>
