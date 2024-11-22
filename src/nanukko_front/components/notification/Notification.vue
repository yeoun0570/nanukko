<script setup>
import NotificationIcon from "./NotificationIcon.vue";
import NotificationList from "./NotificationList.vue";
import axios from "axios";
import { useApi } from "~/composables/useApi";

const { baseURL } = useApi();

// 알림 목록을 저장할 배열
const notifications = ref([]);
// SSE 연결을 저장할 변수
// EventSource : 서버에서 보내는 이벤트(데이터)를 수신하는 역할만 함 (수신기) => 서버의 SseEmitter가 송신기라면 애는 수신기라고 생각하면 됨
const eventSource = ref(null);
// 알림 목록 출력 여부 제어하기 위한 변수
const showNotifications = ref(false);
// 읽지 않은 알림 수를 저장할 변수
const unreadCount = ref(0);

const userId = "seller1"; // 추후에 로그인한 사용자로 변경

// SSE 연결을 설정하는 메서드
const connectSSE = () => {
  // userId가 없으면(로그인하지 않았으면) 연결하지 않음
  if (!userId) {
    console.log("로그인이 필요합니다");
    return;
  }

  //기존 연결이 있으면 닫기
  if (eventSource.value) {
    eventSource.value.close();
  }

  // 마지막으로 받은 이벤트 ID를 localStorage에서 가져옴
  const lastEventId = localStorage.getItem("lastEventId") || "";

  // EventSource 객체 생성하여 서버와 SSE 연결
  eventSource.value = new EventSource(
    `${baseURL}/notice/connect?userId=${userId}&lastEventId=${lastEventId}`
  );

  // SSE 이벤트 리스너 등록 - 'SSE' 이벤트 수신 시 발생('SSE'는 백에서 설정한 전송할 때 이벤트 이름)
  eventSource.value.addEventListener("SSE", (event) => {
    try {
      // 이벤트 ID 저장
      if (event.id) {
        localStorage.setItem("lastEventId", event.id);
      }

      // 수신한 데이터 파싱해서 알림 추가
      const notification = JSON.parse(event.data);

      if (notification.type === "CONNECT") {
        console.log("알림 연결 성공!");
        return;
      }

      addNotification(notification);
      showToast(notification);
    } catch (error) {
      console.error("알림 데이터 처리 중 오류 발생: ", error);
    }
  });

  eventSource.value.onopen = () => {
    console.log("알림 연결 성공!");
  };

  // 에러 발생 시 실행될 콜백
  eventSource.value.onerror = (error) => {
    console.error("EventSource 오류: ", error);
    if (eventSource.value) {
      eventSource.value.close();
    }
    //3초 후 재연결 시도
    setTimeout(() => {
      if (userId) {
        // 여전히 로그인 상태일 때만 재연결
        connectSSE();
      }
    }, 3000);
  };
};

// 로그인 시 호출되는 함수
const initializeNotifications = async () => {
  try {
    // DB에서 이전 알림들 가져오기
    const response = await axios.get(`${baseURL}/notice/previous`, {
      params: { userId },
    });
    const readNotifications =
      JSON.parse(localStorage.getItem("readNotifications")) || [];

    // 기존 알림 설정
    notifications.value = response.data.map((notification) => ({
      ...notification,
      isRead:
        readNotifications.includes(notification.notificationId) ||
        notification.isRead,
    }));

    // 읽지 않은 알림 수 계산
    updateUnreadCount();

    // 그 다음에 SSE 연결 시작
    connectSSE();
  } catch (error) {
    console.log("알림 초기화 실패: ", error);
  }
};

// 로그아웃 시 호출되는 함수
const cleanupNotifications = () => {
  if (eventSource.value) {
    eventSource.value.close();
    eventSource.value = null;
  }
  notifications.value = [];
  unreadCount.value = 0;
  // LastEventId는 유지 (다음 로그인 시 사용)
};

// 새로운 알림을 목록에 추가하는 메서드
const addNotification = (notification) => {
  // 배열 맨 앞에 새 알림 추가
  notifications.value.unshift(notification);
  // 읽지 않은 알림 수 증가
  unreadCount.value++;
  // 토스트 메시지 표시
  showToast(notification);
};

// 알림 타입에 따른 제목을 반환하는 메서드 추가
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

