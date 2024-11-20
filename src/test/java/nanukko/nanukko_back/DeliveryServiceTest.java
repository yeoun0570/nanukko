package nanukko.nanukko_back;

import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.config.RestTemplateConfig;
import nanukko.nanukko_back.domain.order.Delivery;
import nanukko.nanukko_back.domain.order.DeliveryStatus;
import nanukko.nanukko_back.domain.order.Orders;
import nanukko.nanukko_back.domain.order.PaymentStatus;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;
import nanukko.nanukko_back.dto.order.DeliveryRegistrationDTO;
import nanukko.nanukko_back.dto.order.DeliveryResponseDTO;
import nanukko.nanukko_back.repository.DeliveryRepository;
import nanukko.nanukko_back.repository.OrderRepository;
import nanukko.nanukko_back.repository.ProductRepository;
import nanukko.nanukko_back.repository.UserRepository;
import nanukko.nanukko_back.service.DeliveryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Log4j2  // 로깅을 위한 어노테이션
class DeliveryServiceTest {
    @MockBean  // RestTemplate를 Mock으로 대체
    private RestTemplateConfig restTemplateConfig;

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private RestTemplate restTemplate;  // RestTemplate 직접 주입

    @Value("${delivery.webhook.callback-url}")
    private String webhookCallbackUrl;

    private static final String TEST_ORDER_ID = "test-order-id";
    private static final String TEST_BUYER_ID = "buyer1";
    private static final Long TEST_PRODUCT_ID = 1L;  // seller1의 첫 번째 상품 ID

    @BeforeEach
    void setUp() {
        // 기존 테스트 데이터 정리
        deliveryRepository.deleteAll();
        orderRepository.deleteAll();

        // buyer와 product 조회
        User buyer = userRepository.findById(TEST_BUYER_ID)
                .orElseThrow(() -> new IllegalStateException("테스트 구매자가 없습니다."));
        Product product = productRepository.findById(TEST_PRODUCT_ID)
                .orElseThrow(() -> new IllegalStateException("테스트 상품이 없습니다."));

        // 테스트용 주문 생성
        Orders order = Orders.builder()
                .orderId(TEST_ORDER_ID)
                .buyer(buyer)                    // 구매자 설정
                .product(product)               // 상품 설정
                .productAmount(product.getPrice())
                .chargeAmount((int)(product.getPrice() * 0.035))  // 3.5% 수수료
                .shippingFree(product.getShippingFree())
                .status(PaymentStatus.ESCROW_HOLDING)
                .createdAt(LocalDateTime.now())
                .paymentKey("test-payment-key")
                .build();

        order.setTotalAmount(order.getProductAmount() + order.getChargeAmount() + order.getShippingFree());
        orderRepository.save(order);

        log.info("테스트 주문 생성 완료 - orderId: {}, buyerId: {}, productId: {}",
                order.getOrderId(), order.getBuyer().getUserId(), order.getProduct().getProductId());
    }

    @AfterEach
    void cleanUp() {
        deliveryRepository.deleteAll();
        orderRepository.deleteAll();
    }



    @Test
    @DisplayName("더미 운송장으로 전체 배송 프로세스 테스트")
    void testFullDeliveryProcessWithDummy() throws InterruptedException {
        log.info("테스트 시작: 더미 운송장으로 전체 배송 프로세스 테스트");

        // 1. 더미 운송장으로 배송 등록
        DeliveryRegistrationDTO dto = DeliveryRegistrationDTO.builder()
                .orderId(TEST_ORDER_ID)
                .carrierId("kr.cjlogistics")
                .trackingNumber("1111111111")
                .build();

        DeliveryResponseDTO delivery = deliveryService.registerDelivery(dto);
        assertNotNull(delivery);

        // 2. 배송중 상태로 수동 변경
        Thread.sleep(2000); // 2초 대기
        log.info("배송중 상태로 수동 변경");
        deliveryService.updateDeliverStatus("1111111111", DeliveryStatus.IN_TRANSIT);

        // 3. 배송완료 상태로 수동 변경
        Thread.sleep(2000); // 2초 대기
        log.info("배송완료 상태로 수동 변경");
        deliveryService.updateDeliverStatus("1111111111", DeliveryStatus.DELIVERED);

        // 4. 최종 상태 확인
        Orders order = orderRepository.findById(TEST_ORDER_ID).orElseThrow();
        assertEquals(PaymentStatus.DELIVERED, order.getStatus());
        assertNotNull(order.getEscrowDeadline());

        log.info("테스트 완료 - 주문 상태: {}, 구매확정 기한: {}",
                order.getStatus(), order.getEscrowDeadline());
    }
}
