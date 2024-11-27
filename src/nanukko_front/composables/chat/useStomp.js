import { ref } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

export function useStomp() {
  const client = ref(null)
  const connected = ref(false)
  const connectionPromise = ref(null)
  const subscriptions = new Map()
  
  const connectWithRetry = async (userId, token, maxAttempts = 5) => {
    let attempts = 0
    
    while (attempts < maxAttempts) {
      try {
        if (client.value?.active) {
          return true
        }

        const socket = new SockJS('http://localhost:8080/ws-stomp')
        
        await new Promise((resolve, reject) => {
          client.value = new Client({
            webSocketFactory: () => socket,
            connectHeaders: {
              'Authorization': `Bearer ${token}`,
              'userId': userId
            },
            debug: (str) => console.log('STOMP Debug:', str),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
              console.log('연결 성공 - STOMP connection successful!')
              connected.value = true
              resolve(true)
            },
            onStompError: (frame) => {
              console.error('STOMP error:', frame)
              connected.value = false
              reject(new Error('연결 실패 - STOMP connection failed'))
            },
            onWebSocketClose: () => {
              console.log('WebSocket closed')
              connected.value = false
            }
          })

          client.value.activate()
        })

        return true
      } catch (error) {
        console.error(`Connection attempt ${attempts + 1} failed:`, error)
        attempts++
        if (attempts < maxAttempts) {
          await new Promise(resolve => setTimeout(resolve, 2000))
        }
      }
    }
    
    throw new Error('결국 연결 실패..- Failed to establish STOMP connection after max attempts')
  }

  const connectChat = async (userId, token) => {
    if (connectionPromise.value) {
      return connectionPromise.value
    }

    connectionPromise.value = connectWithRetry(userId, token)
    
    try {
      await connectionPromise.value
      return true
    } finally {
      connectionPromise.value = null
    }
  }

  const subscribeToChatRoom = async (roomId, callbacks = {}) => {
    if (!client.value?.active) {
      throw new Error('커넥션 에러 - No active STOMP connection');
    }

    const destination = `/queue/chat/${roomId}`;
    console.log('구독하기-Subscribing to:', destination);

    // 기존 구독 해제
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).unsubscribe();
    }

    return new Promise((resolve, reject) => {
      try {
        const subscription = client.value.subscribe(destination, (message) => {
          try {
            console.log('Raw message received:', message);
            const payload = JSON.parse(message.body);
            console.log('Parsed message:', payload);
            callbacks.onMessage?.(payload);
          } catch (error) {
            console.error('메시지 핸들링 error:', error);
          }
        });

        subscriptions.set(destination, subscription);
        resolve(subscription);
      } catch (error) {
        console.error('구독 error:', error);
        reject(error);
      }
    });
  };


  const sendMessage = async (roomId, message) => {
    if (!client.value?.active) {
      throw new Error('No active STOMP connection')
    }

    const destination = `/app/chat/${roomId}`
    console.log('메시지 전송:', destination, message)

    return client.value.publish({
      destination,
      body: JSON.stringify(message)
    })
  }

  const disconnect = async () => {
    if (client.value?.active) {
      for (const subscription of subscriptions.values()) {
        subscription.unsubscribe()
      }
      subscriptions.clear()
      await client.value.deactivate()
      client.value = null
      connected.value = false
    }
  }

  const unsubscribe = (destination) => {
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).unsubscribe()
      subscriptions.delete(destination)
    }
  }

  return {
    connected,
    connectChat,
    subscribeToChatRoom,
    sendMessage,
    disconnect,
    unsubscribe
  }
}