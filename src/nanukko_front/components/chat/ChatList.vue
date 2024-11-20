<!-- components/chat/ChatList.vue -->
<template>
  <div class="chat-list-section">
    <!-- 헤더 -->
    <div class="chat-header">
      <h1>채팅</h1>
      <div class="connection-status">
        <span class="status-dot" :class="connected ? 'connected' : ''"></span>
        <span class="status-text">{{ connected ? '실시간' : '연결 중...' }}</span>
      </div>
    </div>

    <!-- 채팅방 목록 -->
    <div class="chat-list-container">
      <!-- 로딩 상태 -->
      <div v-if="loading && !chatRooms?.length" class="loading-wrapper">
        <div class="loading-spinner"></div>
        <span>로딩 중...</span>
      </div>

      <!-- 채팅방 목록 -->
      <div v-else-if="chatRooms?.length > 0" class="chat-list">
        <div 
  v-for="room in chatRooms" 
  :key="room.chatRoomId"
  class="chat-room-item"
  :class="{ active: currentRoomId === room.chatRoomId }"
  @click="$emit('select-room', {
    productId: room.productId,
    chatRoomId: room.chatRoomId
  })"
>
          <!-- 프로필 이미지 -->
          <div class="profile-image">
            <img 
              v-if="room.sellerProfileImage"
              :src="room.sellerProfileImage"
              :alt="room.sellerName"
            />
            <div v-else class="profile-placeholder">
              <i class="fas fa-user"></i>
            </div>
          </div>

          <!-- 채팅 내용 -->
          <div class="chat-content">
            <!-- 사용자/상품 정보 -->
            <div class="user-product-info">
              <span class="seller-name">{{ room.sellerName || room.sellerId }}</span>
              <span class="product-name">{{ room.productName }}</span>
            </div>
            
            <!-- 메시지/시간/안읽음 표시 -->
            <div class="message-time-wrapper">
              <p class="last-message">{{ getLastMessage(room) }}</p>
              <!-- 시간과 안읽음 표시 -->
              <div class="time-status-container">
                <span class="message-time">{{ formatDetailedTime(room.updatedAt) }}</span>
                <div v-if="hasUnreadMessages(room)" class="unread-badge"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 채팅방 없음 -->
      <div v-else class="empty-state">
        <i class="fas fa-comments empty-icon"></i>
        <p>채팅방이 없습니다</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useStomp } from '~/composables/chat/useStomp'

// props 정의
const props = defineProps({
  chatRooms: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  currentRoomId: {
    type: String,
    default: null
  },
  userId: {
    type: String,
    required: true
  }
})

// emit 정의
const emit = defineEmits(['select-room'])

// STOMP 연결 상태
const { connected } = useStomp()

// 안읽은 메시지 체크
const hasUnreadMessages = (room) => {
  if (!room?.chatMessages || !props.userId) return false
  return room.chatMessages.some(msg => 
    !msg.isRead && msg.senderId !== props.userId
  )
}

// 마지막 메시지 표시
const getLastMessage = (room) => {
  const lastMessage = room.chatMessages?.[room.chatMessages.length - 1]
  if (!lastMessage) return '새로운 채팅방입니다'

  switch (lastMessage.type) {
    case 'LOCATION':
      return '📍 위치 공유'
    case 'IMAGE':
      return '🖼️ 사진'
    default:
      return lastMessage.chatMessage
  }
}

// 시간 포맷팅
const formatDetailedTime = (timestamp) => {
  if (!timestamp) return ''
  
  const matches = timestamp.match(/(\d{4})년 (\d{2})월 (\d{2})일/)
  if (!matches) return timestamp

  const [_, year, month, day] = matches
  const date = new Date(year, month - 1, day)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())

  if (date.toDateString() === today.toDateString()) {
    return new Intl.DateTimeFormat('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    }).format(date)
  } else {
    return `${month}/${day} ${new Intl.DateTimeFormat('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    }).format(date)}`
  }
}
</script>

<style scoped>
@import '~/assets/chat/chat-list.css';
</style>