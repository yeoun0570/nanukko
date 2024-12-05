<template>
  <div class="notification-wrapper" ref="container">
    <!-- 채팅 알림 버튼 -->
     <!-- 알림 버튼 -->
     <button 
      class="chat-button"
      @click="handleNotificationClick"
      ref="notificationButton"
    >
      <i class="fas fa-comments"></i>
      <span v-if="unreadCount > 0 && isAuthenticated" class="notification-badge">
        {{ unreadCount }}
      </span>
    </button>

    <!-- 드롭다운 알림 메뉴 - 로그인된 경우에만 표시 -->
    <Transition name="slide-fade">
      <div
        v-if="showDropdown && isAuthenticated"
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

const emit = defineEmits(['show-login']);

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


// 알림 클릭 핸들러
const handleNotificationClick = () => {
  if (!isAuthenticated.value) {
    emit('show-login');
    return;
  }
  toggleDropdown();
};

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

const clearAllNotifications = () => {
  // Clear all notifications and reset state
  chatRooms.value = []
  unreadCount.value = 0
  showDropdown.value = false
  
  // Clear localStorage items if needed
  if (process.client) {
    localStorage.removeItem('chat-notifications')
    localStorage.removeItem('chat-notifications-count')
  }
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

// 인증 상태 변화 감시(로그아웃 감지해서 알림 바로 사라질 수 있도록하기)
// 로그아웃 감지 및 상태 초기화
watch(() => isAuthenticated.value, (newValue) => {
  if (!newValue) {
    // 상태 초기화
    chatRooms.value = [];
    unreadCount.value = 0;
    showDropdown.value = false;
    
    // localStorage 정리
    if (process.client) {
      localStorage.removeItem('chat-notifications');
      localStorage.removeItem('chat-notifications-count');
    }
    
    // 구독 해제
    if (notificationSubscription.value) {
      stomp.unsubscribe(notificationSubscription.value);
      notificationSubscription.value = null;
    }
  }
}, { immediate: true });

// 컴포넌트 언마운트 시 정리
onUnmounted(() => {
  clearAllNotifications();
  if (notificationSubscription.value) {
    stomp.unsubscribe(notificationSubscription.value);
  }
});

</script>

<style scoped>
@import '~/assets/chat/chat-notification.css';
</style>
