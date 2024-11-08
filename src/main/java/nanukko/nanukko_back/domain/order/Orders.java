package nanukko.nanukko_back.domain.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class Orders {
    @Id
    @NotNull
    @Column(name = "order_id")
    private String orderId; //주문 ID

    @ManyToOne
    @JoinColumn(name = "user_id")
    @NotNull
    private User buyer; //구매자 ID (FK)

    @OneToOne
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product; //상품 ID (FK)

    @Column(name = "payment_key")
    private String paymentKey; //토스페이먼츠에서 제공하는 결제 고유키
    @NotNull
    @Column(name = "total_amount")
    private int totalAmount; //총 금액

//    private String orderName; //상품명인데 FK로 가져와야 될듯?

    @Enumerated(EnumType.STRING) // 결제 상태 enum으로 저장
    @Column(length = 20)
    @NotNull
    private PaymentStatus status; // 현재 결제 상태

    @Column(name = "created_at")
    private LocalDateTime createdAt;        // 결제 생성 시점
    @Column(name = "expiry_date_time")
    private LocalDateTime expiryDateTime;   // 결제 만료 예정 시점 (생성 시점 + 30분)
    @Column(name = "paid_at")
    private LocalDateTime paidAt;           //결제 완료 시점 -> 구매자가 돈이 빠져나가는 시점
    @Column(name = "escrow_released_at")
    private LocalDateTime escrowReleasedAt; //구매 확정 시점 -> 판매자에게 돈이 들어가는 시점
    @Column(name = "escrow_dead_line")
    private LocalDateTime escrowDeadline;   // 에스크로 자동 확정 기한
}
