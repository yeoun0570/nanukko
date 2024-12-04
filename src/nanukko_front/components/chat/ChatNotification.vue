<template>
  <div class="notification-wrapper" ref="container">
    <!-- 채팅 알림 버튼 -->
    <button 
      class="chat-button"
      @click="toggleDropdown"
      ref="notificationButton"
    >
      <i class="fas fa-comments"></i>
      <!-- 읽지 않은 메시지 수 뱃지 -->
      <span v-if="unreadCount > 0" class="notification-badge">
        {{ unreadCount }}
      </span>
    </button>

    <!-- 드롭다운 알림 메뉴 -->
    <Transition name="slide-fade">
      <div
        v-if="showDropdown"
        class="dropdown-menu"
        ref="dropdownMenu"
      >
        <!-- 알림 헤더 -->
        <div class="dropdown-header">
          <h3>새 메시지</h3>
        </div>
        
        <!-- 알림 메시지 목록 -->
        <div class="messages-container">
          <!-- 로딩 상태 -->
          <div v-if="loading" class="loading-state">
            <i class="fas fa-spinner fa-spin"></i>
          </div>

          <!-- 메시지 목록 -->
          <template v-else>
            <!-- 빈 상태 표시 -->
            <div v-if="chatRooms.length === 0" class="empty-state">
              <p>새로운 메시지가 없습니다</p>
            </div>

            <!-- 메시지 아이템 -->
            <div 
              v-else
              v-for="room in chatRooms" 
              :key="room.chatRoomId"
              class="message-item"
              @click="handleRoomSelect(room)"
            >
              <div class="message-content">
                <p class="product-name" :title="room.productName">
                  {{ room.productName }}
                </p>
                <span class="unread-badge" v-if="room.unreadCount > 0">
                  {{ room.unreadCount }}
                </span>
              </div>
              <div class="message-time">
                {{ formatMessageTime(room.lastMessageTime) }}
              </div>
            </div>
          </template>
        </div>

        <!-- 바로가기 푸터 -->
        <div class="dropdown-footer" @click="navigateToChat">
          채팅방 전체보기
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '~/composables/auth/useAuth'
import { useStomp } from '~/composables/chat/useStomp'
import { useApi } from '~/composables/useApi'

// 라우터 설정
const router = useRouter()
const { isAuthenticated, userId } = useAuth()
const stomp = useStomp()
const api = useApi()

// 상태 관리
const chatRooms = ref([]) // 채팅방 목록
const unreadCount = ref(0) // 읽지 않은 메시지 수
const showDropdown = ref(false) // 드롭다운 표시 여부
const loading = ref(false) // 로딩 상태
const container = ref(null) // 컨테이너 ref
const notificationSubscription = ref(null) // 웹소켓 구독 정보

