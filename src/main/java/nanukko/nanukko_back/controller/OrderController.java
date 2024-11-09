package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.order.OrderConfirmDTO;
import nanukko.nanukko_back.dto.order.OrderResponseDTO;
import nanukko.nanukko_back.dto.order.OrderSuccessDTO;
import nanukko.nanukko_back.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/payments")
public class OrderController {
    private final OrderService orderService;

    //결제 상세정보 조회
    @GetMapping("/{orderId}/detail")
    public OrderResponseDTO getPaymentDetails(@PathVariable String orderId) {
        return orderService.getPaymentDetail(orderId);
    }

    //결제 승인 -> 결제하고 나서 승인 처리 -> IN_PROGRESS 삳태를 DONE 으로 만들어주기
    @PostMapping("/confirm")
    public ResponseEntity<OrderResponseDTO> confirmPayment(@RequestBody OrderConfirmDTO request) {
        return ResponseEntity.ok(orderService.confirmPayment(request));
    }

    //결제 성공 및 결제 자금 보관(에스크로 홀딩)
    @PostMapping("/success")
    public ResponseEntity<OrderResponseDTO> processSuccessPayment(
            @RequestBody OrderSuccessDTO request
    ) {
        OrderResponseDTO response = orderService.processPayment(
                request.getPaymentKey(), request.getBuyerId(), request.getProductId());
        return ResponseEntity.ok(response);
    }

    //구매확정
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponseDTO> confirmPurchase(@PathVariable String orderId) {
        OrderResponseDTO response = orderService.confirmPurchase(orderId);
        return ResponseEntity.ok(response);
    }

    //에스크로 상태 중에 결제 취소하기
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable String orderId) {
        OrderResponseDTO response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
