<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="max-w-md w-full bg-white rounded-lg shadow-md p-8">
      <h2 class="text-2xl font-bold text-center mb-6">로그인</h2>
      
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <input
            v-model="userId"
            type="text"
            placeholder="아이디"
            class="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
        <div>
          <input
            v-model="password"
            type="password"
            placeholder="비밀번호"
            class="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>
        <button
          type="submit"
          class="w-full bg-blue-500 hover:bg-blue-600 text-white p-2 rounded transition duration-200"
        >
          로그인
        </button>
      </form>

      <!-- 토큰 정보 표시 -->
      <div v-if="tokenInfo" class="mt-6 p-4 bg-gray-50 rounded">
        <h3 class="font-bold mb-2">토큰 정보:</h3>
        <pre class="whitespace-pre-wrap break-all text-sm">{{ JSON.stringify(tokenInfo, null, 2) }}</pre>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="mt-4 p-3 bg-red-100 text-red-700 rounded">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { jwtDecode } from 'jwt-decode'  // 수정된 import 구문

const userId = ref('')
const password = ref('')
const tokenInfo = ref(null)
const error = ref(null)

const decodeToken = (token) => {
  try {
    return jwtDecode(token)
  } catch (err) {
    console.error('토큰 디코딩 실패:', err)
    return null
  }
}

const handleLogin = async () => {
  try {
    console.log('로그인 시도:', { username: userId.value, password: password.value })
    
    const response = await fetch('/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        username: userId.value,
        password: password.value
      }),
      credentials: 'include'
    })

    console.log('응답 상태:', response.status)
    console.log('응답 헤더:', [...response.headers.entries()])

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(errorText || `HTTP error! status: ${response.status}`)
    }

    const authHeader = response.headers.get('Authorization')
    console.log('Authorization 헤더:', authHeader)  // 디버깅용 로그 추가

    if (authHeader?.startsWith('Bearer ')) {
      const token = authHeader.substring(7)
      const decoded = decodeToken(token)
      
      if (decoded) {
        tokenInfo.value = {
          token,
          decoded
        }
        error.value = null
        console.log('토큰 디코딩 성공:', decoded)
      } else {
        throw new Error('토큰 디코딩 실패')
      }
    } else {
      throw new Error('인증 토큰을 받지 못했습니다')
    }
  } catch (err) {
    console.error('로그인 에러:', err)
    error.value = err.message
    tokenInfo.value = null
  }
}
</script>