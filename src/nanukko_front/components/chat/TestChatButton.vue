<template>
  <div class="test-chat-button-container">
    <button 
      @click="startNewChat"
      class="test-chat-button"
      :disabled="loading"
    >
      {{ loading ? '채팅방 생성 중...' : '테스트 채팅 시작 (Product ID: 13)' }}
    </button>
    
    
    <!-- 결과 메시지 표시 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    <div v-if="success" class="success-message">
      채팅방 생성 성공! ID: {{ success }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '~/composables/useApi'
import { useAuth } from '~/composables/auth/useAuth'

const router = useRouter()
const api = useApi()
const { isAuthenticated } = useAuth()

const loading = ref(false)
const error = ref(null)
const success = ref(null)

const startNewChat = async () => {
  // 인증 확인
  if (!isAuthenticated) {
    error.value = '로그인이 필요합니다'
    setTimeout(() => {
      router.push('/auth/login')
    }, 1500)
    return
  }

  loading.value = true
  error.value = null
  success.value = null

  try {
    const response = await api.post('/chat/getChat', null, {
      params: { productId: 13 }
    })
    
    success.value = response.chatRoomId
    
    // 3초 후 채팅 페이지로 이동
    setTimeout(() => {
      router.push('/chat')
    }, 1500)
    
  } catch (err) {
    error.value = err.message || '채팅방 생성 실패'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.test-chat-button-container {
  padding: 2rem;
  text-align: center;
}

.test-chat-button {
  background-color: #4CAF50;
  color: white;
  padding: 1rem 2rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.test-chat-button:hover {
  background-color: #45a049;
}

.test-chat-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  color: #ff4444;
  margin-top: 1rem;
}

.success-message {
  color: #4CAF50;
  margin-top: 1rem;
}
</style>