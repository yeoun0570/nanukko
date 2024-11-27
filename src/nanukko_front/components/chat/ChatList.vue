<template>
  <div class="chat-list-section">
    <!-- 헤더 영역 -->
    <div class="chat-header">
      <h1>채팅</h1>
      <!-- 연결 상태 표시 -->
      <div class="connection-status">
        <span 
          class="status-dot" 
          :class="{ 'connected': connected }"
        ></span>
        <span class="status-text">
          {{ connected ? '실시간' : '연결 중...' }}
        </span>
      </div>
    </div>

    <!-- 채팅방 목록 컨테이너 -->
    <div class="chat-list-container">
      <!-- 로딩 상태 표시 -->
      <div v-if="loading" class="loading-wrapper">
        <div class="loading-spinner"></div>
        <span>채팅방 목록을 불러오는 중...</span>
      </div>

      <!-- 채팅방 목록 -->
      <div v-else-if="chatRooms?.length > 0" class="chat-list">
        <div 
          v-for="room in chatRooms" 
          :key="room.chatRoomId"
          class="chat-room-item"
          :class="{ 'active': currentRoomId === room.chatRoomId }"
          @click="handleRoomSelect(room)"
        >
          <!-- 프로필 영역 -->
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

          <!-- 채팅방 정보 -->
          <div class="chat-content">
            <div class="user-product-info">
              <span class="seller-name">
                {{ room.sellerName || room.sellerId }}
              </span>
              <span class="product-name">
                {{ room.productName }}
              </span>
            </div>
            
            <!-- 메시지 정보 -->
            <div class="message-time-wrapper">
              <p class="last-message">
                {{ getLastMessage(room) }}
              </p>
              <div class="time-status-container">
                <span class="message-time">
                  {{ formatMessageTime(room.updatedAt) }}
                </span>
                <div 
                  v-if="hasUnreadMessages(room)" 
                  class="unread-badge"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 채팅방이 없는 경우 -->
      <div v-else class="empty-state">
        <i class="fas fa-comments empty-icon"></i>
        <p>참여 중인 채팅방이 없습니다</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useFormatTime } from '~/composables/useFormatTime'

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
    type: [String, Number],
    default: null
  },
  userId: {
    type: String,
    required: true
  },
  connected: {
    type: Boolean,
    default: false
  }
})

// 이벤트 정의
const emit = defineEmits(['select-room'])

// 시간 포맷팅 유틸리티
const { formatTime } = useFormatTime()

// 채팅방 선택 핸들러
const handleRoomSelect = (room) => {
  emit('select-room', {
    productId: room.productId,
    chatRoomId: room.chatRoomId
  })
}

// 안읽은 메시지 여부 체크
const hasUnreadMessages = (room) => {
  if (!room?.messages?.length || !props.userId) return false
  return room.messages.some(msg => 
    !msg.isRead && msg.senderId !== props.userId
  )
}

// 마지막 메시지 표시
const getLastMessage = (room) => {
  const lastMessage = room.messages?.[room.messages.length - 1]
  if (!lastMessage) return '새로운 채팅방입니다'

  // 메시지 타입에 따른 표시
  switch (lastMessage.type) {
    case 'SYSTEM':
      return '시스템 메시지'
    case 'IMAGE':
      return '🖼️ 사진'
    case 'LOCATION':
      return '📍 위치 공유'
    default:
      return lastMessage.content
  }
}

// 메시지 시간 포맷팅
const formatMessageTime = (timestamp) => {
  if (!timestamp) return ''
  return formatTime(timestamp)
}
</script>

<style scoped>
@import '~/assets/chat/chat-list.css';
</style>