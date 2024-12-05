<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useApi } from "@/composables/useApi";
import ProductList from '@/components/products/ProductList.vue';

const route = useRoute();
const router = useRouter();
const api = useApi();
const userId = route.query.userId;

const pageNumber = ref(0);
const pageSize = ref(8);
const loading = ref(false);
const error = ref(null);
const products = ref({
    content: [],
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true
});
const userInfo = ref({});
const reviewInfo = ref([]);


onMounted(() => {
    loadUserInfo();
    loadUserProducts(pageNumber.value);
    loadReviews();
});

watch(pageNumber, (newPage) => {
    loadUserProducts(newPage);
});

const loadUserInfo = async () => {
    try {
        loading.value = true;
        error.value = null;
        const response = await api.get(`/products/seller-info`, {
            params: { userId: userId }
        });

        userInfo.value = response;
        console.log("받아온 데이터:", userInfo.value); // 데이터 확인
    } catch (err) {
        error.value = "사용자 정보를 불러오는데 실패했습니다.";
        console.error("Error loading user info:", err);
    } finally {
        loading.value = false;
    }
};

const loadReviews = async (page = 0) => {
    try {
        const response = await api.get(`/products/seller-reviews`, {
            params: { userId: userId }
        });
        reviewInfo.value = response;
    } catch (error) {
        console.log("로딩 중 에러: ", error);
    }
};


const loadUserProducts = async (page = 0) => {
    loading.value = true;

    try {
        console.log("요청 파라미터: ", {
            page: page,
            size: pageSize.value,
        });

        const response = await api.get(`/products/seller`, {
            params: {
                page: page,
                size: pageSize.value,
                userId: userId
            },
        });

        products.value = response;
        console.log("Server response:", products.value);
    } catch (error) {
        console.error("상품 조회 실패", error);
    } finally {
        loading.value = false;
    }
}

const goToProduct = (productId) => {
    router.push(`/products/${productId}`);
};

const changePage = (newPage) => {
    pageNumber.value = newPage;
    window.scrollTo(0, 0);
};

</script>


<template>
    <div class="store-page-container">
        <!-- 상품 섹션 -->
        <div class="card-container mx-auto">
            <div class="profile-container">
                <div class="profile-image">
                    <img :src="userInfo.profile || '/image/default-profile.png'" alt="프로필">
                </div>
            </div>
            <div class="store-title">{{ userInfo?.nickname ? `${userInfo.nickname}님의 상점` : '' }}</div>
            <ProductList :products="products" :loading="loading" :page-number="pageNumber" @product-click="goToProduct"
                @page-change="changePage" />
        </div>

        <!-- 후기 섹션 -->
        <div class="reviews-container">
            <div v-if="reviewInfo.length > 0">
                <StoreScore :reviewRate="reviewRate" />
                <div class="reviews-list">
                    <div v-for="review in reviewInfo" :key="review.reviewId" class="review-card">
                        <div class="review-header">
                            <div class="profile-section">
                                <h3 class="author-name">{{ review.authorNickName }}</h3>
                            </div>
                            <div class="rating">
                                <ReadOnlyStarRating :rating="normalizeRating(review.rate)" />
                            </div>
                        </div>

                        <div class="product-info">
                            <p class="product-name">{{ review.productName }}</p>
                        </div>

                        <p class="review-content">{{ review.review }}</p>
                    </div>
                </div>
            </div>
            <div v-else class="no-reviews">아직 받은 후기가 없습니다.</div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
// review
/* 메인 컨테이너 */
.reviews-container {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
}

.main-title {
    font-size: 1.75rem;
    font-weight: 700;
    color: #333;
    margin-bottom: 2rem;
    text-align: center;
}

/* 상점 점수 */
.store-score {
    background: white;
    padding: 1.5rem;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    margin-bottom: 2rem;
    text-align: center;
}

.store-score h2 {
    font-size: 1.25rem;
    color: #666;
}

.store-score span {
    color: #2196f3;
    font-weight: 700;
    font-size: 1.5rem;
}

/* ReadOnlyStarRating */
.star-rating {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
}

.stars-container {
    display: flex;
    gap: 2px;
}

.star {
    position: relative;
    width: 24px;
    height: 24px;
}

.star-svg {
    width: 100%;
    height: 100%;
}

.empty {
    fill: #e2e8f0;
}

.star-fill {
    position: absolute;
    top: 0;
    left: 0;
    overflow: hidden;
    height: 100%;
}

.filled {
    fill: #ffd700;
}

.rating-text {
    font-size: 0.9rem;
    color: #4a5568;
    font-weight: 500;
}

/* 리뷰 리스트 */
.reviews-list {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.review-card {
    background: white;
    border-radius: 12px;
    padding: 1.5rem;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.review-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
}

.author-name {
    font-size: 1rem;
    font-weight: 600;
    color: #333;
}

.rating {
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.rate {
    color: #2196f3;
    font-weight: 600;
}

.product-info {
    margin: 1rem 0;
    padding: 0.75rem;
    background: #f8f9fa;
    border-radius: 8px;
}

.product-name {
    color: #666;
    font-size: 0.9rem;
}

.review-content {
    color: #333;
    line-height: 1.5;
}

.no-reviews {
    text-align: center;
    padding: 3rem;
    color: #666;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}


.card-container {
    max-width: 1200px;
    padding: 2rem;
    margin: 0 auto;
}

.store-title {
    font-size: 32px;
    font-weight: normal;
    text-align: center;
    color: #333;
    padding: 16px 0;
    margin: 16px 0;
}

.profile-container {
    width: 100%;
    display: flex;
    justify-content: center;
    margin: 2rem 0;
}

.profile-image {
    width: 200px;
    height: 200px;
    position: relative;
    margin: 0 auto;
}

.profile-image img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid #fff;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: transform 0.3s ease;
}
</style>
