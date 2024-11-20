<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useState } from 'nuxt/app'
import { useChatRooms } from '~/composables/chat/useChatRooms'
import { useStomp } from '~/composables/chat/useStomp'
import ChatList from '~/components/chat/ChatList.vue'
import ChatRoom from '~/components/chat/ChatRoom.vue'

const route = useRoute()
const userId = computed(() => route.query.userId)

// useState를 사용하여 서버와 클라이언트 간의 상태 동기화
const currentRoomId = useState('currentRoomId', () => null)
const selectedProductId = useState('selectedProductId', () => null)

// 채팅방 관련 상태와 함수들
const {
  chatRooms,
  currentRoom,
  loading,
  error,
  fetchChatRooms,
  fetchChatRoom,
  createOrEnterChatRoom
} = useChatRooms()

// STOMP 관련 상태와 함수들
const { connected, connectChat, subscribeToChatRoom } = useStomp()


// 채팅방 선택 처리

const activeChatRoom = useState('activeChatRoom', () => null)  // 현재 활성화된 채팅방 정보

// 채팅방 선택 처리
const handleRoomSelect = async (roomData) => {
  try {
    const userIdValue = route.query.userId
    console.log('채팅방 선택:', roomData, userIdValue)
    
    const { productId, chatRoomId } = roomData
    
    // 이전 채팅방 정보 초기화
    activeChatRoom.value = null
    currentRoomId.value = null
    selectedProductId.value = null
    
    // 잠시 대기하여 컴포넌트가 완전히 언마운트되도록 함
    await nextTick()
    
    // 새로운 채팅방 정보 설정
    selectedProductId.value = productId?.toString()
    currentRoomId.value = chatRoomId?.toString()

    // 채팅방 생성 또는 입장
    const chatRoomData = await createOrEnterChatRoom(productId, userIdValue)
    console.log('채팅방 데이터 수신:', chatRoomData)
    
    // 채팅방 정보 저장
    activeChatRoom.value = chatRoomData
    
  } catch (err) {
    console.error('채팅방 선택 실패:', err)
    currentRoomId.value = null
    selectedProductId.value = null
    activeChatRoom.value = null
  }
}

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  if (!userId.value) return

  try {
    console.log('[Chat] 초기화 시작:', userId.value)
    await fetchChatRooms(userId.value)
    
    // STOMP 연결
    await connectChat(userId.value)
    
    // 실시간 업데이트 구독
    subscribeToChatRoom('/topic/chat-updates', {
      onMessage: async (message) => {
        console.log('[Chat] 업데이트 수신:', message)
        await fetchChatRooms(userId.value)
        if (currentRoomId.value) {
          await fetchChatRoom(currentRoomId.value)
        }
      }
    })
  } catch (err) {
    console.error('[Chat] 초기화 실패:', err)
  }
})

// URL 변경 감지
watch(() => route.query.userId, async (newUserId) => {
  if (newUserId) {
    await fetchChatRooms(newUserId)
  }
}, { immediate: true })
</script>

<template>
  <div class="chat-layout">
    <ClientOnly>
      <!-- 왼쪽: 채팅 목록 컴포넌트 -->
      <ChatList
        v-if="userId"
        :chat-rooms="chatRooms"
        :loading="loading"
        :current-room-id="currentRoomId"
        :user-id="userId"
        :connected="connected"
        @select-room="handleRoomSelect"
      />
      
      <!-- 오른쪽: 채팅방 컴포넌트 -->
      <div class="chat-room-section">
        <ChatRoom
          v-if="currentRoomId && activeChatRoom"
          :key="currentRoomId"
          :room-id="currentRoomId"
          :user-id="userId"
          :product-id="selectedProductId"
          :current-room="activeChatRoom"
        />
        <div v-else class="empty-state">
          <p>채팅방을 선택해주세요</p>
        </div>
      </div>
    </ClientOnly>
  </div>
</template>

<style scoped>
@import '~/assets/chat/chat-index.css';
</style>