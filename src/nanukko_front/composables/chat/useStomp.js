import { ref } from 'vue'
import { Client } from '@stomp/stompjs'// STOMP 라이브러리에서 제공하는 Client 클래스
import SockJS from 'sockjs-client'

export function useStomp() {
  const client = ref(null) // 웹소켓 클라이언트 저장
  const connected = ref(false) //연결 상태
  const connectionPromise = ref(null) // 연결 진행 상태
  const subscriptions = new Map() // 채팅방 구독 정보 저장
  
  /** 연결 시도 함수 */
  const connectWithRetry = async (userId, token, maxAttempts = 5) => { 
    let attempts = 0 // 시도 횟수
    
    while (attempts < maxAttempts) { //최대 5번까지 시도
      try {
        if (client.value?.active) {// 이미 연결되어 있으면 바로 성공 반환
          return true
        }

        // 1. 웹소켓 연결 시도
        const socket = new SockJS('http://localhost:8080/ws-stomp')//sockJS 사용하면 http 요청해도 ws로 알아서 바꿔줌
        
        //2. Promise로 연결 과정 처리
        await new Promise((resolve, reject) => {// 성공, 실패 설정

          //3. 클라이언트 설정 및 연결 시도
          client.value = new Client({ // 클라이언트 설정(카톡 앱같은 느낌)

            // 웹소켓 생성하는 함수
            webSocketFactory: () => socket,//webSocketFactory는 Client의 설정 옵션 중 하나(WebSocket 연결을 생성하는 방법을 알려주는 옵션)
            connectHeaders: {//연결 정보들
              'Authorization': `Bearer ${token}`, 
              'userId': userId
            },

            //문제가 있을 때 콘솔에 기록
            debug: (str) => console.log('STOMP Debug:', str),
            reconnectDelay: 5000, //재연결 시도 간격(5초)
            heartbeatIncoming: 4000,//서버->클라이언트 생존 확인(4초)
            heartbeatOutgoing: 4000,// 클라이언트 -> 서버 생존 확인(4초)
            
            //열결됐을 때 실행할 함수
            onConnect: () => {
              console.log('연결 성공 - STOMP connection successful!')
              connected.value = true
              resolve(true)
            },

            //에러 발생했을 때 실행할 함수
            onStompError: (frame) => {//에러 정보
              console.error('STOMP error:', frame)
              connected.value = false
              reject(new Error('연결 실패 - STOMP connection failed'))// Promise 실패 처리
            },

            // 연결 끊어지면 실행할 함수
            onWebSocketClose: () => {
              console.log('WebSocket 닫힘')
              connected.value = false
            }
          })
          /////////////////////////////////////////////여기까지는 전부 연결 설정

          client.value.activate()// 연결 시작!
        })

        return true//연결 성공!

      } catch (error) {
        console.error(`연결시도 횟수 ${attempts + 1} 번째 실패..:`, error)
        attempts++
        if (attempts < maxAttempts) {
          await new Promise(resolve => setTimeout(resolve, 2000))//실패하면 2초 후 재시도
        }
      }
    }
    //5번 모두 실패하면 최종 에러
    throw new Error('결국 연결 실패..- Failed to establish STOMP connection after 5 attempts')
  };


  /**사용자가 채팅 페이지에 접속하면 실행될 연결 함수 - 연결 관리(연결 상태, 연결 시도 관리) */
  const connectChat = async (userId, token) => {
    if (connectionPromise.value) {//이미 연결 시도 중이면 기존 Promise 반환
      return connectionPromise.value
    }

    connectionPromise.value = connectWithRetry(userId, token)//기존 연결 없으면 새로운 연결 시도 시작
    
    try {
      //연결 시도 겨로가 기다리기
      await connectionPromise.value

      return true//연결 성공하면 true 반환
    } finally {

      //다음 연결 시도를 위해 connectionPromise 초기화(성공이든 실패든 "전송 중..." 표시 없애기)
      connectionPromise.value = null
    }
  }

  /** 채팅방 구독 - 특정 채팅방의 메시지를 받기 위한 구독 설정 */
  const subscribeToChatRoom = async (roomId, callbacks = {}) => {//채팅방 아이디 기준으로 구독 설정, 메시지 받으면 콜백 함수 통해서 처리
    if (!client.value?.active) {//연결 확인
      throw new Error('커넥션 에러 - No active STOMP connection');
    }

    const destination = `/queue/chat/${roomId}`;//구독
    console.log('구독하기-Subscribing to:', destination);

    // 같은 채팅방에 이미 구독 중이면 해제
    if (subscriptions.has(destination)) {
      subscriptions.get(destination).unsubscribe();
    }

    return new Promise((resolve, reject) => {
      try {
        // 새로운 메시지가 올 때마다 실행될 구독 설정
        const subscription = client.value.subscribe(destination, (message) => {
          try {
            console.log('메시지 수신:', message);
            ////메시지 받으면 JSON으로 변환
            const payload = JSON.parse(message.body);
            console.log('메시지 파싱:', payload);
            
            //받은 메시지를 처리하는 콜백함수 실행
            callbacks.onMessage?.(payload);
          } catch (error) {
            console.error('메시지 핸들링 error:', error);
          }
        });
        
        //구독 정보 저장
        subscriptions.set(destination, subscription);
        
        resolve(subscription);//구독 설정
      } catch (error) {
        console.error('구독 error:', error);
        reject(error);
      }
    });
  };


  /**메시지 전송 */
  const sendMessage = async (roomId, message) => {
    if (!client.value?.active) {
      throw new Error('STOMP connection 끊김')
    }

    const destination = `/app/chat/${roomId}`
    console.log('메시지 전송:', destination, message)

    return client.value.publish({
      destination,
      body: JSON.stringify(message)//메시지 json 형태로 변환
    })
  }

  /**연결 해제 */
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

  /** 구독 해제 */
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