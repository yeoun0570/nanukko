<template>
  <ClientOnly>
    <div class="relative">
      <!-- 알림 버튼 -->
      <button 
        class="chat-button"
        @click="toggleDropdown"
        ref="notificationButton"
      >
        <i class="fas fa-comments"></i>
        <span v-if="unreadCount > 0" class="notification-badge">
          {{ unreadCount }}
        </span>
      </button>

      <!-- 드롭다운 메뉴 -->
      <Transition name="fade">
        <div 
          v-if="showDropdown" 
          class="notifications-dropdown"
          ref="dropdownMenu"
        >
          <div class="notifications-header">
            <span>새 메시지</span>
            <button class="clear-button" @click="clearAllNotifications">모두 지우기</button>
          </div>
          
          <div class="notifications-list">
            <div 
              v-for="notification in notifications" 
              :key="notification.id"
              class="notification-item"
              @click="handleNotificationClick(notification)"
            >
              <div class="notification-sender">{{ notification.sender }}</div>
              <div class="notification-message">{{ notification.message }}</div>
              <div class="notification-time">
                {{ formatTime(notification.createdAt) }}
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </ClientOnly>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useStomp } from '~/composables/chat/useStomp'
import { useAuth } from '~/composables/auth/useAuth'
import { useRouter } from 'vue-router'
import { useFormatTime } from '~/composables/useFormatTime'
import { ca } from 'vuetify/locale'
import { useApi } from '#build/imports'

/**
 * 
 */
// localStorage 관련 유틸리티 함수들을 별도로 정의
const storage = {
  save(key, value) {
    if (process.client) {  // 클라이언트 사이드인 경우에만 실행
      localStorage.setItem(key, JSON.stringify(value));
      updateUnreadCount();// 저장할 때마다 카운트 업데이트
    }
  },
  
  load(key) {
    if (process.client) {  // 클라이언트 사이드인 경우에만 실행
      try {
        const item = localStorage.getItem(key);
        const parsedNotifications = item ? JSON.parse(item) : [];
        notifications.value = parsedNotifications;
        updateUnreadCount(); // 로드할 때도 카운트 업데이트

        return parsedNotifications;
      } catch (error) {
        console.error('로컬 스토리지 로드 실패:', error);
        return [];
      }
    }
    return [];
  }
};

// 컴포넌트 setup 내부
const notifications = ref([]);  // 초기값은 빈 배열로 설정

// onMounted에서 localStorage 데이터 로드
onMounted(() => {
  //저장된 알림 로드
  notifications.value = storage.load('chat-notifications');

  //저장된 카운트 로드(없으면 계산)
  const savedCount = localStorage.getItem('chat-notifications-count');
  if(savedCount !== null){
    unreadCount.value = parseInt(savedCount, 10);
  }else{
    updateUnreadCount();
  }

  if(isAuthenticated.value){
    initializeNotifications();
  }
});





// 필요한 composables 초기화
const router = useRouter()
const stomp = useStomp()
const { userId, isAuthenticated } = useAuth()
const { formatTime } = useFormatTime()
const api = useApi()

// 상태 관리를 위한 refs
const unreadCount = ref(0)
const showDropdown = ref(false)
const notificationButton = ref(null)
const dropdownMenu = ref(null)
let notificationSubscription = null

// 로컬 스토리지 유틸리티 함수
function saveNotifications(notifs) {
  localStorage.setItem('chat-notifications', JSON.stringify(notifs))
}

function loadNotifications() {
  try {
    const saved = localStorage.getItem('chat-notifications')
    return saved ? JSON.parse(saved) : []
  } catch (error) {
    console.error('알림 로드 실패:', error)
    return []
  }
}

