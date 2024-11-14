<script setup>
defineProps({
  loading: {
    type: Boolean,
    default: false
  }
});

defineEmits(['startPayment']);
</script>

<template>
  <button @click="$emit('startPayment')" :disabled="loading" class="payment-button">
    결제하기
  </button>
</template>

<style scoped>
@import url('../../assets/payments/PaymentButton.css');
</style>