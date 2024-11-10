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
//사용자의 판매 상품 조회하기 위한 DTO
public class UserProductDTO {
    private String thumbnailImage;
    private String productName;
    private int price;
    private ProductStatus status;
}
