// useStomp.js
import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const client = ref(null)
const connected = ref(false)
const subscriptions = new Map()

// 백엔드의 WebSocket 엔드포인트에 맞게 수정
const WS_URL = 'http://localhost:8080/ws-stomp'

const connectChat = async (userId) => {
  if (client.value?.active) {
    console.log('[STOMP] 이미 연결')
    return
  }

  return new Promise((resolve, reject) => {
    try {
      console.log('[STOMP] Connecting to:', WS_URL)
      const socket = new SockJS(WS_URL)
      
      client.value = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          userId: userId
        },
        debug: function (str) {
          console.log('[STOMP Debug]:', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
      })

      client.value.onConnect = () => {
        console.log('[STOMP] Connected successfully')
        connected.value = true
        resolve()
      }

      client.value.onStompError = (frame) => {
        console.error('[STOMP] Error:', frame)
        reject(new Error('STOMP error'))
      }

      client.value.activate()
    } catch (error) {
      console.error('[STOMP] Connection error:', error)
      reject(error)
    }
  })
}

const disconnectChat = async () => {
  if (client.value?.active) {
    try {
      await client.value.deactivate()
      console.log('[STOMP] Disconnected successfully')
    } catch (error) {
      console.error('[STOMP] Disconnect error:', error)
    } finally {
      connected.value = false
      client.value = null
    }
  }
}

const subscribeToChatRoom = async (destination, handlers) => {
  if (!client.value?.active) {
    throw new Error('STOMP client is not connected')
  }

  console.log('[STOMP] Subscribing to:', destination)
  
  const subscription = client.value.subscribe(destination, (message) => {
    console.log('[STOMP] Received message from:', destination, message)
    if (handlers?.onMessage) {
      handlers.onMessage(message)
    }
  })

  subscriptions.set(destination, subscription)
  return subscription
}

const sendMessage = async (destination, message, headers = {}) => {
  if (!client.value?.active) {
    throw new Error('STOMP client is not connected')
  }

  console.log('[STOMP] Sending message to:', destination, message)
  
  return new Promise((resolve, reject) => {
    try {
      client.value.publish({
        destination,
        body: typeof message === 'string' ? message : JSON.stringify(message),
        headers: {
          'content-type': 'application/json',
          ...headers
        }
      })
      resolve()
    } catch (error) {
      console.error('[STOMP] Failed to send message:', error)
      reject(error)
    }
  })
}

const cleanup = () => {
  console.log('[STOMP] Cleaning up connections')
  
  // 모든 구독 해제
  subscriptions.forEach((subscription, destination) => {
    try {
      subscription.unsubscribe()
      console.log(`[STOMP] Unsubscribed from: ${destination}`)
    } catch (error) {
      console.warn(`[STOMP] Failed to unsubscribe from ${destination}:`, error)
    }
  })
  subscriptions.clear()
  
  // STOMP 클라이언트 연결 해제
  if (client.value?.active) {
    client.value.deactivate()
  }
  client.value = null
  connected.value = false
}

// 구독 상태 확인 함수 추가
const hasActiveSubscription = (destination) => {
  return subscriptions.has(destination) && subscriptions.get(destination).active
}

// 단일 구독 해제 함수 추가
const unsubscribe = (destination) => {
  if (subscriptions.has(destination)) {
    try {
      subscriptions.get(destination).unsubscribe()
      subscriptions.delete(destination)
      console.log(`[STOMP] Unsubscribed from: ${destination}`)
    } catch (error) {
      console.warn(`[STOMP] Failed to unsubscribe from ${destination}:`, error)
    }
  }
}

export function useStomp() {
  return {
    connected,
    connectChat,
    disconnectChat,
    subscribeToChatRoom,
    sendMessage,
    cleanup,
    hasActiveSubscription,
    unsubscribe
  }
}