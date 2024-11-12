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
public class UserWishlistDTO {
    private Long productId;
    private String thumbnailImage;
    private String productName;
    private int price;
    private ProductStatus status;
}
