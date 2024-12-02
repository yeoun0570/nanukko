<template>
  <div class="relative">
    <!-- 채팅 아이콘과 알림 카운트 -->
    <button 
      class="chat-button"
      @click="handleChatClick"
    >
      <i class="fas fa-comments"></i>
      <span 
        v-if="unreadCount > 0" 
        class="notification-badge"
      >
        {{ unreadCount }}
      </span>
    </button>

    <!-- 알림 드롭다운 -->
    <div 
      v-if="showNotifications && notifications.length > 0" 
      class="notifications-dropdown"
    >
      <div class="notifications-header">
        새 메시지
        <button 
          @click="clearNotifications" 
          class="clear-button"
        >
          모두 지우기
        </button>
      </div>
      <div class="notifications-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item"
          @click="goToChatRoom(notification.chatRoomId)"
        >
          <div class="notification-sender">{{ notification.senderName }}</div>
          <div class="notification-message">{{ notification.message }}</div>
          <div class="notification-time">
            {{ formatTime(notification.createdAt) }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useStomp } from '~/composables/chat/useStomp'
import { useAuth } from '~/composables/auth/useAuth'
import { useRouter } from 'vue-router'
import { useFormatTime } from '~/composables/useFormatTime'

const router = useRouter()
const stomp = useStomp()
const { userId, isAuthenticated } = useAuth()
const { formatTime } = useFormatTime()

// 상태 관리
const unreadCount = ref(0)
const notifications = ref([])
const showNotifications = ref(false)

// STOMP 구독 정보 저장
let notificationSubscription = null

// 알림 초기화 및 STOMP 연결
const initializeNotifications = async () => {
  if (!isAuthenticated || !userId) return

  try {
    // STOMP 연결이 없으면 연결
    if (!stomp.connected.value) {
      await stomp.connectChat(userId)
    }

    // 개인 알림 채널 구독
    // 경로 수정: /user/{userId}/queue/notifications
    const destination = `/user/${userId}/queue/notifications`
    console.log('알림 구독 경로:', destination)
    
    notificationSubscription = await stomp.subscribeToChatRoom(
      destination,
      {
        onMessage: handleNewMessage
      }
    )
  } catch (error) {
    console.error('알림 초기화 실패:', error)
  }
}

// 새 메시지 처리
const handleNewMessage = (message) => {
  console.log('새 메시지 수신:', message)
  try {
    const messageData = typeof message === 'string' ? 
      JSON.parse(message) : 
      message.body ? JSON.parse(message.body) : message

    // 이미 나간 채팅방에서 온 메시지인 경우도 포함
    if (messageData.type === 'CHAT' || messageData.hasNewMessageAfterLeave) {
      unreadCount.value++
      notifications.value.unshift({
        id: Date.now(),
        chatRoomId: messageData.chatRoomId,
        senderName: messageData.senderName || '알 수 없음',
        message: messageData.chatMessage || '새 메시지가 도착했습니다.',
        createdAt: messageData.createdAt || new Date().toISOString()
      })
    }
  } catch (error) {
    console.error('메시지 처리 중 오류:', error)
  }
}

// 채팅방으로 이동
const goToChatRoom = async (chatRoomId) => {
  try {
    await router.push(`/chat?roomId=${chatRoomId}`)
    showNotifications.value = false
    
    // 해당 알림 삭제 및 카운트 감소
    notifications.value = notifications.value.filter(
      notif => notif.chatRoomId !== chatRoomId
    )
    if (unreadCount.value > 0) {
      unreadCount.value--
    }
  } catch (error) {
    console.error('채팅방 이동 실패:', error)
  }
}

// 채팅 버튼 클릭 처리
const handleChatClick = () => {
  if (!isAuthenticated) {
    alert('채팅을 이용하려면 로그인이 필요합니다.')
    router.push('/auth/login')
    return
  }
  showNotifications.value = !showNotifications.value
}

// 모든 알림 지우기
const clearNotifications = () => {
  notifications.value = []
  unreadCount.value = 0
  showNotifications.value = false
}

// 컴포넌트 마운트/언마운트 시 처리
onMounted(() => {
  if (isAuthenticated) {
    initializeNotifications()
  }
})

// cleanup
onUnmounted(() => {
  if (notificationSubscription) {
    stomp.unsubscribe(notificationSubscription)
    notificationSubscription = null
  }
})
</script>

<style scoped>
.chat-button {
  position: relative;
  padding: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #ef4444;
  color: white;
  font-size: 0.75rem;
  padding: 2px 6px;
  border-radius: 9999px;
  min-width: 18px;
}

.notifications-dropdown {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 0.5rem;
  width: 300px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  z-index: 50;
}

.notifications-header {
  padding: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 500;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.clear-button {
  font-size: 0.75rem;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
}

.clear-button:hover {
  color: #4b5563;
}

.notifications-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 0.75rem;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #f9fafb;
}

.notification-sender {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.notification-message {
  font-size: 0.875rem;
  color: #4b5563;
  margin-bottom: 0.25rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-time {
  font-size: 0.75rem;
  color: #6b7280;
}
</style>