package nanukko.nanukko_back.dto.order;

import lombok.*;
import nanukko.nanukko_back.domain.order.PaymentStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderResponseDTO {
    private String orderId;
    private Long productId;
    private String productName;
    private int totalAmount;
    private PaymentStatus status;
    private String paymentKey;
    private LocalDateTime paidAt;
    private LocalDateTime escrowReleasedAt;
    private String buyerId; // User 정보
    private String sellerId; // Product의 seller 정보
    private String sellerEmail;
}
