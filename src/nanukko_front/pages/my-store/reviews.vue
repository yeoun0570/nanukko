<script setup>
import axios from "axios";
import ReviewList from "~/components/my-store/reviews/ReviewList.vue";
import StoreScore from "~/components/my-store/reviews/StoreScore.vue";
import Pageination from "~/components/Pageination.vue";

const reviewInfo = ref([]);
const userId = "seller1";
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);
const reviewRate = ref(0);

const loadReviews = async (page = 0) => {
  try {
    const response = await axios.get(
      "http://localhost:8080/api/my-store/reviews",
      {
        params: {
          userId: userId,
          page: page,
          size: pageSize.value,
        },
      }
    );
    reviewInfo.value = response.data.content;
    totalPages.value = response.data.totalPages;
    currentPage.value = response.data.currentPage;
    reviewRate.value = reviewInfo.value[0]?.reviewRate || 0;
  } catch (error) {
    console.log("로딩 중 에러: ", error);
  }
};

const handlePageChange = async (page) => {
  await loadReviews(page);
  window.scrollTo(0, 0);
};

onMounted(() => {
  loadReviews();
});
</script>

<template>
  <div class="reviews-container">
    <h1 class="main-title">내가 받은 후기 모음</h1>
    <div v-if="reviewInfo.length > 0">
      <StoreScore :reviewRate="reviewRate" />
      <ReviewList :reviews="reviewInfo" />
      <Pageination
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
@import url('../../assets/my-store/Reviews.css');
</style>