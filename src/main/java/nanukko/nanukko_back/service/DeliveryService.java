package nanukko.nanukko_back.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.DeliveryWebhookConfig;
import nanukko.nanukko_back.domain.order.Delivery;
import nanukko.nanukko_back.domain.order.DeliveryStatus;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.dto.order.DeliveryRegistrationDTO;
import nanukko.nanukko_back.dto.order.DeliveryResponseDTO;
import nanukko.nanukko_back.dto.order.DeliveryUpdateStatusDTO;
import nanukko.nanukko_back.repository.DeliveryRepository;
import nanukko.nanukko_back.repository.OrderRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class DeliveryService {
    private final ModelMapper modelMapper;
    private final DeliveryWebhookConfig deliveryWebhookConfig;
    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final MailService mailService;

    // 판매자가 운송장 등록
    @Transactional
    public DeliveryResponseDTO registerDelivery(DeliveryRegistrationDTO dto) {
        // 상품 정보 조회
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 주문 조회
        Orders order = orderRepository.findByProductAndProductStatus(product, ProductStatus.RESERVED)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        // 주문 상태 검증
        if (product.getStatus() != ProductStatus.RESERVED) {
            throw new IllegalStateException("예약중인 상품만 운송장을 등록할 수 있습니다.");
        }

        // 이미 배송이 등록되어 있는지 확인
        if (deliveryRepository.findByOrderOrderId(order.getOrderId()).isPresent()) {
            throw new IllegalStateException("이미 배송이 등록된 주문입니다.");
        }

        // 배송 정보 저장
        Delivery delivery = Delivery.builder()
                .order(order)
                .carrierId(dto.getCarrierId())
                .trackingNumber(dto.getTrackingNumber())
                .status(DeliveryStatus.PREPARING)
                .createdAt(LocalDateTime.now())
                .build();
        delivery = deliveryRepository.save(delivery);

        // 배송 추적 웹훅 등록
        registerOrRefreshWebhook(delivery);

        // 상품 배송여부
        product.updateHasDelivery(true);

        return modelMapper.map(delivery, DeliveryResponseDTO.class);
    }

    // 배송 추적 만료시간 자동 갱신 메서드
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24시간마다 실행
    public void refreshTrackingWebhooks() {
        List<Delivery> activeDeliveries = deliveryRepository.findByStatus(DeliveryStatus.IN_TRANSIT);

        //각 배송 정보에 대해 웹훅 갱신
        for (Delivery delivery : activeDeliveries) {
            try {
                registerOrRefreshWebhook(delivery);
                log.info("Webhook 갱신 성공 - trackingNumber : {}", delivery.getTrackingNumber());
            } catch (Exception e) {
                log.error("Webhook 갱신 실패 - trackingNumber: {}", delivery.getTrackingNumber());
            }
        }
    }

    // 배송 추적 웹훅 등록/갱신 메서드
    private void registerOrRefreshWebhook(Delivery delivery) {
        try {
            // GraphQL mutation 쿼리 생성
            String mutation = """
                    mutation {
                        registerTrackWebhook(
                            carrierId: "%s"
                            trackingNumber: "%s"
                            callbackUrl: "%s"
                            expirationTime: "%s"
                        ) {
                            success
                            message
                        }
                    }
                    """.formatted(
                    delivery.getCarrierId(),
                    delivery.getTrackingNumber(),
                    deliveryWebhookConfig.getCallbackUrl(),
                    LocalDateTime.now().plusDays(2).toString()
            );

            // Authorization 헤더 생성
            String credentials = deliveryWebhookConfig.getClientId() + ":" + deliveryWebhookConfig.getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

            // API 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // GraphQL API 엔드포인트 URL
            String apiUrl = "https://apis.tracker.delivery/graphql";

            // API 요청 본문 생성
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", mutation);

            // API 호출 실행
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            //응답 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("배송 추적 웹훅 등록 실패: " + response.getBody());
            }

            log.info("배송 추적 웹훅 등록 성공 - trackingNumber: {}", delivery.getTrackingNumber());
        } catch (Exception e) {
            log.error("배송 추적 웹훅 등록 실패: ", e);
            throw new RuntimeException("배송 추적 등록 실패: ", e);
        }
    }

    // 웹훅으로 받은 배송 상태 업데이트
    @Transactional
    public void updateDeliveryStatusFromWebhook(String trackingNumber, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        // 현재 상태와 같으면 무시(중복 상태 업데이트 방지)
        if (delivery.getStatus() == status) {
            log.info("이미 같은 상태입니다. trackingNumber: {}, status: {}", trackingNumber, status);

            if (status == DeliveryStatus.IN_TRANSIT) {
                // 구매자에게 알림 발송 -> 배송이 시작되었다고 알림
                notificationService.sendStartDeliveryToBuyer(
                        delivery.getOrder().getBuyer().getUserId(),
                        delivery.getOrder().getOrderId()
                );

                // 배송시작 시 구매자에게 메일 전송
                // 비동기로 알림과 메일 전송
                CompletableFuture.runAsync(() -> {
                    try {
                        mailService.sendMailStartDeliveryToBuyer(
                                delivery.getOrder().getBuyer().getUserId(),
                                delivery.getOrder().getOrderId()
                        );
                    } catch (Exception e) {
                        log.error("메일 전송 실패", e);
                    }
                });
            }
        }

        delivery.updateStatus(status);

        // 주문 상태 및 알림 처리
        handleDeliveryStatusChange(delivery);
    }

    // 배송 상태 변경에 따른 처리 메서드
    private void handleDeliveryStatusChange(Delivery delivery) {
        Orders order = delivery.getOrder();

        switch (delivery.getStatus()) {
            case IN_TRANSIT -> {
                // 배송 시작 시 결제 취소 불가능하도록 처리
                if (order.getStatus() == PaymentStatus.ESCROW_HOLDING) {
                    order.updateReleased(PaymentStatus.IN_DELIVERY, null);
                }

                // 구매자에게 알림 발송 -> 배송이 시작되었다고 알림
                notificationService.sendStartDeliveryToBuyer(
                        delivery.getOrder().getBuyer().getUserId(),
                        delivery.getOrder().getOrderId()
                );

                // 배송시작 시 구매자에게 메일 전송
                // 비동기로 알림과 메일 전송
                CompletableFuture.runAsync(() -> {
                    try {
                        mailService.sendMailStartDeliveryToBuyer(
                                delivery.getOrder().getBuyer().getUserId(),
                                delivery.getOrder().getOrderId()
                        );
                    } catch (Exception e) {
                        log.error("메일 전송 실패", e);
                    }
                });
            }
            case DELIVERED -> {
                // 배송 완료되면 3일 후로 escrowDeadline 설정
                order.updateEscrowDeadline(LocalDateTime.now().plusDays(3));
                order.updateDelivery(PaymentStatus.DELIVERED);

                // 구매자에게 알림 -> 배송이 완료되었으니 3일 이내에 구매확정 하지 않으면 구매확정 된다는 얘기
                notificationService.sendDeliveredToBuyer(order.getBuyer().getUserId(), order.getOrderId());

                // 배송완료 시 구매자에게 메일 전송
                // 비동기로 알림과 메일 전송
                CompletableFuture.runAsync(() -> {
                    try {
                        mailService.sendMailDeliveredToBuyer(
                                delivery.getOrder().getBuyer().getUserId(),
                                delivery.getOrder().getOrderId()
                        );
                    } catch (Exception e) {
                        log.error("메일 전송 실패", e);
                    }
                });
            }
        }
    }

    //////////테스트를 위한 배송상태 업데이트

    @Transactional
    public DeliveryUpdateStatusDTO updateDeliveryStatus(DeliveryUpdateStatusDTO dto) {
        Delivery delivery = deliveryRepository.findByTrackingNumber(dto.getTrackingNumber())
                .orElseThrow(() -> new IllegalArgumentException("배송을 찾을 수 없습니다."));

        // 현재 상태와 같으면 무시
        if (delivery.getStatus() == dto.getStatus()) {
            log.info("이미 같은 상태입니다. trackingNumber: {}, status: {}",
                    dto.getTrackingNumber(), dto.getStatus());
            return modelMapper.map(delivery, DeliveryUpdateStatusDTO.class);
        }

        //상태 업데이트
        delivery.updateStatus(dto.getStatus());
        deliveryRepository.save(delivery);

        // 주문 상태 및 알림 처리
        handleDeliveryStatusChange(delivery);

        return modelMapper.map(delivery, DeliveryUpdateStatusDTO.class);
    }
}
