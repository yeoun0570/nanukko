import { defineStore } from 'pinia'
import { ref } from 'vue'
import { jwtDecode } from 'jwt-decode'
import { useApi } from '~/composables/useApi'

//import { useChatStore } from './chatStore'

export const useAuthStore = defineStore('auth', () => {
  const isAuthenticated = ref(false)
  const logoutTriggered = ref(false)
  const userId = ref(null)
  const token = ref(null)
  const api = useApi()


    // 토큰 디코딩 및 사용자 정보 설정
    const decodeAndSetUserInfo = (accessToken) => {
      try {
        const decoded = jwtDecode(accessToken)
        userId.value = decoded.userId
        isAuthenticated.value = true
        token.value = accessToken
        return true
      } catch (error) {
        console.error('토큰 디코딩 실패:', error)
        return false
      }
    }
  // 로그인 상태 설정
  const setAuthenticated = (value, userInfo = null) => {
    isAuthenticated.value = value
    if (userInfo) {
      userId.value = userInfo.userId
    }
  }

  // 토큰 설정
  const setToken = (accessToken) => {
    if (!accessToken) return false

    if (validateToken(accessToken)) {
      localStorage.setItem('access_token', accessToken)
      return decodeAndSetUserInfo(accessToken)
    }
    return false
  }


  // 초기화 함수
  const initialize = () => {
    if (process.client) {
      const storedToken = localStorage.getItem('access_token')
      if (storedToken && validateToken(storedToken)) {
        if (decodeAndSetUserInfo(storedToken)) {
          isAuthenticated.value = true
          return
        }
      }
      // 토큰이 없거나 유효하지 않은 경우
      triggerLogout()
    }
  }

  // 토큰 검증 함수 추가
  const validateToken = (accessToken) => {
    try {
      const decoded = jwtDecode(accessToken)
      const currentTime = Date.now() / 1000
      
      if (decoded.exp && decoded.exp < currentTime) {
        return false
      }
      return true
    } catch {
      return false
    }
  }
  

  // 로그아웃
  const triggerLogout = () => {
    //const chatStore = useChatStore()
    logoutTriggered.value = true
    isAuthenticated.value = false
    userId.value = null
    token.value = null
    
    
    if (process.client) {
      localStorage.removeItem('access_token')
    }

    setTimeout(() => {
      logoutTriggered.value = false
    }, 100)
  }


   // 로그아웃
   const logout = async () => {
    try {
      await api.post('/logout')
      isAuthenticated.value = false
      userId.value = null
      token.value = null
      
      if (process.client) {
        localStorage.removeItem('access_token')
      }
      
      return true
    } catch (error) {
      console.error('Logout failed:', error)
      throw error
    }
  }

  // 컴포저블이 생성될 때 초기화 실행
  if (process.client) {
    initialize()
  }

  return {
    isAuthenticated,
    userId,
    token,
    setAuthenticated,
    setToken,
    initialize,
    triggerLogout,
    logout
  }
}, {
  //pinia 옵션으로 상태 유지
  persist: {
    storage: process.client ? localStorage : null,
    paths: ['isAuthenticated', 'userId', 'token']
  }
})