// 시간 포맷팅 함수
const formatMessageTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '방금 전'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}분 전`
  if (diff < 86400000) {
    return date.toLocaleTimeString('ko-KR', { 
      hour: 'numeric', 
      minute: '2-digit',
      hour12: true 
    })
  }
  return date.toLocaleDateString('ko-KR', { 
    month: 'short', 
    day: 'numeric' 
  })
}

// 채팅방 목록 로드
const loadChatRooms = async () => {
  loading.value = true
  try {
    const response = await api.get('/chat/unread-messages')
    chatRooms.value = response || []
    updateUnreadCount()
  } catch (error) {
    console.error('채팅방 목록 로드 실패:', error)
  } finally {
    loading.value = false
  }
}

// 새 메시지 처리
const handleNewMessage = async (message) => {
  try {
    const messageData = typeof message === 'string' ? 
      JSON.parse(message) : 
      message.body ? JSON.parse(message.body) : message

    console.log('채팅 알림 메시지 데이터:', messageData);

    // 읽음 처리 메시지인 경우
    if (messageData.messageIds && Array.isArray(messageData.messageIds)) {
      console.log('Processing read status update:', {
        messageIds: messageData.messageIds,
        chatRoomId: messageData.chatRoom
      });

      chatRooms.value = chatRooms.value.map(room => {
        if (room.chatRoomId === messageData.chatRoom) {
          console.log('Updating room:', room.chatRoomId, 
            'current unreadCount:', room.unreadCount,
            'messages marked as read:', messageData.messageIds.length);
          
          return {
            ...room,
            unreadCount: Math.max(0, (room.unreadCount || 0) - messageData.messageIds.length)
          };
        }
        return room;
      });

      console.log('Updated chatRooms:', chatRooms.value);
      
      updateUnreadCount();
      return;
    }

    // 새 메시지인 경우 기존 로직 실행
    if (!messageData.isRead) {
      await loadChatRooms();
    }
  } catch (error) {
    console.error('메시지 처리 실패:', error);
  }
};

// 드롭다운 토글
const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

// 채팅방 선택 처리
const handleRoomSelect = async (room) => {
  showDropdown.value = false
  router.push(`/chat?roomId=${room.chatRoomId}`)
}

// 채팅 목록으로 이동
const navigateToChat = () => {
  showDropdown.value = false
  router.push('/chat')
}

// 읽지 않은 메시지 수 업데이트
const updateUnreadCount = () => {
  const previousCount = unreadCount.value;
  unreadCount.value = chatRooms.value.reduce(
    (sum, room) => sum + (room.unreadCount || 0), 
    0
  );
  console.log('Updated unread count:', {
    previous: previousCount,
    current: unreadCount.value,
    rooms: chatRooms.value.map(r => ({
      id: r.chatRoomId,
      unread: r.unreadCount
    }))
  });
}

// 알림 초기화
// ChatNotification.vue의 initializeNotifications 함수 수정
const initializeNotifications = async () => {
  if (!isAuthenticated.value || !userId.value) return

  try {
    if (!stomp.connected.value) {
      await stomp.connectChat(userId.value)
    }

    // 먼저 채팅방 목록을 로드
    await loadChatRooms()

    // 개인 알림 채널 구독
    const notificationDestination = `/user/${userId.value}/queue/chat.notification`;
    console.log('Subscribing to notifications:', notificationDestination);
    
    // 알림 구독
    notificationSubscription.value = await stomp.subscribeToChatRoom(
      notificationDestination,
      { onMessage: handleNewMessage }
    );

    // 각 채팅방의 읽음 처리 구독
    for (const room of chatRooms.value) {
      const readStatusDestination = `/queue/chat/${room.chatRoomId}`;
      console.log('Subscribing to read status:', readStatusDestination);
      
      await stomp.subscribeToChatRoom(
        readStatusDestination,
        { onMessage: handleReadStatus }
      );
    }

  } catch (error) {
    console.error('알림 초기화 실패:', error)
  }
}


// 읽음 처리 핸들러 추가
const handleReadStatus = (message) => {
  try {
    const data = typeof message === 'string' ? 
      JSON.parse(message) : 
      message.body ? JSON.parse(message.body) : message;

    console.log('Received read status update:', data);

    if (data.messageIds && Array.isArray(data.messageIds)) {
      // 채팅방 unreadCount 갱신
      const roomId = data.chatRoom || message.headers?.destination?.split('/').pop();
      
      chatRooms.value = chatRooms.value.map(room => {
        if (room.chatRoomId.toString() === roomId?.toString()) {
          console.log(`Updating unread count for room ${roomId}`);
          return {
            ...room,
            unreadCount: Math.max(0, (room.unreadCount || 0) - data.messageIds.length)
          };
        }
        return room;
      });

      // 전체 알림 수 업데이트
      updateUnreadCount();
    }
  } catch (error) {
    console.error('읽음 상태 처리 실패:', error);
  }
};


// 외부 클릭 감지
const handleOutsideClick = (event) => {
  if (container.value && !container.value.contains(event.target)) {
    showDropdown.value = false
  }
}

// 컴포넌트 마운트
onMounted(() => {
  if (isAuthenticated.value) {
    initializeNotifications()
  }
  document.addEventListener('click', handleOutsideClick)
})

// 컴포넌트 언마운트
onUnmounted(() => {
  if (notificationSubscription.value) {
    stomp.unsubscribe(notificationSubscription.value)
  }
  document.removeEventListener('click', handleOutsideClick)
})

// 인증 상태 변화 감시
watch(isAuthenticated, (newValue) => {
  if (newValue) {
    initializeNotifications()
  } else {
    chatRooms.value = []
    unreadCount.value = 0
  }
})
</script>

<style scoped>
@import '~/assets/chat/chat-notification.css';
</style>

<!-- <script setup>
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
</script> -->


