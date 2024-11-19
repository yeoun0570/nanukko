// useChatRooms.js
import { ref } from 'vue'
import { useStomp } from './useStomp'
import { useRuntimeConfig } from 'nuxt/app'  // Nuxt의 런타임 설정 사용

export const useChatRooms = () => {
  const chatRooms = ref([])
  const currentRoom = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase || 'http://localhost:8080'  // 기본값 설정

  // STOMP hooks 사용
  const { subscribeToChatRoom } = useStomp()

  // 채팅방 목록 조회
  const fetchChatRooms = async (userId, page = 0, size = 30) => {
    loading.value = true
    error.value = null
    
    try {
      const response = await fetch(
        `${baseURL}/api/chat/list?userId=${userId}&page=${page}&size=${size}`,
        {
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include'  // 쿠키 포함
        }
      )
      
      if (!response.ok) {
        const errorData = await response.text()
        console.error('[Chat] 서버 응답 에러:', response.status, errorData)
        throw new Error(`HTTP 에러! 상태 코드: ${response.status}`)
      }
      
      const data = await response.json()
      chatRooms.value = data.content || []
      return data
    } catch (err) {
      console.error('[useChatRooms] 채팅방 목록 조회 실패:', err)
      error.value = err
      throw err
    } finally {
      loading.value = false
    }
  }

  // 특정 채팅방 메시지 조회
  const fetchChatRoom = async (roomId, userId, page = 0, size = 50) => {
    try {
      const response = await fetch(
        `${baseURL}/api/chat/list?chatRoomId=${roomId}&userId=${userId}&page=${page}&size=${size}`,
        {
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include'
        }
      )
      
      if (!response.ok) {
        const errorData = await response.text()
        console.error('[Chat] 채팅방 조회 실패:', response.status, errorData)
        throw new Error(`채팅방 조회 실패: ${response.status}`)
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
  const createOrEnterChatRoom = async (productId, userId, page = 0, size = 50) => {
    try {
      const response = await fetch(`${baseURL}/api/chat/getChat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          productId: productId,
          userId: userId,
          page: page.toString(),
          size: size.toString()
        }),
        credentials: 'include'
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
      const response = await fetch(
        `${baseURL}/api/chat/leave?chatRoomId=${roomId}&userId=${userId}&page=${page}&size=${size}`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          credentials: 'include'
        }
      )
      
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