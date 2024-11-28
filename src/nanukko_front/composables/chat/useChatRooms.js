import { ref, watch } from 'vue'
import { useStomp } from './useStomp'
import { useAuth } from '../auth/useAuth'
import { useApi } from '../useApi'

export const useChatRooms = () => {
  const chatRooms = ref([])
  const currentRoom = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const currentSubscription = ref(null)

  const { getToken } = useAuth()
  const stomp = useStomp()
  const api = useApi()

  const fetchChatRooms = async () => {
    loading.value = true
    try {
      const response = await api.get('/chat/list')
      console.log('채팅방 목록 응답:', response)
      chatRooms.value = response.content || []
    } catch (err) {
      console.error('[useChatRooms] 채팅방 목록 조회 실패:', err)
      error.value = err
    } finally {
      loading.value = false
    }
  }

  // 채팅방 생성 또는 입장
  const createOrEnterChatRoom = async (productId) => {
    loading.value = true
    try {
      // URL 파라미터로 productId 전달
      const response = await api.post(`/chat/getChat?productId=${productId}`, null)
      
      console.log('채팅방 생성/입장 응답:', response)
      
      if (!response) {
        throw new Error('채팅방 생성/입장에 실패했습니다.')
      }

      currentRoom.value = response

      // WebSocket 연결 및 구독 설정
      if (stomp.connected.value && response.chatRoomId) {
        currentSubscription.value = await stomp.subscribeToChatRoom(
          response.chatRoomId,
          {
            onMessage: (message) => {
              if (currentRoom.value?.chatRoomId === response.chatRoomId) {
                if (!currentRoom.value.messages) {
                  currentRoom.value.messages = []
                }
                currentRoom.value.messages.push(message)
              }
            }
          }
        )
      }

      return response
    } catch (err) {
      console.error('[useChatRooms] 채팅방 생성/입장 실패:', err)
      error.value = err
      throw err
    } finally {
      loading.value = false
    }
  }

  // cleanup 함수
  const cleanup = async () => {
    if (currentSubscription.value) {
      await stomp.unsubscribe(currentSubscription.value)
      currentSubscription.value = null
    }
    currentRoom.value = null
    chatRooms.value = []
  }

  return {
    chatRooms,
    currentRoom,
    loading,
    error,
    fetchChatRooms,
    createOrEnterChatRoom,
    cleanup
  }
}