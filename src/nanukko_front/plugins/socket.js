// plugins/socket.js
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

class StompSocketClient {
  constructor() {
    this.client = null
    this.connected = false
    this.subscriptions = new Map()
    
    // WebSocket 서버 URL 설정
    this.SOCKET_URL = 'http://localhost:8080/ws-stomp'
  }

  init(userId) {
    if (!process.client) return

    try {
      console.log('[StompSocketClient] 연결 시작:', { url: this.SOCKET_URL, userId })

      // SockJS 연결 생성
      const socket = new SockJS(this.SOCKET_URL)

      // SockJS 이벤트 리스너
      socket.onopen = () => {
        console.log('[SockJS] 연결 성공')
      }

      socket.onclose = (event) => {
        console.log('[SockJS] 연결 종료:', event)
      }

      socket.onerror = (error) => {
        console.error('[SockJS] 에러:', error)
      }

      // STOMP 클라이언트 설정
      this.client = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
          login: userId,
          passcode: '',
          userId: userId
        },
        debug: (str) => {
          console.log('[STOMP] Debug:', str)
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          console.log('[STOMP] 연결 성공')
          this.connected = true
          if (process.client) {
            window.dispatchEvent(new Event('stomp:connected'))
          }
        },

        onDisconnect: () => {
          console.log('[STOMP] 연결 종료')
          this.connected = false
          if (process.client) {
            window.dispatchEvent(new Event('stomp:disconnected'))
          }
        },

        onStompError: (frame) => {
          console.error('[STOMP] 프로토콜 에러:', frame.body)
        },

        onWebSocketError: (event) => {
          console.error('[STOMP] WebSocket 에러:', event)
        }
      })

      // 클라이언트 활성화
      this.client.activate()

    } catch (error) {
      console.error('[StompSocketClient] 초기화 에러:', error)
    }
  }

  subscribe(destination, callback) {
    if (!this.client || !this.connected) {
      console.warn('[StompSocketClient] 연결되지 않음')
      return null
    }

    console.log('[StompSocketClient] 구독 시도:', destination)

    try {
      const subscription = this.client.subscribe(destination, (message) => {
        console.log(`[StompSocketClient] 메시지 수신 (${destination}):`, message.body)
        try {
          const data = JSON.parse(message.body)
          callback(data)
        } catch (error) {
          console.error('[StompSocketClient] 메시지 파싱 에러:', error)
        }
      })

      this.subscriptions.set(destination, subscription)
      return subscription
    } catch (error) {
      console.error('[StompSocketClient] 구독 에러:', error)
      return null
    }
  }

  send(destination, body) {
    if (!this.client || !this.connected) {
      console.warn('[StompSocketClient] 연결되지 않음')
      return
    }

    console.log('[StompSocketClient] 메시지 전송:', { destination, body })

    try {
      this.client.publish({
        destination,
        body: JSON.stringify(body),
        headers: { 'content-type': 'application/json' }
      })
      console.log('[StompSocketClient] 메시지 전송 성공')
    } catch (error) {
      console.error('[StompSocketClient] 메시지 전송 에러:', error)
    }
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(destination)
      console.log('[StompSocketClient] 구독 해제:', destination)
    }
  }

  disconnect() {
    if (this.client) {
      this.subscriptions.forEach((sub, dest) => {
        this.unsubscribe(dest)
      })
      this.client.deactivate()
      this.connected = false
      console.log('[StompSocketClient] 연결 종료 완료')
    }
  }
}

// 싱글톤 인스턴스 생성
export const stompClient = process.client ? new StompSocketClient() : null

// Nuxt 플러그인으로 등록
export default defineNuxtPlugin(() => {
  return {
    provide: {
      stompClient: process.client ? stompClient : {
        init: () => {},
        subscribe: () => {},
        send: () => {},
        unsubscribe: () => {},
        disconnect: () => {}
      }
    }
  }
})