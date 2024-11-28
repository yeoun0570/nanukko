<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import ProductImage from '~/components/products/products-detail/ProductImage.vue';
import ProductActions from '~/components/products/products-detail/ProductActions.vue';
import ProductMiniSummary from '~/components/products/products-detail/ProductMiniSummary.vue';
import TimeAgo from '~/components/common/TimeAgo.vue';
import Map from '~/components/products/products-detail/Map.vue';

const route = useRoute();
const product = ref(null);

// 상품 정보 로드
const loadProductDetail = async () => {
    try {
        // URL 파라미터 이름을 id로 변경
        const response = await axios.get(`http://localhost:8080/api/products/${route.params.id}`);
        product.value = response.data;
    } catch (error) {
        console.error('상품 정보 로드 실패:', error);
        alert('상품 정보를 불러오는데 실패했습니다.');
    }
};

// 가격 포맷팅
const formattedPrice = computed(() => {
    if (!product.value) return '0';
    return product.value.price.toLocaleString();
});

const handleActionClick = () => {
    console.log("확인!");
};

onMounted(() => {
    loadProductDetail();
    loadRelatedProducts();
});

const relatedProducts = ref([]);

// 연관 상품 로드
const loadRelatedProducts = async () => {
    try {
        const response = await fetch(
            `http://localhost:8080/api/products/related?productId=${route.params.id}&limit=5`
        );
        const data = await response.json();
        relatedProducts.value = data;
    } catch (error) {
        console.error('연관 상품 로드 실패:', error);
    }
};

const productImages = computed(() => {
    if (!product.value) return [];

    // 각 이미지 필드를 배열로 변환
    const images = [
        product.value.image1,
        product.value.image2,
        product.value.image3,
        product.value.image4,
        product.value.image5
    ];

    // null, undefined, 빈 문자열 제거
    return images.filter(image => image);
});

</script>

<template>
    <div v-if="product" class="card-container">
        <div class="product-page-top">
            <div class="product-image">
                <ProductImage :images="productImages" />
            </div>

            <div class="product-content">
                <div class="product-info">
                    <p class="product-name">{{ product.productName }}</p>
                    <p class="product-price">
                        {{ formattedPrice }}<span class="price-unit">원</span>
                    </p>
                    <hr>
                    <!-- 미니 아이콘 섹션 -->
                    <div class="product-stats">
                        <div class="stat-item">
                            <v-icon>mdi-heart</v-icon>
                            <span>{{ product.favoriteCount || 0 }}</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-eye-outline</v-icon>
                            <span>{{ product.viewCount || 0 }}</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-message-outline</v-icon>
                            <span>{{ product.talkCount || 0 }}</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-clock-outline</v-icon>
                            <TimeAgo :timestamp="product.updated_time" />
                        </div>
                    </div>
                </div>

                <ProductMiniSummary :condition="product.condition" :isPerson="product.isPerson"
                    :isShipping="product.isShipping" :shippingFee="product.shippingFee"
                    :freeShipping="product.freeShipping" :address="product.address" :isCompanion="product.isCompanion"
                    :isDeputy="product.isDeputy" :gender="product.gender" :ageGroup="product.ageGroup" />

                <ProductActions @action-click="handleActionClick" />
            </div>
        </div>

        <!-- 상품 설명 -->
        <div class="product-description">
            <h3>상품 설명</h3>
            <p>{{ product.content }}</p>
        </div>

        <!-- 거래 장소 (직거래인 경우) -->
        <div v-if="product.isPerson" class="trade-location">
            <h3>거래 장소</h3>
            <p class="address">
                {{ product.address }}
                <span v-if="product.detailAddress" class="detail-address">
                    {{ product.detailAddress }}
                </span>
            </p>
            <div class="map-container">
                <Map :lat="Number(product.latitude)" :lon="Number(product.longitude)" />
            </div>
        </div>

        <!-- 판매자 정보 섹션 -->
        <div class="seller-section card-container">
            <div class="seller-info">
                <!-- 프로필 및 닉네임 -->
                <div class="seller-profile">
                    <div class="profile-image">
                        <img :src="product.seller.profileImage || '/default-profile.jpg'" alt="판매자 프로필">
                    </div>
                    <div class="seller-name">
                        {{ product.seller.nickname }}
                    </div>
                </div>

                <!-- 판매 상품 정보 -->
                <div class="seller-products">
                    <div class="total-products">
                        총 판매상품 <span class="count">{{ product.seller.totalProducts }}개</span>
                    </div>
                    <ul class="recent-products">
                        <li v-for="item in product.seller.recentProducts" :key="item.productId">
                            {{ item.productName }}
                        </li>
                    </ul>
                </div>
            </div>
        </div>

        <!-- 연관 상품 섹션 -->
        <div class="related-products card-container">
            <h3>연관 상품</h3>
            <div class="products-row">
                <div v-for="relatedProduct in relatedProducts" :key="relatedProduct.productId"
                    class="related-product-card" @click="goToProduct(relatedProduct.productId)">
                    <div class="product-image">
                        <img :src="relatedProduct.thumbnailImage" :alt="relatedProduct.productName">
                    </div>
                    <div class="product-info">
                        <div class="product-name">{{ relatedProduct.productName }}</div>
                        <div class="product-price">{{ formatPrice(relatedProduct.price) }}원</div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div v-else class="loading">
        상품 정보를 불러오는 중...
    </div>

