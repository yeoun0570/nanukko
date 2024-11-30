<template>
  <div class="chat-container">
    <header class="chat-header">
      <h3 class="chat-title">
        {{ currentRoom?.productName }}
        <span v-if="!connected" class="connection-status">(연결 중...)</span>
      </h3>
    </header>

    <main 
      ref="messageContainer" 
      class="chat-messages"
      @scroll.passive="handleScroll"
    >
      <div v-if="isLoading" class="chat-loading">
        <span>로딩 중...</span>
      </div>
      
      <div v-if="!isLoading && !messages.length" class="chat-empty">
        대화를 시작해보세요!
      </div>

      <template v-else>
        <template v-for="(group, date) in groupedMessages" :key="date">
        <!-- 날짜 구분선 -->
        <div class="date-divider">
          <div class="date-line"></div>
          <span class="date-text">{{ formatDate(date) }}</span>
          <div class="date-line"></div>
        </div>
        
        <!-- 해당 날짜의 메시지들 -->
        <div 
          v-for="message in group" 
          :key="message.chatMessageId"
          class="message-wrapper"
          :class="{ 
            'message-sent': isSentByCurrentUser(message),
            'message-received': !isSentByCurrentUser(message)
          }"
        >
            <div class="message-bubble">
              <p class="message-text">{{ message.chatMessage }}</p>
              <div class="message-info">
                <span class="message-time">
                  {{ formatMessageTime(message.createdAt) }}
                </span>
                <span v-if="isSentByCurrentUser(message)" class="message-status">
                  {{ message.isRead ? '읽음' : '안읽음' }}
                </span>
              </div>
            </div>
          </div>
        </template>
      </template>
    </main>

    <footer class="chat-input">
      <div class="input-wrapper">
        <input
          v-model="newMessage"
          type="text"
          class="message-input"
          placeholder="메시지를 입력하세요..."
          :disabled="!connected || isLoading"
          @keyup.enter="handleSendMessage"
        >
        <button
          class="send-button"
          :disabled="!connected || !newMessage.trim() || isLoading"
          @click="handleSendMessage"
        >
          전송
        </button>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useFormatTime } from '~/composables/useFormatTime'
import { useStomp } from '~/composables/chat/useStomp'
import { useAuth } from '~/composables/auth/useAuth'
import { useChatRooms } from '~/composables/chat/useChatRooms'

// 메시지 정렬을 위한 computed 속성 추가
const sortedMessages = computed(() => {
  return [...messages.value].sort((a, b) => {
    return new Date(a.createdAt) - new Date(b.createdAt);
  });
});

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


const messageContainer = ref(null)
const newMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const currentPage = ref(0);  // 추가
const hasMore = ref(true);   // 추가
// 스크롤 위치 관리를 위한 변수
const lastScrollHeight = ref(0)
const isUserScrolling = ref(false)


const {loadChatMessages} = useChatRooms()
const stomp = useStomp()
const { getToken } = useAuth()
const { formatTime } = useFormatTime()

// 메시지 전송 처리
const handleSendMessage = async () => {
  if (!newMessage.value.trim() || !stomp.connected.value || isLoading.value) return
  
  try {
    const messageData = {
      sender: props.userId,
      chatMessage: newMessage.value.trim(), // 공백 제거된 메시지
      type: 'CHAT',
      chatRoomId: props.roomId,
      createdAt: new Date().toISOString(),// ISO 형식 날짜
      isRead: false
    }
    
    // 먼저 UI에 메시지 추가
    messages.value.push(messageData);//메시지 목록에 추가
    scrollToBottom(true);//스크롤 이동
    
    // 서버로 메시지 전송
    await stomp.sendMessage(props.roomId, messageData);
    newMessage.value = '';//입력창 초기화
    
  } catch (error) {
    console.error('메시지 전송 실패:', error);
    // 전송 실패시 마지막 메시지 제거(메시지 배열의 마지막 요소(전송 실패 메시지)를 제외한 모든 요소를 반환)
    messages.value = messages.value.slice(0, -1);
  }
}

// 새 메시지 수신 처리
const handleNewMessage = (message) => {
  try {
    const receivedMsg = JSON.parse(message.body);
    console.log('새 메시지 수신:', receivedMsg);
    
    messages.value.push({
      ...receivedMsg,
      sender: receivedMsg.sender || receivedMsg.senderId
    });
    
    // 메시지가 추가된 후 자동 스크롤
    nextTick(() => {
      scrollToBottom(true);
    });
    
  } catch (error) {
    console.error('메시지 처리 실패:', error);
  }
};



// 채팅 초기화
const initializeChat = async () => {
  isLoading.value = true;

  try {
    if (!stomp.connected.value) {
      console.log('STOMP 연결 시도 중...');
      await stomp.connectChat(props.userId);
    }

    // 초기 메시지와 페이징 상태 설정
    messages.value = [...props.initialMessages].reverse();  // 순서 뒤집기
    currentPage.value = 0;  // 초기 페이지는 0
    //hasMore.value = props.currentRoom?.hasMore || false;
    hasMore.value = true; // 명시적으로 true로 설정

    // 채팅방 구독 설정
    const subscription = await stomp.subscribeToChatRoom(props.roomId, {
      onMessage: (payload) => {
        if (payload.sender !== props.userId) {
          messages.value.push({
            ...payload,
            sender: payload.sender || payload.senderId,
            isRead: false
          });
          scrollToBottom(true);
        }
      }
    });

    scrollToBottom();
    return subscription;

  } catch (error) {
    console.error('채팅 초기화 에러:', error);
    throw error;
  } finally {
    isLoading.value = false;
  }
};

