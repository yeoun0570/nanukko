<template>
    <div v-if="show" class="search-results">
        <div v-if="isLoading" class="loading">검색 중...</div>
        <div v-else-if="results.content?.length === 0" class="no-results">
            검색 결과가 없습니다.
        </div>
        <div v-else-if="results.content" class="results-list">
            <div v-for="product in results.content" :key="product.productId" class="result-item"
                @click="handleProductClick(product.id)">
                <div class="product-info">
                    <span class="product-name">{{ product.productName }}</span>
                    <span class="product-price">{{ formatPrice(product.price) }}원</span>
                </div>
            </div>
            <div v-if="results.totalElements > 5" class="view-all">
                <button @click="viewAllResults" class="view-all-button">
                    전체 결과 보기 (총 {{ results.totalElements }}개)
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const props = defineProps({
    show: {
        type: Boolean,
        required: true
    },
    results: {
        type: Object,
        default: () => ({ content: [] })
    },
    isLoading: {
        type: Boolean,
        default: false
    },
    searchQuery: {
        type: String,
        required: true
    }
});

const formatPrice = (price) => {
    return price?.toLocaleString() ?? '0';
};

const handleProductClick = (productId) => {
    router.push(`/products/${productId}`);
    emit('close');
};

const viewAllResults = () => {
    router.push({
        path: '/search',
        query: { q: props.searchQuery }
    });
    emit('close');
};

const emit = defineEmits(['close']);
</script>

<style scoped>
.search-results {
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    width: 400px;
    max-height: 500px;
    background-color: white;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    overflow-y: auto;
    margin-top: 8px;
}

.loading,
.no-results {
    padding: 1rem;
    text-align: center;
    color: #666;
}

.result-item {
    padding: 0.75rem 1rem;
    cursor: pointer;
    border-bottom: 1px solid #f0f0f0;
}

.result-item:hover {
    background-color: #f8f9fa;
}

.product-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.product-name {
    font-size: 0.9rem;
    color: #333;
}

.product-price {
    font-size: 0.9rem;
    color: #666;
}

.view-all {
    padding: 0.75rem;
    border-top: 1px solid #e0e0e0;
    text-align: center;
}

.view-all-button {
    width: 100%;
    padding: 0.5rem;
    background-color: #f8f9fa;
    border: none;
    border-radius: 4px;
    color: #4c6ef5;
    cursor: pointer;
    font-size: 0.9rem;
}

.view-all-button:hover {
    background-color: #e9ecef;
}
</style>