</template>

<style scoped>
/* 판매자 정보 스타일 */
.seller-section {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: white;
    border-radius: 0.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.seller-info {
    display: flex;
    gap: 2rem;
}

.seller-profile {
    display: flex;
    align-items: center;
    gap: 1rem;
    min-width: 200px;
}

.profile-image {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    overflow: hidden;
}

.profile-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.seller-name {
    font-size: 1.1rem;
    font-weight: 500;
}

.seller-products {
    flex: 1;
}

.total-products {
    margin-bottom: 1rem;
    color: #666;
}

.count {
    color: #4c6ef5;
    font-weight: 500;
}

.recent-products {
    list-style: none;
    padding: 0;
    margin: 0;
}

.recent-products li {
    padding: 0.5rem 0;
    color: #333;
}

/* 연관 상품 스타일 */
.related-products {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: white;
    border-radius: 0.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.related-products h3 {
    margin-bottom: 1.5rem;
    font-size: 1.25rem;
    font-weight: 600;
}

.products-row {
    display: flex;
    gap: 1rem;
    overflow-x: auto;
    padding-bottom: 1rem;
}

.related-product-card {
    flex: 0 0 200px;
    border: 1px solid #dee2e6;
    border-radius: 0.5rem;
    overflow: hidden;
    cursor: pointer;
    transition: transform 0.2s;
}

.related-product-card:hover {
    transform: translateY(-4px);
}

.related-product-card .product-image {
    width: 100%;
    height: 200px;
}

.related-product-card .product-image img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.related-product-card .product-info {
    padding: 1rem;
}

.related-product-card .product-name {
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.related-product-card .product-price {
    font-weight: 500;
    color: #4c6ef5;
}

.card-container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
}

.product-page-top {
    display: flex;
    gap: 2rem;
    /* max-height: 500px; */
}

.product-image {
    flex: 0 0 500px;
    /* 고정 너비 500px로 변경 */
    height: 500px;
    /* 높이도 500px로 설정 */
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

.product-description {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: white;
    border-radius: 0.5rem;
}

.product-description h3 {
    font-size: 1.25rem;
    margin-bottom: 1rem;
}

.loading {
    text-align: center;
    padding: 2rem;
    color: #666;
}

.trade-location {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: white;
    border-radius: 0.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.trade-location h3 {
    font-size: 1.25rem;
    font-weight: 600;
    margin-bottom: 1rem;
    color: #333;
}

.trade-location .address {
    margin-bottom: 1rem;
    color: #666;
    font-size: 0.95rem;
}

.trade-location .detail-address {
    margin-left: 0.5rem;
    color: #888;
    font-size: 0.9rem;
}

.map-container {
    width: 100%;
    height: 400px;
    border-radius: 0.5rem;
    overflow: hidden;
    border: 1px solid #eee;
}
</style>