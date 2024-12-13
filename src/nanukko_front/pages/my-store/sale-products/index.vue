<script setup>
import ProductsGrid from "~/components/my-store/sale-products/ProductsGrid.vue";
import StatusFilter from "~/components/my-store/sale-products/StatusFilter.vue";
import Pageination from "~/components/Pagination.vue";
import { useApi } from "@/composables/useApi";

const api = useApi();

definePageMeta({
  layout: "mystore",
});

const userProducts = ref([]);
const currentStatus = ref("SELLING"); //상태 기본값 설정
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);

//판매 상품 출력
const loadUserProducts = async (page = 0) => {
  try {
    // API 요청 파라미터 로깅
    console.log("Request params:", {
      status: currentStatus.value,
      page: page,
      size: pageSize.value,
    });

    //params 객체를 사용하면 Axios가 파라미터들을 자동으로 인코딩해줌
    const response = await api.get(`/my-store/sale-products`, {
      params: {
        status: currentStatus.value,
        page: page,
        size: pageSize.value,
      },
    });
    // 서버 응답 로깅
    console.log("Server response:", response);
    
    userProducts.value = response.content; //백의 PageResponseDTO의 실제 데이터를 가리키는 content
    totalPages.value = response.totalPages; //백의 PageResponseDTO의 전체 페이지수 = totalPages
    currentPage.value = response.currentPage; //백의 PageResponseDTO의 현재 페이지 = currentPage
    console.log("데이터 로드됨: ", userProducts.value);
  } catch (error) {
    console.error("로딩중 에러: ", error);
  }
};

const handleStatusChange = async (newStatus) => {
  console.log("상태 변경: ", newStatus);
  currentStatus.value = newStatus;
  currentPage.value = 0; //상태 변경시 첫 페이지로 리셋
  await loadUserProducts();
};

const handlePageChange = async (page) => {
  await loadUserProducts(page);
  window.scrollTo(0, 0);
};

onMounted(() => {
  loadUserProducts();
});
</script>

<template>
  <div class="product-modify-page">
    <h1>내 판매 상품</h1>
    <hr color="black" />
    <br />

    <StatusFilter
      :currentStatus="currentStatus"
      @status-change="handleStatusChange"
    />

    <ProductsGrid
      :products="userProducts"
      @products-updated="loadUserProducts"
    />

    <Pageination
      v-if="totalPages > 1"
      :currentPage="currentPage"
      :totalPages="totalPages"
      @page-change="handlePageChange"
    />
  </div>
</template>

<style>
@import url("../../../assets/my-store/SaleProducts.css");
</style>
