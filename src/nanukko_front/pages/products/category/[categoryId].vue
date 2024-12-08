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
        <h2 class="search-title">
            <span class="category-name">{{ categoryName }}</span>
            <span style="margin-right: 20px;"></span>
            <span class="total-count">총 {{ products.totalElements }} 개가 검색 되었습니다.</span>
        </h2>

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

        <ProductList :products="products" :loading="loading" :page-number="pageNumber" @product-click="goToProduct"
            @page-change="changePage" />
    </div>
</template>
<style>
.card-container {
    max-width: 1200px;
    padding: 2rem;
    margin: 0 auto;
}

.search-title {
    margin-bottom: 1rem;
    font-size: 1.5rem;
    color: #333;
}

.category-name {
    color: #FF9D14;
}

.total-count {
    font-size: 1rem;
}

.middle-categories {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1rem;
}

.category-button {
    padding: 0.5rem 1rem;
    border: 1px solid #dee2e6;
    border-radius: 8px;
    background: white;
    cursor: pointer;
    font-size: 1rem;
    transition: all 0.2s;
    display: flex;
    align-items: center;
}

.category-button.active {
    background-color: #618EFF;
    color: white;
    border-color: #618EFF;
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