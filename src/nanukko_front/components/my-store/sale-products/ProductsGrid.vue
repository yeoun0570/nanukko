<script setup>
import ProductCard from './ProductCard.vue';

defineProps({
  products: {
    type: Array,
    required: true,
  },
  userId: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["products-updated"]);

const handleProductUpdate = () => {
  emit("products-updated");
};
</script>

<template>
  <div v-if="products.length > 0" class="products-grid">
    <ProductCard
      v-for="product in products"
      :key="product.productId"
      :product="product"
      :userId="userId"
      @product-updated="handleProductUpdate"
    />
  </div>
  <div v-else>등록된 상품이 없습니다.</div>
</template>
