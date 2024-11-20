package nanukko.nanukko_back.repository.notification;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class EmitterRepositoryImpl implements EmitterRepository {

    // Emitter 보관소
    // 모든 Emitter를 저장하는 ConcurrentHashMap
    // ConcurrentHashMap을 사용하는 이유: 동시성 문제 처리를 위해
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 이벤트 임시 저장소
    // 이벤트를 저장하는 ConcurrentHashMap
    // 각 이벤트를 임시로 저장하여 나중에 재연결시 미전송된 이벤트를 전송하기 위함
    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    @Override
    public SseEmitter save(String emitterId, SseEmitter sseEmitter) {
        // Emitter를 키를 부여해서 저장하고 그것을 반환
        // emitterId는 "userId_currentTimestamp" 형식으로 저장하는 것을 권장
        emitters.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        // 발생한 이벤트를 키를 부여해서 저장
        // eventCacheId는 "userId_eventId" 형식으로 저장하는 것을 권장
        eventCache.put(eventCacheId, event);
    }

    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithByUserId(String userId) {
        // userId로 시작하는 모든 Emitter 를 찾아서 반환
        // 한 유저가 여러 브라우저나 기기로 접속할 수 있으니 모든 연결을 찾아야 함
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<String, Object> findAllEventCachesStartWithByUserId(String userId) {
        // userId로 시작하는 모든 이벤트 캐시를 찾아서 반환
        // 미전송된 이벤트들을 찾기 위함
        return eventCache.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(userId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void deleteById(String id) {
        // 특정 Emitter를 찾아서 제거
        emitters.remove(id);
    }

    @Override
    public void deleteAllEmittersByUserId(String userId) {
        // 특정 유저의 모든 Emitter를 제거
        // 유저가 로그아웃하거나 세션이 종료될 때 호출
        emitters.forEach((key, emitter) -> {
            if (key.startsWith(userId)) {
                emitters.remove(key);
            }
        });
    }

    @Override
    public void deleteAllEventCachesByUserId(String userId) {
        // 특정 유저의 모든 이벤트 캐시를 제거
        eventCache.forEach((key, event) -> {
            if (key.startsWith(userId)) {
                eventCache.remove(key);
            }
        });
    }
}
