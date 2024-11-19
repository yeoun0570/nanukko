package nanukko.nanukko_back.domain.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import nanukko.nanukko_back.domain.product.Product;
import nanukko.nanukko_back.domain.user.User;

import java.time.LocalDateTime;

@Getter
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
    @Column(name = "product_amount")
    private int productAmount; //상품 금액

    @NotNull
    @Column(name = "charge_amount")
    private int chargeAmount; // 수수료 금액
    
    @Column(name = "shipping_free")
    private int shippingFree; // 배송비

    @NotNull
    @Column(name = "total_amount")
    private int totalAmount; //총 금액 (상품 금액 + 수수료 금액)

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
    @Column(name = "escrow_deadline")
    private LocalDateTime escrowDeadline;   // 에스크로 자동 확정 기한

    //주문 상태를 배송 완료로 변경하기 위한 메서드
    public void updateDelivery(PaymentStatus status) {
        this.status = status;
    }

    //주문 상태를 구매 확정으로 변경하기 위한 메서드
    public void updateReleased(PaymentStatus status, LocalDateTime escrowReleasedAt) {
        this.status = status;
        this.escrowReleasedAt = escrowReleasedAt;
    }

    // 데드라인 설정
    public void updateEscrowDeadline(LocalDateTime escrowDeadline) {
        this.escrowDeadline = escrowDeadline;
    }

    //주문 취소 메서드 추가
    public void cancel() {
        if(this.status != PaymentStatus.ESCROW_HOLDING) {
            throw new IllegalArgumentException("에스크로 상태에서만 취소가 가능합니다.");
        }
        this.status = PaymentStatus.CANCELED;
    }
}
