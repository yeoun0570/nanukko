<script setup>
const props = defineProps({
  notifications: {
    type: Array,
    required: true,
  },
});

const activeTab = ref("unread"); //탭 상태 관리

defineEmits(["select", "markAllAsRead"]);

//알림을 읽음/안읽음으로 분류
const groupedNotifications = computed(() => {
  const result = {
    unread: props.notifications.filter((n) => !n.isRead),
    read: props.notifications.filter((n) => n.isRead),
  };
  console.log("알림 섹션: ", result);
  return result;
});

const removeNotification = (notificationId) => {
  const index = props.notifications.findIndex(
    (n) => n.notificationId === notificationId
  );
  if (index !== -1) {
    props.notifications.splice(index, 1);
  }
};
</script>

<template>
  <div class="notification-list">
    <div class="notification-header">
      <h3>알림</h3>
    </div>

    <div class="tabs">
      <div
        :class="{ active: activeTab === 'unread' }"
        @click="activeTab = 'unread'"
      >
        읽지 않은 알림({{ groupedNotifications.unread.length }})
      </div>

      <div
        :class="{ active: activeTab === 'read' }"
        @click="activeTab = 'read'"
      >
        읽은 알림({{ groupedNotifications.read.length }})
      </div>
    </div>

    <div
      v-if="(groupedNotifications.unread.length > 0) & (activeTab === 'unread')"
      @click="$emit('markAllAsRead')"
      class="mark-all-read-btn"
    >
      모두 읽기
    </div>

    <div class="notifications-container">
      <!-- 읽지 않은 알림 섹션 -->
      <div
        v-if="
          (groupedNotifications.unread.length > 0) & (activeTab === 'unread')
        "
        class="notification-section"
      >
        <div class="section-title">읽지 않은 알림</div>
        <NotificationItem
          v-for="notification in groupedNotifications.unread"
          :key="notification.notificationId"
          :notification="notification"
          @click="$emit('select', notification)"
          @remove="removeNotification"
        />
      </div>

      <!-- 읽은 알림 섹션 -->
      <div
        v-if="(groupedNotifications.read.length > 0) & (activeTab === 'read')"
        class="notification-section"
      >
        <div class="section-title">읽은 알림</div>
        <NotificationItem
          v-for="notification in groupedNotifications.read"
          :key="notification.notificationId"
          :notification="notification"
          @click="$emit('select', notification)"
          @remove="removeNotification"
        />
      </div>

      <div v-if="notifications.length === 0" class="no-notifications">
        새로운 알림이 없습니다.
      </div>
    </div>
  </div>
</template>
