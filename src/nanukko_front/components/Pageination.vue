<script setup>
defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(["page-change"]);

const changePage = (page) => {
  emit("page-change", page);
};
</script>

<template>
  <div class="pagination">
    <button :disabled="currentPage === 0" @click="changePage(currentPage - 1)">
      이전
    </button>

    <button
      v-for="page in totalPages"
      :key="page"
      :class="{ active: currentPage === page - 1 }"
      @click="changePage(page - 1)"
    >
      {{ page }}
    </button>

    <button
      :disabled="currentPage === totalPages - 1"
      @click="changePage(currentPage + 1)"
    >
      다음
    </button>
  </div>
</template>

<style scoped>
@import url('../assets/Pagination.css');
</style>
