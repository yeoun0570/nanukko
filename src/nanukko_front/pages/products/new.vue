<script setup>//new.vue
import { ref } from 'vue';
import axios from 'axios';
import ImageUploader from '~/components/products/products-new/ImageUploader.vue';
import ProductForm from '~/components/products/products-new/ProductForm.vue';

const product = ref({
    title: '',
    majorId: '',
    middleId: '',
    price: 0,
    description: '',
    images: [],
    deliveryType: '',
    deliveryFee: 0,
    dealLocation: {
        zipCode: '',
        address: '',
        detailAddress: ''
    },
    // 거래 관련 정보
    person: false,
    companion: false,
    companionGender: false,
    deputy: false,
    deputyGender: false,
    // 카테고리 정보
    majorId: '',
    majorName: '',
    middleId: '',
    middleName: '',
});

// 상품 정보 업데이트
const updateProduct = (updatedProduct) => {
    product.value = { ...product.value, ...updatedProduct };
};

// 이미지 업데이트
const updateImages = (newImages) => {
    product.value.images = newImages;
};

// 상품 등록
const submitProduct = async () => {
    try {
        // 필수 필드 검증
        if (!product.value.title || !product.value.majorId || !product.value.middleId ||
            !product.value.price || !product.value.description) {
            alert('필수 정보를 모두 입력해주세요.');
            return;
        }

        if (product.value.images.length === 0) {
            alert('최소 1장의 상품 이미지를 등록해주세요.');
            return;
        }

        if (!product.value.deliveryType) {
            alert('배송 방식을 선택해주세요.');
            return;
        }

        if (product.value.deliveryType === 'delivery' && !product.value.deliveryFee) {
            alert('배송비를 입력해주세요.');
            return;
        }

        if (product.value.deliveryType === 'direct' && !product.value.dealLocation.address) {
            alert('거래 장소를 입력해주세요.');
            return;
        }

        // FormData 생성
        const formData = new FormData();

        // 이미지 파일 추가
        product.value.images.forEach((image, index) => {
            formData.append(`images`, image.file);
        });

        // 나머지 상품 정보를 JSON으로 변환하여 추가
        const productInfo = { ...product.value };
        delete productInfo.images; // 이미지 정보 제외
        formData.append('productInfo', JSON.stringify(productInfo));

        // API 호출
        await axios.post('http://localhost:8080/api/products', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });

        alert('상품이 등록되었습니다.');
        navigateTo('/products');
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

            <ProductForm v-model="product" @update:modelValue="updateProduct" />

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