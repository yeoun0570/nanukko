<script setup>
defineProps({
  deliveryData: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <div class="delivery-container">
    <div class="header">
      <h2>배송지</h2>
      <button class="edit-button">변경</button>
    </div>
    <div class="delivery-section">
      <div class="delivery-info">
        <div class="name-tag">
          <span class="name">{{ deliveryData.nickname }}</span>
          <span class="badge">최근</span>
        </div>
        <p class="address">
          ({{ deliveryData.addrZipcode }}) {{ deliveryData.addrMain }}
          {{ deliveryData.addrDetail }}
        </p>
        <p class="phone">{{ deliveryData.mobile }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("../../assets/payments/DeliverySection.css");

h3 {
    font-weight: bold;
}
</style>
