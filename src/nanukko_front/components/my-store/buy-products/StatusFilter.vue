<script setup>
const props = defineProps({
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
      @click="changeStatus('ESCROW_HOLDING')"
      :class="{ active: currentStatus === 'ESCROW_HOLDING' }"
    >
      구매중
    </button>
    <button
      @click="changeStatus('IN_DELIVERY')"
      :class="{ active: currentStatus === 'IN_DELIVERY' }"
    >
      배송중
    </button>
    <button
      @click="changeStatus('DELIVERED')"
      :class="{ active: currentStatus === 'DELIVERED' }"
    >
      배송완료
    </button>
    <button
      @click="changeStatus('ESCROW_RELEASED')"
      :class="{ active: currentStatus === 'ESCROW_RELEASED' }"
    >
      구매완료
    </button>
  </div>
</template>
