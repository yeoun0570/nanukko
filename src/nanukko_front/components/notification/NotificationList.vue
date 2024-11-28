<script setup>
import NotificationItem from "./NotificationItem.vue";
import { useApi } from '@/composables/useApi';

const api = useApi();

const props = defineProps({
  notifications: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["select", "markAllAsRead", "removeAll"]);

const activeTab = ref("unread"); //탭 상태 관리

//알림을 읽음/안읽음으로 분류
const groupedNotifications = computed(() => {
  const result = {
    unread: props.notifications.filter((n) => !n.isRead),
    read: props.notifications.filter((n) => n.isRead),
  };
  console.log("알림 섹션: ", result);
  return result;
});

// 알림 개별 삭제
const removeNotification = (notificationId) => {
  const index = props.notifications.findIndex(
    (n) => n.notificationId === notificationId
  );
  if (index !== -1) {
    props.notifications.splice(index, 1);
  }
};

// 알림 섹션별 전체 삭제
const removeAllNotifications = async (type) => {
  try {
    if (
      !confirm(
        `${type === "unread" ? "읽지 않은" : "읽은"} 알림을 모두 삭제하시겠습니까?`
      )
    ) {
      return;
    }

    const notificationsToRemove =
      type === "unread"
        ? groupedNotifications.value.unread
        : groupedNotifications.value.read;

    const notificationIds = notificationsToRemove.map((n) => n.notificationId);
    console.log("notificationIds: ", notificationIds);

    // 요청 데이터 및 헤더 출력
    console.log("Sending request to remove notifications", {
      url: `${baseURL}/notice/removeAll`,
      data: { notificationIds },
    });

    await api.post(`/notice/removeAll`, notificationIds);

    // 부모 컴포넌트에 삭제된 알림 ID 목록 전달
    emit("removeAll", notificationIds);
  } catch (error) {
    console.error("알림 일괄 삭제 실패: ", error);
    alert("알림 삭제에 실패했습니다.");
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
        <button
          @click="removeAllNotifications('unread')"
          class="remove-all-btn"
        >
          모두 삭제
        </button>
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
        <button @click="removeAllNotifications('read')" class="remove-all-btn">
          모두 삭제
        </button>
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

<style scoped>
.remove-all-btn {
  padding: 4px 8px;
  font-size: 12px;
  color: #666;
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.remove-all-btn:hover {
  background-color: #e0e0e0;
  color: #ff4444;
}
</style>
