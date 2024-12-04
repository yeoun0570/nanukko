<template>
  <div class="chat-container">
    <!-- 채팅방 헤더 -->
    <ChatRoomHeader 
      :product="headerData"
      :connected="connected"
      :room-id="roomId"
      :user-id="userId"
      @close-chat="handleCloseChat"
      @leave-room="handleLeaveRoom"
    />

    <!-- 메시지 영역 -->
    <main 
      ref="messageContainer" 
      class="chat-messages"
      @scroll.passive="handleScroll"
    >
      <!-- 로딩 상태 -->
      <div v-if="isLoading" class="chat-loading">
        <span>로딩 중...</span>
      </div>
      
      <!-- 빈 상태 -->
      <div v-if="!isLoading && !messages.length" class="chat-empty">
        대화를 시작해보세요!
      </div>

      <!-- 메시지 표시 영역 -->
      <template v-else>
        <template v-for="(group, date) in groupedMessages" :key="date">
          <!-- 날짜 구분선 -->
          <div class="date-divider">
            <div class="date-line"></div>
            <span class="date-text">{{ formatDate(date) }}</span>
            <div class="date-line"></div>
          </div>
          
          <!-- 메시지 목록 -->
          <template v-for="message in group" :key="message.chatMessageId">
            <div 
              class="message-wrapper"
              :class="{ 
                'message-sent': isSentByCurrentUser(message),
                'message-received': !isSentByCurrentUser(message)
              }"
            >
              <div class="message-bubble">
                <!-- 이미지 메시지 -->
                <div v-if="message.image" class="image-message">
                  <img 
                    :src="message.image"
                    alt="Shared image"
                    class="chat-image"
                    @click="openImageViewer(message.image)"
                    @error="handleImageError"
                  />
                </div>
                <!-- 텍스트 메시지 -->
                <p v-if="message.chatMessage" class="message-text">
                  {{ message.chatMessage }}
                </p>
              </div>
              
              <div class="message-info">
                <span class="message-time">
                  {{ formatMessageTime(message.createdAt) }}
                </span>
                <span v-if="isSentByCurrentUser(message)" class="message-status">
                  {{ message.isRead ? '읽음' : '안읽음' }}
                </span>
              </div>
            </div>
          </template>
        </template>
      </template>
    </main>

    <!-- 이미지 뷰어 모달 -->
    <div v-if="previewImage" class="image-preview-modal" @click="closePreview">
      <div class="modal-content">
        <img :src="previewImage" alt="Preview" />
        <button class="close-button" @click="closePreview">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>

    <!-- 메시지 입력 영역 -->
    <ChatMessageInput
      :user-id="userId"
      :room-id="roomId"
      :connected="connected"
      :is-loading="isLoading"
      @send-message="handleSendMessage"
      @send-image="handleSendImage"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useStomp } from '~/composables/chat/useStomp'
import { useFormatTime } from '~/composables/useFormatTime'
import ChatRoomHeader from '~/components/chat/ChatRoomHeader.vue'
import ChatMessageInput from './ChatMessageInput.vue'

