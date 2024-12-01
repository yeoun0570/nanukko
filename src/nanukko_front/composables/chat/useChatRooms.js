import { ref, watch } from 'vue'
import { useStomp } from './useStomp'
import { useAuth } from '../auth/useAuth'
import { useApi } from '../useApi'

export const useChatRooms = () => {
  const chatRooms = ref([])// 전체 채팅방 목록
  const currentRoom = ref(null)//현재 활성화된 채팅방
  const loading = ref(false)//로딩상태
  const error = ref(null)// 에러
  const currentSubscription = ref(null)//현재 웹소켓 구독 정보

  const stomp = useStomp()//웹소켓 stomp 통신
  const api = useApi()//http api 통신

  // 채팅방 목록 들고오기
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

  // 채팅방 생성/입장만 처리하는 메서드
  const createOrEnterChatRoom = async (productId, page = 0, size = 50) => {
    loading.value = true;
    try {
      const response = await api.post(`/chat/getChat`,{
        params: {
          productId,
          page,
          size
        }}, {
        rawResponse: true,
      });
      
      if (!response.ok) {
        throw new Error('채팅방 생성/입장에 실패했습니다.');
      }
  
      const data = await response.json();
      if (!data) {
        throw new Error('채팅방 데이터를 받지 못했습니다.');
      }
  
      currentRoom.value = data;
  
      // WebSocket 연결 및 구독 설정
      if (stomp.connected.value && data.chatRoomId) {
        currentSubscription.value = await stomp.subscribeToChatRoom(
          data.chatRoomId,
          {
            onMessage: (message) => {
              if (currentRoom.value?.chatRoomId === data.chatRoomId) {
                if (!currentRoom.value.messages) {
                  currentRoom.value.messages = [];
                }
                currentRoom.value.messages.push(message);
              }
            }
          }
        );
      }
  
      return data;
    } catch (err) {
      console.error('[useChatRooms] 채팅방 생성/입장 실패:', err);
      error.value = err;
      throw err;
    } finally {
      loading.value = false;
    }
  };

// 메시지 로드를 처리하는 별도 메서드
const loadChatMessages = async (chatRoomId, page = 0, size = 20) => {
  
  //if (!currentRoom.value) return null;
  
  try {
    console.log('Requesting messages with page:', page) // 디버깅용
    
    const response = await api.get('/chat/getMessages', {
      params: {
        chatRoomId,
        page,
        size
      }
    });


    // 응답 데이터 로깅
    console.log('서버 응답:', response)

    return {
      content: response.content || [],
      last: response.last,
      number: page // 현재 페이지 번호 반환
    };
  } catch (error) {
    console.error('[useChatRooms] 메시지 로드 실패:', error);
    throw error;
  }
};


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
    chatRooms,//채팅방 목록
    currentRoom,//현재 채팅방

    loading,//로딩 상태
    error,

    fetchChatRooms,
    createOrEnterChatRoom,//채팅방 생성 및 입장
    loadChatMessages,
    cleanup
  }
}