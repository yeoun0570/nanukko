sㄴ
<script setup>
import axios from "axios";

const props = defineProps({
  notification: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["click", "remove"]);

// 알림 삭제
const removeNotice = async () => {
  try {
    await axios.post("http://localhost:8080/api/notice/remove", null, {
      params: {
        notificationId: props.notification.notificationId,
      },
    });
    emit("remove", props.notification.notificationId);
  } catch (error) {
    console.error("알림 삭제 실패: ", error);
  }
};

const handleRemove = async () => {
  if (confirm("이 알림을 삭제하시겠습니까?")) {
    await removeNotice();
  }
};

const showDeliveryInfo = ref(false);

// 배송 정보 토글
const toggleDeliveryInfo = () => {
  showDeliveryInfo.value = !showDeliveryInfo.value;
};

// 메인 내용만 추출 (첫 번째 줄)
const getMainContent = (content) => {
  if (!content) return "";
  return content.split("|||")[0];
};

// 배송 정보가 있는지 확인
const hasDeliveryInfo = (content) => {
  if (!content) return false;
  return content.split("|||").length > 1;
};

// 배송 정보 추출(첫 번째 줄 제외)
const getDeliveryInfo = (content) => {
  if (!content) return [];
  return content.split("|||").slice(1);
};

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
    :class="{
      unread: !notification.isRead,
      read: notification.isRead,
    }"
    @click="$emit('click', notification)"
  >
    <div class="notification-content-wrapper">
      <div class="notification-type">
        {{ getNotificationTitle(notification.type) }}
      </div>
      <div class="notification-content">
        {{ getMainContent(notification.content) }}
        <button
          v-if="hasDeliveryInfo(notification.content)"
          class="delivery-info-btn"
          @click.stop="toggleDeliveryInfo"
        >
          배송지 확인
        </button>
        <div
          v-if="showDeliveryInfo && hasDeliveryInfo(notification.content)"
          class="delivery-info"
        >
          <div
            v-for="(line, index) in getDeliveryInfo(notification.content)"
            :key="index"
            class="delivery-info-line"
          >
            {{ line }}
          </div>
        </div>
      </div>
      <div class="notification-time">
        {{ formatTime(notification.createdAt) }}
      </div>
    </div>
    <button class="remove-btn" @click.stop="handleRemove">삭제</button>
  </div>
</template>

<style scoped>
.remove-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: transparent;
  border: none;
  color: #ff4444;
  font-size: 12px;
  cursor: pointer;
}

.remove-btn:hover {
  color: #d32f2f;
}

.delivery-info-btn {
  margin-top: 8px;
  padding: 4px 12px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.delivery-info-btn:hover {
  background-color: #1565c0;
}

.delivery-info {
  margin-top: 8px;
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 13px;
}

.delivery-info-line {
  padding: 4px 0;
  color: #333;
  line-height: 1.4;
}

.delivery-info-line:not(:last-child) {
  border-bottom: 1px solid #eee;
}

.notification-content {
  display: flex;
  flex-direction: column;
}

/* 버튼 위치 조정을 위한 스타일 */
.notification-content-wrapper {
  width: 100%;
}
</style>
