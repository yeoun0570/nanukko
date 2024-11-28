<script setup>
import OrderCard from "./OrderCard.vue";

const props = defineProps({
  orders: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["orders-updated"]);
</script>

<template>
  <div v-if="orders.length > 0" class="orders-list">
    <OrderCard
      v-for="order in orders"
      :key="order.orderId"
      :order="order"
      @order-updated="$emit('orders-updated')"
    />
  </div>
  <div v-else class="no-orders">구매한 상품이 없습니다.</div>
</template>