// 새 메시지 처리 함수
const handleNewMessage = (message) => {
  try {
    console.log('수신된 알림:', message)
    const messageData = typeof message === 'string' ? 
      JSON.parse(message) : 
      message.body ? JSON.parse(message.body) : message

      // 읽음 처리 알림인 경우 처리하지 않음
    if (messageData.isRead) {
      return;
    }

    // 이미 존재하는 알림인지 확인
    // 중복 체크를 위한 키 생성
    const notificationKey = `${messageData.chatRoom}-${messageData.createdAt}`;
    const isDuplicate = notifications.value.some(n => 
      `${n.chatRoomId}-${n.createdAt}` === notificationKey
    );

    if (!isDuplicate && messageData.chatRoom) {
      const notification = {
        id: Date.now(),
        chatRoomId: messageData.chatRoom,
        message: messageData.chatMessage,
        sender: messageData.sender,
        createdAt: messageData.createdAt,
        isRead: false
      };

      notifications.value = [notification, ...notifications.value];
      storage.save('chat-notifications', notifications.value);
    }
  } catch (error) {
    console.error('알림 처리 중 오류:', error);
  }
};




//읽음 상태 업데이트
const updateUnreadCount = () => {
  const count = notifications.value.filter(n => !n.isRead).length;
  unreadCount.value = count;

  //localStorage에 카운트도 함께 저장
  if(process.client){
    localStorage.setItem('chat-notifications-count', count.toString());
  }
}


// 알림 확인 시 처리
const handleNotificationClick = async (notification) => {
  try {
    // 알림 읽음 처리
    markNotificationAsRead(notification);
    showDropdown.value = false;


    // 채팅방으로 이동
    await navigateToChatRoom(notification.chatRoomId);
  } catch (error) {
    console.error('알림 처리 실패:', error);
  }
};

//채팅방 이동 
const navigateToChatRoom = async (roomId) => {
  try {
    await router.push({
      path: '/chat',
      query: roomId? { roomId: roomId.toString() } : undefined
    });
    return true;
  }catch(error){
    console.error('라우터 이동 실패', error);
    return false;
  }
}




// 드롭다운 토글 함수
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
  unreadCount.value = 0;
}

// 외부 클릭 핸들러 추가
onMounted(() => {
  document.addEventListener('click', (event) => {
    const dropdown = dropdownMenu.value
    const button = notificationButton.value
    
    if (showDropdown.value && dropdown && !dropdown.contains(event.target) && 
        button && !button.contains(event.target)) {
      showDropdown.value = false
    }
  })
})

// 모든 알림 지우기
const clearAllNotifications = () => {
  notifications.value = []
  unreadCount.value = 0
  if(process.client){
    localStorage.removeItem('chat-notifications');
    localStorage.removeItem('chat-notifications-count');
  }
  showDropdown.value = false;
}

// 외부 클릭 핸들러
const handleOutsideClick = (event) => {
  if (showDropdown.value && 
      dropdownMenu.value && 
      notificationButton.value && 
      !dropdownMenu.value.contains(event.target) &&
      !notificationButton.value.contains(event.target)) {
    showDropdown.value = false
  }
}

// 알림 초기화
const initializeNotifications = async () => {
  if (!isAuthenticated.value || !userId.value) return

  try {
    // STOMP 연결
    if (!stomp.connected.value) {
      await stomp.connectChat(userId.value)
    }

    // 개인 알림 채널 구독
    const destination = `/user/${userId.value}/queue/chat.notification`
    console.log('알림 구독:', destination)
    
    notificationSubscription = await stomp.subscribeToChatRoom(
      destination,
      { onMessage: handleNewMessage }
    )
  } catch (error) {
    console.error('알림 초기화 실패:', error)
  }
}


