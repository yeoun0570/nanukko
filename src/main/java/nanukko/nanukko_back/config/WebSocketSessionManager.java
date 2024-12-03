package nanukko.nanukko_back.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
public class WebSocketSessionManager {

    // 사용자 id를 키로, 세션 id 집합을 값으로 저장(한 사용자가 여러 브라우저/탭에서 접속 가능)
    private final Map<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser().getName();

        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        log.info("사용자 연결됨. 연결 정보 - userId: {}, sessionId: {}", userId, sessionId);
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser().getName();

        if(userId != null){
            Set<String> sessions = userSessions.get(userId);
            if(sessions != null){
                sessions.remove(sessionId);
                if(sessions.isEmpty()){
                    userSessions.remove(userId);
                }
            }
        }
        log.info("사용자 연결 안됨. disconnected 정보 - userId : {}, sessionId: {}", userId, sessionId);
    }


    //사용자 접속 상태 확인(하나라도 연결된 세션이 있으면 true
    public boolean isUserConnected(String userId){
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    // 현재 접속 중인 모든 사용자 Id  조회
    public  Set<String> getConnectedUsers(){
        return userSessions.keySet();
    }
}



