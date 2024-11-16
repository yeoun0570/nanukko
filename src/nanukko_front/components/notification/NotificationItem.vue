<script setup>
const props = defineProps({
  notification: {
    type: Object,
    required: true,
  },
});

defineEmits(["click"]);

// 알림 타입에 따른 제목을 반환하는 메서드
const getNotificationTitle = (type) => {
  const titles = {
    CHAT: "채팅 알림",
    PAYMENT: "결제 알림",
    DELIVERY: "배송 알림",
    REVIEW: "리뷰 알림",
    WARNING: "경고 알림",
  };
  return titles[type] || "알림";
};

// 타임스탬프를 상대적 시간으로 변환하는 함수
const formatTime = (timestamp) => {
  if (!timestamp) return "";

  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;

  // 1분 미만
  if (diff < 60000) {
    return "방금 전";
  }
  // 1시간 미만
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000);
    return `${minutes}분 전`;
  }
  // 24시간 미만
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000);
    return `${hours}시간 전`;
  }
  // 그 외
  return date.toLocaleDateString();
};
</script>

<template>
  <div
    class="notification-item"
    :class="{ 'unread': !notification.isRead }"
    @click="$emit('click', notification)"
  >
    <div class="notification-type">
      {{ getNotificationTitle(notification.type) }}
    </div>
    <div class="notification-content">
      {{ notification.content }}
    </div>
    <div class="notification-time">
      {{ formatTime(notification.createdAt) }}
    </div>
  </div>
</template>
