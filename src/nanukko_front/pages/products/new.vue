<script setup>//new.vue
import { ref } from 'vue';
import axios from 'axios';
import ImageUploader from '~/components/products/products-new/ImageUploader.vue';
import ProductForm from '~/components/products/products-new/ProductForm.vue';

const product = ref({
    // 기본 정보 필드
    productName: '',           // 상품명 (필수)
    price: 0,                 // 가격 (필수)
    content: '',              // 상품 설명 (필수)
    condition: 'NEW',         // 상품 사용감 (필수, Enum)
    images: [],               // 이미지 배열

    // 카테고리 정보
    middleCategory: {
        majorId: '',
        middleId: ''
    },

    // 배송 관련 정보
    isPerson: false,          // 직거래 여부
    isShipping: false,        // 배송 여부
    freeShipping: false,      // 배송비 포함 여부
    shippingFee: 0,          // 배송비 (기본값 0)

    // 거래 관련 정보
    isCompanion: false,       // 동행인 여부
    isDeputy: false,         // 대리인 여부
    gender: true,            // 동행인/대리인 성별 (true=남자, false=여자)
    ageGroup: '20S',         // 동행인/대리인 연령대

    // 거래 장소 정보
    zipCode: '',             // 우편번호
    address: '',             // 주소
    detailAddress: '',       // 상세주소
    latitude: null,          // 위도
    longitude: null,         // 경도

    // UI 표시용 필드 (백엔드로 전송하지 않음)
    majorId: '',             // 대분류 ID (UI용)
    majorName: '',           // 대분류 이름 (UI용)
    middleName: '',          // 중분류 이름 (UI용)
});

// 상품 등록
const submitProduct = async () => {
    try {
        // 필수 필드 검증
        if (!product.value.productName || !product.value.middleCategory.middleId ||
            !product.value.price || !product.value.content ||
            !product.value.condition) {
            alert('필수 정보를 모두 입력해주세요.');
            return;
        }

        if (product.value.images.length === 0) {
            alert('최소 1장의 상품 이미지를 등록해주세요.');
            return;
        }

        // 배송 방식 검증
        if (!product.value.isPerson && !product.value.isShipping) {
            alert('최소 하나의 거래 방식을 선택해주세요.');
            return;
        }

        // 배송 선택 시 배송비 검증
        if (product.value.isShipping && !product.value.freeShipping && product.value.shippingFee === 0) {
            alert('배송비를 입력해주세요.');
            return;
        }

        // 직거래 선택 시 주소 및 좌표 검증
        if (product.value.isPerson && (!product.value.address || product.value.latitude === null || product.value.longitude === null)) {
            alert('거래 장소를 입력해주세요.');
            return;
        }

        // FormData 생성
        const formData = new FormData();

        // 이미지 파일 추가
        product.value.images.forEach((image, index) => {
            formData.append(`images`, image.file);
        });

        // 백엔드로 전송할 데이터 구조 생성
        const productData = {
            // 기본 정보
            productName: product.value.productName,
            price: product.value.price,
            content: product.value.content,
            condition: product.value.condition,

            // 카테고리 정보
            middleCategory: {
                majorId: product.value.middleCategory.majorId,
                middleId: product.value.middleCategory.middleId
            },

            // 배송 관련 정보
            isPerson: product.value.isPerson,
            isShipping: product.value.isShipping,
            freeShipping: product.value.freeShipping,
            shippingFee: product.value.shippingFee,

            // 거래 관련 정보
            isCompanion: product.value.isCompanion,
            isDeputy: product.value.isDeputy,
            gender: product.value.gender,
            ageGroup: product.value.ageGroup,
        };

        // 직거래인 경우 거래 장소 정보 추가
        if (product.value.isPerson) {
            productData.zipCode = product.value.zipCode;
            productData.address = product.value.address;
            productData.detailAddress = product.value.detailAddress;
            productData.latitude = product.value.latitude;
            productData.longitude = product.value.longitude;
        }

        // productData 객체를 BLOB 객체로 변환해서 FormData에 추가
        formData.append('productInfo', new Blob([JSON.stringify(productData)], {
            type: 'application/json'
        }));

        // API 호출
        await axios.post('http://localhost:8080/api/products/new', formData, {
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
            <ImageUploader :images="product.images" @update:images="images => product.images = images" />

            <ProductForm v-model="product" @update:modelValue="newValue => product = newValue" />

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

.product-new form.product-form {
    margin-top: 2rem;
    padding: 0;
}

.button-group {
    display: flex;
    gap: 1rem;
    margin-top: 3rem;
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