// 유틸리티 함수들
const formatMessageTime = (timestamp) => {
  if (!timestamp) return ''
  return formatTime(timestamp)
}

const isSentByCurrentUser = (message) => {
  if (!message?.sender || !props.userId) return false
  return message.sender.toString() === props.userId.toString()
}

const scrollToBottom = (smooth = false) => {
  if (messageContainer.value) {//DOM 요소에 접근
    nextTick(() => {//DOM 업데이트가 완료된 후 스크롤 동작 실행
      messageContainer.value.scrollTo({
        top: messageContainer.value.scrollHeight,// 컨테이너의 총 스크롤 가능한 높이를 가져옴
        behavior: smooth ? 'smooth' : 'auto'// 스크롤 동작 방식
      })
    })
  }
}


// 이전 메시지 로드 후 스크롤 위치 유지
const loadPreviousMessages = async () => {
  if (isLoading.value || !hasMore.value) {
    console.log('Loading prevented:', { isLoading: isLoading.value, hasMore: hasMore.value });
    return;
  }

  try {
    isLoading.value = true;
    const nextPage = currentPage.value + 1;
    console.log('Attempting to load page:', nextPage);

    const response = await loadChatMessages(props.roomId, nextPage, 20);
    console.log('Received response:', response); // 응답 데이터 확인

    if (response?.content?.length > 0) {
      const oldHeight = messageContainer.value.scrollHeight;
      
      // 새 메시지를 기존 메시지 앞에 추가
      messages.value = [...response.content, ...messages.value];
      currentPage.value = nextPage;
      hasMore.value = !response.last;

      // 스크롤 위치 유지
      nextTick(() => {
        const newHeight = messageContainer.value.scrollHeight;
        const scrollOffset = newHeight - oldHeight;
        messageContainer.value.scrollTop = scrollOffset;
      });

      console.log('Messages loaded successfully', {
        newMessagesCount: response.content.length,
        totalMessages: messages.value.length,
        hasMore: hasMore.value
      });
    } else {
      hasMore.value = false;
      console.log('No more messages to load');
    }
  } catch (error) {
    console.error('Failed to load messages:', error);
  } finally {
    isLoading.value = false;
  }
};
///////////////////////////////////////////////////////////
// 스크롤 이벤트 핸들러 수정
const handleScroll = (event) => {
  const container = event.target;
  const scrollTop = container.scrollTop;
  const threshold = 150;

  // 스크롤이 상단에 충분히 가까워졌을 때
  if (scrollTop <= threshold && !isLoading.value && hasMore.value) {
    console.log('Triggering previous messages load');
    loadPreviousMessages();
  }
};



// 컴포넌트 마운트 시 이벤트 리스너 등록
onMounted(() => {
  const container = messageContainer.value
  if (container) {
    console.log('Adding scroll event listener') // 이벤트 리스너 등록 확인
    container.addEventListener('scroll', handleScroll)
    scrollToBottom()
  }
})

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  const container = messageContainer.value
  if (container) {
    console.log('Removing scroll event listener') // 이벤트 리스너 제거 확인
    container.removeEventListener('scroll', handleScroll)
  }
})




// 메시지를 날짜별로 그룹화하는 computed 속성 추가
const groupedMessages = computed(() => {
  const groups = {};
  sortedMessages.value.forEach(message => {
    const date = new Date(message.createdAt).toLocaleDateString();
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(message);
  });
  return groups;
});


// 날짜 포맷팅 함수
const formatDate = (dateStr) => {
  const date = new Date(dateStr);
  const today = new Date();
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);

  if (dateStr === today.toLocaleDateString()) {
    return '오늘';
  } else if (dateStr === yesterday.toLocaleDateString()) {
    return '어제';
  }
  return dateStr;
};

///////////////////////////////////////////////////////////////

// Lifecycle hooks
onMounted(() => {
  initializeChat()
})

onUnmounted(() => {
  stomp.unsubscribe(`/queue/chat/${props.roomId}`)
})

// Watchers
watch([() => props.roomId, () => props.connected], async ([newRoomId, isConnected]) => {
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
}, { immediate: true });

</script>

<style scoped>
@import '~/assets/chat/chat-room.css';


/* 기존 스타일에 추가 */
.date-divider {
  display: flex;
  align-items: center;
  margin: 1rem 0;
  padding: 0 1rem;
}

.date-line {
  flex: 1;
  height: 1px;
  background-color: #e0e0e0;
}

.date-text {
  margin: 0 1rem;
  padding: 0.25rem 0.75rem;
  font-size: 0.875rem;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 1rem;
}

.chat-messages {
  height: calc(100vh - 160px);
  overflow-y: auto;
  padding: 1rem;
}

.chat-messages {
  height: calc(100vh - 160px);
  overflow-y: auto; /* 스크롤 가능하도록 설정 */
  padding: 1rem;
  -webkit-overflow-scrolling: touch; /* iOS 스크롤 개선 */
}

/* 스크롤바 스타일링 (선택사항) */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-track {
  background-color: transparent;
}
</style>