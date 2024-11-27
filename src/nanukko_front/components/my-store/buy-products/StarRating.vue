<!-- components/StarRating.vue -->
<script setup>
import { ref, computed } from "vue";

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0,
  },
});

const emit = defineEmits(["update:modelValue"]);

const hoverRating = ref(0);
const stars = ref([1, 2, 3, 4, 5]);

const calculateRating = (event, starPosition) => {
  const rect = event.currentTarget.getBoundingClientRect();
  const width = rect.width;
  const clickX = event.clientX - rect.left;
  return clickX < width / 2 ? starPosition * 2 - 1 : starPosition * 2;
};

const displayRating = computed(() => {
  return ((hoverRating.value || props.modelValue) / 2).toFixed(1);
});

const getFillPercentage = (starPosition) => {
  const currentRating = hoverRating.value || props.modelValue;
  if (currentRating >= starPosition * 2) return 100;
  if (currentRating >= starPosition * 2 - 1) return 50;
  return 0;
};

const handleMouseMove = (event, starPosition) => {
  hoverRating.value = calculateRating(event, starPosition);
};

const handleMouseLeave = () => {
  hoverRating.value = 0;
};

const handleClick = (event, starPosition) => {
  const newRating = calculateRating(event, starPosition);
  emit("update:modelValue", newRating);
};
</script>

<template>
  <div class="star-rating">
    <div class="stars-container">
      <div
        v-for="star in stars"
        :key="star"
        class="star"
        @mousemove="handleMouseMove($event, star)"
        @mouseleave="handleMouseLeave"
        @click="handleClick($event, star)"
      >
        <!-- 빈 별 (배경) -->
        <svg
          class="star-svg empty"
          viewBox="0 0 36 36"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M18 4.5c.9-2.2 3.1-3.5 5.4-3.3.7.1 1.3.3 1.9.6.6.3 1.1.7 1.6 1.2.4.5.8 1 1 1.6.2.6.3 1.2.3 1.9 0 .6-.1 1.3-.4 1.9l-3.1 6.7 7.3 1.1c.7.1 1.3.3 1.9.7.6.3 1.1.8 1.5 1.3.4.5.7 1.1.9 1.8.2.6.2 1.3.2 2-.1.7-.3 1.3-.6 1.9-.3.6-.7 1.1-1.2 1.5l-5.6 4.9 1.6 7.2c.1.7.1 1.3 0 2-.2.6-.5 1.2-.9 1.7-.4.5-.9.9-1.5 1.2-.6.3-1.2.4-1.9.4-.6 0-1.3-.2-1.9-.5L18 27.9l-6.5 3.8c-.6.3-1.2.5-1.9.5-.6 0-1.3-.1-1.9-.4-.6-.3-1.1-.7-1.5-1.2-.4-.5-.7-1.1-.9-1.7-.1-.6-.1-1.3 0-2l1.6-7.2-5.6-4.9c-.5-.4-.9-.9-1.2-1.5-.3-.6-.5-1.2-.6-1.9-.1-.7 0-1.3.2-2 .2-.6.5-1.2.9-1.8.4-.5.9-1 1.5-1.3.6-.3 1.2-.6 1.9-.7l7.3-1.1-3.1-6.7c-.2-.6-.4-1.2-.4-1.9 0-.6.1-1.3.3-1.9.2-.6.6-1.1 1-1.6.4-.5 1-.9 1.6-1.2.6-.3 1.2-.5 1.9-.6 2.3-.2 4.5 1.1 5.4 3.3z"
          />
        </svg>

        <!-- 채워진 별 -->
        <div
          class="star-fill"
          :style="{ width: getFillPercentage(star) + '%' }"
        >
          <svg
            class="star-svg filled"
            viewBox="0 0 36 36"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M18 4.5c.9-2.2 3.1-3.5 5.4-3.3.7.1 1.3.3 1.9.6.6.3 1.1.7 1.6 1.2.4.5.8 1 1 1.6.2.6.3 1.2.3 1.9 0 .6-.1 1.3-.4 1.9l-3.1 6.7 7.3 1.1c.7.1 1.3.3 1.9.7.6.3 1.1.8 1.5 1.3.4.5.7 1.1.9 1.8.2.6.2 1.3.2 2-.1.7-.3 1.3-.6 1.9-.3.6-.7 1.1-1.2 1.5l-5.6 4.9 1.6 7.2c.1.7.1 1.3 0 2-.2.6-.5 1.2-.9 1.7-.4.5-.9.9-1.5 1.2-.6.3-1.2.4-1.9.4-.6 0-1.3-.2-1.9-.5L18 27.9l-6.5 3.8c-.6.3-1.2.5-1.9.5-.6 0-1.3-.1-1.9-.4-.6-.3-1.1-.7-1.5-1.2-.4-.5-.7-1.1-.9-1.7-.1-.6-.1-1.3 0-2l1.6-7.2-5.6-4.9c-.5-.4-.9-.9-1.2-1.5-.3-.6-.5-1.2-.6-1.9-.1-.7 0-1.3.2-2 .2-.6.5-1.2.9-1.8.4-.5.9-1 1.5-1.3.6-.3 1.2-.6 1.9-.7l7.3-1.1-3.1-6.7c-.2-.6-.4-1.2-.4-1.9 0-.6.1-1.3.3-1.9.2-.6.6-1.1 1-1.6.4-.5 1-.9 1.6-1.2.6-.3 1.2-.5 1.9-.6 2.3-.2 4.5 1.1 5.4 3.3z"
            />
          </svg>
        </div>
      </div>
    </div>
    <span class="rating-text">{{ displayRating }} / 5</span>
  </div>
</template>