package nanukko.nanukko_back.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.order.DeliveryStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseDTO {
    private Long deliveryId;
    private String orderId;
    private String carrierId;
    private String trackingNumber;
    private DeliveryStatus status;
    private LocalDateTime deliveryStartedAt;
    private LocalDateTime deliveryCompletedAt;
    private LocalDateTime createdAt;
}
