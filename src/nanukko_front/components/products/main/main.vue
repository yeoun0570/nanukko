<template>
    <div class="products-container">
        <div v-if="!isAuthenticated" class="section-title">
            최근 등록된 장난감 둘러보기
        </div>
        <div v-if="isAuthenticated" class="section-title">
            {{ nickname }}님을 위한 장난감 추천
        </div>


        <div v-if="loading && !products.content.length" class="loading-spinner">
            로딩중...
        </div>

        <div v-else-if="error" class="error-message">
            {{ error }}
        </div>

        <div v-else>
            <div v-if="products.content.length === 0" class="empty-message">
                등록된 상품이 없습니다.
            </div>

            <!-- 상품 그리드 -->
            <div v-else class="product-grid">
                <div v-for="product in products.content" :key="product.productId" class="product-card"
                    @click="goToProduct(product.productId)">
                    <div class="card card-fixed-height">
                        <div class="card-img-wrapper">
                            <img :src="product.thumbnailImage" class="card-img-top" :alt="product.productName"
                                width="300" height="300" />


                            <div v-if="product.status === 'RESERVED' || product.status === 'SOLD_OUT'"
                                class="status-overlay" :class="{
                                    'reserved': product.status === 'RESERVED',
                                    'sold-out': product.status === 'SOLD_OUT'
                                }">
                                {{ getStatusText(product.status) }}
                            </div>


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

            <div v-if="!products.last" class="load-more-container">
                <button @click="loadMore" class="load-more-button" :disabled="loading">
                    <span v-if="loading">로딩중...</span>
                    <span v-else>더보기</span>
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from "@/composables/useApi";
import { useAuth } from "@/composables/auth/useAuth";
import TimeAgo from '@/components/common/TimeAgo.vue';

const api = useApi();
const router = useRouter();
const { isAuthenticated, nickname } = useAuth();
const getStatusText = (status) => {
    return status === 'RESERVED' ? '예약중' : '판매완료';
};

const products = ref({
    content: [],
    last: true,
    totalPages: 0,
    totalElements: 0,
    first: true,
    numberOfElements: 0
});

const pageNumber = ref(0);
const loading = ref(false);
const error = ref(null);

const fetchProducts = async (page) => {
    loading.value = true;
    error.value = null;

    try {
        // 로그인 상태에 따라 다른 엔드포인트 호출
        const endpoint = isAuthenticated.value ? '/products/main' : '/products/main/logout';

        const response = await api.get(endpoint, {
            params: {
                page,
                size: 20,
                sort: 'updatedAt,DESC'
            }
        });

        console.log('API Response:', response);

        if (page === 0) {
            products.value = response;
        } else {
            products.value = {
                ...response,
                content: [...products.value.content, ...response.content]
            };
        }
    } catch (err) {
        error.value = '상품을 불러오는데 실패했습니다.';
        console.error('API 에러 상세:', err.response);
    } finally {
        loading.value = false;
    }
};

const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price);
};

const goToProduct = async (productId) => {
    if (!productId) {
        console.error('상품 ID가 없습니다');
        return;
    }

    await router.push(`/products/${productId}`);
};

const loadMore = async () => {
    if (loading.value || products.value.last) return;
    pageNumber.value += 1;
    await fetchProducts(pageNumber.value);
};

onMounted(() => {
    fetchProducts(0);
});
</script>

<style scoped>
.section-title {
    font-size: 1.5rem;
    color: #333;
    text-align: left;
    margin: 50px 0 0;
    font-weight: 500;
}

.products-container {
    max-width: 1200px;
    margin: 0 auto;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin: 20px 0 50px;
}

.product-card {
    cursor: pointer;
    transition: transform 0.2s;
    height: 100%;
    width: 100%;
    display: flex;
    flex-direction: column;
    border-radius: 8px;
}

.product-card:hover {
    transform: translateY(-5px);
}

.card-fixed-height {
    height: 100%;
    display: flex;
    flex-direction: column;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    overflow: hidden;
}

.card-img-wrapper {
    position: relative;
    padding-top: 100%;
}

.card-img-wrapper img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
}


.status-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 1rem;
    font-weight: bold;
    color: white;
    background-color: rgba(0, 0, 0, 0.6);
}

.status-overlay.reserved {
    background-color: #FFC755;
}

.status-overlay.sold-out {
    background-color: rgba(0, 0, 0, 0.7);
}

.card-body {
    padding: 12px;
    flex-grow: 1;
}

.card-footer {
    padding: 0.5rem 1rem;
    background-color: #f8f9fa;
    border-top: 1px solid #dee2e6;
}

.flex-between-center {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 0.5rem;
}

.card-title {
    font-size: 1rem;
    margin-bottom: 0.5rem;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    height: 1.5em;
    line-height: 1.4;
}

.card-price {
    font-weight: bold;
    font-size: 1.2rem;
    color: #333;
}

.small-text-muted {
    color: #6c757d;
    font-size: 0.8rem;
}

.load-more-container {
    display: flex;
    justify-content: center;
    margin: 30px 0;
}

.load-more-button {
    padding: 12px 24px;
    background-color: #fff;
    border: 1px solid #dee2e6;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: background-color 0.2s;
}

.load-more-button:hover:not(:disabled) {
    background-color: #f8f9fa;
}

.load-more-button:disabled {
    background-color: #e9ecef;
    cursor: not-allowed;
}

.loading-spinner {
    text-align: center;
    padding: 40px;
}

.error-message {
    text-align: center;
    color: #dc3545;
    padding: 20px;
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
</style>