//토스트 알림을 화면에 표시하는 메서드
//토스트 : 화면 구석에 잠깐 나타났다가 사라지는 알림 메시지
const showToast = (notification) => {
  // 메시지의 첫번째 줄만 추출
  const mainMessage = notification.content.split("|||")[0];

  //토스트 요소 생성
  const toast = document.createElement("div");
  toast.className = "notification-toast";
  // 토스트 내용 설정
  toast.innerHTML = `
    <div class="toast-content">
      <div class="toast-title">${getNotificationTitle(notification.type)}</div>
      <div class="toast-message">${mainMessage}</div>
    </div>
  `;

  // 토스트를 body에 추가
  document.body.appendChild(toast);

  // 토스트 애니메이션 처리
  setTimeout(() => {
    toast.classList.add("show");
    setTimeout(() => {
      toast.classList.remove("show");
      setTimeout(() => {
        document.body.removeChild(toast);
      }, 300);
    }, 3000);
  }, 100);
};

// 알림 목록 표시 상태를 토글하는 함수
const toggleNotfications = () => {
  showNotifications.value = !showNotifications.value;
};

// 알림 클릭 시 처리하는 함수
const handleNotificationClick = async (notification) => {
  // 읽지 않은 알림이면 읽음 처리
  if (!notification.isRead) {
    try {
      await markAsRead(notification);

      // 해당 알림의 인덱스를 찾아서
      const index = notifications.value.findIndex(
        (n) => n.notificationId === notification.notificationId
      );

      if (index !== -1) {
        // 새로운 배열을 생성하면서 해당 알림만 새 객체로 교체
        notifications.value = [
          ...notifications.value.slice(0, index),
          { ...notification, isRead: true },
          ...notifications.value.slice(index + 1),
        ];
      }

      // 읽은 알림 ID를 LocalStorage에 저장
      const readNotifications =
        JSON.parse(localStorage.getItem("readNotifications")) || [];
      if (!readNotifications.includes(notification.notificationId)) {
        readNotifications.push(notification.notificationId);
        localStorage.setItem(
          "readNotifications",
          JSON.stringify(readNotifications)
        );
      }

      //읽지 않은 알림 수 업데이트
      updateUnreadCount();

      // URL이 있으면 해당 페이지로 이동
      if (notification.url) {
        console.log(notification.url);
        await navigateTo(notification.url, {
          external: true, // 외부 URL 허용
        });
      }
      // 알림 목록 닫기
      showNotifications.value = false;
    } catch (error) {
      console.error("알림 읽음 처리 실패: ", error);
    }
  }
};

// 읽지 않은 알림 수를 계산하는 메서드
const updateUnreadCount = () => {
  unreadCount.value = notifications.value.filter((n) => !n.isRead).length;
};

// 알림을 읽음 처리하는 함수
const markAsRead = async (notification) => {
  try {
    // 서버에 읽음 처리 요청
    await axios.post(`${baseURL}/notice/${notification.notificationId}/read`);
    notification.isRead = true;
  } catch (error) {
    console.error("알림 읽음 처리 실패: ", error);
  }
};

// 모든 알림 읽음 처리 메서드
const markAllAsRead = async () => {
  try {
    await axios.post(`${baseURL}/notice/read-all`, null, {
      params: {
        userId: userId,
      },
    });

    // 모든 알림을 읽음으로 처리
    notifications.value.forEach((notification) => {
      notification.isRead = true;
    });

    // 읽은 알림 ID를 LocalStorage에 저장
    const allReadIds = notifications.value.map((n) => n.notificationId);
    localStorage.setItem("readNotifications", JSON.stringify(allReadIds));

    unreadCount.value = 0;
  } catch (error) {
    console.error("전체 알림 읽음 처리 실패: ", error);
  }
};

// document 클릭 이벤트 핸들러
const handleClickOutside = (event) => {
  // notifcation-wrapper 외부 클릭시 알림창 닫기
  if (
    showNotifications.value &&
    !event.target.closest(".notifcation-wrapper")
  ) {
    showNotifications.value = false;
  }
};

// 컴포넌트 마운트 시 실행
onMounted(() => {
  // SSE 연결 시작
  if (userId) {
    // connectSSE() 대신 initializeNotifications() 사용
    initializeNotifications();
  }
  document.addEventListener("click", handleClickOutside);
});

// 컴포넌트 언마운트 시 실행
onUnmounted(() => {
  // SSE 연결이 있으면 종료
  if (eventSource.value) {
    eventSource.value.close();
    eventSource.value = null;
  }
  document.removeEventListener("click", handleClickOutside);
});
</script>

<template>
  <div class="notification-wrapper" @click.stop>
    <NotificationIcon
      :unread-count="unreadCount"
      :show-badge="!showNotifications"
      @toggle="toggleNotfications"
    />
    <transition name="slide-fade">
      <NotificationList
        v-if="showNotifications"
        :notifications="notifications"
        @select="handleNotificationClick"
        @markAllAsRead="markAllAsRead"
      />
    </transition>
  </div>
</template>

<style>
@import url("../../assets/notification/Notification.css");
</style>
