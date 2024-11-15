<!-- Pagination.vue -->
<script setup>
const props = defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(['page-change']);

// 현재 페이지 그룹 계산 (1-10, 11-20, 21-30, ...)
const currentGroup = computed(() => Math.floor(props.currentPage / 10));
const pageGroupStart = computed(() => currentGroup.value * 10);

// 현재 그룹의 페이지 번호 배열 생성
const pageNumbers = computed(() => {
  const start = pageGroupStart.value;
  const end = Math.min(start + 10, props.totalPages);
  return Array.from({ length: end - start }, (_, i) => start + i);
});

// 이전 그룹이 있는지 확인
const hasPrevGroup = computed(() => pageGroupStart.value > 0);

// 다음 그룹이 있는지 확인
const hasNextGroup = computed(() => {
  const nextGroupStart = (currentGroup.value + 1) * 10;
  return nextGroupStart < props.totalPages;
});

const changePage = (page) => {
  emit('page-change', page);
};

// 이전 그룹의 마지막 페이지로 이동
const goToPrevGroup = () => {
  const prevGroupLastPage = pageGroupStart.value - 1;
  changePage(prevGroupLastPage);
};

// 다음 그룹의 첫 페이지로 이동
const goToNextGroup = () => {
  const nextGroupFirstPage = pageGroupStart.value + 10;
  changePage(nextGroupFirstPage);
};
</script>

<template>
  <div class="pagination">
    <!-- 첫 페이지로 이동 -->
    <button 
      :disabled="currentPage === 0" 
      @click="changePage(0)"
      class="nav-btn"
    >
      &lt;&lt;
    </button>

    <!-- 이전 그룹으로 이동 -->
    <button 
      v-if="hasPrevGroup"
      @click="goToPrevGroup"
      class="nav-btn"
    >
      &lt;
    </button>

    <!-- 페이지 번호 버튼들 -->
    <button
      v-for="pageNum in pageNumbers"
      :key="pageNum"
      @click="changePage(pageNum)"
      :class="{ active: currentPage === pageNum }"
      class="page-btn"
    >
      {{ pageNum + 1 }}
    </button>

    <!-- 다음 그룹으로 이동 -->
    <button 
      v-if="hasNextGroup"
      @click="goToNextGroup"
      class="nav-btn"
    >
      &gt;
    </button>

    <!-- 마지막 페이지로 이동 -->
    <button 
      :disabled="currentPage === totalPages - 1"
      @click="changePage(totalPages - 1)"
      class="nav-btn"
    >
      &gt;&gt;
    </button>
  </div>
</template>

<style scoped>
@import url('../assets/Pagination.css');
</style>