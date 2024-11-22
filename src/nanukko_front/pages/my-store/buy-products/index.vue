<script setup>
import axios from 'axios';
import Pagination from '~/components/Pagination.vue';
import OrdersGrid from '~/components/my-store/buy-products/OrdersGrid.vue';
import StatusFilter from '~/components/my-store/buy-products/StatusFilter.vue';
import { useApi } from '~/composables/useApi';

const { baseURL } = useApi();
const userOrders = ref([]);
const userId = "buyer1";
const currentStatus = ref("ESCROW_HOLDING");
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);

const loadUserOrders = async (page = 0) => {
  try {
    const response = await axios.get(
      `${baseURL}/my-store/buy-products`,
      {
        params: { userId, status: currentStatus.value, page, size: pageSize.value }
      }
    );
    userOrders.value = response.data.content;
    totalPages.value = response.data.totalPages;
    currentPage.value = response.data.currentPage;
  } catch (error) {
    console.error("로딩중 에러: ", error);
  }
};

const handleStatusChange = async (newStatus) => {
  currentStatus.value = newStatus;
  await loadUserOrders(0);
};

const handlePageChange = async (page) => {
  await loadUserOrders(page);
  window.scrollTo(0, 0);
};

onMounted(() => {
  loadUserOrders();
});
</script>

<template>
  <div class="buy-products-page">
    <h1>내 구매 상품</h1>
    <hr><br>
    
    <StatusFilter
      :currentStatus="currentStatus"
      @status-change="handleStatusChange"
    />

    <OrdersGrid
      :orders="userOrders"
      :userId="userId"
      @orders-updated="loadUserOrders"
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
@import url('../../../assets/my-store/BuyProducts.css');
</style>