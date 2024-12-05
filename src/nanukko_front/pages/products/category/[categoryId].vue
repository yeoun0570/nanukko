<script setup>
import { ref, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from "@/composables/useApi";
import { useURL } from "~/composables/useURL";
import ProductList from '@/components/products/ProductList.vue';

const axiosInstance = useURL();
const api = useApi();
const route = useRoute();
const router = useRouter();
const products = ref({
    content: [],
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true
});
const loading = ref(false);
const pageNumber = ref(0);
const pageSize = 20;
const majorCategoriesAll = ref([]); //대분류 전체 상품 리스트
const middleCategoriesTitle = ref([]); //중분류 리스트 모음 (중분류 버튼)
const selectedMiddleId = ref(null); //중분류 상품 리스트
const categoryNames = {
    '100000': '0~3개월',
    '200000': '3~6개월',
    '300000': '6~12개월',
    '400000': '만1세',
    '500000': '만2세',
    '600000': '유아'
};
const categoryName = computed(() => categoryNames[route.params.categoryId] || '카테고리');

onMounted(() => {
    watch(() => route.params.categoryId, (newId) => {
        console.log('categoryId changed:', newId);
        if (newId) {
            console.log('Fetching data for categoryId:', newId);
            selectedMiddleId.value = null;
            pageNumber.value = 0;
            fetchMiddleCategories();
            fetchMajorCategoriesProducts();
            fetchMiddleCategoriesProducts();
        }
    }, { immediate: true });

    watch(pageNumber, () => {
        fetchMiddleCategoriesProducts();
    });
});

const fetchMiddleCategories = async () => {
    console.log('fetchMiddleCategories 시작');
    try {
        const response = await api.get(`/categories/middle/${route.params.categoryId}`);
        middleCategoriesTitle.value = response;
        console.log("중분류 카테고리 리스트 : ", middleCategoriesTitle);
    } catch (error) {
        console.log("중분류 카테고리 리스트 조회 실패 ", error);
    }
}

const fetchMajorCategoriesProducts = async (page = 0, size = 20) => {
    console.log('fetchMajorCategoriesProducts 시작', route.params.categoryId);
    try {
        const response = await axiosInstance.get("/products/category/major", {
            params: {  // params 객체로 쿼리 파라미터 전달
                majorId: route.params.categoryId,
                page: page,
                size: size
            }
        });

        majorCategoriesAll.value = response;
        console.log("대분류 전체 상품 조회 : ", response);
    } catch (error) {
        console.error('대분류 전체 상품 조회 실패:', error);
    }
};

const fetchMiddleCategoriesProducts = async () => {
    if (!route.params.categoryId) return; // 카테고리 ID가 없으면 실행하지 않음

    loading.value = true;
    try {
        const url = selectedMiddleId.value
            ? `/products/category/middle` // URL 수정
            : `/products/category/major`; // URL 수정

        const params = selectedMiddleId.value
            ? {
                middleId: selectedMiddleId.value,
                page: pageNumber.value,
                size: pageSize
            }
            : {
                majorId: route.params.categoryId,
                page: pageNumber.value,
                size: pageSize
            };

        const response = await api.get(url, { params }); // params 객체로 쿼리 파라미터 전달
        products.value = response;
    } catch (error) {
        console.error('상품 로드 실패:', error);
    } finally {
        loading.value = false;
    }
};

const goToProduct = (productId) => {
    router.push(`/products/${productId}`);
};

const selectMiddleCategory = (middleId) => {
    selectedMiddleId.value = middleId;
    pageNumber.value = 0;
    fetchMiddleCategoriesProducts();
};

const changePage = (newPage) => {
    pageNumber.value = newPage;
    window.scrollTo(0, 0);
};
</script>

<template>
    <div class="card-container mx-auto">
        <!-- 중분류 카테고리 목록 -->
        <div class="middle-categories">
            <button class="category-button" :class="{ active: !selectedMiddleId }" @click="selectMiddleCategory(null)">
                전체보기
            </button>

            <button v-for="category in middleCategoriesTitle" :key="category.middleId" class="category-button"
                :class="{ active: selectedMiddleId === category.middleId }"
                @click="selectMiddleCategory(category.middleId)">
                {{ category.middleName }}
                <span class="count">{{ category.count || '' }}</span>
            </button>
        </div>

        <h2 class="search-title">
            <span class="category-name">{{ categoryName }}</span>
            <span style="margin-right: 10px;"></span>
            <span class="total-count">전체 상품 수 : {{ products.totalElements }}</span>
        </h2>

        <ProductList :products="products" :loading="loading" :page-number="pageNumber" @product-click="goToProduct"
            @page-change="changePage" />
    </div>
</template>

<style scoped>
.card-container {
    max-width: 1200px;
    padding: 2rem;
    margin: 0 auto;
}

.search-title {
    margin-bottom: 2rem;
    font-size: 1.2rem;
    color: #333;
}

.category-name {
    color: #3B82F6;
}

.total-count {
    font-size: 1rem;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 2rem;
}

.product-card {
    display: flex;
    flex-direction: column;
    border-radius: 8px;
    cursor: pointer;
    width: 100%;
}

.card-img-wrapper {
    position: relative;
    padding-top: 100%;
    width: 100%;
}


.card-img-top {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.card-fixed-height {
    height: 100%;
    display: flex;
    flex-direction: column;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    overflow: hidden;
    width: 100%;
}

.card-fixed-height:hover {
    transform: translateY(-5px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-body {
    padding: 1rem;
}

.card-title {
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    height: 2.8em;
    line-height: 1.4;
}

.card-price {
    font-weight: bold;
    font-size: 1.1rem;
    color: #333;
}

.flex-between-center {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.5rem;
}

.small-text-muted {
    font-size: 0.8rem;
    color: #6c757d;
}

.card-footer {
    padding: 0.75rem 1rem;
    background-color: #f8f9fa;
    border-top: 1px solid #dee2e6;
}

.loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
}

.loading {
    font-size: 1.2rem;
    color: #666;
}

.no-results {
    text-align: center;
    padding: 3rem;
    color: #666;
    font-size: 1.2rem;
}

.pagination-container {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 1rem;
    margin-top: 2rem;
    padding: 1rem;
}

.page-button {
    padding: 0.5rem 1rem;
    border: 1px solid #dee2e6;
    background-color: white;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.2s;
}

.page-button:hover:not(:disabled) {
    background-color: #f8f9fa;
}

.page-button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
}

.page-info {
    font-size: 0.9rem;
    color: #666;
}

@media (max-width: 1400px) {
    .product-grid {
        grid-template-columns: repeat(3, 1fr);
    }
}

@media (max-width: 1100px) {
    .product-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media (max-width: 750px) {
    .product-grid {
        grid-template-columns: 1fr;
    }
}


.middle-categories {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 2rem;
    padding: 1rem;
    border-bottom: 1px solid #dee2e6;
}

.category-button {
    padding: 0.5rem 1rem;
    border: 1px solid #dee2e6;
    border-radius: 20px;
    background: white;
    cursor: pointer;
    font-size: 1rem;
    transition: all 0.2s;
    display: flex;
    align-items: center;
}

.category-button.active {
    background-color: #4c6ef5;
    color: white;
    border-color: #4c6ef5;
}

.category-button:hover:not(.active) {
    background-color: #f8f9fa;
}

.count {
    font-size: 0.8rem;
    color: #666;
}

.category-button.active .count {
    color: white;
}
</style>