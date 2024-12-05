<script setup>
// WebSocket 통신을 위한 커스텀 훅 import
import { useStomp } from "~/composables/chat/useStomp";

// 채팅 메시지 이력을 저장하는 반응형 배열
const messages = ref([]);
// 현재 입력창의 메시지를 저장하는 반응형 변수
const currentMessage = ref("");
// 로딩 상태를 관리하는 반응형 변수
const isLoading = ref(false);
// 채팅창 DOM 요소 참조를 위한 ref
const chatContainer = ref(null);

// 버튼형 응답 관련 상태 관리
// 현재 표시할 버튼들의 배열
const currentButtons = ref([]);
// 버튼 응답 대기 상태 (버튼 응답 필요시 true)
const isWaitingForButtonResponse = ref(false);

// WebSocket 관련 메서드들을 가져옴
const { connectChat, subscribeToChatRoom, sendClovaMessage, disconnect } =
  useStomp();

// 새 메시지가 추가될 때마다 채팅창을 맨 아래로 스크롤
const scrollToBottom = () => {
  if (chatContainer.value) {
    setTimeout(() => {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }, 100); // 애니메이션을 위한 약간의 지연
  }
};

// 버튼 클릭시 실행되는 핸들러 함수
const handleButtonClick = async (button) => {
  if (isLoading.value) return;

  try {
    console.log("=== 버튼 클릭 처리 시작 ===");
    console.log("클릭된 버튼 전체 데이터:", button);
    console.log("버튼 텍스트:", button.text);
    console.log("버튼 postback:", button.postback);
    console.log("버튼 postbackFull:", button.postbackFull);

    // 버튼 텍스트를 UI에 추가
    messages.value.push({
      type: "user",
      content: button.text,
    });

    // 전송할 메시지 결정 (postbackFull, postback, text 순으로 우선)
    const messageToSend = button.postbackFull || button.text;
    console.log("masseageToSend: ", messageToSend);

    // WebSocket으로 메시지 전송
    await sendClovaMessage(messageToSend);

    console.log("sendClovaMessage 호출 완료");
    scrollToBottom();
  } catch (error) {
    console.error("버튼 응답 전송 실패:", error);
  } finally {
    isLoading.value = false;
  }
};

// 텍스트 메시지 전송 함수
const sendChatMessage = async () => {
  if (!currentMessage.value.trim() || isLoading.value) return;

  try {
    isLoading.value = true;

    // 사용자 메시지를 채팅창에 추가
    messages.value.push({
      type: "user",
      content: currentMessage.value,
    });

    await sendClovaMessage(currentMessage.value);

    // 입력창 초기화
    currentMessage.value = "";
    scrollToBottom();
  } catch (error) {
    console.error("메시지 전송 실패:", error);
  } finally {
    isLoading.value = false;
  }
};

// 챗봇 응답 메시지 파싱 함수
const parseResponse = (response) => {
  try {
    // response가 이미 객체인 경우
    const data = typeof response === "string" ? JSON.parse(response) : response;
    const responseJson = {
      text: data.bubbles[0].data.description,
    };

    console.log("quickButtons: ", data.quickButtons);

    if (data.quickButtons) {
      responseJson.buttons = data.quickButtons.map((btn) => ({
        text: btn.title,
        postback: btn.data.action.data.postbackFull,
      }));
    }

    console.log("responseJson: ",responseJson);
    return responseJson;
  } catch (e) {
    console.error("응답 파싱 에러:", e);
    return null;
  }
};

// WebSocket 연결 설정 함수
const setupWebSocket = async () => {
  try {
    // WebSocket 연결 시도
    await connectChat("chatbot-user");

    // 채팅방 구독 및 메시지 수신 처리 설정
    await subscribeToChatRoom("/topic/public", {
      onMessage: (response) => {
        const parsedResponse = parseResponse(response);
        if (parsedResponse) {
          // 메시지 추가
          messages.value.push({
            type: "bot",
            content: parsedResponse.text,
          });

          // 버튼 업데이트
          if (parsedResponse.buttons && parsedResponse.buttons.length > 0) {
            currentButtons.value = parsedResponse.buttons;
            isWaitingForButtonResponse.value = true;
          } else {
            // 버튼이 없으면 초기화
            currentButtons.value = [];
            isWaitingForButtonResponse.value = false;
          }
        }
        scrollToBottom();
      },
    });
    // 빈 메시지를 보내되, 서버에서 event: 'open'으로 처리
    await sendClovaMessage("");
  } catch (error) {
    console.error("WebSocket 연결 실패:", error);
  }
};

// 컴포넌트 마운트 시 WebSocket 연결
onMounted(() => {
  setupWebSocket();
});

// 컴포넌트 언마운트 시 연결 해제
onUnmounted(() => {
  disconnect();
});
</script>

<template>
  <!-- 챗봇 전체 컨테이너 -->
  <div class="chatbot-container">
    <!-- 챗봇 헤더 -->
    <div class="chat-header">
      <h2>나누꼬 챗봇</h2>
    </div>

    <!-- 메시지 표시 영역 -->
    <div ref="chatContainer" class="chat-messages">
      <!-- 각각의 메시지 표시 -->
      <div
        v-for="(message, index) in messages"
        :key="index"
        :class="['message', message.type]"
      >
        <div class="message-bubble">
          {{ message.content }}
        </div>
      </div>

      <!-- 버튼형 응답 UI -->
      <div
        v-if="currentButtons.length > 0 && isWaitingForButtonResponse"
        class="button-responses"
      >
        <button
          v-for="button in currentButtons"
          :key="button.text"
          class="response-button"
          @click="handleButtonClick(button)"
          :disabled="isLoading"
        >
          {{ button.text }}
        </button>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="isLoading" class="loading">
        <span>답변 준비중...</span>
      </div>
    </div>

    <!-- 메시지 입력 영역 -->
    <div class="chat-input">
      <input
        v-model="currentMessage"
        type="text"
        placeholder="메시지를 입력하세요..."
        @keyup.enter="sendChatMessage"
        :disabled="isWaitingForButtonResponse"
      />
      <button
        @click="sendChatMessage"
        :disabled="isLoading || isWaitingForButtonResponse"
      >
        전송
      </button>
    </div>
  </div>
</template>

<style scoped>
@import url("/assets/chatbot/ClovaChatbot.css");
</style>
