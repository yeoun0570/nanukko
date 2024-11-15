<script setup>
const status = null;

defineProps({
  currentStatus: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["status-change"]);

const changeStatus = (status) => {
  emit("status-change", status);
};
</script>

<template>
  <div class="status-filter">
    <button
      @click="changeStatus('SELLING')"
      :class="{ active: currentStatus === 'SELLING' }"
    >
      판매중
    </button>
    <button
      @click="changeStatus('RESERVED')"
      :class="{ active: currentStatus === 'RESERVED' }"
    >
      예약중
    </button>
    <button
      @click="changeStatus('SOLD_OUT')"
      :class="{ active: currentStatus === 'SOLD_OUT' }"
    >
      판매완료
    </button>
  </div>
</template>
