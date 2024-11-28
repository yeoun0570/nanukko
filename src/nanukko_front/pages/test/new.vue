<script setup>//new.vue
import { ref } from 'vue';
import axios from 'axios';
import ImageUploader from './ImageUploader.vue';
// import ProductForm from '~/components/products/products-new/ProductForm.vue';

const product = ref({
    // title: '',
    // price: 0,
    // description: '',
    images: [],
    // deliveryType: '',
    // deliveryFee: 0,
    // dealLocation: {
    //     zipCode: '',
    //     address: '',
    //     detailAddress: ''
    // },
    // // 거래 관련 정보
    // person: false,
    // companion: false,
    // companionGender: false,
    // deputy: false,
    // deputyGender: false,
    // // 카테고리 정보
    // majorId: '',
    // majorName: '',
    // middleId: '',
    // middleName: '',
});

// 상품 정보 업데이트
// const updateProduct = (updatedProduct) => {
//     product.value = { ...product.value, ...updatedProduct };
// };

// 이미지 업데이트
const updateImages = (newImages) => {
    product.value.images = newImages;
};

// 상품 등록
const submitProduct = async () => {
    try {
        const formData = new FormData();
        product.value.images.forEach((image, index) => {
            console.log(`Adding image ${index}:`, image.file);
            formData.append('images', image.file);
        });

        console.log("FormData to be sent:", formData);

        const response = await axios.post('http://localhost:8080/api/test/products/new', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });

        console.log("Response:", response.data);
        alert('상품이 등록되었습니다.');
    } catch (error) {
        console.error('상품 등록 실패:', error);
        alert('상품 등록에 실패했습니다.');
    }
};


// 취소 처리
const handleCancel = () => {
    if (confirm('상품 등록을 취소하시겠습니까?')) {
        navigateTo('/products');
    }
};

</script>

<template>
    <div class="product-new">
        <h1 class="title">상품등록</h1>
        <hr>

        <form @submit.prevent="submitProduct" class="product-form">
            <ImageUploader :images="product.images" @update:images="updateImages" />

            <!-- <ProductForm v-model="product" @update:modelValue="updateProduct" /> -->

            <div class="button-group">
                <button type="submit" class="submit-button">등록하기</button>
                <button type="button" class="cancel-button" @click="handleCancel">
                    취소
                </button>
            </div>
        </form>
    </div>
</template>

<style scoped>
.product-new {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
}

.title {
    font-size: 1.5rem;
    font-weight: 600;
    margin-bottom: 1rem;
}

.product-form {
    margin-top: 2rem;
}

.button-group {
    display: flex;
    gap: 1rem;
    margin-top: 2rem;
    justify-content: center;
}

.submit-button,
.cancel-button {
    padding: 0.75rem 2rem;
    border: none;
    border-radius: 0.25rem;
    font-weight: 500;
    cursor: pointer;
    transition: opacity 0.2s;
    min-width: 120px;
}

.submit-button {
    background-color: #007bff;
    color: white;
}

.cancel-button {
    background-color: #6c757d;
    color: white;
}

.submit-button:hover,
.cancel-button:hover {
    opacity: 0.9;
}

hr {
    border: none;
    border-top: 1px solid #dee2e6;
    margin: 1rem 0;
}
</style>