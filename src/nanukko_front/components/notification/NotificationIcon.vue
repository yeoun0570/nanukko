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
    <i class="fas fa-bell" :class="{ 'has-notifications': unreadCount > 0 }">
      <span v-show="unreadCount > 0 && showBadge" class="notification-badge">
        {{ unreadCount }}
      </span>
    </i>
  </div>
</template>
