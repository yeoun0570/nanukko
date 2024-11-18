import { ref } from 'vue'
import { useStomp } from './useStomp'  // STOMP hooks import

export const useChatRooms = () => {
  const chatRooms = ref([])
  const currentRoom = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // STOMP hooks 사용
  const { subscribeToChatRoom } = useStomp()

  // 채팅방 목록 조회
  const fetchChatRooms = async (userId, page = 0, size = 30) => {
    loading.value = true
    try {
      const response = await fetch(`/api/chat/list?userId=${userId}&page=${page}&size=${size}`)
      if (!response.ok) {
        throw new Error(`HTTP 에러! 상태 코드: ${response.status}`)
      }
      const data = await response.json()
      chatRooms.value = data.content || []
    } catch (err) {
      console.error('[useChatRooms] 채팅방 목록 조회 실패:', err)
      error.value = err
    } finally {
      loading.value = false
    }
  }

  // 특정 채팅방 메시지 조회
  const fetchChatRoom = async (roomId, userId, page = 0, size = 50) => {
    try {
      // 일반 HTTP 요청으로 변경
      const response = await fetch(`/api/chat/list?chatRoomId=${roomId}&userId=${userId}&page=${page}&size=${size}`)
      if (!response.ok) {
        throw new Error(`HTTP 에러! 상태 코드: ${response.status}`)
      }
      const data = await response.json()
      currentRoom.value = data
      return data
    } catch (err) {
      console.error('[useChatRooms] 채팅방 조회 실패:', err)
      error.value = err
      throw err
    }
  }

  // 채팅방 생성 또는 입장
  // createOrEnterChatRoom 함수 수정
const createOrEnterChatRoom = async (productId, userId, page = 0, size = 50) => {
  try {
    const response = await fetch(`/api/chat/getChat`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        productId: productId,
        userId: userId,
        page: page.toString(),
        size: size.toString()
      })
    })
    
    if (!response.ok) {
      throw new Error(`HTTP 에러! 상태 코드: ${response.status}`)
    }

    const data = await response.json()
    currentRoom.value = data
    return data
  } catch (err) {
    console.error('[useChatRooms] 채팅방 생성/입장 실패:', err)
    error.value = err
    throw err
  }
}

  // WebSocket을 통한 채팅방 입장
  const enterChatRoom = async (roomId, userId, page = 0, size = 50) => {
    try {
      await subscribeToChatRoom(`/chat/enter/${roomId}`, {
        userId,
        page,
        size
      })
    } catch (err) {
      console.error('[useChatRooms] 채팅방 입장 실패:', err)
      error.value = err
      throw err
    }
  }

  // 채팅방 나가기
  const leaveChatRoom = async (roomId, userId, page = 0, size = 30) => {
    try {
      const response = await fetch(`/api/chat/leave?chatRoomId=${roomId}&userId=${userId}&page=${page}&size=${size}`, {
        method: 'POST'
      })
      
      if (!response.ok) {
        throw new Error(`HTTP 에러! 상태 코드: ${response.status}`)
      }

      const data = await response.json()
      return data
    } catch (err) {
      console.error('[useChatRooms] 채팅방 나가기 실패:', err)
      error.value = err
      throw err
    }
  }

  return {
    chatRooms,
    currentRoom,
    loading,
    error,
    fetchChatRooms,
    fetchChatRoom,
    createOrEnterChatRoom,
    enterChatRoom,
    leaveChatRoom
  }
}