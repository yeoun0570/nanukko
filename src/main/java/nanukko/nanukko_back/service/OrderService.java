package nanukko.nanukko_back.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.RestTemplateConfig;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.product.ProductStatus;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.order.OrderConfirmDTO;
import nanukko.nanukko_back.dto.order.OrderResponseDTO;
import nanukko.nanukko_back.repository.OrderRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final String secretKey = "test_sk_DnyRpQWGrN5Xzapz6XA0VKwv1M9E:";

    //토스 API와 통신할 HTTP 헤더 설정
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = secretKey;
        //토스에선 Base64로 시크릿키를 인코딩 시켜줘야 한다.
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        //헤더 설정은 인가한다는 Authorization를 키로, Basic 에 인코딩된 시크릿키를 값으로 보내면 됨(JSON형태)
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    //결제 정보 상세 조회
    public OrderResponseDTO getPaymentDetail(String orderId) {

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

//        //토스에서 특정 결제에 대한 상세정보가 저장된 URL
//        String url = "https://api.tosspayments.com/v1/payments/" + order.getPaymentKey();
//
//        //RestTemplate을 이용하여 결제 승인 URL에 GET 방식으로 요청을 보내 OrderResponseDTO로 응답받기
//        try {
//            ResponseEntity<OrderResponseDTO> response = restTemplate.restTemplate().exchange(
//                    url, //요청할 URL
//                    HttpMethod.GET, //HTTP 메서드 방식
//                    new HttpEntity<>(getHeaders()), // 요청 보낼 헤더
//                    OrderResponseDTO.class
//            );
//            return response.getBody();
//        } catch (Exception e) {
//            log.error("Payment details fetch failed", e);
//            throw new RuntimeException("결제 정보 조회에 실패했습니다.");
//        }

        OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);
        dto.setProductName(order.getProduct().getProductName());
        dto.setBuyerId(order.getBuyer().getUserId());
        dto.setSellerId(order.getProduct().getSeller().getUserId());
        return dto;
    }

    @Transactional
    //결제 처리 메서드 -> 결제 정보 저장하고 에스크로로 홀딩 처리
    public OrderResponseDTO processPayment(String paymentKey, String buyerId, Long productId) {
        try {
            double chargeRate = 0.035; //3.5% 수수료율

            log.info("전달받은 값 - paymentKey: {}, buyerId: {}, productId: {}", paymentKey, buyerId, productId);

            //구매자 조회
            User buyer = userRepository.findById(buyerId)
                    .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다."));

            //상품 조회
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            log.info("결제 처리 시작 - paymentKey: {}, buyerId: {}, productId: {}",
                    paymentKey, buyerId, productId);

            //수수료 계산
            int productAmount = product.getPrice();
            int chargeAmount = (int) (productAmount * chargeRate);
            int totalAmount = productAmount + chargeAmount;

            //구매자의 잔액 확인
            if (buyer.getBalance() < totalAmount) {
                throw new IllegalArgumentException("잔액이 부족합니다.");
            }
            //구매자 잔액 차감
            buyer.subtractBalance(totalAmount);
            userRepository.save(buyer);
            log.info("구매자 잔액 차감 - buyerId: {}, 차감액: {}, 남은 잔액: {}",
                    buyerId, totalAmount, buyer.getBalance());


            OrderResponseDTO tossResponse = getPaymentDetail(paymentKey);

            log.info("토스페이먼츠 응답 - status: {}, orderId: {}, amount: {}",
                    tossResponse.getStatus(), tossResponse.getOrderId(), totalAmount);

            //새로운 주문(결제) 엔티티 생성
            //받은 결제에 대해 그것을 에스크로 홀딩 시키고 VO에 저장
            Orders order = Orders.builder()
                    .orderId(tossResponse.getOrderId())
                    .buyer(buyer)
                    .product(product)
                    .paymentKey(paymentKey)
                    .productAmount(productAmount)
                    .chargeAmount(chargeAmount)
                    .totalAmount(totalAmount)
                    .status(PaymentStatus.ESCROW_HOLDING)
                    .createdAt(LocalDateTime.now())
                    .paidAt(LocalDateTime.now())
                    .escrowDeadline(LocalDateTime.now().plusDays(14)) //에스크로 되고 14일 이후에 자동 구매 확정
                    .build();
            Orders savedOrder = orderRepository.save(order);

            //결제 상태 예약중으로 변경
            product.updateStatus(ProductStatus.RESERVED);
            productRepository.save(product);

            log.info("상품 상태 변경 완료 - productId: {}, status: {}",
                    product.getProductId(), product.getStatus());

            return modelMapper.map(savedOrder, OrderResponseDTO.class);
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생 - productId: {}, error: {}",
                    productId, e.getMessage());
            // 트랜잭션 어노테이션으로 인해 자동 롤백됨
            throw new RuntimeException("결제 처리 실패", e);
        }
    }

    @Transactional
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
                return processPayment(
                        request.getPaymentKey(),
                        request.getBuyerId(),
                        request.getProductId()
                );
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

        //판매자 계좌에 금액 추가
        User seller = order.getProduct().getSeller();
        seller.addBalance(order.getProductAmount());
        userRepository.save(seller);
        log.info("판매자 잔액 증가 - sellerId: {}, 증가액: {}, 최종 잔액: {}",
                seller.getUserId(), order.getTotalAmount(), seller.getBalance());

        //주문 상태 업데이트
        try {

            //테스트 시크릿 키로는 에스크로 API 호출 불가능하여 스킵. 즉, 실패해도 진행되도록 try-catch 처리
            //토스 페이먼츠 API를 호출하여 판매자에게 돈을 이체
            try {
                releaseEscrowPayment(order.getPaymentKey());
                log.info("에스크로 해재 완료 - paymentKey: {}", order.getPaymentKey());
            } catch (Exception e) {
                log.warn("에스크로 해제 API 호출 실패 (테스트 환경인 현재는 무시) - error: {}", e.getMessage());
            }
            order.updateReleased(PaymentStatus.ESCROW_RELEASED, LocalDateTime.now());
        } catch (Exception e) {
            log.error("구매 확정 처리 실패 - orderId: {}", orderId, e);
            throw new RuntimeException("구매 확정 처리에 실패했습니다.", e);
        }

        //상품 판매완료 상태로 변경
        Product product = order.getProduct();
        product.updateStatus(ProductStatus.SOLD_OUT);
        productRepository.save(product);

        log.info("상품 상태 변경 완료 - productId: {}, status: {}",
                product.getProductId(), product.getStatus());

        return modelMapper.map(order, OrderResponseDTO.class);
    }

    //구매 확정하여 에스크로 해제
    private void releaseEscrowPayment(String paymentKey) {
        //해당 토스 페이먼츠 엔드포인트를 호출하면 에스크로 해제하고 판매 정산
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/release";

        HttpHeaders headers = new HttpHeaders();
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
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

        //구매자에게 전체 금액 환불
        User buyer = order.getBuyer();
        buyer.addBalance(order.getTotalAmount());
        userRepository.save(buyer);

        order.cancel();

        //수수료 컬럼을 삭제해야되나? 아니면 테이블을 따로 파서 거기서 우리 계좌 자금 임의로 생성하고 차감해야되나?


        return modelMapper.map(order, OrderResponseDTO.class);
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
