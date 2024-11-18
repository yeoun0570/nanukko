<!-- pages/socket-test.vue -->
<template>
  <div class="p-4">
    <h1 class="text-2xl mb-4">WebSocket 연결 테스트</h1>
    
    <!-- 연결 상태 표시 -->
    <div class="mb-4">
      <p>연결 상태: 
        <span 
          :class="connected ? 'text-green-500' : 'text-red-500'"
          class="font-bold"
        >
          {{ connected ? '연결됨' : '연결 안됨' }}
        </span>
      </p>
    </div>

    <!-- 연결/해제 버튼 -->
    <div class="mb-4 space-x-2">
      <button 
        @click="connect"
        class="bg-blue-500 text-white px-4 py-2 rounded"
        :disabled="connected"
      >
        연결하기
      </button>
      <button 
        @click="disconnect"
        class="bg-red-500 text-white px-4 py-2 rounded"
        :disabled="!connected"
      >
        연결 해제
      </button>
    </div>

    <!-- 테스트 메시지 전송 -->
    <div class="mb-4">
      <input 
        v-model="testMessage" 
        placeholder="테스트 메시지 입력"
        class="border p-2 rounded mr-2"
      />
      <button 
        @click="sendTestMessage"
        class="bg-green-500 text-white px-4 py-2 rounded"
        :disabled="!connected"
      >
        메시지 전송
      </button>
    </div>

    <!-- 로그 표시 -->
    <div class="border p-4 rounded bg-gray-100">
      <h2 class="text-lg mb-2">연결 로그:</h2>
      <div class="h-60 overflow-y-auto">
        <p 
          v-for="(log, index) in logs" 
          :key="index"
          :class="{'text-green-600': log.type === 'success', 
                  'text-red-600': log.type === 'error',
                  'text-blue-600': log.type === 'info'}"
          class="mb-1"
        >
          {{ log.time }} - {{ log.message }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const { $stompClient } = useNuxtApp()
const connected = ref(false)
const testMessage = ref('')
const logs = ref([])

// 로그 추가 함수
const addLog = (message, type = 'info') => {
  const time = new Date().toLocaleTimeString()
  logs.value.unshift({ time, message, type })
}

// 이벤트 핸들러들
const handleConnect = () => {
  connected.value = true
  addLog('WebSocket 연결 성공!', 'success')
}

const handleDisconnect = () => {
  connected.value = false
  addLog('WebSocket 연결 해제됨', 'info')
}

const handleError = () => {
  connected.value = false
  addLog('WebSocket 연결 실패', 'error')
}

// 클라이언트 사이드에서만 실행되도록 onMounted 사용
onMounted(() => {
  if (process.client) {
    window.addEventListener('stomp:connected', handleConnect)
    window.addEventListener('stomp:disconnected', handleDisconnect)
    window.addEventListener('stomp:failed', handleError)
  }
})

// 연결 시작
const connect = () => {
  addLog('WebSocket 연결 시도 중...')
  try {
    $stompClient.init('test-user-1')
  } catch (error) {
    addLog(`연결 시도 중 에러 발생: ${error.message}`, 'error')
  }
}

// 연결 해제
const disconnect = () => {
  addLog('연결 해제 중...')
  try {
    $stompClient.disconnect()
  } catch (error) {
    addLog(`연결 해제 중 에러 발생: ${error.message}`, 'error')
  }
}

// 테스트 메시지 전송
const sendTestMessage = () => {
  if (!testMessage.value.trim()) return
  
  try {
    $stompClient.sendChatMessage(1, testMessage.value, 'seller1')
    addLog(`메시지 전송: ${testMessage.value}`, 'success')
    testMessage.value = ''
  } catch (error) {
    addLog(`메시지 전송 실패: ${error.message}`, 'error')
  }
}

// 컴포넌트 제거 시 정리 (클라이언트 사이드에서만)
onUnmounted(() => {
  if (process.client) {
    window.removeEventListener('stomp:connected', handleConnect)
    window.removeEventListener('stomp:disconnected', handleDisconnect)
    window.removeEventListener('stomp:failed', handleError)
    if (connected.value) {
      disconnect()
    }
  }
})
</script>