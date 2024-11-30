package nanukko.nanukko_back.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import nanukko.nanukko_back.dto.order.OrderConfirmDTO;
import nanukko.nanukko_back.dto.order.OrderPageDTO;
import nanukko.nanukko_back.dto.order.OrderResponseDTO;
import nanukko.nanukko_back.dto.user.CustomUserDetails;
import nanukko.nanukko_back.dto.user.UserAddrDTO;
import nanukko.nanukko_back.exception.ErrorResponse;
import nanukko.nanukko_back.service.OrderService;
import nanukko.nanukko_back.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/payments")
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;

    //주문 구매내역
    @GetMapping("/page/{productId}")
    public ResponseEntity<OrderPageDTO> getOrderPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long productId) {
        String userId = userDetails.getUsername();
        OrderPageDTO dto = orderService.getOrder(productId, userId);

        return ResponseEntity.ok(dto);
    }

    //결제 상세정보 조회
    @GetMapping("/{orderId}/detail")
    public OrderResponseDTO getPaymentDetails(@PathVariable String orderId) {
        return orderService.getPaymentDetail(orderId);
    }

    //결제 승인 -> 결제하고 나서 승인 처리 -> IN_PROGRESS 삳태를 DONE 으로 만들어주기
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody OrderConfirmDTO request) {
        try {
            return ResponseEntity.ok(orderService.confirmPayment(request));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    //구매확정
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderResponseDTO> confirmPurchase(@PathVariable String orderId) {
        OrderResponseDTO response = orderService.confirmPurchase(orderId);
        return ResponseEntity.ok(response);
    }

    //에스크로 상태 중에 결제 취소하기
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId) {
        try {
            OrderResponseDTO response = orderService.cancelOrder(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    //사용자 배송지 수정
    @PostMapping("/modify-address")
    public ResponseEntity<UserAddrDTO> modifyUserAddr(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserAddrDTO addrDTO
    ) {
        String userId = userDetails.getUsername();
        UserAddrDTO response = userService.modifyUserAddr(userId, addrDTO);
        return ResponseEntity.ok(response);
    }
}
