package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.FrontendURL;
import nanukko.nanukko_back.domain.notification.Notification;
import nanukko.nanukko_back.domain.notification.NotificationType;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.notification.NotificationIsReadDTO;
import nanukko.nanukko_back.dto.notification.NotificationResponseDTO;
import nanukko.nanukko_back.repository.OrderRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.repository.notification.EmitterRepository;
import nanukko.nanukko_back.repository.notification.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationService {
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

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
                .type(NotificationType.CONNECT)
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
    @Transactional
    public void send(User receiver, NotificationType type, String content, String url) {
        try {
            log.info("알림 전송 시작 - userId: {}, type: {}", receiver.getUserId(), type);

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
                                .createdAt(notification.getCreatedAt())
                                .build());
                    }
            );
        } catch (Exception e) {
            log.error("알림 전송 중 에러 발생: ", e);
            throw new RuntimeException("알림 전송 실패", e);
        }
    }

    // 이전 알림 조회
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getPreviousNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByReceiverUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(notification -> NotificationResponseDTO.builder()
                        .notificationId(notification.getNoticeId())
                        .type(notification.getType())
                        .url(notification.getUrl())
                        .isRead(notification.isRead())
                        .content(notification.getContent())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .toList();
    }

    // 알림 읽음 처리
    @Transactional
    public NotificationIsReadDTO markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

        // 읽음 처리 및 읽은 시간 업데이트
        notification.updateRead(true, LocalDateTime.now());

        notificationRepository.save(notification);
        log.info("알림 읽음 처리 확인 - notificationId : {}, isRead: {}",
                notification.getNoticeId(), notification.isRead());

        return NotificationIsReadDTO.builder()
                .notificationId(notification.getNoticeId())
                .isRead(notification.isRead())
                .build();
    }

    // 알림 모두 읽음 처리
    @Transactional
    public List<NotificationIsReadDTO> markAllAsRead(String userId) {
        List<Notification> notifications = notificationRepository
                .findByReceiverUserIdAndIsReadFalse(userId);

        notifications.forEach(notification -> {
            notification.updateRead(true, LocalDateTime.now());
        });

        notificationRepository.saveAll(notifications);

        return notifications.stream()
                .map(notification -> NotificationIsReadDTO.builder()
                        .notificationId(notification.getNoticeId())
                        .isRead(notification.isRead())
                        .build())
                .collect(Collectors.toList());
    }

    // 알림 삭제
    @Transactional
    public void removeNotice(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }


    //////////////////////////////////////여기서부턴 알림 보내기위한 메서드 구현(공통으로 사용할 애들)

    //사용자 검색
    @Transactional(readOnly = true)
    protected User findAndValidateUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    //상품 검색
    @Transactional(readOnly = true)
    protected void findAndValidateProduct(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
    }

    //주문 검색
    @Transactional(readOnly = true)
    protected Orders findAndValidateOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    //////////////////////////////////////여기서부턴 알림 보내기위한 메서드 구현(판매자)

    //사용자가 자신이 판매하는 상품이 결제가 되었을 때 배송해달라는 알림
    @Transactional
    public void sendConfirmPaymentToSeller(String userId, Long productId, String buyerId) {
        log.info("판매자 결제 확인 알림 전송 시작 - userId: {}, productId: {}, buyerId: {}", userId, productId, buyerId);

        // DB 조회 및 검증
        User receiver = findAndValidateUser(userId);
        findAndValidateProduct(productId);
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다."));


        String url = frontendURL.getUrl() + "/my-store/sale-products";
        String content = String.format("상품이 판매되었습니다. 확인하고 배송을 보내주세요!|||%s|||(%s) %s|||%s|||%s",
                buyer.getNickname(),
                buyer.getAddress().getAddrZipcode(),
                buyer.getAddress().getAddrMain(),
                buyer.getAddress().getAddrDetail(),
                buyer.getMobile());


        send(receiver, NotificationType.PAYMENT, content, url);
        log.info("판매자 결제 확인 알림 전송 완료");
    }

    // 결제가 취소되었다는 알림
    @Transactional
    public void sendCancelPaymentToSeller(String userId, Long productId) {
        // DB 조회 및 검증
        User receiver = findAndValidateUser(userId);
        findAndValidateProduct(productId);

        String content = "결제가 취소되었습니다. 상품을 확인해주세요.";
        String url = frontendURL.getUrl() + "/my-store/sale-products";

        send(receiver, NotificationType.PAYMENT, content, url);
        log.info("판매자 결제 취소 알림 전송 완료");
    }

    // 구매 확정 확인하라는 알림
    @Transactional
    public void sendConfirmPurchaseToSeller(String userId, Long productId) {
        // DB 조회 및 검증
        User receiver = findAndValidateUser(userId);
        findAndValidateProduct(productId);

        String content = "구매가 확정되었습니다. 상품을 확인해주세요.";
        String url = frontendURL.getUrl() + "/my-store/sale-products";

        send(receiver, NotificationType.PAYMENT, content, url);
        log.info("판매자 구매 확정 알림 전송 완료");
    }

    // 리뷰 작성 완료 알림
    @Transactional
    public void sendConfirmReview(String userId, String buyerName) {
        // DB 조회 및 검증
        User receiver = findAndValidateUser(userId);

        String content = buyerName + "님이 리뷰를 작성주었어요! 확인해보세요!";
        String url = frontendURL.getUrl() + "/my-store/reviews";

        send(receiver, NotificationType.REVIEW, content, url);
        log.info("판매자 구매 확정 알림 전송 완료");
    }

    //////////////////////////////////////여기서부턴 알림 보내기위한 메서드 구현(구매자)

    //배송시작
    @Transactional
    public void sendStartDeliveryToBuyer(String userId, String orderId) {
        User receiver = findAndValidateUser(userId);
        Orders order = findAndValidateOrder(orderId);

        String content = order.getProduct().getProductName()
                + " 배송이 출발했어요! 확인해주세요!";
        String url = frontendURL.getUrl() + "/my-store/buy-products";

        send(receiver, NotificationType.DELIVERY, content, url);
    }

    //배송완료(구매확정, 리뷰작성요청)
    @Transactional
    public void sendDeliveredToBuyer(String userId, String orderId) {
        User receiver = findAndValidateUser(userId);
        Orders order = findAndValidateOrder(orderId);

        String content = order.getProduct().getProductName()
                + " 배송이 도착했어요! "
                + order.getProduct().getSeller().getNickname()
                + "님에게 리뷰를 작성해볼까요?(3일 뒤 자동으로 구매확정 됩니다.)";
        String url = frontendURL.getUrl() + "/my-store/buy-products";

        send(receiver, NotificationType.DELIVERY, content, url);
    }
}
