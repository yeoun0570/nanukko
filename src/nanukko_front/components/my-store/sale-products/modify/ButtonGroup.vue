<script setup>
const emit = defineEmits(['submit', 'cancel']);
</script>

<template>
  <div class="button-group">
    <button @click="emit('submit')" class="submit-btn">수정 완료</button>
    <button @click="emit('cancel')" class="cancel-btn">취소</button>
  </div>
</template>
