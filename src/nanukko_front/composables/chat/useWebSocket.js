

export function useWebSocket() {

    //웹소켓 연결 상태와 메시지 저장
    const isConnected = ref(false);
    const messages = ref([]);
    let stompClient = null;//값이 변경되어야 하므로 const가 아닌 let 사용

    const connect = (roomId, userId) => {
        stompClient = new Client({
            brokerURL: 'http://localhost:8080/ws-stomp',//브로커와 통신할 때 사용할 기본 URL 지정(실제 웹소켓 통신에는 사용 안됨, stomp클라이언트가 내부적으로 사용하는 설정)
            webSocketFactory: () => new SockJS('http://localhost:8080/ws-stomp'),//실제 웹소켓 연결을 생성하는 팩토리 함수, SockJS를 사용해서 웹소켓 연결 생성,
            connectHeaders: {
                // 필요한 경우 헤더 추가
                userId: userId,
             },
            reconnectDelay: 5000,//연결 끊김 시 5초 후 재연결 시도
            debug: function (str) {
                console.log(str);
             },
            onConnect: (frame) => {//연결 성공 시 실행되는 콜백 함수
                isConnected.value = true;
                console.log('연결됨: ', frame);//CONNECTED
                subscribeToChatRoom(roomId)//연결 성공하면 해당 채팅방 구독
            },
            onWebSocketError: (error) => {
                console.error("웹소켓 에러 발생: ", error);
                isConnected.value = false;  // 에러 발생 시 연결 상태 업데이트
            },
            onStompError: (frame) => {
                console.error('Broker 에러: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
                isConnected.value = false;  // 에러 발생 시 연결 상태 업데이트
            }
        })

        stompClient.activate();
    }

    //채팅방 구독 - 컨트롤러에서 메시지 보내면 여기서 받음
    const subscribeToChatRoom = (roomId) => {

        if (!stompClient) return;  // stompClient null 체크 추가
        stompClient.subscribe(`/topic/${roomId}/message`, (message) => {
            const newMessage = JSON.parse(message.body);
            messages.value.push(newMessage);
        })
    }


    //메시지 전송 - 컨트롤러 @MessageMapping으로 전송됨
    const sendMessage = (roomId, message, sender) => {
        if (!stompClient || !stompClient.active) return;  // 연결 상태 체크

        //ChatMessageDTO와 똑같이 작성
        const chatMessage = {
            chatMessageId: null,
            sender,
            chatRoom: roomId,
            chatMessage: message,
            createdAt: new Date(),
            isRead: false,
            image: null, 
            isLatest: true
        }

        //"/app/chat/{roomId}"로 메시지 전송
    stompClient.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify(chatMessage)
    })

     const disconnect = () => {
        if(stompClient?.activate){
            stompClient.deactivate();
            isConnected.value = false;
        }
    }

    //필요한 함수들 반환
    return {
        isConnected,
        messages,
        connect,
        disconnect,
        sendMessage
    }

    }
}