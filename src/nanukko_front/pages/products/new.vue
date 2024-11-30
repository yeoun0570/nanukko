<script setup>
import { useToast } from 'vue-toastification';
import { useRouter } from 'vue-router';
import { useURL } from "~/composables/useURL";
import { useAuth } from "~/composables/auth/useAuth";
import ImageUploader from '~/components/products/products-new/ImageUploader.vue';
import ProductForm from '~/components/products/products-new/ProductForm.vue';

const toast = useToast();
const router = useRouter();
const auth = useAuth();
const { axiosInstance } = useURL();

onMounted(() => {
    axiosInstance.interceptors.request.use((config) => {
        const token = auth.getToken();

        console.log("Current token:", token);
        console.log("Request URL:", config.url);
        console.log("Request Headers:", config.headers);
        console.log("Token exists:", !!token);

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        if (!config.headers['Content-Type']) {
            config.headers['Content-Type'] = 'multipart/form-data';
        }
        return config;
    });
});

const product = ref({
    // 기본 정보 필드
    productName: '',
    price: 0,
    content: '',
    condition: 'NEW',
    images: [],

    // 카테고리 정보
    middleCategory: {
        majorId: null,
        middleId: null
    },

    // 배송 관련 정보
    isShipping: false,
    isPerson: false,
    freeShipping: false,
    shippingFee: 0,

    // 거래 장소 정보
    zipCode: '',
    address: '',
    detailAddress: '',
    latitude: null,
    longitude: null,

    // 거래 관련 정보
    isCompanion: false,
    isDeputy: false,
    gender: null,
    ageGroup: ''
});

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

        // 백엔드로 전송할 데이터 구조 생성
        const productData = {
            // 기본 정보
            productName: product.value.productName,
            price: product.value.price,
            content: product.value.content,
            condition: product.value.condition,

            // 카테고리 ID들 직접 전달
            majorId: product.value.middleCategory.majorId,
            middleId: product.value.middleCategory.middleId,

            // 배송 관련 정보
            isPerson: product.value.isPerson,
            isShipping: product.value.isShipping,
            freeShipping: product.value.freeShipping,
            shippingFee: product.value.shippingFee,

            // 거래 관련 정보
            isCompanion: product.value.isCompanion,
            isDeputy: product.value.isDeputy,
            gender: product.value.gender,
            ageGroup: product.value.ageGroup
        };

        // 직거래인 경우 거래 장소 정보 추가
        if (product.value.isPerson) {
            productData.zipCode = product.value.zipCode;
            productData.address = product.value.address;
            productData.detailAddress = product.value.detailAddress;
            productData.latitude = product.value.latitude;
            productData.longitude = product.value.longitude;
        }

        // productData 로깅
        console.log('===== 전송할 데이터 확인 =====');
        console.log('productData:', productData);

        // 이미지 파일 추가
        if (product.value.images?.length > 0) {
            product.value.images.forEach((image, index) => {
                if (image.file) {
                    formData.append('images', image.file);
                    console.log(`이미지 ${index + 1}:`, image.file.name, image.file.size);
                }
            });
        }

        // FormData에 데이터 추가
        formData.append('productInfo', JSON.stringify(productData));

        // 실제 API 요청 전 FormData 내용 확인
        console.log('===== FormData 내용 확인 =====');
        for (let pair of formData.entries()) {
            console.log(pair[0], pair[1]);
        }

        // API 요청 전 formData 내용 로깅
        console.log('===== FormData 내용 확인 =====');
        formData.forEach((value, key) => {
            console.log(`${key}:`, value);
        });

        // API 요청 직전 로깅
        console.log('===== API 요청 시작 =====');

        //Content-Type만 config에서 설정하고 Authorization은 인터셉터가 처리
        const config = {
            headers: {
                'Content-Type': 'multipart/form-data; boundary=' + Math.random().toString().substr(2)
            }
        };

        const response = await axiosInstance.post(`/products/new`, formData, config);

        //toast
        toast.success('상품이 등록되었습니다.');

        // 요청 완료 후 로깅
        console.log('===== API 응답 =====');
        console.log('응답 상태:', response.data.status);
        console.log('응답 데이터:', response.data);

    } catch (error) {
        // 에러 상세 정보 출력
        console.error('===== API 에러 =====');
        console.error('에러:', error);
        if (error.response) {
            console.error('응답 상태:', error.response.status);
            console.error('응답 데이터:', error.response.data);
            console.error('응답 헤더:', error.response.headers);
        }
        toast.error('상품 등록에 실패했습니다.');
    }
};


const handleCancel = () => {
    toast.confirmCancel('작성중인 내용이 모두 삭제됩니다. 취소하시겠습니까?');
    if (confirmCancel) {
        router.push('/');
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