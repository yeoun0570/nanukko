// ProductDetail.vue
<script setup>
import { computed } from 'vue';
// import { ProductData } from '../assets/productDetail';
import ProductImage from '~/components/products/products-detail/ProductImage.vue';
import ProductInfo from '~/components/products/products-detail/ProductInfo.vue';
import ProductActions from '~/components/products/products-detail/ProductActions.vue';
import ProductMiniSummary from '~/components/products/products-detail/ProductMiniSummary.vue';
import TimeAgo from '~/components/common/TimeAgo.vue';

const logMessage = () => {
    console.log("확인!");
};

// 가격 포맷팅
const formattedPrice = computed(() => {
    return ProductData.price.toLocaleString();
});

// 배송비 포맷팅
const formattedShippingFee = computed(() => {
    return ProductData.shipping_fee.toLocaleString();
});
</script>

<template>
    <div class="card-container">
        <div class="product-page-top">
            <div class="product-image">
                <ProductImage :images="ProductData.img_url" />
            </div>

            <div class="product-content">
                <div class="product-info">
                    <p class="product-name">{{ ProductData.name }}</p>
                    <p class="product-price">
                        {{ formattedPrice }}<span class="price-unit">원</span>
                    </p>
                    <hr>
                    <!-- 미니 아이콘 섹션 -->
                    <div class="product-stats">
                        <div class="stat-item">
                            <v-icon>mdi-heart</v-icon>
                            <span>3</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-eye-outline</v-icon>
                            <span>{{ ProductData.view_count }}</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-clock-outline</v-icon>
                            <TimeAgo :timestamp="ProductData.updated_time" />
                        </div>
                    </div>
                </div>

                <ProductMiniSummary :categoryId="ProductData.category_id" :offlineType="ProductData.offline_type"
                    :gender="ProductData.gender" :condition="ProductData.condition"
                    :shippingFee="ProductData.shipping_fee" :isOffline="ProductData.is_offline"
                    :location="ProductData.location" />

                <ProductActions @action-click="logMessage" />
            </div>
        </div>
    </div>
</template>

<style scoped>
.card-container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.product-page-top {
    display: flex;
    gap: 2rem;
    max-height: 500px;
}

.product-image {
    flex: 0 0 40%;
    border: 2px solid black;
}

.product-content {
    flex: 0 0 60%;
    display: flex;
    flex-direction: column;
}

.product-info {
    margin-bottom: 1.5rem;
}

.product-name {
    font-size: 1.125rem;
    margin-bottom: 0.5rem;
}

.product-price {
    font-size: 1.5rem;
    font-weight: bold;
}

.price-unit {
    font-size: 1rem;
    margin-left: 0.3125rem;
}

.product-stats {
    display: flex;
    gap: 1rem;
    color: #ccc;
    font-size: 0.875rem;
    margin-top: 1rem;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 0.3125rem;
}

hr {
    border: none;
    border-top: 1px solid #ddd;
    margin: 1rem 0;
}
</style>