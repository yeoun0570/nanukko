<script setup>
defineProps({
  productData: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <div class="payment-container">
    <h2>상품 정보</h2>
    <div class="product-info-section">
      <div class="product-info-content">
        <img
          :src="productData.thumbnailImage"
          alt="상품 이미지"
          class="thumbnail"
        />
        <div class="product-details">
          <div class="price">{{ productData.price.toLocaleString() }}원</div>
          <div class="product-name">{{ productData.productName }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
