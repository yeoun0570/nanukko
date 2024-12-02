<template>
  <div class="chat-container">
    <!-- 헤더 컴포넌트 -->
    <ChatRoomHeader 
      :product="headerData"
      :connected="connected"
    />

    <!-- 메인 메시지 영역 -->
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
<!-- 메시지 목록 -->
<template v-for="message in group" :key="message.chatMessageId || `${message.createdAt}-${message.sender}`">
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
        <p v-if="message.chatMessage" class="message-text">
          {{ message.chatMessage }}
          {{ message.image }}
          <span>이미지</span>
        </p>
      </div>
      <!-- 일반 텍스트 메시지 -->
      <p v-else-if="message.chatMessage" class="message-text">
        {{ message.chatMessage }}
      </p>
      <!-- 이미지 로드 실패 시 폴백 -->
      <p v-else-if="message.type === 'IMAGE'" class="message-text error">
        이미지를 불러올 수 없습니다
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

    <!-- 이미지 미리보기 모달 -->
    <div v-if="previewImage" class="image-preview-modal" @click="closePreview">
      <div class="modal-content" @click.stop>
        <img :src="previewImage" alt="Preview" />
        <button class="close-button" @click="closePreview">
          <i class="fas fa-times"></i>
        </button>
      </div>
    </div>

    <!-- 입력 컴포넌트 -->
    <ChatMessageInput
      :user-id="userId"
      :room-id="roomId"
      :connected="connected"
      :is-loading="isLoading"
      @send-message="handleSendMessage"
      @send-image="handleSendImage"
      @send-location="handleSendLocation"
      @show-preview="showPreview"
    />
  </div>
</template>



<script setup>
import ChatRoomHeader from '~/components/chat/ChatRoomHeader.vue'
import ChatMessageInput from './ChatMessageInput.vue'
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import { useFormatTime } from '~/composables/useFormatTime'
import { useStomp } from '~/composables/chat/useStomp'
import { useChatRooms } from '~/composables/chat/useChatRooms'

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
});

// 상태 관리
const messageContainer = ref(null);
const messages = ref([]);
const isLoading = ref(false);
const currentPage = ref(0);
const hasMore = ref(true);
const previewImage = ref(null);

// Composables
const { loadChatMessages } = useChatRooms();
const stomp = useStomp();
const { formatTime } = useFormatTime();

// Computed Properties
const sortedMessages = computed(() => {
  return [...messages.value].sort((a, b) => 
    new Date(a.createdAt) - new Date(b.createdAt)
  );
});

const headerData = computed(() => ({
  productName: props.currentRoom?.productName,
  price: props.currentRoom?.price,
  thumbnailImage: props.currentRoom?.thumbnailImage,
  status: props.currentRoom?.status,
  condition: props.currentRoom?.condition
}));

// 메시지 송수신 처리
const handleSendMessage = async (messageData) => {
  try {
    const fullMessageData = {
      ...messageData,
      sender: props.userId,
      isRead: false,
      createdAt: new Date().toISOString()
    };
    
    console.log('메시지 전송:', fullMessageData);
    await stomp.sendMessage(props.roomId, fullMessageData);
  } catch (error) {
    console.error('메시지 전송 실패:', error);
    alert('메시지 전송에 실패했습니다. 다시 시도해주세요.');
  }
};


