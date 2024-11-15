<script setup>
defineProps({
  reviewRate: {
    type: Number,
    required: true,
  },
});
</script>

<template>
  <div class="store-score">
    <h2>
      상점 점수: <span>{{ reviewRate }}점</span>
    </h2>
  </div>
</template>
