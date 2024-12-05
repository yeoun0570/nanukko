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
const pageSize = ref(20);
const loading = ref(false);
const products = ref({
    content: [],
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true
});

onMounted(() => {
    loadUserProducts(pageNumber.value);
});

watch(pageNumber, (newPage) => {
    loadUserProducts(newPage);
});

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
    <div class="card-container mx-auto">
        <ProductList :products="products" :loading="loading" :page-number="pageNumber" @product-click="goToProduct"
            @page-change="changePage" />
    </div>
</template>

<style lang="scss" scoped>
.card-container {
    max-width: 1200px;
    padding: 2rem;
    margin: 0 auto;
}
</style>