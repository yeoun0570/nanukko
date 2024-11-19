package nanukko.nanukko_back.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRegistrationDTO {
    private String orderId;
    private String carrierId;
    private String trackingNumber;
}
