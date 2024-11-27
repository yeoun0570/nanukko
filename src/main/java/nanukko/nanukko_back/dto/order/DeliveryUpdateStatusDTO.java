package nanukko.nanukko_back.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.order.DeliveryStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryUpdateStatusDTO {
    private String trackingNumber;
    private DeliveryStatus status;
}
