<template>
  <div class="chat-layout">
    <ClientOnly>
      <template v-if="isAuthenticted">
        <ChatList
          :chat-rooms="chatRooms"
          :loading="loading"
          :current-room-id="currentRoomId"
          :user-id="userId"
          :connected="stompState.connected"
          @select-room="handleRoomSelect"
        />
        
        <section class="chat-room-section">
          <ChatRoom
            v-if="currentRoomId && activeChatRoom && stompState.connected"
            :key="currentRoomId"
            :room-id="currentRoomId"
            :user-id="userId"
            :current-room="activeChatRoom"
            :connected="stompState.connected"
          />
          <div v-else class="empty-state">
            <p>채팅방을 선택해주세요</p>
          </div>
        </section>
      </template>

      <div v-else class="auth-required">
        <p>채팅을 이용하려면 로그인이 필요합니다.</p>
        <NuxtLink to="/auth/login" class="login-link">로그인하러 가기</NuxtLink>
      </div>
    </ClientOnly>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useChatRooms } from '~/composables/chat/useChatRooms'
import { useStomp } from '~/composables/chat/useStomp'
import { useAuth } from '~/composables/auth/useAuth'

// 상태 관리
const currentRoomId = ref(null)
const activeChatRoom = ref(null)
const {isAuthenticted, userId} = useAuth()
const stompState = useStomp()

const { 
  chatRooms, 
  loading, 
  error,
  fetchChatRooms,
  createOrEnterChatRoom 
} = useChatRooms()

// 초기화 함수
const initializeChat = async () => {
  console.log('채팅 초기화 중...로그인 상태', isAuthenticted)
  
  if (!isAuthenticted) {
    console.log('Not authenticated')
    return
  }

  try {
    // STOMP 연결
    await stompState.connectChat(userId)
    
    // 채팅방 목록 조회
    await fetchChatRooms()
  } catch (err) {
    console.error('Chat initialization failed:', err)
  }
}

// 채팅방 선택 처리
const handleRoomSelect = async (roomData) => {
  try {
    // 현재 채팅방 상태 초기화
    activeChatRoom.value = null
    currentRoomId.value = null
    
    console.log('채팅방 선택:', roomData)
    
    const { productId, chatRoomId } = roomData
    
    // 채팅방 생성 또는 입장
    const chatRoomData = await createOrEnterChatRoom(productId)
    console.log('채팅방 생성/입장 결과:', chatRoomData)
    
    if (chatRoomData) {
      activeChatRoom.value = chatRoomData
      currentRoomId.value = chatRoomData.chatRoomId?.toString()
    }
  } catch (err) {
    console.error('채팅방 선택 실패:', err)
    currentRoomId.value = null
    activeChatRoom.value = null
  }
}

// 라이프사이클 훅
onMounted(() => {
  console.log('Chat component mounted')
  initializeChat()
})

// 상태 변화 감지
watch(() => isAuthenticted, (newValue) => {
  console.log('Auth state changed:', newValue)
  if (newValue) {
    initializeChat()
  }
})

watch(() => stompState.connected, (isConnected) => {
  console.log('STOMP connection state changed:', isConnected)
  if (isConnected && isAuthenticted) {
    fetchChatRooms()
  }
})

// 에러 감지
watch(error, (newError) => {
  if (newError) {
    console.error('Chat error:', newError)
  }
})
</script>

<style scoped>
@import '~/assets/chat/chat-index.css';

</style>