// Props 정의
const props = defineProps({
  roomId: {
    type: [String, Number],
    required: true
  },
  userId: {
    type: String,
    required: true
  },
  currentRoom: {
    type: Object,
    required: true
  },
  initialMessages: {
    type: Array,
    default: () => []
  },
  connected: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close-chat'])

// 상태 관리
const messageContainer = ref(null)
const messages = ref([])
const isLoading = ref(false)
const currentPage = ref(0)
const hasMore = ref(true)
const previewImage = ref(null)

// Composables
const stomp = useStomp()
const { formatTime } = useFormatTime()

// 메시지 정렬 및 헤더 데이터 계산
const sortedMessages = computed(() => {
  return [...messages.value].sort((a, b) => 
    new Date(a.createdAt) - new Date(b.createdAt)
  )
})

const headerData = computed(() => ({
  productName: props.currentRoom?.productName,
  price: props.currentRoom?.price,
  thumbnailImage: props.currentRoom?.thumbnailImage,
  status: props.currentRoom?.status,
  condition: props.currentRoom?.condition
}))

// 메시지 송수신 처리
const handleNewMessage = async (message) => {
  try {
    let messageData = typeof message === 'string' ? JSON.parse(message) : 
                     message.body ? JSON.parse(message.body) : message;

    console.log('수신된 메시지 데이터:', messageData);

    // 읽음 상태 업데이트 메시지 처리
    if (messageData.messageIds) {
      messages.value = messages.value.map(msg => ({
        ...msg,
        isRead: messageData.messageIds.includes(msg.chatMessageId) ? true : msg.isRead
      }));
      return;
    }

    // 새 메시지 생성
    const newMessage = {
      chatMessageId: messageData.chatMessageId,
      sender: messageData.sender,
      chatRoom: messageData.chatRoom || props.roomId,
      chatMessage: messageData.chatMessage,
      createdAt: messageData.createdAt,
      isRead: messageData.isRead || false,
      type: messageData.type || 'CHAT',
      image: messageData.image
    };

    // 중복 체크
    const isDuplicate = messages.value.some(msg => 
      (msg.chatMessageId && msg.chatMessageId === newMessage.chatMessageId) || 
      (msg.chatMessage === newMessage.chatMessage && 
       msg.sender === newMessage.sender && 
       msg.createdAt === newMessage.createdAt)
    );

    if (!isDuplicate) {
      messages.value = [...messages.value, newMessage];

      // 상대방 메시지를 받았을 때 즉시 읽음 처리 요청
      if (!isSentByCurrentUser(newMessage)) {
        try {

          console.log('Sending read status update:', {
      chatRoomId: props.roomId,
      newMessage,
    });


          await stomp.sendMessage(`${props.roomId}/read-realtime`, {
            messageIds: [newMessage.chatMessageId],
            userId: props.userId,
            page: currentPage.value,
            size: 20
          });
        } catch (error) {
          console.error('읽음 처리 실패:', error);
        }
      }

      nextTick(() => {
        scrollToBottom(true);
      });
    }
  } catch (error) {
    console.error('메시지 처리 중 에러:', error);
  }
};

// 채팅방 초기화
const initializeChat = async () => {
  isLoading.value = true

  try {
    if (!stomp.connected.value) {
      await stomp.connectChat(props.userId)
    }

    messages.value = props.initialMessages || []

    // 읽지 않은 메시지 처리
    const unreadMessages = messages.value
      .filter(msg => !msg.isRead && !isSentByCurrentUser(msg))
      .map(msg => msg.chatMessageId)
      .filter(Boolean)

    if (unreadMessages.length > 0) {
      console.log('Marking initial messages as read:', unreadMessages);

      await stomp.sendMessage(`${props.roomId}/read-realtime`, {
        messageIds: unreadMessages,
        userId: props.userId,
        page: currentPage.value,
        size: 20,
        chatRoom: props.roomId //채팅방 아이디
      })
    }

    const subscription = await stomp.subscribeToChatRoom(
      `/queue/chat/${props.roomId}`,
      { onMessage: handleNewMessage }
    )

    await nextTick()
    scrollToBottom()
    return subscription

  } catch (error) {
    console.error('채팅 초기화 실패:', error)
    throw error
  } finally {
    isLoading.value = false
  }
}

// 유틸리티 함수들
const formatMessageTime = timestamp => timestamp ? formatTime(timestamp) : ''
const isSentByCurrentUser = (message) => {return message?.sender === props.userId;};
const scrollToBottom = (smooth = false) => {
  if (messageContainer.value) {
    nextTick(() => {
      messageContainer.value.scrollTo({
        top: messageContainer.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      })
    })
  }
}
const formatDate = dateStr => {
  const date = new Date(dateStr);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  // 날짜 형식을 현지화된 형식으로 변환
  if (dateStr === today.toLocaleDateString()) {
    return '오늘';
  }
  if (dateStr === yesterday.toLocaleDateString()) {
    return '어제';
  }

  // 년도가 같으면 월/일만 표시
  if (date.getFullYear() === today.getFullYear()) {
    return new Intl.DateTimeFormat('ko-KR', {
      month: 'long',
      day: 'numeric'
    }).format(date);
  }

  // 년도가 다르면 년/월/일 모두 표시
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  }).format(date);
};


