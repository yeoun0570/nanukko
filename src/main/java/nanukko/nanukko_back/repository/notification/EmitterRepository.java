package nanukko.nanukko_back.repository.notification;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {
    //SseEmitter 를 저장(Emitter 생성)
    //1. 새로운 연락망 등록
    // "userId_timestamp" 형식으로 저장
    // 예: "user123_1616234"
    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    // 이벤트를 저장(캐시 역할)
    // 사용자에게 전송되지 못한 이벤트를 캐시로 저장
    void saveEventCache(String eventCacheId, Object event);

    //특정 사용자와 관련된 모든 Emitter를 찾기
    //2. 특정 사용자의 모든 연락망 찾기
    // 예: user123의 모든 기기(브라우저)에 연결된 Emitter 찾기
    Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId);

    //특정 유저와 관련된 모든 이벤트를 찾음
    //캐시에서 이벤트 가져오기
    Map<String, Object> findAllEventCachesStartWithByUserId(String userId);

    // Emitter를 지움
    // 3. 연락망 삭제 (사용자가 웹사이트 종료 등)
    // 연결 종료된 Emitter 삭제
    void deleteById(String id);

    // 특정 유저와 관련된 모든 Emitter 지움
    void deleteAllEmittersByUserId(String userId);

    // 특정 유저와 관련된 캐시에 저장해놓은 모든 이벤트 지움
    void deleteAllEventCachesByUserId(String userId);
}
