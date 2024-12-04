<script setup>
import { ref, computed, defineAsyncComponent } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';
import { useAuth } from "~/composables/auth/useAuth";
import { useApi } from "@/composables/useApi";
import ProductImage from '~/components/products/products-detail/ProductImage.vue';
import ProductMiniSummary from '~/components/products/products-detail/ProductMiniSummary.vue';
import Map from '~/components/products/products-detail/Map.vue';

const route = useRoute();
const router = useRouter();
const toast = useToast();
const { isAuthenticated } = useAuth();
const api = useApi();
const isLoading = ref(true);
const product = ref(null);
const relatedProducts = ref([]);
const TimeAgo = defineAsyncComponent(() => import('~/components/common/TimeAgo.vue'));

onMounted(async () => {
    try {
        await loadProductDetail();
        await loadRelatedProducts();
    } finally {
        isLoading.value = false;
    }
});


// 상품 정보 로드
const loadProductDetail = async () => {
    console.log('현재 route.params.id:', route.params.id);
    try {
        const response = await api.get(`/products/${route.params.id}`);
        product.value = response;
        await api.post(`/wishlist/${route.params.id}/view`); // 조회수 증가 API 호출
    } catch (error) {
        console.error('상품 정보 로드 실패:', error);
        toast.error('존재하지 않는 상품입니다.');
        await router.push('/');
        return;
    }
};

// 연관 상품 로드
const loadRelatedProducts = async () => {
    console.log('연관상품 로드 시 route.params.id:', route.params.id);
    try {
        const response = await api.get(`/products/related?productId=${route.params.id}`);
        if (Array.isArray(response)) {
            relatedProducts.value = response;
        } else {
            console.error('연관 상품 데이터 형식 오류:', response);
            relatedProducts.value = [];
        }
    } catch (error) {
        console.error('연관 상품 로드 실패:', error);
        relatedProducts.value = [];
    }
};

const goToProduct = (productId) => {
    router.push(`/products/${productId}`);
};

// 가격 포맷팅
const formattedPrice = computed(() => {
    if (!product.value?.price) return '0';
    return new Intl.NumberFormat('ko-KR').format(product.value.price);
});

const productImages = computed(() => {
    if (!product.value) return [];
    return [
        product.value.image1,
        product.value.image2,
        product.value.image3,
        product.value.image4,
        product.value.image5
    ].filter(image => image);
});

// 버튼 클릭 시
const handleWishClick = async () => {
    if (!isAuthenticated) {
        //에러 toast 추가 !!!

        setTimeout(() => {
            router.push('/auth/login')
        }, 1500)
        return
    }

    try {
        const response = await api.post(`/wishlist/${route.params.id}`);
        console.log('위시리스트 응답:', response);

        if (response) { // response.data 대신 response 확인
            product.value = {
                ...product.value,
                isWished: response.isWished,
                favorite_count: response.isWished
                    ? product.value.favorite_count + 1
                    : product.value.favorite_count - 1
            };

            toast.success(response.message);
        }
    } catch (error) {
        console.error('위시리스트 에러:', error); // 에러 로깅
        if (error.response?.status === 401) {
            toast.warning('로그인이 필요한 서비스입니다.');
            router.push('auth/login');
        } else {
            toast.error('처리 중 오류가 발생했습니다.');
        }
    }
};

const handleChatClick = () => {
    if (!isAuthenticated) {
        //에러 toast 추가 !!!

        setTimeout(() => {
            router.push('/auth/login')
        }, 1500)
        return
    }

    router.push({
        path: '/chat',
        query: { productId: route.params.id }
    });
}

