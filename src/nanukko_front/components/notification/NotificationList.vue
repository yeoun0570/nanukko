<script setup>
import NotificationItem from "./NotificationItem.vue";
import { useApi } from "@/composables/useApi";
import { useURL } from "@/composables/useURL";

const api = useApi();

const { baseURL } = useURL();

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

    <div class="notification-actions">
      <button
        v-if="
          (groupedNotifications.unread.length > 0) & (activeTab === 'unread')
        "
        @click="$emit('markAllAsRead')"
        class="mark-all-read-btn"
      >
        모두 읽기
      </button>
      <button @click="removeAllNotifications(activeTab)" class="remove-all-btn">
        모두 삭제
      </button>
    </div>

    <div class="notifications-container">
      <!-- 읽지 않은 알림 섹션 -->
      <div
        v-if="
          (groupedNotifications.unread.length > 0) & (activeTab === 'unread')
        "
        class="notification-section"
      >
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
/* 버튼 컨테이너 추가 */
.notification-actions {
  padding: 0 10px;
  text-align: right;
  overflow: hidden; /* float 처리 */
}

.mark-all-read-btn {
  display: inline-block; /* block에서 inline-block으로 변경 */
  margin: 6px 8px; /* 여백 축소 */
  padding: 1px 8px; /* 패딩 축소 */
  font-size: 12px; /* 폰트 크기 축소 */
  color: #666; /* 더 부드러운 텍스트 색상 */
  background-color: #f5f5f5; /* 더 부드러운 배경색 */
  border: 1px solid #e0e0e0; /* 테두리 추가 */
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  width: auto; /* 고정 너비 제거 */
  float: right; /* 우측 정렬 */
}

.mark-all-read-btn:hover {
  background-color: #eeeeee;
  color: #1976d2; /* 호버 시 텍스트 색상 변경 */
  border-color: #1976d2; /* 호버 시 테두리 색상 변경 */
}

.remove-all-btn {
  display: inline-block; /* block에서 inline-block으로 변경 */
  margin: 6px 8px; /* 여백 축소 */
  padding: 1px 8px; /* 패딩 축소 */
  font-size: 12px; /* 폰트 크기 축소 */
  color: #666; /* 더 부드러운 텍스트 색상 */
  background-color: #f5f5f5; /* 더 부드러운 배경색 */
  border: 1px solid #e0e0e0; /* 테두리 추가 */
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
  width: auto; /* 고정 너비 제거 */
  float: right; /* 우측 정렬 */
}

.remove-all-btn:hover {
  background-color: #eeeeee;
  color: #1976d2; /* 호버 시 텍스트 색상 변경 */
  border-color: #1976d2; /* 호버 시 테두리 색상 변경 */
}
</style>
