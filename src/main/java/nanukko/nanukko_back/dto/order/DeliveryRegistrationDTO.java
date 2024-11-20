package nanukko.nanukko_back.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryRegistrationDTO {
    private Long productId;
    private String orderId;
    private String carrierId;
    private String trackingNumber;
}
