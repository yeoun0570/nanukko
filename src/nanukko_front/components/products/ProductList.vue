<template>
    <div class="card-container">
        <!-- 로딩 상태 -->
        <div v-if="loading" class="loading-container">
            <div class="loading">{{ loadingText }}</div>
        </div>

        <!-- 결과 없음 -->
        <div v-else-if="products.content?.length === 0" class="no-results">
            {{ emptyMessage }}
        </div>

        <!-- 상품 그리드 -->
        <div v-else class="product-grid">
            <div v-for="product in products.content" :key="product.productId" class="product-card"
                @click="goToProduct(product.productId)">
                <div class="card card-fixed-height">
                    <div class="card-img-wrapper">
                        <img :src="product.thumbnailImage" class="card-img-top" :alt="product.productName" width="300"
                            height="300" />


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

        <!-- 페이지네이션 -->
        <div v-if="products.content?.length > 0" class="pagination-container">
            <button :disabled="products.first" @click="emit('page-change', pageNumber - 1)" class="page-button">
                이전
            </button>
            <span class="page-info">{{ pageNumber + 1 }} / {{ products.totalPages }}</span>
            <button :disabled="products.last" @click="emit('page-change', pageNumber + 1)" class="page-button">
                다음
            </button>
        </div>
    </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';
import TimeAgo from '@/components/common/TimeAgo.vue';

const getStatusText = (status) => {
    return status === 'RESERVED' ? '예약중' : '판매완료';
};

const props = defineProps({
    products: {
        type: Object,
        required: true
    },
    loading: {
        type: Boolean,
        default: false
    },
    pageNumber: {
        type: Number,
        required: true
    },
    loadingText: {
        type: String,
        default: '로딩 중...'
    },
    emptyMessage: {
        type: String,
        default: '상품이 없습니다.'
    }
});

const emit = defineEmits(['product-click', 'page-change']);

const formatPrice = (price) => {
    return price.toLocaleString();
};

const goToProduct = (productId) => {
    emit('product-click', productId);
};
</script>


<style scoped>
.card-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1rem 0 0;
}

.product-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin: 0 0 50px;
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

.card-img-wrapper {
    position: relative;
    padding-top: 100%;
    width: 100%;
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
    background-color: #5F5F5F;
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

.card-body {
    padding: 12px;
    flex-grow: 1;
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
    padding: 0.5rem 1rem;
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

</style>