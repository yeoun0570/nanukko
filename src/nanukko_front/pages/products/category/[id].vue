<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import TimeAgo from '~/components/common/TimeAgo.vue';

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
const middleCategories = ref([]);
const selectedMiddleId = ref(null);

const categoryNames = {
    '100000': '0~3개월',
    '200000': '3~6개월',
    '300000': '6~12개월',
    '400000': '만1세',
    '500000': '만2세',
    '600000': '유아'
};

const categoryName = computed(() => categoryNames[route.params.majorId] || '카테고리');

const fetchMiddleCategories = async () => {
    try {
        const response = await fetch(`http://localhost:8080/api/categories/middle/${route.params.majorId}`);
        const data = await response.json();
        middleCategories.value = data;
    } catch (error) {
        console.error('중분류 카테고리 로드 실패:', error);
    }
};

const fetchProducts = async () => {
    loading.value = true;
    try {
        const url = selectedMiddleId.value
            ? `http://localhost:8080/api/products/middle?middleId=${selectedMiddleId.value}&page=${pageNumber.value}&size=${pageSize}`
            : `http://localhost:8080/api/products/major?majorId=${route.params.majorId}&page=${pageNumber.value}&size=${pageSize}`;
        const response = await fetch(url);
        const data = await response.json();
        products.value = data;
    } catch (error) {
        console.error('상품 로드 실패:', error);
    } finally {
        loading.value = false;
    }
};

const formatPrice = (price) => {
    return price?.toLocaleString() ?? '0';
};

const goToProduct = (productId) => {
    router.push(`/products/${productId}`);
};

const selectMiddleCategory = (middleId) => {
    selectedMiddleId.value = middleId;
    pageNumber.value = 0;
    fetchProducts();
};

const changePage = (newPage) => {
    pageNumber.value = newPage;
    window.scrollTo(0, 0);
};

watch(() => route.params.majorId, () => {
    selectedMiddleId.value = null;
    pageNumber.value = 0;
    fetchMiddleCategories();
    fetchProducts();
}, { immediate: true });

watch(pageNumber, () => {
    fetchProducts();
});
</script>

<template>
    <div class="card-container mx-auto">
        <!-- 중분류 카테고리 목록 -->
        <div class="middle-categories">
            <button class="category-button" :class="{ active: !selectedMiddleId }" @click="selectMiddleCategory(null)">
                전체보기
            </button>
            <button v-for="category in middleCategories" :key="category.middleId" class="category-button"
                :class="{ active: selectedMiddleId === category.middleId }"
                @click="selectMiddleCategory(category.middleId)">
                {{ category.middleName }}
                <span class="count">{{ category.count || '' }}</span>
            </button>
        </div>

        <h2 class="search-title">
            {{ categoryName }} ({{ products.totalElements }}개)
        </h2>

        <div v-if="loading" class="loading-container">
            <div class="loading">로딩 중...</div>
        </div>

        <div v-else-if="products.content?.length === 0" class="no-results">
            상품이 없습니다.
        </div>

        <div v-else class="product-grid">
            <div v-for="product in products.content" :key="product.productId" class="product-card"
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
                                    <TimeAgo :timestamp="product.updatedTime" />
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

        <div v-if="products.content?.length > 0" class="pagination-container">
            <button :disabled="products.first" @click="changePage(pageNumber - 1)" class="page-button">
                이전
            </button>
            <span class="page-info">{{ pageNumber + 1 }} / {{ products.totalPages }}</span>
            <button :disabled="products.last" @click="changePage(pageNumber + 1)" class="page-button">
                다음
            </button>
        </div>
    </div>
</template>

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
    cursor: pointer;
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
    font-size: 0.9rem;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 0.5rem;
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