<!-- WriteReview.vue -->
<script setup>
import axios from "axios";

const route = useRoute();

const reviewInfo = ref({
  authorId: route.query.userId,
  orderId: route.query.orderId,
  productName: route.query.productName,
  thumbnailImage: route.query.thumbnailImage,
  review: "",
  rate: 5,
});

onMounted(() => {
  // 필수 파라미터 체크
  console.log("productId: ", reviewInfo.value.orderId);
  if (!reviewInfo.value.orderId) {
    alert("잘못된 접근입니다.");
    navigateTo("/my-store/buy-products");
  }
});

const writeReview = async () => {
  try {
    // 입력값 검증
    if (!reviewInfo.value.review.trim()) {
      alert("리뷰 내용을 입력해주세요.");
      return;
    }
    if (reviewInfo.value.rate < 1 || reviewInfo.value.rate > 5) {
      alert("평점은 1~5 사이의 값을 입력해주세요.");
      return;
    }

    // ReviewRegisterDTO 형식에 맞게 데이터 전송
    const reviewData = {
      orderId: reviewInfo.value.orderId,
      authorId: reviewInfo.value.authorId,
      review: reviewInfo.value.review,
      rate: reviewInfo.value.rate,
    };

    console.log(route.query.userId);

    console.log("Sending review data:", reviewData); // 전송되는 데이터 확인

    await axios.post("http://localhost:8080/api/review/write", reviewData);
    alert("리뷰가 작성되었습니다.");
    navigateTo("/my-store/buy-products");
  } catch (error) {
    console.error("리뷰 작성 실패: ", error);
    alert(error.response?.data?.message || "리뷰 작성에 실패했습니다.");
  }
};

const back = async () => {
  await navigateTo("/my-store/buy-products");
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

    <!-- 평점 입력 -->
    <div class="rating-input">
      <label>평점</label>
      <select v-model="reviewInfo.rate">
        <option v-for="n in 5" :key="n" :value="n">{{ n }}점</option>
      </select>
    </div>

    <!-- 리뷰 내용 입력 -->
    <div class="review-input">
      <label>리뷰 내용</label>
      <textarea
        v-model="reviewInfo.review"
        placeholder="상품에 대한 후기를 작성해주세요."
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
