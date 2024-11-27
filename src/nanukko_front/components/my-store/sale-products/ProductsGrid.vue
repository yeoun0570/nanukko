<script setup>
import ProductCard from "./ProductCard.vue";

defineProps({
  products: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["products-updated"]);

const handleProductUpdate = () => {
  emit("products-updated");
};
</script>

<template>
  <div v-if="products.length > 0" class="products-list">
    <ProductCard
      v-for="product in products"
      :key="product.productId"
      :product="product"
      @product-updated="handleProductUpdate"
    />
  </div>
  <div v-else class="no-products">등록된 상품이 없습니다.</div>
</template>
