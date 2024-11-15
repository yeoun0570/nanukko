<script setup>
defineProps({
  reviews: {
    type: Array,
    required: true,
  },
});
</script>

<template>
  <div class="reviews-list">
    <div v-for="review in reviews" :key="review.reviewId" class="review-card">
      <div class="review-header">
        <div class="profile-section">
          <img
            :src="review.profile || '/default-profile.png'"
            alt="프로필 사진"
            class="profile-image"
          />
          <h3 class="author-name">{{ review.authorName }}</h3>
        </div>
        <div class="rating">
          <span class="rate">{{ review.rate }}점</span>
        </div>
      </div>

      <div class="product-info">
        <p class="product-name">{{ review.productName }}</p>
      </div>

      <p class="review-content">{{ review.review }}</p>
    </div>
  </div>
</template>
