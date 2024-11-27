<script setup>
import ReadOnlyStarRating from "./ReadOnlyStarRating.vue";

const props = defineProps({
  reviewRate: {
    type: Number,
    required: true,
  },
});

const normalizedRating = computed(() => {
  // 100점 만점을 5점 만점으로 변환
  return props.reviewRate / 2;
});
</script>

<template>
  <div class="store-score">
    <h2>상점 점수</h2>
    <div class="score-display">
      <ReadOnlyStarRating :rating="normalizedRating" />
    </div>
  </div>
</template>
