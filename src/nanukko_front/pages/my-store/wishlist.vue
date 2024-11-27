<script setup>
import axios from "axios";
import WishlistGrid from "~/components/my-store/wishlist/WishlistGrid.vue";
import Pagination from "~/components/Pagination.vue";
import { useApi } from '@/composables/useApi';

const api = useApi();

definePageMeta({
  layout: 'mystore'
});

const userWishlist = ref([]);
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);

const loadUserWishlist = async (page = 0) => {
  try {
    const response = await api.get(
      `/my-store/wishlist`,
      {
        params: {
          page,
          size: pageSize.value,
        },
      }
    );
    userWishlist.value = response.content;
    totalPages.value = response.totalPages;
    currentPage.value = response.currentPage;
  } catch (error) {
    console.error("로딩 중 에러:", error);
  }
};

const handlePageChange = async (page) => {
  await loadUserWishlist(page);
  window.scrollTo(0, 0);
};

onMounted(() => {
  loadUserWishlist();
});
</script>

<template>
  <div class="wishlist-container">
    <h1 class="main-title">나의 위시리스트</h1>

    <WishlistGrid
      :products="userWishlist"
      @wishlist-updated="loadUserWishlist"
    />

    <Pagination
      v-if="totalPages > 1"
      :currentPage="currentPage"
      :totalPages="totalPages"
      @page-change="handlePageChange"
    />
  </div>
</template>

<style>
@import url("../../assets/my-store/Wishlist.css");
</style>
