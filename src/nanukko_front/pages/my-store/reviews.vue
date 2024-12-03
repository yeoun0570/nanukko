<script setup>
import ReadOnlyStarRating from "~/components/my-store/reviews/ReadOnlyStarRating.vue";
import StoreScore from "~/components/my-store/reviews/StoreScore.vue";
import { useApi } from "@/composables/useApi";

const api = useApi();
const refreshUserProfile = inject('refreshUserProfile');

definePageMeta({
  layout: "mystore",
});

const reviewInfo = ref([]);
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);
const reviewRate = ref(0);

const loadReviews = async (page = 0) => {
  try {
    const response = await api.get(`/my-store/reviews`, {
      params: {
        page: page,
        size: pageSize.value,
      },
    });
    reviewInfo.value = response.content;
    totalPages.value = response.totalPages;
    currentPage.value = response.currentPage;
    reviewRate.value = reviewInfo.value[0]?.reviewRate || 0;
  } catch (error) {
    console.log("로딩 중 에러: ", error);
  }
};

const handlePageChange = async (page) => {
  await loadReviews(page);
  window.scrollTo(0, 0);
};

const normalizeRating = (rating) => {
  // 100점 만점을 5점 만점으로 변환
  return rating / 2;
};

onMounted(() => {
  loadReviews();
  refreshUserProfile();
});
</script>

<template>
  <div class="reviews-container">
    <h1 class="main-title">내가 받은 후기 모음</h1>
    <div v-if="reviewInfo.length > 0">
      <StoreScore :reviewRate="reviewRate" />
      <div class="reviews-list">
        <div
          v-for="review in reviewInfo"
          :key="review.reviewId"
          class="review-card"
        >
          <div class="review-header">
            <div class="profile-section">
              <img
                :src="review.thumbnail || '/default-profile.png'"
                alt="상품 썸네일"
                class="thumbnail-image"
              />
              <h3 class="author-name">{{ review.authorNickName }}</h3>
            </div>
            <div class="rating">
              <ReadOnlyStarRating :rating="normalizeRating(review.rate)" />
            </div>
          </div>

          <div class="product-info">
            <p class="product-name">{{ review.productName }}</p>
          </div>

          <p class="review-content">{{ review.review }}</p>
        </div>
      </div>
      <Pagination
        v-if="totalPages > 1"
        :currentPage="currentPage"
        :totalPages="totalPages"
        @page-change="handlePageChange"
      />
    </div>
    <div v-else class="no-reviews">아직 받은 후기가 없습니다.</div>
  </div>
</template>

<style>
@import url("../../assets/my-store/Reviews.css");
</style>
