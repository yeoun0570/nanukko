<script setup>
import CategorySelect from "~/components/my-store/CategorySelect.vue";
import BasicInfoFrom from "~/components/my-store/sale-products/modify/BasicInfoFrom.vue";
import ButtonGroup from "~/components/my-store/sale-products/modify/ButtonGroup.vue";
import TransactionForm from "~/components/my-store/sale-products/modify/TransactionForm.vue";
import { useApi } from '@/composables/useApi';

const api = useApi();


definePageMeta({
  layout: 'mystore'
});

const route = useRoute();
const productId = route.query;
const productInfo = ref(route.state?.productInfo || {});

// 디버깅을 위한 로그
console.log('Initial productInfo:', productInfo.value);
console.log('ProductId:', productId);

// 상품 정보 수정
const updateProduct = async () => {
  try {
    await api.post(
      `/my-store/sale-products/modify`,
      productInfo.value,
      {
        params: {
          productId: productId,
        },
      }
    );
    alert("상품 수정이 완료되었습니다.");
    navigateTo("/my-store/sale-products/");
  } catch (error) {
    console.log("상품 수정 실패: ", error);
    alert("상품 수정에 실패하였습니다.");
  }
};

const handleCancel = () => {
  navigateTo('/my-store/sale-products');
};

const updateProductInfo = (field, value) => {
  productInfo.value = { ...productInfo.value, [field]: value };
};
</script>

<template>
  <div class="product-form">
    <h2>상품 수정</h2>
    <hr color="black"><br>

    <CategorySelect
      v-model:productInfo="productInfo"
      @update:category="updateProductInfo"
    />

    <BasicInfoFrom
      v-model:productInfo="productInfo"
      @update:info="updateProductInfo"
    />

    <TransactionForm
      v-model:productInfo="productInfo"
      @update:transaction="updateProductInfo"
    />

    <ButtonGroup @submit="updateProduct" @cancel="handleCancel" />
  </div>
</template>

<style>
@import url("../../../assets/my-store/ModifyProduct.css");
</style>
