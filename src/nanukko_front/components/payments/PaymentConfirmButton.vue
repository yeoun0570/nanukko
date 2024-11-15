<script setup>
defineProps({
  productData: {
    type: Object,
  },
});

defineEmits(["confirmPurchase"]);
</script>

<template>
  <button
    v-show="paymentDetails && paymentDetails.status === 'ESCROW_HOLDING'"
    @click="$emit('confirmPurchase')"
    class="confirm-btn"
  >
    구매 확정
  </button>
</template>