/**핸들러 메소드들*************************************************** */
// 메시지 전송 처리
const handleSendMessage = async (messageData) => {
  try {
    const fullMessageData = {
      ...messageData,
      sender: props.userId,
      isRead: false,
      createdAt: new Date().toISOString()
    };
    
    console.log('메시지 전송:', fullMessageData);
    await stomp.sendMessage(`${props.roomId}`, fullMessageData);
  } catch (error) {
    console.error('메시지 전송 실패:', error);
    alert('메시지 전송에 실패했습니다. 다시 시도해주세요.');
  }
};

// 이미지 전송을 위한 핸들러도 추가
const handleSendImage = async (messageData) => {
  try {
    const fullMessageData = {
      ...messageData,
      sender: props.userId,
      isRead: false,
      type: 'IMAGE',
      createdAt: new Date().toISOString()
    };

    console.log('이미지 메시지 전송:', fullMessageData);
    await stomp.sendMessage(`${props.roomId}`, fullMessageData);
  } catch (error) {
    console.error('이미지 전송 실패:', error);
    alert('이미지 전송에 실패했습니다. 다시 시도해주세요.');
  }
};



// 스크롤 및 가시성 처리
const handleScroll = event => {
  const { scrollTop } = event.target
  if (scrollTop <= 150 && !isLoading.value && hasMore.value) {
    loadPreviousMessages()
  }
}

const handleVisibilityChange = async () => {
  if (!document.hidden && messages.value.length > 0) {
    const unreadMessages = messages.value
      .filter(msg => !msg.isRead && !isSentByCurrentUser(msg))
      .map(msg => msg.chatMessageId)
      .filter(Boolean)

    if (unreadMessages.length > 0) {
      await stomp.sendMessage(`${props.roomId}/read-realtime`, {
        messageIds: unreadMessages,
        userId: props.userId,
        page: currentPage.value,
        size: 20
      })
    }
  }
}

// 이미지 관련 처리
const openImageViewer = (imageUrl) => {
  previewImage.value = imageUrl
}

const closePreview = () => {
  previewImage.value = null
}

const handleImageError = (event) => {
  event.target.src = '/fallback-image.png'
}

// 메시지 그룹화
const groupedMessages = computed(() => {
  const groups = {}
  sortedMessages.value.forEach(message => {
    const date = new Date(message.createdAt).toLocaleDateString()
    if (!groups[date]) groups[date] = []
    groups[date].push(message)
  })
  return groups
})

// 채팅방 액션 핸들러
const handleCloseChat = () => {
  emit('close-chat')
}

const handleLeaveRoom = async ({ chatRoomId }) => {
  try {
    await stomp.sendMessage(`leave/${chatRoomId}`, {
      page: 0,
      size: 30
    })
    stomp.unsubscribe(`/queue/chat/${chatRoomId}`)
    emit('close-chat')
  } catch (error) {
    console.error('채팅방 나가기 실패:', error)
    alert('채팅방 나가기에 실패했습니다.')
  }
}

// 라이프사이클 훅
onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  const container = messageContainer.value
  if (container) {
    container.addEventListener('scroll', handleScroll)
  }
  initializeChat()
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  const container = messageContainer.value
  if (container) {
    container.removeEventListener('scroll', handleScroll)
  }
  stomp.unsubscribe(`/queue/chat/${props.roomId}`)
})

// 감시자
watch([() => props.roomId, () => props.connected], 
  async ([newRoomId, isConnected]) => {
    if (newRoomId && isConnected) {
      messages.value = []
      currentPage.value = 0
      hasMore.value = true
      try {
        await initializeChat()
      } catch (error) {
        console.error('채팅방 초기화 실패:', error)
      }
    }
  }, 
  { immediate: true }
)

// 메시지 변경 감시
watch(messages, () => {
  nextTick(() => {
    const unreadMessages = messages.value
      .filter(msg => !msg.isRead && !isSentByCurrentUser(msg))
      .map(msg => msg.chatMessageId)
      .filter(Boolean)

    if (unreadMessages.length > 0) {
      stomp.sendMessage(`${props.roomId}/read-realtime`, {
        messageIds: unreadMessages,
        userId: props.userId,
        page: currentPage.value,
        size: 20
      })
    }
  })
}, { deep: true })
</script>

<style scoped>
@import '~/assets/chat/chat-room.css';
</style>