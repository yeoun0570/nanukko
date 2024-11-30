<template>
  <!--채팅방 목록-->
  <div class="chat-layout">
    <!-- ClientOnly: 클라이언트 사이드에서만 렌더링되도록 보장 -->
    <ClientOnly>
      <template v-if="isAuthenticated">
        <!--채팅방 목록, 로딩상태, 현재 선택된 채팅방(선택한 채팅방 하이라이트 해 줌), 로그인한 user, 채팅방 선택 핸들러-->
        <ChatList
          :chat-rooms="chatRooms"
          :loading="loading"
          :current-room-id="currentRoomId"
          :user-id="userId"
          :connected="stompState.connected"
          @select-room="handleRoomSelect"
        />
        <!--채팅창-->
        <section class="chat-room-section">
          <!-- key:채팅방이 변경될 때마다 컴포넌트를 완전히 새로 생성(재랜더링)-->
          <ChatRoom
            v-if="currentRoomId && activeChatRoom && stompState.connected"
            :key="currentRoomId"
            :room-id="currentRoomId"
            :user-id="userId"
            :current-room="activeChatRoom"
            :initial-messages="currentMessages"
            :connected="stompState.connected"
          />
          <div v-else class="empty-state">
            <p>채팅방을 선택해주세요</p>
          </div>
        </section>
      </template>

      <div v-else class="auth-required">
        <p>채팅을 이용하려면 로그인이 필요합니다.</p>
        <NuxtLink to="/auth/login" class="login-link">로그인하러 가기</NuxtLink>
      </div>
    </ClientOnly>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useChatRooms } from '~/composables/chat/useChatRooms'
import { useStomp } from '~/composables/chat/useStomp'
import { useAuth } from '~/composables/auth/useAuth'

// 상태 관리
const currentRoomId = ref(null)//현재 선택된 채팅방
const activeChatRoom = ref(null)//활성화된 채팅방 정보
const {isAuthenticated, userId} = useAuth()
const stompState = useStomp() //웹 소켓 연결 상태
const currentMessages = ref([]); // 현재 채팅방의 메시지들

const { 
  chatRooms, //전체 채팅방 목록
  loading, //로딩 상태(사용자 경험UX를 위해 데이터 로딩 중임을 시각적으로 보여줌)
  error,
  fetchChatRooms,//채팅방 목록 조회
  createOrEnterChatRoom, //채팅방 생성/입장
  loadChatMessages
} = useChatRooms()

// 초기화 함수
const initializeChat = async () => {
  console.log('채팅 초기화 중...로그인 상태', isAuthenticated)
  
  if (!isAuthenticated) {
    console.log('로그인 안됨')
    return
  }

  try {
    
    await stompState.connectChat(userId)// STOMP 웹소켓 연결
    
    await fetchChatRooms()// 채팅방 목록 조회
  
  } catch (err) {
    console.error('채팅방 초기화 실패:', err)
  }
}

// 채팅방 선택 처리
const handleRoomSelect = async (chatRoomId) => {
  try {
    activeChatRoom.value = null;
    currentRoomId.value = null;
    currentMessages.value = [];
    
    // 메시지 직접 로드
    const response = await loadChatMessages(chatRoomId, 0, 20);
    console.log('채팅방 메시지 로드:', response);

    if (response) {
      currentMessages.value = response.content || [];
      activeChatRoom.value = {
        chatRoomId,
        hasMore: !response.last,
        currentPage: response.number
      };
      currentRoomId.value = chatRoomId.toString();
    }
  } catch (err) {
    console.error('채팅방 선택 실패:', err);
    currentRoomId.value = null;
    activeChatRoom.value = null;
    currentMessages.value = [];
  }
};

// 라이프사이클 훅
onMounted(() => {
  console.log('채팅 컴포넌트 mounted')
  initializeChat()
})

// 인증 상태 변화 감시
watch(() => isAuthenticated, (newValue) => {
  console.log('사용자 상태 바뀜:', newValue)
  if (newValue) {
    initializeChat()
  }
})

// 웹소켓 연결 상태 감지
watch(() => stompState.connected, (isConnected) => {
  console.log('STOMP 연결 상태 감시:', isConnected)
  if (isConnected && isAuthenticated) {
    fetchChatRooms()
  }
})

// 에러 감지
watch(error, (newError) => {
  if (newError) {
    console.error('채팅 error:', newError)
  }
})
</script>

<style scoped>
@import '~/assets/chat/chat-index.css';

</style>