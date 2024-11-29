<script setup>
import StarRating from "~/components/my-store/buy-products/StarRating.vue";
import { useAuth } from "~/composables/auth/useAuth";
import { useApi } from '@/composables/useApi';

const api = useApi();

definePageMeta({
  layout: "mystore",
});

const route = useRoute();

const reviewInfo = ref({
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
      review: reviewInfo.value.review.trim(), //trim()으로 문자열임을 확실히
      rate: Number(reviewInfo.value.rate), //숫자임을 확실히
    };
    console.log("리뷰 데이터: ", reviewData);

    await api.post(`/review/write`, reviewData);
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
