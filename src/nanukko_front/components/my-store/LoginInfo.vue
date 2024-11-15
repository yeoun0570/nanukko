<script setup>
defineProps({
  userId: String,
  password: String,
});
</script>

<template>
  <div class="login-info">
    <div class="info-item">
      <label>아이디:</label>
      <span>{{ userId }}</span>
    </div>
    <div class="info-item">
      <label>비밀번호:</label>
      <span>{{ password }}</span>
    </div>
  </div>
</template>