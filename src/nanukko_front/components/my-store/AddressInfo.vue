<script setup>
defineProps({
  addrMain: String,
  addrZipcode: String,
  addrDetail: String,
});
</script>

<template>
    <div class="basic-info">
      <div class="info-item">
        <label>주소:</label>
        <span>({{ addrZipcode }}) {{ addrMain }}</span>
      </div>
      <div class="info-item">
        <label>상세 주소:</label>
        <span>{{ addrDetail }}</span>
      </div>
    </div>
  </template>
