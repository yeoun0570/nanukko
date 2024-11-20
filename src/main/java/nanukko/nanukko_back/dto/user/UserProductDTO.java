package nanukko.nanukko_back.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nanukko.nanukko_back.domain.product.ProductStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
//마이페이지에서 사용자의 판매 상품 조회하기 위한 DTO
public class UserProductDTO {
    private Long productId;
    private String thumbnailImage;
    private String productName;
    private int price;
    private ProductStatus status;
    private boolean hasDelivery;
}
