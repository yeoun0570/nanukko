package nanukko.nanukko_back.domain.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Orders order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;  //배송 상태

    private String carrierId;      // 택배사 ID
    private String trackingNumber; // 운송장 번호

    @Column(name = "delivery_started_at")
    private LocalDateTime deliveryStartedAt;  // 배송 시작 시간

    @Column(name = "delivery_completed_at")
    private LocalDateTime deliveryCompletedAt; // 배송 완료 시간

    @Column(name = "created_at")
    private LocalDateTime createdAt;    // 운송장 등록 시간

    // 배송 상태 업데이트
    public void updateStatus(DeliveryStatus status) {
        this.status = status;
        if (status == DeliveryStatus.IN_TRANSIT) {
            this.deliveryStartedAt = LocalDateTime.now();
        } else if (status == DeliveryStatus.DELIVERED) {
            this.deliveryCompletedAt = LocalDateTime.now();
        }
    }
}
