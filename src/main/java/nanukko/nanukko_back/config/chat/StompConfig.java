package nanukko.nanukko_back.config.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
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
        registry.addEndpoint("/ws-stomp")//클라이언트 웹소켓 연결을 위한 엔드포인트
                .setAllowedOrigins("http://localhost:8080")//CORS 설정
                .withSockJS();//추가 옵션으로 웹소켓을 지원하지 않는 브라우저와의 호환성을 위해 폴백 메커니즘 제공(웹소켓 사용 안되는 호나경에서 XHR Polling을 사용하여 유사한 기능을 제공함)

    }

    /*메시지 브로커 설정*/
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic","/queue");//메시지를 전달할 경로 설정하여 메시지를 브로커가 처리하게 함
        /*바로 브로커로 가는 경우가 아니라 메시지의 처리나 가공이 필요한 경우 핸들러를 타게 할 수 있는 설정*/
        registry.setApplicationDestinationPrefixes("/app");//클라이언트가 서버로 메시지를 보낼 때 사용할 접두사를 지정
    }

    //WebSocket 전송 제한 설정
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration
                .setMessageSizeLimit(64 * 1024)     // 메시지 크기 제한: 64KB(너무 큰 메시지로 인한 서버 부하 방지)
                .setSendTimeLimit(20 * 1000)        // 메시지 전송 시간 제한: 20초(답이 늦어지는 경우 타임아웃 처리)
                .setSendBufferSizeLimit(512 * 1024);// 버퍼 크기 제한: 512KB(메모리 사용량 제어)
    }

    // 추가: 클라이언트 인바운드 채널 설정 (메시지 전처리, 인증 등)
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 여기에 인증 로직 추가 가능
                    // 예: JWT 토큰 검증 등
                }
                return message;
            }
        });
    }
}
