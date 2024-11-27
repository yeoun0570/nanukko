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
      @scroll="handleScroll"
    >
      <div v-if="isLoading" class="chat-loading">
        <span>로딩 중...</span>
      </div>
      
      <div v-if="!isLoading && !messages.length" class="chat-empty">
        대화를 시작해보세요!
      </div>

      <template v-else>
        <div 
          v-for="message in messages" 
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
  connected: {
    type: Boolean,
    required: true
  }
})

// Refs & Composables
const messageContainer = ref(null)
const newMessage = ref('')
const messages = ref([])
const isLoading = ref(false)

// STOMP 관련 함수들을 구조 분해 할당으로 가져오기
const stomp = useStomp()
const { getToken } = useAuth()
const { formatTime } = useFormatTime()

// 메시지 전송 처리
const handleSendMessage = async () => {
  if (!newMessage.value.trim() || !stomp.connected.value || isLoading.value) return
  
  try {
    const messageData = {
      sender: props.userId,
      chatMessage: newMessage.value.trim(),
      type: 'CHAT',
      chatRoomId: props.roomId,
      createdAt: new Date().toISOString(),
      isRead: false
    }
    
    // 먼저 UI에 메시지 추가
    messages.value.push(messageData);
    scrollToBottom(true);
    
    // 메시지 전송
    await stomp.sendMessage(props.roomId, messageData);
    newMessage.value = '';
    
  } catch (error) {
    console.error('메시지 전송 실패:', error);
    // 전송 실패시 마지막 메시지 제거
    messages.value = messages.value.slice(0, -1);
  }
}

// 새 메시지 수신 처리
const handleNewMessage = (message) => {
  try {
    const receivedMsg = JSON.parse(message.body)
    console.log('새 메시지 수신:', receivedMsg)
    
    messages.value.push({
      ...receivedMsg,
      sender: receivedMsg.sender || receivedMsg.senderId
    })
    
    if (receivedMsg.sender !== props.userId) {
      stomp.sendMessage(`/app/chat/${props.roomId}/read`, {
        chatMessageId: receivedMsg.chatMessageId,
        sender: props.userId,
        chatRoom: props.roomId,
        type: 'SYSTEM'
      })
    }
    
    scrollToBottom(true)
  } catch (error) {
    console.error('메시지 처리 실패:', error)
  }
}


const connectChat = async (userId) => {
  // 이미 연결된 경우
  if (client.value?.active) {
    console.log('Already connected');
    return Promise.resolve();
  }

  // 연결 시도 중인 경우
  if (connectionPromise.value) {
    console.log('Connection in progress, waiting...');
    return connectionPromise.value;
  }

  // 새로운 연결 시도
  connectionPromise.value = new Promise((resolve, reject) => {
    try {
      console.log('Initializing STOMP connection...');
      const socket = new SockJS('http://localhost:8080/ws-stomp');

      const maxAttempts = 5;
      let attempts = 0;

      const connectWithRetry = () => {
        if (attempts >= maxAttempts) {
          reject(new Error('Unable to establish STOMP connection'));
          return;
        }

        client.value = new Client({
          // 클라이언트 설정
        });

        client.value.activate();

        client.value.onConnect = () => {
          console.log('STOMP connection successful!');
          connected.value = true;
          resolve();
        };

        client.value.onStompError = (frame) => {
          console.error('STOMP error:', frame);
          connected.value = false;
          attempts++;
          setTimeout(connectWithRetry, 2000);
        };

        client.value.onWebSocketClose = () => {
          console.log('WebSocket closed');
          connected.value = false;
          attempts++;
          setTimeout(connectWithRetry, 2000);
        };
      };

      connectWithRetry();
    } catch (error) {
      console.error('Connection error:', error);
      connected.value = false;
      reject(error);
    }
  });

  try {
    await connectionPromise.value;
    return true;
  } catch (error) {
    connectionPromise.value = null;
    throw error;
  }
};



// 채팅방 초기화
const initializeChat = async () => {
  isLoading.value = true;

  try {
    // STOMP 연결 확인 및 설정
    if (!stomp.connected.value) {
      console.log('STOMP 연결 시도 중...');
      await stomp.connectChat(props.userId);
    }

    // 기존 메시지 로드
    messages.value = props.currentRoom?.chatMessages?.map(msg => ({
      ...msg,
      sender: msg.sender || msg.senderId
    })) || [];

    // 채팅방 구독
    const subscription = await stomp.subscribeToChatRoom(props.roomId, {
      onMessage: (payload) => {
        console.log('New message received:', payload);
        // 자신이 보낸 메시지는 이미 UI에 표시되어 있으므로 건너뛰기
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

    // 입장 메시지 전송
    await stomp.sendMessage(props.roomId, {
      type: 'CHAT',
      chatRoomId: props.roomId,
      userId: props.userId,
      timestamp: new Date().toISOString()
    });

    scrollToBottom();
    
    return subscription;
  } catch (error) {
    console.error('Chat initialization error:', error);
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
  if (messageContainer.value) {
    nextTick(() => {
      messageContainer.value.scrollTo({
        top: messageContainer.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto'
      })
    })
  }
}

const handleScroll = () => {
  const container = messageContainer.value
  if (!container) return

  const scrollTop = container.scrollTop
  const threshold = 100

  if (scrollTop <= threshold) {
    loadPreviousMessages()
  }
}

// 이전 메시지 로드
const loadPreviousMessages = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true
    const token = getToken()
    if (!token) {
      throw new Error('인증 토큰이 없습니다')
    }

    const response = await fetch(
      `/api/chat/messages?roomId=${props.roomId}&userId=${props.userId}&page=${Math.ceil(messages.value.length / 20)}&size=20`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()
    if (data.content?.length > 0) {
      messages.value = [...data.content.reverse(), ...messages.value]
    }
  } catch (error) {
    console.error('이전 메시지 로드 실패:', error)
  } finally {
    isLoading.value = false
  }
}

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
    messages.value = []
    try {
      await initializeChat()
    } catch (error) {
      console.error('채팅방 초기화 실패:', error)
    }
  }
}, { immediate: true })

</script>

<style scoped>
@import '~/assets/chat/chat-room.css';
</style>