<script setup>
defineProps({
  notifications: {
    type: Array,
    required: true,
  },
});

defineEmits(["select"]);
</script>

<template>
  <div class="notification-list">
    <div class="notification-header">
      <h3>알림</h3>
    </div>

    <div v-if="notifications.length === 0" class="no-notifications">
      새로운 알림이 없습니다.
    </div>

    <div v-else class="notifications-container">
      <NotificationItem
        v-for="notification in notifications"
        :key="notification.notificationId"
        :notification="notification"
        @click="$emit('select', notification)"
      />
    </div>
  </div>
</template>