const handleBuyClick = () => {
    if (!isAuthenticated) {
        //에러 toast 추가 !!!

        setTimeout(() => {
            router.push('/auth/login')
        }, 1500)
        return
    }

    router.push({
        path: '/payments',
        query: { productId: route.params.id }
    });
};
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
                    <div class="product-stats">
                        <div class="stat-item">
                            <v-icon>mdi-heart</v-icon>
                            <span>{{ product.favorite_count || 0 }}</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-eye-outline</v-icon>
                            <span>{{ product.view_count || 0 }}</span>
                        </div>
                        <div class="stat-item">
                            <v-icon>mdi-message-outline</v-icon>
                            <span>{{ product.talk_count || 0 }}</span>
                        </div>
                    </div>
                </div>

                <ProductMiniSummary :condition="product.condition" :isPerson="product.isPerson"
                    :isShipping="product.isShipping" :shippingFee="product.shippingFee"
                    :freeShipping="product.freeShipping" :address="product.address" :isCompanion="product.isCompanion"
                    :isDeputy="product.isDeputy" :gender="product.gender" :ageGroup="product.ageGroup" />

                <div class="product-actions">
                    <button class="action-button like" @click="handleWishClick">
                        <v-icon>{{ product.isWished ? 'mdi-heart' : 'mdi-heart-outline' }}</v-icon>
                        <span>찜</span>
                    </button>
                    <button class="action-button chat" @click="handleChatClick">
                        <v-icon>mdi-chat</v-icon>
                        <span>채팅하기</span>
                    </button>
                    <button class="action-button buy" @click="handleBuyClick">
                        <v-icon>mdi-gift-open</v-icon>
                        <span>즉시결제</span>
                    </button>
                </div>
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
            <Map :lat="Number(product.latitude)" :lon="Number(product.longitude)" />
        </div>

        <!-- 판매자 -->
        <div class="seller-section card-container" v-if="product.userId">
            <div class="seller-info">
                <div class="seller-profile">
                    <div class="profile-image">
                        <img :src="product.profile || '/image/default-profile.png'" alt="판매자 프로필"
                            :style="!product.profile ? 'width: 80%; height: auto;' : ''">
                    </div>
                    <div class="seller-name">
                        {{ product.userId }}
                        <div class="seller-rating" v-if="product.reviewRate != null">
                            평점: {{ product.reviewRate.toFixed(1) }}
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 연관 상품 섹션 -->
        <div class="related-products card-container">
            <h3>연관 상품</h3>
            <div class="products-row">
                <div v-for="relatedProduct in relatedProducts" :key="relatedProduct.id" class="related-product-card"
                    @click="goToProduct(relatedProduct.id)">
                    <div class="product-image">
                        <img :src="relatedProduct.image1" :alt="relatedProduct.productName">
                    </div>
                    <div class="product-info">
                        <div class="product-name">{{ relatedProduct.productName }}</div>
                        <div class="product-price">{{
                            new Intl.NumberFormat('ko-KR').format(relatedProduct.price) }}원
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div v-else-if="isLoading" class="loading">
        상품 정보를 불러오는 중...
    </div>

    <div v-else class="error">
        상품 정보를 찾을 수 없습니다.
    </div>

</template>

<style scoped>
.product-actions {
    display: flex;
    justify-content: center;
    gap: 1rem;
    padding: 1.25rem 0;
    width: 100%;
}

.action-button {
    display: flex;
    align-items: center;
    gap: 0.3125rem;
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 0.3125rem;
    color: white;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
}

.action-button:hover {
    opacity: 0.9;
    transform: translateY(-1px);
}

.action-button:active {
    transform: translateY(0);
}

.like {
    background-color: #007bff;
}

.chat {
    background-color: #ffc107;
}

.buy {
    background-color: #dc3545;
}

/* 판매자 정보 스타일 */
.seller-rating {
    font-size: 0.9rem;
    color: #666;
    margin-top: 0.3rem;
}

.error {
    text-align: center;
    padding: 2rem;
    color: #dc3545;
}

.seller-section {
    margin-top: 2rem;
    padding: 1.5rem;
    background-color: white;
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
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: center;
    align-items: center;
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
    height: 500px;
    border: none;
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
    /* border-radius: 0.5rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); */
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
    height: 300px;
    border-radius: 0.5rem;
    overflow: hidden;
    border: 1px solid #eee;
}
</style>