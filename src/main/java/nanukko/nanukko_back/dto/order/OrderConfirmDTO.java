package nanukko.nanukko_back.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
//결제 승인 처리에 사용할 DTO
public class OrderConfirmDTO {
    private String paymentKey;
    private String orderId;
    private int amount;
    private String buyerId;
    private Long productId;
}
