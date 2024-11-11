// WebSocket 연결 설정
const stompClient = new StompJs.Client({
   brokerURL: 'http://localhost:8080/ws-stomp',// ws:// 대신 http:// 사용 (SockJS가 알맞은 프로토콜로 변환)
   connectHeaders: {
      // 필요한 경우 헤더 추가
   },
   // SockJS 사용 설정 추가
   webSocketFactory: () => new SockJS('http://localhost:8080/ws-stomp'),
   reconnectDelay: 5000,
   debug: function (str) {
      console.log(str);
   },
   onConnect: (frame) => {//연결 성공 시 실행되는 콜백 함수
      setConnected(true);
      console.log('연결됨: ' + frame);//CONNECTED
      // 특정 채팅방 구독
      stompClient.subscribe('/topic/1/message', (message) => {
         showMessage(JSON.parse(message.body).chatMessage);//메시지 수신 시 showMessage 함수 호출
      });
   },
   onWebSocketError: (error) => {
      console.error('WebSocket 에러 발생: ', error);
   },
   onStompError: (frame) => {
      console.error('Broker 에러: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
   }
});

// 연결 상태에 따른 버튼 설정
function setConnected(connected) {
   $("#connect").prop("disabled", connected);
   $("#disconnect").prop("disabled", !connected);
   if (connected) {
      $("#conversation").show();
   } else {
      $("#conversation").hide();
   }
   $("#chatrooms").html(""); // 메시지 초기화
}

/*
* interface ServiceWorkerGlobalScopeEventMap extends WorkerGlobalScopeEventMap {
    "activate": ExtendableEvent;
    "fetch": FetchEvent;
    "install": ExtendableEvent;
    "message": ExtendableMessageEvent;
    "messageerror": MessageEvent;
    "notificationclick": NotificationEvent;
    "notificationclose": NotificationEvent;
    "push": PushEvent;
    "pushsubscriptionchange": Event;
}
* */




// WebSocket 연결 함수
function connect() {
   stompClient.activate();
}

// WebSocket 연결 해제 함수
function disconnect() {
   if (stompClient.active) { // stompClient.connected 대신 stompClient.active 사용
      stompClient.deactivate();//WebSocket 연결을 비활성화
      setConnected(false);//연결 상태를 나타내는 connected를 false로 설정하여, 연결이 끊겼다는 것을 화면이나 로직에서 알 수 있게 해 줌
      console.log("연결 해제");
   }
}

// 메시지 보내기 함수
function sendMessage() {
   const messageContent = $('#msg').val();
   if (messageContent.trim() === "") {
      alert("메시지를 입력하세요.");
      return;
   }

   const chatMessage = {
      chatMessageId: null, // 필요 시 설정
      sender: "사용자1", // 예시: 사용자 ID
      chatRoom: 1, // 채팅방 ID
      chatMessage: messageContent,
      createdAt: new Date(), // 현재 시간
      isRead: false, // 초기 상태
      image: null, // 필요 시 설정
      isLatest: true // 최신 메시지 여부
   };

   stompClient.publish({
      destination: "/app/chat/1", // 채팅방 ID 1로 메시지 전송
      body: JSON.stringify(chatMessage)
   });

   $('#msg').val(""); // 메시지 입력 필드 초기화
}


function showMessage(message) {// 수신된 메시지 표시
   $("#chatrooms").append("<tr><td>" + message + "</td></tr>");
}

// 페이지 로드 후 이벤트 핸들러 설정
$(function () {
   $("form").on('submit', (e) => e.preventDefault()); // 폼 제출 시 페이지 새로고침 방지
   $("#connect").click(() => connect()); // 연결 버튼 클릭 시 connect 함수 호출
   $("#disconnect").click(() => disconnect()); // 연결 해제 버튼 클릭 시 disconnect 함수 호출
   $("#send").click(() => sendMessage()); // 전송 버튼 클릭 시 sendMessage 함수 호출
});
