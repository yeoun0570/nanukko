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
          v-for="(message, index) in messages" 
          :key="`${message.chatMessageId}-${index}`"
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
//import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { useStomp } from '~/composables/chat/useStomp'
import { useFormatTime } from '~/composables/useFormatTime'

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
  }
})

// Refs
const messageContainer = ref(null)
const newMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const currentSubscription = ref(null)
const retryCount = ref(0)
const maxRetries = 3

// Composables
const { connected, connectChat, subscribeToChatRoom, sendMessage, cleanup, unsubscribe } = useStomp()
const { formatTime } = useFormatTime()

// Methods
const formatMessageTime = (timestamp) => {
  if (!timestamp) return ''
  return formatTime(timestamp)
}

const isSentByCurrentUser = (message) => {
  if (!message || !message.sender || !props.userId) return false
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

const loadPreviousMessages = async () => {
  if (isLoading.value) return

  try {
    isLoading.value = true
    const response = await fetch(
      `/api/chat/messages?roomId=${props.roomId}&userId=${props.userId}&page=${Math.ceil(messages.value.length / 20)}&size=20`
    )
    const data = await response.json()
    
    if (data.content && data.content.length > 0) {
      const newMessages = data.content.reverse()
      messages.value = [...newMessages, ...messages.value]
    }
  } catch (error) {
    console.error('이전 메시지 로드 실패:', error)
  } finally {
    isLoading.value = false
  }
}

const initializeChat = async () => {
  isLoading.value = true
  try {
    console.log('채팅방 초기화 시작:', props.roomId)
    
    if (!connected.value) {
      console.log('STOMP 연결 시도')
      await connectChat(props.userId)
    }

    // 메시지 초기화
    messages.value = props.currentRoom?.chatMessages?.map(msg => ({
      ...msg,
      sender: msg.sender || msg.senderId
    })) || []

    // 이전 구독 해제
    if (currentSubscription.value) {
      unsubscribe(`/topic/chat/${currentSubscription.value}`)
    }

    // 새로운 구독 설정
    if (connected.value && props.roomId) {
      const topicPath = `/topic/chat/${props.roomId}`
      console.log('채팅방 구독:', topicPath)
      
      currentSubscription.value = props.roomId
      
      await subscribeToChatRoom(topicPath, {
        onMessage: (message) => {
          try {
            console.log('새 메시지 수신:', message)
            if (message && message.body) {
              const parsedMessage = JSON.parse(message.body)
              messages.value = [...messages.value, {
                ...parsedMessage,
                sender: parsedMessage.sender || parsedMessage.senderId
              }]
              scrollToBottom(true)
            }
          } catch (error) {
            console.error('메시지 처리 실패:', error)
          }
        }
      })

      await sendMessage(`/app/chat/enter/${props.roomId}`, {
        userId: props.userId,
        chatRoomId: props.roomId
      })
    }

    await nextTick()
    scrollToBottom()
  } catch (error) {
    console.error('채팅방 초기화 실패:', error)
    if (retryCount.value < maxRetries) {
      retryCount.value++
      console.log(`재시도 (${retryCount.value}/${maxRetries})...`)
      await new Promise(resolve => setTimeout(resolve, 2000))
      await initializeChat()
    }
  } finally {
    isLoading.value = false
  }
}

const handleSendMessage = async () => {
  if (!newMessage.value.trim() || !connected.value || isLoading.value) return
  
  try {
    const message = {
      chatRoomId: Number(props.roomId),
      sender: props.userId,
      chatMessage: newMessage.value.trim(),
      type: 'CHAT',
      createdAt: new Date().toISOString()
    }
    
    await sendMessage(`/app/chat/${props.roomId}`, message)
    messages.value = [...messages.value, message]
    
    newMessage.value = ''
    await nextTick()
    scrollToBottom(true)
  } catch (error) {
    console.error('메시지 전송 실패:', error)
  }
}

// Watchers
watch(() => props.roomId, async (newRoomId, oldRoomId) => {
  if (newRoomId !== oldRoomId) {
    console.log('채팅방 변경:', newRoomId)
    messages.value = []
    await initializeChat()
  }
}, { immediate: true })

// Lifecycle hooks
onMounted(async () => {
  console.log('채팅방 컴포넌트 마운트')
  await initializeChat()
})

onUnmounted(() => {
  console.log('채팅방 컴포넌트 언마운트')
  if (currentSubscription.value) {
    unsubscribe(`/topic/chat/${currentSubscription.value}`)
  }
  cleanup()
  messages.value = []
})

watch(connected, async (isConnected) => {
  if (isConnected) {
    console.log('STOMP 연결됨, 채팅방 재초기화')
    await initializeChat()
  }
})
</script>

<style scoped>
@import '~/assets/chat/chat-room.css';
</style>