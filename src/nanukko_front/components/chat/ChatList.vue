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
        <!--동적 클래스 : 현재 선택된 채팅방 하이라이트를 통해 강조-->
        <div 
          v-for="room in chatRooms" 
          :key="room.chatRoomId"
          class="chat-room-item"
          :class="{ 'active': currentRoomId === room.chatRoomId }"
          @click="handleRoomSelect(room.chatRoomId)"
        >
          <!-- 프로필 영역 -->
          <div class="profile-image">
            <img 
              v-if="room.sellerProfile"
              :src="room.sellerProfile"
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
                {{ getOtherUserName(room) }}
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

// 상대방 이름 표시 메서드
const getOtherUserName = (room) => {
  if (!room || !props.userId) return '';
  
  // 현재 사용자가 판매자인 경우 구매자 이름을, 구매자인 경우 판매자 이름을 반환
  return props.userId === room.sellerId ? room.buyerName : room.sellerName;
}

// 이벤트 정의
const emit = defineEmits(['select-room'])

// 시간 포맷팅 유틸리티
const { formatTime } = useFormatTime()

// 채팅방 선택 핸들러
const handleRoomSelect = (chatRoomId) => {
  emit('select-room', chatRoomId)
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
  // chatMessages 배열이 있고 길이가 0보다 큰지 확인
  if (room.chatMessages && room.chatMessages.length > 0) {
    // 마지막 메시지 가져오기
    const lastMessage = room.chatMessages[room.chatMessages.length - 1]
    
    // 메시지 타입에 따른 표시
    switch (lastMessage.type) {
      case 'SYSTEM':
        return '시스템 메시지'
      case 'IMAGE':
        return '🖼️ 사진'
      case 'LOCATION':
        return '📍 위치 공유'
      default:
        return lastMessage.chatMessage || '메시지 없음'
    }
  }
  
  return '새로운 채팅방입니다'
}

// 메시지 시간 포맷팅
const formatMessageTime = (timestamp) => {
  if (!timestamp) return ''
  
  const date = new Date(timestamp)
  const now = new Date()
  const diffHours = Math.abs(now - date) / 36e5 // 시간 차이

  if (diffHours < 24) {
    // 24시간 이내면 시:분
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    })
  } else {
    // 24시간 이상이면 월/일
    return `${date.getMonth() + 1}/${date.getDate()}`
  }
}
</script>

<style scoped>
@import '~/assets/chat/chat-list.css';
</style>