// 메시지 수신 처리
// messages 배열 업데이트 로직만 수정
const handleNewMessage = async (message) => {
  try {
    let messageData = typeof message === 'string' ? JSON.parse(message) : 
                     message.body ? JSON.parse(message.body) : message;
    
    console.log('처리할 메시지 데이터:', messageData);

    // 읽음 상태 업데이트 메시지 처리
    if (messageData.messageIds) {
      console.log('읽음 상태 업데이트 요청:', messageData);
      // 메시지 읽음 상태 업데이트
      messages.value = messages.value.map(msg => ({
        ...msg,
        isRead: messageData.messageIds.includes(msg.chatMessageId) ? true : msg.isRead
      }));
      return;
    }

    // 새 메시지 처리
    const newMessage = {
      chatMessageId: messageData.chatMessageId || `temp-${Date.now()}`,
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

      // 상대방 메시지 즉시 읽음 처리
      if (!isSentByCurrentUser(newMessage)) {
        await markMessageAsRead(newMessage.chatMessageId);
        
        // 읽지 않은 모든 메시지 처리
        const unreadMessages = messages.value
          .filter(msg => !msg.isRead && !isSentByCurrentUser(msg))
          .map(msg => msg.chatMessageId)
          .filter(Boolean);

        if (unreadMessages.length > 0) {
          await stomp.sendMessage(`/app/chat/${props.roomId}/read-realtime`, {
            messageIds: unreadMessages,
            userId: props.userId,
            page: currentPage.value,
            size: 20
          });
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

// 메시지 변경 감지를 위한 watch 추가
watch(() => messages.value, (newMessages) => {
  console.log('메시지 배열 변경됨:', newMessages);
}, { deep: true });

// 메시지 읽음 처리 메소드 추가
const markMessageAsRead = async (messageId) => {
  try {
    await stomp.sendMessage(`/app/chat/${props.roomId}/mark-read`, {
      messageId: messageId,
      userId: props.userId
    });
  } catch (error) {
    console.error('메시지 읽음 처리 실패:', error);
  }
};

// 채팅 초기화
const initializeChat = async () => {
  isLoading.value = true;

  try {
    if (!stomp.connected.value) {
      await stomp.connectChat(props.userId);
    }

    // 초기 메시지 설정
    messages.value = props.initialMessages || [];

    // 안 읽은 메시지 읽음 처리
    const unreadMessages = messages.value
      .filter(msg => !msg.isRead && !isSentByCurrentUser(msg))
      .map(msg => msg.chatMessageId)
      .filter(Boolean);

    if (unreadMessages.length > 0) {
      await stomp.sendMessage(`/app/chat/${props.roomId}/read-realtime`, {
        messageIds: unreadMessages,
        userId: props.userId,
        page: currentPage.value,
        size: 20
      });
    }

    const subscription = await stomp.subscribeToChatRoom(props.roomId, {
      onMessage: handleNewMessage
    });

    await nextTick();
    scrollToBottom();
    return subscription;

  } catch (error) {
    console.error('채팅 초기화 실패:', error);
    throw error;
  } finally {
    isLoading.value = false;
  }
};

// 이미지 클릭 핸들러
const openImageViewer = (imageUrl) => {
  previewImage.value = imageUrl;
};

// 이미지 미리보기 닫기
const closePreview = () => {
  previewImage.value = null;
};

// 이미지 에러 핸들러
const handleImageError = (event) => {
  event.target.src = '/fallback-image.png'; // 폴백 이미지 경로
};

// 유틸리티 함수들
const formatMessageTime = timestamp => timestamp ? formatTime(timestamp) : '';
const isSentByCurrentUser = message => 
  message?.sender?.toString() === props.userId?.toString();
const scrollToBottom = (smooth = false) => {
  if (messageContainer.value) {
    nextTick(() => {
      messageContainer.value.scrollTo({
        top: messageContainer.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      });
    });
  }
};

// 이전 메시지 로드
const loadPreviousMessages = async () => {
  if (isLoading.value || !hasMore.value) return;

  try {
    isLoading.value = true;
    const nextPage = currentPage.value + 1;
    const response = await loadChatMessages(props.roomId, nextPage, 20);

    if (response?.content?.length > 0) {
      const oldHeight = messageContainer.value.scrollHeight;
      messages.value = [...response.content, ...messages.value];
      currentPage.value = nextPage;
      hasMore.value = !response.last;

      nextTick(() => {
        const newHeight = messageContainer.value.scrollHeight;
        messageContainer.value.scrollTop = newHeight - oldHeight;
      });
    } else {
      hasMore.value = false;
    }
  } catch (error) {
    console.error('메시지 로드 실패:', error);
  } finally {
    isLoading.value = false;
  }
};

// 스크롤 이벤트 핸들러
const handleScroll = event => {
  const { scrollTop } = event.target;
  if (scrollTop <= 150 && !isLoading.value && hasMore.value) {
    loadPreviousMessages();
  }
};

// 메시지 그룹화
const groupedMessages = computed(() => {
  const groups = {};
  sortedMessages.value.forEach(message => {
    const date = new Date(message.createdAt).toLocaleDateString();
    if (!groups[date]) groups[date] = [];
    groups[date].push(message);
  });
  return groups;
});

// 날짜 포맷팅
const formatDate = dateStr => {
  const date = new Date(dateStr);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (dateStr === today.toLocaleDateString()) return '오늘';
  if (dateStr === yesterday.toLocaleDateString()) return '어제';
  return dateStr;
};

// 이미지 미리보기 관련
const showPreview = (url) => {
  previewImage.value = url;
};


// Lifecycle Hooks
onMounted(() => {
  const container = messageContainer.value;
  if (container) {
    container.addEventListener('scroll', handleScroll);
    scrollToBottom();
  }
  initializeChat();
});

onUnmounted(() => {
  const container = messageContainer.value;
  if (container) {
    container.removeEventListener('scroll', handleScroll);
  }
  stomp.unsubscribe(`/queue/chat/${props.roomId}`);
});

// Watchers
watch([() => props.roomId, () => props.connected], 
  async ([newRoomId, isConnected]) => {
    if (newRoomId && isConnected) {
      messages.value = [];
      currentPage.value = 0;
      hasMore.value = true;
      try {
        await initializeChat();
      } catch (error) {
        console.error('채팅방 초기화 실패:', error);
      }
    }
  }, 
  { immediate: true }
);

// 컴포넌트 정리 로직 추가
onUnmounted(() => {
  console.log('채팅방 컴포넌트 정리');
  const unsubscribeDestination = `/queue/chat/${props.roomId}`;
  stomp.unsubscribe(unsubscribeDestination);
});

// 컴포넌트 watch 추가
watch(messages, (newMessages) => {
  console.log('메시지 배열 변경:', newMessages);
}, { deep: true });


</script>



<style scoped>
@import '~/assets/chat/chat-room.css';


</style>