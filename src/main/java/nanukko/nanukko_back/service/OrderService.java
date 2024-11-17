package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.RestTemplateConfig;
import nanukko.nanukko_back.config.TossPaymentsConfig;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.order.OrderConfirmDTO;
import nanukko.nanukko_back.dto.order.OrderPageDTO;
import nanukko.nanukko_back.dto.order.OrderResponseDTO;
import nanukko.nanukko_back.repository.OrderRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class OrderService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    //RestTemplate : REST API를 호출하고 응답할 때까지 기다리는 동기 방식
    private final RestTemplateConfig restTemplate;
    private final NotificationService notificationService;
    private final TossPaymentsConfig tossPaymentsConfig;

    //결제창 페이지에 출력할 데이터 정의
    @Transactional(readOnly = true)
    public OrderPageDTO getOrder(Long productId, String userId) {
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        //수수료 계산
        int productPrice = product.getPrice();
        int chargeAmount = (int) (productPrice * 0.035);
        int totalAmount = productPrice + chargeAmount;

        return OrderPageDTO.builder()
                .thumbnailImage(product.getThumbnailImage())
                .productName(product.getProductName())
                .status(product.getStatus())
                .price(product.getPrice())
                .chargeAmount(chargeAmount)
                .totalAmount(totalAmount)
                .nickname(buyer.getNickname())
                .addrMain(buyer.getAddress().getAddrMain())
                .addrDetail(buyer.getAddress().getAddrDetail())
                .addrZipcode(buyer.getAddress().getAddrZipcode())
                .mobile(buyer.getMobile())
                .shippingFree(product.getShippingFree())
                .build();
    }


    //토스 API와 통신할 HTTP 헤더 설정
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        //토스에선 Base64로 시크릿키를 인코딩 시켜줘야 한다.
        String encodedAuth = Base64.getEncoder().encodeToString(tossPaymentsConfig.getSecretKey().getBytes());
        //헤더 설정은 인가한다는 Authorization를 키로, Basic 에 인코딩된 시크릿키를 값으로 보내면 됨(JSON형태)
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    //결제 정보 상세 조회
    @Transactional(readOnly = true)
    public OrderResponseDTO getPaymentDetail(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);
        dto.setProductName(order.getProduct().getProductName());
        dto.setBuyerId(order.getBuyer().getUserId());
        dto.setSellerId(order.getProduct().getSeller().getUserId());
        return dto;
    }

    @Transactional(timeout = 30)
    //결제 처리 메서드 -> 결제 정보 저장하고 에스크로로 홀딩 처리
    public OrderResponseDTO processPayment(OrderConfirmDTO confirmDTO) {
        try {
            // 핵심 결제 처리 로직만 별도 트랜잭션으로 분리
            OrderResponseDTO result = processPaymentCore(confirmDTO);

            // 결제완료 했을 시 판매자에게 알림 전송
            notificationService.sendConfirmPaymentToSeller(
                    result.getSellerId(),
                    result.getProductId()
            );


            return result;
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생 - productId: {}, error: {}",
                    confirmDTO.getPaymentKey(), e.getMessage());
            // 트랜잭션 어노테이션으로 인해 자동 롤백됨
            throw new RuntimeException("결제 처리 실패", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected OrderResponseDTO processPaymentCore(OrderConfirmDTO confirmDTO) {
        double chargeRate = 0.035; //3.5% 수수료율

        log.info("전달받은 값 - orderId: {}, buyerId: {}, productId: {}",
                confirmDTO.getOrderId(), confirmDTO.getBuyerId(), confirmDTO.getProductId());

        //구매자 조회
        User buyer = userRepository.findById(confirmDTO.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다."));

        //상품 조회
        Product product = productRepository.findById(confirmDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        log.info("결제 처리 시작 - orderId: {}, buyerId: {}, productId: {}",
                confirmDTO.getOrderId(), confirmDTO.getBuyerId(), confirmDTO.getProductId());

        //수수료 + 배송비 계산
        int productAmount = product.getPrice();
        int chargeAmount = (int) (productAmount * chargeRate);
        int shippingFree = product.getShippingFree();
        int totalAmount = productAmount + chargeAmount + shippingFree;


        //구매자의 잔액 확인
        if (buyer.getBalance() < totalAmount) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        //구매자 잔액 차감
        buyer.subtractBalance(totalAmount);
        userRepository.save(buyer);
        log.info("구매자 잔액 차감 - buyerId: {}, 차감액: {}, 남은 잔액: {}",
                confirmDTO.getBuyerId(), totalAmount, buyer.getBalance());
        //새로운 주문(결제) 엔티티 생성
        //받은 결제에 대해 그것을 에스크로 홀딩 시키고 VO에 저장
        Orders order = Orders.builder()
                .orderId(confirmDTO.getOrderId())
                .buyer(buyer)
                .product(product)
                .paymentKey(confirmDTO.getPaymentKey())
                .productAmount(productAmount)
                .chargeAmount(chargeAmount)
                .shippingFree(shippingFree)
                .totalAmount(totalAmount)
                .status(PaymentStatus.ESCROW_HOLDING)
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();
        Orders savedOrder = orderRepository.save(order);

        //결제 상태 예약중으로 변경
        product.updateStatus(ProductStatus.RESERVED);
        productRepository.save(product);
        log.info("상품 상태 변경 완료 - productId: {}, status: {}",
                product.getProductId(), product.getStatus());

        // ModelMapper 사용 전에 명시적으로 값을 설정
        OrderResponseDTO responseDTO = modelMapper.map(savedOrder, OrderResponseDTO.class);
        responseDTO.setProductId(product.getProductId());
        responseDTO.setSellerId(product.getSeller().getUserId());

        // 값이 제대로 설정되었는지 로그로 확인
        log.info("알림 전송을 위한 정보 - sellerId: {}, productId: {}",
                responseDTO.getSellerId(), responseDTO.getProductId());

        return responseDTO;
    }

    @Transactional(timeout = 30)
    //결제 자동 승인 처리 메서드
    public OrderResponseDTO confirmPayment(OrderConfirmDTO request) {
        //결제 승인 처리해주는 메서드
        //원래 토스 API에선 결제 확인을 누르고 결제를 진행해준 뒤 다시 결제승인까지 해줘야 처리된다.
        //그 과정에서 결제를 진행하면 결제 승인 시켜서 바로 결제가 되도록 해주는것
        //결제만 하면 결제 상태가 IN_PROGRESS 로 변경되고, 결제 승인하면 DONE 으로 변경됨
        //즉, IN_PROGRESS 과정에서 바로 DONE 으로 만들어주는 메서드

        log.info("결제 승인 시작 - paymentKey: {}, productId: {}, buyerId: {}",
                request.getPaymentKey(), request.getProductId(), request.getBuyerId());


        try {
            // 토스페이먼츠 API 요청용 데이터 생성
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("paymentKey", request.getPaymentKey());
            payloadMap.put("orderId", request.getOrderId());
            payloadMap.put("amount", request.getAmount());


            // API 호출 전 요청 데이터 로깅
            log.info("API 요청 데이터: {}", payloadMap);

            //토스 페이먼츠 결제 승인 API 호출
            //RestTemplate을 이용하여 결제 승인 URL에 POST 요청을 보내 OrderResponseDTO로 응답받기
            ResponseEntity<OrderResponseDTO> response = restTemplate.restTemplate().exchange(
                    //토스 결제 승인 API 호출 URL
                    "https://api.tosspayments.com/v1/payments/confirm",
                    //POST 방식으로 요청
                    HttpMethod.POST,
                    //OrderConfirmDTO로 받은 값들을 body, 앞서 설정한 헤더를 헤더로 설정하여 요청
                    new HttpEntity<>(payloadMap, getHeaders()),
                    OrderResponseDTO.class);

            // API 응답 로깅
            log.info("토스 API 응답: {}", response.getBody());

            //결제 승인이 완료되면 에스크로 처리진행
            //위에 결제승인 API로 응답받은 body 가 null 값이 아니고 결제상태가 DONE 이면 진행
            if (response.getBody() != null && response.getBody().getStatus() == PaymentStatus.DONE) {
                return processPayment(request);
            } else {
                throw new RuntimeException("결제 승인 실패");
            }
        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            throw new RuntimeException("결제 승인 실패", e);
        }
    }

    @Transactional
    //구매 확정 메서드
    //판매자에게 돈이 간다.
    public OrderResponseDTO confirmPurchase(String orderId) {
        //주문했던 주문 찾기
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != PaymentStatus.ESCROW_HOLDING) {
            throw new IllegalArgumentException("에스크로 상태가 아닙니다.");
        }

        // 판매자 정산 처리
        processSellerPayment(order);

        // 상태 업데이트
        updateOrderAndProductStatus(order);
        // 에스크로 해제는 비동기로 처리
        CompletableFuture.runAsync(() -> {
            try {
                releaseEscrowPayment(order.getPaymentKey());
                log.info("에스크로 해제 완료 - paymentKey: {}", order.getPaymentKey());
            } catch (Exception e) {
                log.warn("에스크로 해제 실패 (무시됨) - paymentKey: {}, error: {}",
                        order.getPaymentKey(), e.getMessage());
            }
        });

        // 판매자에게 구매확정 알림 전송
        notificationService.sendConfirmPurchaseToSeller(
                order.getProduct().getSeller().getUserId(), order.getProduct().getProductId());

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    //판매자 계좌에 금액 추가 메서드 별도의 트랜잭션으로 관리(커넥션 풀 때문에)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processSellerPayment(Orders order) {
        try {
            User seller = order.getProduct().getSeller();
            seller.addBalance(order.getProductAmount() + order.getShippingFree());
            userRepository.save(seller);
            log.info("판매자 잔액 증가 - sellerId: {}, 증가액: {}, 최종 잔액: {}",
                    seller.getUserId(), order.getTotalAmount(), seller.getBalance());
        } catch (Exception e) {
            log.error("판매자 정산 처리 실패 - orderId: {}, error: {}",
                    order.getOrderId(), e.getMessage());
            throw e; // 상위로 예외를 전파하여 전체 트랜잭션 롤백
        }
    }

    //주문 상태 업데이트 하는것 별도의 트랜잭션으로 관리(커넥션 풀 때문에)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateOrderAndProductStatus(Orders order) {
        try {
            // 주문 상태 업데이트
            order.updateReleased(PaymentStatus.ESCROW_RELEASED, LocalDateTime.now());

            // 상품 상태 업데이트
            Product product = order.getProduct();
            product.updateStatus(ProductStatus.SOLD_OUT);
            productRepository.save(product);
        } catch (Exception e) {
            log.error("주문/상품 상태 업데이트 실패 - orderId: {}, error: {}",
                    order.getOrderId(), e.getMessage());
            throw e; // 상위로 예외를 전파하여 전체 트랜잭션 롤백
        }
    }

    //구매 확정하여 에스크로 해제
    private void releaseEscrowPayment(String paymentKey) {
        //해당 토스 페이먼츠 엔드포인트를 호출하면 에스크로 해제하고 판매 정산
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/release";

        HttpHeaders headers = new HttpHeaders();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((tossPaymentsConfig.getSecretKey()).getBytes());
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.info("에스크로 해제 API 호출 시작 - paymentKey: {}", paymentKey);

            HttpEntity<String> entity = new HttpEntity<>("{}", headers);

            ResponseEntity<String> response = restTemplate.restTemplate().exchange(
                    url, HttpMethod.POST, entity, String.class
            );

            log.info("에스크로 해제 API 응답 - stauts: {}", response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("에스크로 해제 API 호출 실패 - stauts: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("에스크로 해제 실패 - paymentKey: {}", paymentKey, e);
            throw new RuntimeException("에스크로 해제에 실패했습니다.", e);
        }
    }

    @Transactional
    //에스크로 상태일 때 결제 취소해서 환불받기
    public OrderResponseDTO cancelOrder(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (order.getStatus() != PaymentStatus.ESCROW_HOLDING) {
            throw new IllegalStateException("에스크로 상태에서만 취소가 가능합니다.");
        }
        // 환불 처리
        processRefund(order);

        // 주문 취소 처리
        order.cancel();

        // 상품 판매 상태로 재변경
        updateProductCancelOrder(order);

        //판매자에게 결제 취소 알림 전송
        notificationService.sendCancelPaymentToSeller(
                order.getProduct().getSeller().getUserId(), order.getProduct().getProductId()
        );

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    // 금액 환불 별도의 트랜잭션 처리(커넥션풀 최적화)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processRefund(Orders order) {
        try {
            //구매자에게 전체 금액 환불
            User buyer = order.getBuyer();
            buyer.addBalance(order.getTotalAmount());
            userRepository.save(buyer);
        } catch (Exception e) {
            log.error("환불 취소 실패 - orderId: {}, error: {}",
                    order.getOrderId(), e.getMessage());
            throw e;
        }
    }

    // 주문 취소로 인한 상품 상태 판매로 변경 - 별도의 트랜잭션 처리(커넥션풀 최적화)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateProductCancelOrder(Orders order) {
        try {
            Product product = order.getProduct();
            product.updateStatus(ProductStatus.SELLING);
        } catch (Exception e) {
            log.error("상품 재판매 상태 업데이트 실패 - orderId: {}, error: {}",
                    order.getOrderId(), e.getMessage());
        }
    }


    // 매일 자정에 실행되는 스케줄러
    // 에스크로 상태가 되고 10일 지나면 자동으로 구매확정 시켜주는 메서드
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoConfirmExpiredEscrow() {
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);

        try {
            // 14일이 지난 에스크로 주문들 조회
            List<Orders> expiredOrders = orderRepository.findByStatusAndEscrowDeadlineBefore(
                    PaymentStatus.ESCROW_HOLDING,
                    tenDaysAgo
            );

            log.info("자동 구매확정 대상 주문 수: {}", expiredOrders.size());

            for (Orders order : expiredOrders) {
                try {
                    // 판매자에게 금액 지급
                    User seller = order.getProduct().getSeller();
                    seller.addBalance(order.getProductAmount());
                    userRepository.save(seller);

                    log.info("자동 구매확정 판매자 정산 완료 - orderId: {}, sellerId: {}, amount: {}",
                            order.getOrderId(), seller.getUserId(), order.getProductAmount());

                    // 주문 상태 업데이트
                    order.updateReleased(PaymentStatus.ESCROW_RELEASED, LocalDateTime.now());

                    // 상품 상태 변경
                    Product product = order.getProduct();
                    product.updateStatus(ProductStatus.SOLD_OUT);
                    productRepository.save(product);

                    log.info("자동 구매확정 완료 - orderId: {}", order.getOrderId());

                } catch (Exception e) {
                    log.error("자동 구매확정 처리 실패 - orderId: {}", order.getOrderId(), e);
                    // 개별 주문 실패 시 다음 주문 처리를 위해 계속 진행
                }
            }
        } catch (Exception e) {
            log.error("자동 구매확정 배치 처리 실패", e);
        }
    }
}
