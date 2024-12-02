package nanukko.nanukko_back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class StompConfig implements WebSocketMessageBrokerConfigurer {



    /*엔드포인트 생성해서 클라이언트가 webSocket에 연결할 수 있도록 함(핸드셰이크를 위한 설정)*/
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")//클라이언트 웹소켓 연결을 위한 엔드포인트(ex) ws://localhost:8080/ws-stomp)
                .setAllowedOrigins(//CORS 설정
                        "http://localhost:3000",  // Nuxt 개발 서버
                        "http://localhost:8080"   // 백엔드 개발 서버
                )
                .withSockJS()//추가 옵션으로 웹소켓을 지원하지 않는 브라우저와의 호환성을 위해 폴백 메커니즘 제공(웹소켓 사용 안되는 호나경에서 XHR Polling을 사용하여 유사한 기능을 제공함)
                .setHeartbeatTime(25000)           // 클라이언트의 하트비트 전송 간격 (ms) - 연결 유지 확인용
                .setDisconnectDelay(5000);         // 연결 종료 시 지연 시간 (ms) - 재연결 기회 제공
    }

    /*메시지 브로커 설정*/
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic: 1:N 메시징에 사용 (채팅방 내 전체 메시지)
        // /queue: 1:1 메시징에 사용 (개인 메시지)
        registry.enableSimpleBroker("/topic","/queue");//메시지를 전달할 경로 설정하여 메시지를 브로커가 처리하게 함

        /*바로 브로커로 가는 경우가 아니라 메시지의 처리나 가공이 필요한 경우 핸들러를 타게 할 수 있는 설정*/
        // 클라이언트가 메시지를 보낼 때 사용할 프리픽스 설정
        registry.setApplicationDestinationPrefixes("/app");//클라이언트가 서버로 메시지를 보낼 때 사용할 접두사를 지정
        // 예: /app/chat/send로 메시지를 보내면 @MessageMapping("/chat/send")가 처리

        // 특정 사용자에게 메시지를 보낼 때 사용할 프리픽스
        registry.setUserDestinationPrefix("/user");
        // 예: /user/123/queue/messages는 ID가 123인 사용자에게만 메시지 전송

    }

    //WebSocket 전송 제한 설정(메시지 크기, 시간 제한 등을 설정)
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(64 * 1024)     // 메시지 크기 제한: 64KB(너무 큰 메시지로 인한 서버 부하 방지)
                .setSendTimeLimit(20 * 1000)        // 메시지 전송 시간 제한: 20초(답이 늦어지는 경우 타임아웃 처리)
                .setSendBufferSizeLimit(512 * 1024)
                .setTimeToFirstMessage(30 * 1000);   // 첫 메시지 수신 대기 시간 (30초);// 버퍼 크기 제한: 512KB(메모리 사용량 제어)
    }

    // 추가: 클라이언트 인바운드 채널 설정 (메시지 전처리, 인증 등)
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {//Client에서 메시지를 보내게 될 때 interceptor를 통해 핸들링
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 연결 시 토큰 검증, JWT 토큰 검증
                    // 검증 실패시 연결 거부
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);

                    }
                }
                return message;
            }
        });
    }

    /**
     * 하트비트 전송을 위한 스케줄러 빈 설정
     * 주기적으로 하트비트를 전송하여 연결 상태를 확인
     */
    @Bean
    public ThreadPoolTaskScheduler heartBeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);                        // 단일 스레드 사용 (하트비트용)
        scheduler.setThreadNamePrefix("wss-heartbeat-"); // 스레드 이름 프리픽스 설정
        scheduler.setDaemon(true);  // 애플리케이션 종료 시 스레드도 함께 종료
        return scheduler;
    }
}
