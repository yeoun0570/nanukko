package nanukko.nanukko_back.domain.order;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryWebhook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private String webhookId;        // 웹훅 고유 ID
    private String trackingNumber;   // 운송장 번호
    private String rawPayload;       // 원본 웹훅 데이터
    private boolean processed;       // 처리 여부 -> true면 처리, false면 처리 못함
    private int retryCount;         // 재시도 횟수

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;   // 배송 상태

    private LocalDateTime createdAt;
    private LocalDateTime processedAt; // 처리 시간

    
    // 처리시키는 메서드
    public void markAsProcessed() {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
    }

    // 추적 재연결 횟수 증가시키는 메서드
    public void incrementRetryCount() {
        this.retryCount++;
    }
}
