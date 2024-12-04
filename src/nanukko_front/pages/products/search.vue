<template>
    <div class="card-container mx-auto">
        <h2 class="search-title">
            "{{ searchQuery }}" 검색 결과 ({{ searchResults.totalElements }}개)
        </h2>

        <!-- 로딩 상태 -->
        <div v-if="isLoading" class="loading-container">
            <div class="loading">검색 중...</div>
        </div>

        <!-- 결과 없음 -->
        <div v-else-if="searchResults.content?.length === 0" class="no-results">
            검색 결과가 없습니다.
        </div>

        <!-- 검색 결과 그리드 -->
        <div v-else class="product-grid">
            <div v-for="product in searchResults.content" :key="product.productId" class="product-card"
                @click="goToProduct(product.productId)">
                <div class="card card-fixed-height">
                    <div class="card-img-wrapper">
                        <img :src="product.thumbnailImage" class="card-img-top" :alt="product.productName" width="300"
                            height="300" />
                    </div>

                    <div class="card-body">
                        <div>
                            <small class="card-title">{{ product.productName }}</small>
                            <div class="flex-between-center">
                                <span class="card-price mb-0">{{ formatPrice(product.price) }}원</span>
                                <span class="small-text-muted">
                                    <TimeAgo :timestamp="product.updatedAt" />
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="card-footer">
                        <span class="small-text-muted">{{ product.address || '지역정보 없음' }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- 페이지네이션 -->
        <div v-if="searchResults.content?.length > 0" class="pagination-container">
            <button :disabled="searchResults.first" @click="changePage(pageNumber - 1)" class="page-button">
                이전
            </button>
            <span class="page-info">{{ pageNumber + 1 }} / {{ searchResults.totalPages }}</span>
            <button :disabled="searchResults.last" @click="changePage(pageNumber + 1)" class="page-button">
                다음
            </button>
        </div>
    </div>
</template>

<script setup>

import { ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import TimeAgo from '~/components/common/TimeAgo.vue';
import { useApi } from "@/composables/useApi";

const api = useApi();
const route = useRoute();
const router = useRouter();
const searchQuery = ref('');
const searchResults = ref({
    content: [],
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true
});
const isLoading = ref(false);
const pageNumber = ref(0);
const pageSize = 20;

// 가격 포맷팅
const formatPrice = (price) => {
    return price?.toLocaleString() ?? '0';
};

// 상품 상세 페이지로 이동
const goToProduct = (productId) => {
    router.push(`/products/${productId}`);
};

// 검색 실행
const searchProducts = async () => {
    if (!searchQuery.value) return;

    isLoading.value = true;
    try {
        const response = await api.get(`/products/search`, {
            params: {
                query: searchQuery.value,
                page: pageNumber.value,
                size: pageSize,
                sort: 'updatedAt,desc'
            }
        });
        searchResults.value = response;
    } catch (error) {
        console.error('검색 오류:', error);
        searchResults.value = {
            content: [],
            totalElements: 0,
            totalPages: 0,
            first: true,
            last: true
        };
    } finally {
        isLoading.value = false;
    }
};

// 페이지 변경
const changePage = (newPage) => {
    pageNumber.value = newPage;
    window.scrollTo(0, 0);
};

// URL 쿼리 파라미터 변경 감지
watch(() => route.query.q, (newQuery) => {
    if (newQuery) {
        searchQuery.value = newQuery;
        pageNumber.value = 0;
        searchProducts();
    }
}, { immediate: true });

// 페이지 번호 변경 감지
watch(pageNumber, () => {
    searchProducts();
});
</script>

<style scoped>
.card-container {
    max-width: 1400px;
    padding: 2rem;
    margin: 0 auto;
}

.search-title {
    margin-bottom: 2rem;
    font-size: 1.5rem;
    color: #333;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 2rem;
    margin-bottom: 2rem;
}

.product-card {
    display: flex;
    justify-content: center;
}

.card-img-wrapper {
    width: 300px;
    height: 300px;
    margin: 0 auto;
}

.card-img-top {
    width: 300px;
    height: 300px;
    object-fit: cover;
}

.card-fixed-height {
    width: 300px;
    margin: 0 auto;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    border: 1px solid #dee2e6;
    border-radius: 8px;
    overflow: hidden;
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

/* 반응형 레이아웃 */
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
</style>