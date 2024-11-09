package nanukko.nanukko_back.dto.order;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class OrderSuccessDTO {
    //결제 성공해서 에스크로 홀딩시킬 때 사용할 DTO
    private String paymentKey;
    private String orderId;
    private Integer amount;
    private String buyerId;
    private Long productId;
}