// 로그인 시 최신 메시지 체크를 위한 함수 추가
const initializeUnreadMessages = async () => {
  try {
    // API 호출을 통해 읽지 않은 최신 메시지 조회
    const response = await api.get('/chat/unread-messages');

    // response.data가 유효한 배열인지 확인하고 기본값 설정
    const unreadMessages = Array.isArray(response.data) ? response.data : [];

    // 읽지 않은 메시지들을 알림으로 변환
    const newNotifications = unreadMessages.map(msg => ({
      id: Date.now(),
      chatRoomId: msg.chatRoom,
      message: msg.chatMessage,
      sender: msg.sender,
      createdAt: msg.createdAt,
      isRead: false
    }));

    // 알림 상태 업데이트
    notifications.value = newNotifications;
    updateUnreadCount();//읽지 않은 메시지 수 갱신
    saveNotifications(notifications.value);// 알림 상태 저장
  } catch (error) {
    console.error('읽지 않은 메시지 조회 실패:', error);
  }
};


// 알림 확인 시 처리하는 함수
const markNotificationAsRead = (notification) => {
  notification.isRead = true;
  storage.save('chat-notifications', notifications.value);
  //참고로 updateUnreadCount는 storage.save 내부에서 호출됨
}



// 라이프사이클 훅//
// useAuth의 logout 함수 호출 후 알림 초기화되도록 watch 추가
watch(isAuthenticated, (newValue) => {
  if (!newValue) {
    clearAllNotifications();
  }
});

onMounted(async () => {
  if (isAuthenticated.value) {
    //로그인하면 알림 초기화
    await initializeNotifications();
    await initializeUnreadMessages();
  }
  
  document.addEventListener('click', handleOutsideClick);
  unreadCount.value = notifications.value.filter(n => !n.isRead).length;
});

onUnmounted(() => {
  // 구독 해제
  if (notificationSubscription) {
    stomp.unsubscribe(notificationSubscription)
    notificationSubscription = null
  }
  
  // 이벤트 리스너 제거
  document.removeEventListener('click', handleOutsideClick)
})

// 인증 상태 감시
watch(isAuthenticated, (newValue) => {
  if (newValue) {
    initializeNotifications()
  } else {
    clearAllNotifications()
  }
})

// 알림 상태 변화 감시
watch(notifications, (newNotifications) => {
  if (process.client) {
    storage.save('chat-notifications', newNotifications);
    updateUnreadCount();
  }
}, { deep: true });

watch(notifications, (newValue) => {
  console.log('알림 목록 변경:', newValue);
}, { deep: true });
</script>

<style scoped>
/* 채팅 버튼 스타일 */
.chat-button {
  padding: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  color: #4b5563;
  transition: color 0.2s;
}

.chat-button:hover {
  color: #1d4ed8;
}

/* 알림 뱃지 스타일 */
.notification-badge {
  position: absolute;
  top: 0;
  right: 0;
  background-color: #ef4444;
  color: white;
  font-size: 0.75rem;
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  min-width: 20px;
  text-align: center;
}

/* 드롭다운 메뉴 스타일 */
.notifications-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 0.5rem;
  width: 320px;
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.notifications-dropdown[v-show] {
  display: block;  /* v-show가 true일 때 표시 */
}

/* 드롭다운 헤더 스타일 */
.notifications-header {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notifications-header span {
  font-weight: 500;
  color: #1f2937;
}

.clear-button {
  font-size: 0.875rem;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
}

.clear-button:hover {
  color: #4b5563;
}

/* 알림 목록 스타일 */
.notifications-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f3f4f6;
}

.notification-item.unread {
  background-color: #f0f9ff;
}

/* 알림 내용 스타일 */
.notification-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.notification-sender {
  font-weight: 500;
  color: #1f2937;
}

.notification-message {
  font-size: 0.875rem;
  color: #4b5563;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.notification-time {
  font-size: 0.75rem;
  color: #6b7280;
}

/* 빈 상태 스타일 */
.empty-notifications {
  padding: 2rem;
  text-align: center;
  color: #6b7280;
  font-size: 0.875rem;
}

.relative {
  position: relative;
  display: inline-block;
}

/* 트랜지션 효과 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>