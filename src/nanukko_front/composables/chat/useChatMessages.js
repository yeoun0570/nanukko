import { ref, computed } from 'vue'
import { useStomp } from './useStomp'

export const useChatMessages = (initialMessages = []) => {
  const messages = ref(initialMessages)
  const loading = ref(false)
  const currentPage = ref(0)
  const hasMore = ref(true)
  const subscription = ref(null)
  
  const { connected, subscribeToChatRoom, sendMessage } = useStomp()

  // 읽지 않은 메시지 수 계산
  const unreadCount = computed(() => 
    messages.value.filter(msg => !msg.isRead).length
  )

  // 채팅방 구독 설정
  const subscribeToRoom = async (roomId) => {
    if (subscription.value) {
      subscription.value.unsubscribe()
    }

    subscription.value = await subscribeToChatRoom(
      `/queue/${roomId}/message`,
      {
        onMessage: (message) => {
          if (message) {
            addMessage({
              ...message,
              senderId: message.senderId || message.sender
            })
          }
        }
      }
    )
  }

  // 채팅방 입장 및 이전 메시지 로드
 // 채팅방 입장 및 이전 메시지 로드
const enterRoom = async (chatRoomId, userId, page = 0, size = 50) => {
  loading.value = true
  try {
    // 연결 상태 확인
    if (!connected.value) {
      console.log('STOMP 연결 대기 중...')
      await new Promise(resolve => {
        const checkConnection = setInterval(() => {
          if (connected.value) {
            clearInterval(checkConnection)
            resolve()
          }
        }, 100)
      })
    }

    // HTTP로 이전 메시지 조회 (WebSocket 입장 메시지 전에 수행)
    const response = await fetch(
      `/api/chat/messages?roomId=${chatRoomId}&userId=${userId}&page=${page}&size=${size}`
    )
    const data = await response.json()

    if (page === 0) {
      messages.value = Array.isArray(data.content) ? data.content : []
    } else {
      messages.value = [...messages.value, ...(Array.isArray(data.content) ? data.content : [])]
    }

    // 페이지 정보 안전하게 처리
    currentPage.value = data.pageable?.pageNumber ?? page
    hasMore.value = data.last === false

    // WebSocket을 통한 입장 메시지 전송
    await sendMessage(`/app/chat/enter/${chatRoomId}`, {
      userId,
      page,
      size
    })

    // 채팅방 구독 설정
    await subscribeToRoom(chatRoomId)

    // 메시지 읽음 처리
    markAsRead(userId)
  } catch (error) {
    console.error('채팅방 입장 실패:', error)
    throw error
  } finally {
    loading.value = false
  }
}

  // 메시지 전송
  const sendChatMessage = async (roomId, messageData) => {
    try {
      await sendMessage(`/app/chat/${roomId}`, {
        ...messageData,
        type: 'CHAT'
      })
    } catch (error) {
      console.error('메시지 전송 실패:', error)
      throw error
    }
  }

  // 새 메시지 추가
  const addMessage = (message) => {
    messages.value = [...messages.value, message]
  }

  // 메시지 읽음 처리
  const markAsRead = (userId) => {
    messages.value = messages.value.map(msg => ({
      ...msg,
      isRead: msg.senderId !== userId || msg.isRead
    }))
  }

  // 채팅방 나가기
  const leaveRoom = async (roomId, userId) => {
    try {
      if (subscription.value) {
        subscription.value.unsubscribe()
        subscription.value = null
      }

      await sendMessage(`/app/chat/leave/${roomId}`, { userId })
      messages.value = []
      currentPage.value = 0
      hasMore.value = true
    } catch (error) {
      console.error('채팅방 나가기 실패:', error)
      throw error
    }
  }

  // 이전 메시지 로드
  const loadMoreMessages = async (roomId, userId) => {
    if (!hasMore.value || loading.value) return

    loading.value = true
    try {
      const nextPage = currentPage.value + 1
      await enterRoom(roomId, userId, nextPage)
    } finally {
      loading.value = false
    }
  }

  // Cleanup function
  const cleanup = () => {
    if (subscription.value) {
      subscription.value.unsubscribe()
      subscription.value = null
    }
    messages.value = []
    currentPage.value = 0
    hasMore.value = true
  }

  return {
    messages,
    loading,
    currentPage,
    hasMore,
    unreadCount,
    connected,
    enterRoom,
    sendChatMessage,
    addMessage,
    markAsRead,
    leaveRoom,
    loadMoreMessages,
    cleanup
  }
}