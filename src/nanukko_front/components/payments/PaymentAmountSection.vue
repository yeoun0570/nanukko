<script setup>
defineProps({
  productData: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <h2 class="title">결제금액</h2>
  <div class="product-section">
    <div class="product-info">
      <div class="info-row">
        <span>상품금액</span>
        <span class="amount">{{ productData.price.toLocaleString() }}원</span>
      </div>
      <div class="info-row">
        <span>수수료(3.5%)</span>
        <span class="chargeAmount"
          >{{ productData.chargeAmount.toLocaleString() }}원</span
        >
      </div>
      <div class="info-row">
        <span>배송비</span>
        <span class="shippingFree"
          >{{ productData.shippingFree.toLocaleString() }}원</span
        >
      </div>
      <div class="info-row total">
        <span>총 결제금액</span>
        <span class="total-amount"
          >{{ productData.totalAmount.toLocaleString() }}원</span
        >
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("../../assets/payments/PaymentAmountSection.css");
</style>
