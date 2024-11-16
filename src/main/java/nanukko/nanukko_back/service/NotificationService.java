package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.FrontendURL;
import nanukko.nanukko_back.domain.notification.Notification;
import nanukko.nanukko_back.domain.notification.NotificationType;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.notification.NotificationResponseDTO;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.repository.notification.EmitterRepository;
import nanukko.nanukko_back.repository.notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationService {
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final FrontendURL frontendURL;

    // SSE 연결 지속 시간 설정 (30분)
    private static final Long DEFAULT_TIMEOUT = 30L * 1000 * 30;

    // Emitter 아이디 생성에 사용할 메서드, userId와 시간 합침
    private String makeTimeIncludeId(String userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    // 알림 전송 메서드
    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .name("SSE")
                    .data(data)
            );
        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
        }
    }

    // 클라이언트가 마지막으로 받은 이벤트가 있는지 확인하는 메서드
    // lastEventId : 클라가 수신한 마지막 이벤트(데이터)의 id
    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isBlank(); //lastEventId가 존재하면 true를 반환
    }

    // 파라미터로 받은 lastEventId 보다 이후에 발생하고 캐시에 저장된 이벤트들 전송
    private void sendLostData(String lastEventId, String userId, String emitterId, SseEmitter emitter) {
        //특정 유정에 대한 이벤트들 모두 찾기
        Map<String, Object> eventCaches = emitterRepository.findAllEventCachesStartWithByUserId(userId);
        // 찾은 이벤트들 중에서 필터링으로 lastEventId 보다 이후에 발생한 이벤트들 sendNotification 으로 전송
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }


    //클라이언트가 알림을 구독하는 기능을 가진 메서드
    //즉, Emitter 만들어서 반환하여 클라이언트와 서버가 연결을 지속할 수 있도록 만드는 메서드
    public SseEmitter subscribe(String userId, String lastEventId) {
        // Emitter 아이디 생성
        String emitterId = makeTimeIncludeId(userId);
        // Emitter 생성
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        //onCompletion() 은 SSE 연결이 종료될 때 실행될 동작을 정의하는 메서드
        //비동기 요청이나 시간 초과되면 자동 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        //최초 연결시에 더미데이터가 없으면 503 에러가 나니 더미데이터 생성해서 전송
        //클라이언트에게 연결이 생성되었음을 알리는 더미 이벤트 전송
        //이를 통해 클라이언트와 서버 간의 연결이 유지되도록 한다
        String eventId = makeTimeIncludeId(userId);
        NotificationResponseDTO dummyNotification = NotificationResponseDTO.builder()
                .content("연결이 성공적으로 완료되었습니다.")
                .type(NotificationType.PAYMENT)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        sendNotification(emitter, eventId, emitterId, dummyNotification);

        //hasLostData(lastEventId)로 클라이언트의 마지막 이벤트가 존재하는지 확인
        //lastEventId가 존재하면 if문을 탄다.
        //sendLostData 로 lastEventId 이후의 이벤트들 전부 전송
        //즉, 클라이언트가 미수신한 이벤트 목록이 존재하면 전송하여 이벤트 유실을 예방
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, userId, emitterId, emitter);
        }

        //위의 로직으로 완성된 Emitter(연락망)을 반환
        //클라이언트가 해당 Emitter 을 받으면 이제 해당 연락망으로 계속해서 알림을 받는 것
        return emitter;
    }

    // 전송할 알림 데이터 생성하는 메서드
    private Notification createNotification(User receiver, NotificationType type, String content, String url) {
        return Notification.builder()
                .receiver(receiver)
                .type(type)
                .content(content)
                .url(url)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }


    //알림 데이터를 생성하고 그것을 클라이언트한테 전달하는 메서드
    private void send(User receiver, NotificationType type, String content, String url) {
        //파라미터로 받은 값들을 저장
        Notification notification = notificationRepository.save(
                createNotification(receiver, type, content, url)
        );

        //userId를 찾아서 선언하고 그것을 이용하여 eventId 생성
        String userId = receiver.getUserId();
        String eventId = userId + "_" + System.currentTimeMillis();

        //찾은 사용자에 대한 Emitter 찾기
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUserId(userId);
        //찾은 Emitter 에 이벤트 저장하고 그에 맞는 알림 데이터 설정
        emitters.forEach(
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notification);
                    sendNotification(emitter, eventId, key, NotificationResponseDTO.builder()
                            .notificationId(notification.getNoticeId())
                            .type(notification.getType())
                            .url(notification.getUrl())
                            .isRead(notification.isRead())
                            .content(notification.getContent())
                            .createdAt(LocalDateTime.now())
                            .build());
                }
        );
    }

    //////////////////////////////////////여기서부턴 알림 보내기위한 메서드 구현

    //사용자가 자신이 판매하는 상품이 결제가 되었을 때 배송해달라는 알림
    public void sendConfirmPaymentToSeller(String userId, Long productId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));


        String content = "상품이 판매되었습니다. 확인하고 배송을 보내주세요!";
        String url = frontendURL.getUrl() + "/payments/sale-products?productId=" + product.getProductId();

        send(receiver, NotificationType.PAYMENT, content, url);
    }
}
