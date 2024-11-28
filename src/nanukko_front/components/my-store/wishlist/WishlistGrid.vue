<script setup>
import WishlistCard from "./WishlistCard.vue";

const props = defineProps({
  products: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["wishlist-updated"]);
</script>

<template>
  <div v-if="products.length > 0" class="wishlist-grid">
    <WishlistCard
      v-for="product in products"
      :key="product.productId"
      :product="product"
      @remove-success="$emit('wishlist-updated')"
    />
  </div>
  <div v-else class="no-items">찜한 상품이 없습니다.</div>
</template>
