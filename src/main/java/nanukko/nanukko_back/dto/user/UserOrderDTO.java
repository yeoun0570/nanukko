package nanukko.nanukko_back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.order.PaymentStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//사용자의 구매 상품을 조회하기 위한 DTO
public class UserOrderDTO {
    private String thumbnailImage;
    private String productName;
    private int price;
    private PaymentStatus status;
}
