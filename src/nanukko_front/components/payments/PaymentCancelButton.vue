<script setup>
defineProps({
    productData: {
        type: Object,
    }
});

defineEmits(['cancelOrder']);
</script>

<template>
  <button
    v-show="productData && productData.status === 'ESCROW_HOLDING'"
    @click="$emit('cancelOrder')"
    class="cancel-btn"
  >
    결제 취소
  </button>
</template>
