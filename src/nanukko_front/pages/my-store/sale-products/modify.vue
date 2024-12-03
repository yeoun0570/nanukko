<script setup>
import { useToast } from 'vue-toastification';
import { useRouter, useRoute } from 'vue-router';
import { useURL } from "~/composables/useURL";
import { useAuth } from "~/composables/auth/useAuth";
import ImageUploader from '~/components/products/products-new/ImageUploader.vue';
import ProductForm from '~/components/products/products-new/ProductForm.vue';

const toast = useToast();
const router = useRouter();
const route = useRoute();
const auth = useAuth();
const { axiosInstance } = useURL();

const product = ref({
  productName: '',
  price: 0,
  content: '',
  condition: 'NEW',
  images: [],
  middleCategory: {
    majorId: null,
    middleId: null
  },
  isShipping: false,
  isPerson: false,
  freeShipping: false,
  shippingFee: 0,
  zipCode: '',
  address: '',
  detailAddress: '',
  latitude: null,
  longitude: null,
  isCompanion: false,
  isDeputy: false,
  gender: null,
  ageGroup: ''
});

const setupAxiosInterceptor = () => {
  axiosInstance.interceptors.request.use((config) => {
    const token = auth.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'multipart/form-data';
    }
    return config;
  });
};



onMounted(async () => {
  setupAxiosInterceptor();
  await loadProductData();
});


// 이미지 업데이트 핸들러
const updateImages = (images) => {
  if (Array.isArray(images)) {
    product.value.images = images;
  } else {
    product.value.images = [];
  }
};
const handleSubmit = async () => {
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

    if (!product.value.isPerson && !product.value.isShipping) {
      alert('최소 하나의 거래 방식을 선택해주세요.');
      return;
    }

    if (product.value.isShipping && !product.value.freeShipping && product.value.shippingFee === 0) {
      alert('배송비를 입력해주세요.');
      return;
    }

    if (product.value.isPerson && (!product.value.address || product.value.latitude === null || product.value.longitude === null)) {
      alert('거래 장소를 입력해주세요.');
      return;
    }

    const formData = new FormData();

    const productData = {
      id: route.query.productId,
      productName: product.value.productName,
      price: product.value.price,
      content: product.value.content,
      condition: product.value.condition,
      majorId: product.value.middleCategory.majorId,
      middleId: product.value.middleCategory.middleId,
      isPerson: product.value.isPerson,
      isShipping: product.value.isShipping,
      freeShipping: product.value.freeShipping,
      shippingFee: product.value.shippingFee,
      isCompanion: product.value.isCompanion,
      isDeputy: product.value.isDeputy,
      gender: product.value.gender,
      ageGroup: product.value.ageGroup
    };

    if (product.value.isPerson) {
      productData.zipCode = product.value.zipCode;
      productData.address = product.value.address;
      productData.detailAddress = product.value.detailAddress;
      productData.latitude = product.value.latitude;
      productData.longitude = product.value.longitude;
    }

    console.log('전송할 productData:', productData);
    formData.append('productInfo', JSON.stringify(productData));

    if (Array.isArray(product.value.images)) {
      // 새로 추가된 파일이 있는 이미지
      const newImages = product.value.images.filter(image => image.file);

      // 기존 이미지 URL을 File 객체로 변환하여 추가
      const existingImages = product.value.images.filter(image => image.isExisting);

      // 모든 이미지를 순차적으로 처리
      const processImages = [...newImages, ...existingImages];

      processImages.forEach(async (image, index) => {
        if (image.file) {
          // 새로운 이미지인 경우
          formData.append('images', image.file);
        } else if (image.url) {
          // 기존 이미지인 경우 URL을 그대로 전송
          formData.append('images', image.url);
        }
      });
    }

    const response = await axiosInstance.post('/my-store/sale-products/modify', formData);
    console.log('수정 응답:', response.data);
    toast.success('상품이 수정되었습니다.');
    router.push('/my-store/sale-products');

  } catch (error) {
    console.error('상품 수정 실패:', error);
    console.error('에러 상세:', error.response?.data);
    toast.error(error.response?.data?.message || '상품 수정에 실패했습니다.');
  }
};

const handleCancel = () => {
  toast.confirmCancel('수정중인 내용이 모두 취소됩니다. 취소하시겠습니까?');
  if (confirmCancel) {
    router.push('/my-store/sale-products');
  }
};


const loadProductData = async () => {
  try {
    const productId = route.query.productId;
    const response = await axiosInstance.get(`/products/${productId}`);
    const productData = response.data;

    console.log("매핑할 상품 데이터:", productData);

    if (!productData) {
      throw new Error('상품 정보를 찾을 수 없습니다.');
    }

    // 이미지 배열 생성
    const images = [];
    for (let i = 1; i <= 5; i++) {
      const imageUrl = productData[`image${i}`];
      if (imageUrl) {
        images.push({
          url: imageUrl,
          file: null,
          id: i,
          isExisting: true
        });
      }
    }

    // 기존 데이터를 product ref에 매핑
    product.value = {
      ...product.value, // 기존 기본값 유지
      productName: productData.productName,
      price: productData.price,
      content: productData.content,
      condition: productData.condition,
      images: images,
      middleCategory: {
        majorId: productData.majorId,
        middleId: productData.middleId
      },
      isShipping: productData.isShipping,
      isPerson: productData.isPerson,
      freeShipping: productData.freeShipping,
      shippingFee: productData.shippingFee,
      zipCode: productData.zipCode || '',
      address: productData.address || '',
      detailAddress: productData.detailAddress || '',
      latitude: productData.latitude,
      longitude: productData.longitude,
      isCompanion: productData.isCompanion,
      isDeputy: productData.isDeputy,
      gender: productData.gender,
      ageGroup: productData.ageGroup || ''
    };

    console.log("매핑된 product state:", product.value);

  } catch (error) {
    console.error('상품 정보 로드 실패:', error);
    toast.error('상품 정보를 불러오는데 실패했습니다.');
    router.push('/my-store/sale-products');
  }
};

</script>

<template>
  <div class="product-modify">
    <h1 class="title">상품수정</h1>
    <hr>

    <form @submit.prevent="handleSubmit" class="product-form">
      <ImageUploader :images="product.images" @update:images="images => product.images = images" />

      <ProductForm v-model="product" />

      <div class="button-group">
        <button type="submit" class="submit-button">수정하기</button>
        <button type="button" class="cancel-button" @click="handleCancel">
          취소
        </button>
      </div>
    </form>
  </div>
</template>

<style scoped>
.product-modify {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.product-modify form.product-form {
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