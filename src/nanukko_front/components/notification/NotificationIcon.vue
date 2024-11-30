<script setup>
defineProps({
  unreadCount: {
    type: Number,
    default: 0,
  },
  showBadge: {
    type: Boolean,
    default: true,
  },
});

defineEmits(["toggle"]);
</script>

<template>
  <div class="notification-icon" @click="$emit('toggle')">
    <span class="notification-text">알림</span>
    <span v-if="unreadCount > 0" class="notification-badge">
      {{ unreadCount }}
    </span>
  </div>
</template>
