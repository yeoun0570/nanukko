<script setup>
defineProps({
  unreadCount: {
    type: Number,
    default: 0,
  },
});

defineEmits(["toggle"]);
</script>

<template>
  <div class="notification-icon" @click="$emit('toggle')">
    <span class="icon-text">알림</span>
    <!-- 읽지 않은 알림이 있을때만 badge 표시 -->
    <span v-if="unreadCount > 0" class="notification-badge">
      {{ unreadCount }}
    </span>
    <!-- <i class="fas fa-bell" :class="{ 'has-notifications': unreadCount > 0 }">
      <span v-if="unreadCount > 0" class="notification-badge">
        {{ unreadCount }}F
      </span>
    </i> -->
  </div>
</template>
