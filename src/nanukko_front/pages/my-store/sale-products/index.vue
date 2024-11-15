<script setup>
import axios from "axios";
import ProductsGrid from "~/components/my-store/sale-products/ProductsGrid.vue";
import StatusFilter from "~/components/my-store/sale-products/StatusFilter.vue";
import Pageination from "~/components/Pageination.vue";

const userProducts = ref([]);
const userId = "seller1"; //추후에 로그인 한 유저로 변경
const currentStatus = ref("SELLING"); //상태 기본값 설정
const currentPage = ref(0);
const totalPages = ref(0);
const pageSize = ref(5);

//판매 상품 출력
const loadUserProducts = async (page = 0) => {
  try {
    //params 객체를 사용하면 Axios가 파라미터들을 자동으로 인코딩해줌
    const response = await axios.get(
      `http://localhost:8080/api/my-store/sale-products`,
      {
        params: {
          userId: userId,
          status: currentStatus.value,
          page: page,
          size: pageSize.value,
        },
      }
    );
    userProducts.value = response.data.content; //백의 PageResponseDTO의 실제 데이터를 가리키는 content
    totalPages.value = response.data.totalPages; //백의 PageResponseDTO의 전체 페이지수 = totalPages
    currentPage.value = response.data.currentPage; //백의 PageResponseDTO의 현재 페이지 = currentPage
    console.log('데이터 로드됨: ', userProducts.value);
  } catch (error) {
    console.error("로딩중 에러: ", error);
  }
};

const handleStatusChange = async (newStatus) => {
  console.log('상태 변경: ', newStatus);
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
  <div>
    <h1>내 판매 상품</h1>
    

    <StatusFilter
      :currentStatus="currentStatus"
      @status-change="handleStatusChange"
    />

    <ProductsGrid
      :products="userProducts"
      :userId="userId"
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
